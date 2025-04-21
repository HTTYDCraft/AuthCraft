package com.httydcraft.authcraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TwoFACommand implements CommandExecutor, TabCompleter {
    private final AuthCraft plugin;
    private final AuthManager authManager;
    private final com.httydcraft.authcraft.BotManager botManager;
    private final MessageUtils messageUtils;
    private final TOTPUtils totpUtils;
    // Для хранения временных (pending) секретов
    private final Map<UUID, String> pendingTotpSecrets = new HashMap<>();
    // Для хранения временных VK/TG 2FA: Map<UUID, String> (userId/chatId)
    private final Map<UUID, PendingBot2FA> pendingBot2FA = new HashMap<>();

    private static class PendingBot2FA {
        String method;
        String contact;
        String code;
        PendingBot2FA(String method, String contact, String code) {
            this.method = method;
            this.contact = contact;
            this.code = code;
        }
    }

    public TwoFACommand(AuthCraft plugin, AuthManager authManager, com.httydcraft.authcraft.BotManager botManager, UtilsManager utilsManager) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.botManager = botManager;
        this.messageUtils = utilsManager.getMessageUtils();
        this.totpUtils = utilsManager.getTOTPUtils();
    }

    public void register() {
        plugin.getCommand("2fa").setExecutor(this);
        plugin.getCommand("2fa").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            messageUtils.sendMessage(sender, "command.player_only");
            return true;
        }
        Player player = (Player) sender;
        if (args.length < 1 || !args[0].matches("enable|disable|verify")) {
            messageUtils.sendMessage(player, "2fa.usage");
            return true;
        }

        String identifier = plugin.getConfig().getString("auth.method", "uuid").equalsIgnoreCase("nickname")
                ? player.getName() : player.getUniqueId().toString();

        if (args[0].equalsIgnoreCase("enable")) {
            if (args.length != 2 || !args[1].matches("TOTP|TELEGRAM|VK")) {
                messageUtils.sendMessage(player, "2fa.usage");
                return true;
            }
            if (authManager.getPlayerState(player.getUniqueId()) != PlayerState.AUTHENTICATED) {
                messageUtils.sendMessage(player, "2fa.not_authenticated");
                return true;
            }
            String method = args[1].toUpperCase();
            try {
                String twofaData = null;
                if (method.equals("TOTP")) {
                    // Генерируем otpauth URI и ссылку на QR-код
                    String secret = totpUtils.generateSecret();
                    String otpauthUrl = "otpauth://totp/AuthCraft:" + player.getName() + "?secret=" + totpUtils.getPlainSecret(secret) + "&issuer=AuthCraft";
                    String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + java.net.URLEncoder.encode(otpauthUrl, "UTF-8");
                    // Сохраняем секрет только как pending
                    pendingTotpSecrets.put(player.getUniqueId(), secret);
                    player.sendMessage("§a[2FA] Секрет для Google Authenticator/Яндекс.Ключ: " + totpUtils.getPlainSecret(secret));
                    // Ссылка на QR-код как кликабельная (Minecraft 1.16+ поддерживает JSON-команды)
                    net.md_5.bungee.api.chat.TextComponent qrButton = new net.md_5.bungee.api.chat.TextComponent("§b[Открыть QR]");
                    qrButton.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                        net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, qrUrl));
                    net.md_5.bungee.api.chat.TextComponent prefix = new net.md_5.bungee.api.chat.TextComponent("§a[2FA] QR для сканирования: ");
                    prefix.addExtra(qrButton);
                    player.spigot().sendMessage(prefix);
                    player.sendMessage("§7Установите Google Authenticator или Яндекс.Ключ на смартфон и отсканируйте QR-код или введите секрет вручную.");
                    player.sendMessage("§eТеперь введите: /2fa verify <код из приложения>");
                    return true;
                } else if (method.equals("TELEGRAM")) {
                    String code = botManager.startTelegram2FA(player);
                    player.sendMessage("§a[2FA] Для привязки Telegram отправьте этот код боту: §b" + code + " §aв ЛС https://t.me/" + botManager.getTelegramBot().getBotUsername());
                    player.sendMessage("§7После отправки кода боту, вы получите подтверждение о привязке. Затем используйте /2fa verify для завершения.");
                    return true;
                } else if (method.equals("VK")) {
                    botManager.startVK2FA(player);
                    player.sendMessage("§a[2FA] После отправки кода боту, вы получите подтверждение о привязке. Затем используйте /2fa verify для завершения.");
                    return true;
                }
            } catch (Exception e) {
                messageUtils.sendMessage(player, "error.internal");
            }
        } else if (args[0].equalsIgnoreCase("disable")) {
            if (authManager.getPlayerState(player.getUniqueId()) != PlayerState.AUTHENTICATED) {
                messageUtils.sendMessage(player, "2fa.not_authenticated");
                return true;
            }
            try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE players SET twofa_method = NULL, twofa_data = NULL WHERE identifier = ?")) {
                stmt.setString(1, identifier);
                stmt.executeUpdate();
                messageUtils.sendMessage(player, "2fa.disabled");
            } catch (java.sql.SQLException e) {
                messageUtils.sendMessage(player, "error.database");
            }
        } else if (args[0].equalsIgnoreCase("verify")) {
            if (args.length != 1 && args.length != 2) {
                messageUtils.sendMessage(player, "2fa.verify_usage");
                return true;
            }
            // Проверяем pending секрет
            String pendingSecret = pendingTotpSecrets.get(player.getUniqueId());
            if (pendingSecret != null && args.length == 2) {
                if (totpUtils.verifyCode(pendingSecret, args[1])) {
                    try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
                         java.sql.PreparedStatement stmt = conn.prepareStatement("UPDATE players SET twofa_method = ?, twofa_data = ? WHERE identifier = ?")) {
                        stmt.setString(1, "TOTP");
                        stmt.setString(2, pendingSecret);
                        String idForTotp = plugin.getConfig().getString("auth.method", "uuid").equalsIgnoreCase("nickname")
                                ? player.getName() : player.getUniqueId().toString();
                        stmt.setString(3, idForTotp);
                        stmt.executeUpdate();
                        pendingTotpSecrets.remove(player.getUniqueId());
                        messageUtils.sendMessage(player, "2fa.enabled");
                    } catch (Exception e) {
                        messageUtils.sendMessage(player, "error.database");
                    }
                } else {
                    player.sendMessage("§cНеверный код из приложения. Проверьте, что вы сканировали правильный QR и попробуйте снова.");
                }
                return true;
            }
            // Проверка Telegram/VK: просто проверяем, что привязка есть
            if (botManager.getLinkedTelegram(player.getUniqueId()) != null) {
                try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement("UPDATE players SET twofa_method = ?, twofa_data = ? WHERE identifier = ?")) {
                    stmt.setString(1, "TELEGRAM");
                    stmt.setString(2, botManager.getLinkedTelegram(player.getUniqueId()));
                    String idForTg = plugin.getConfig().getString("auth.method", "uuid").equalsIgnoreCase("nickname")
                            ? player.getName() : player.getUniqueId().toString();
                    stmt.setString(3, idForTg);
                    stmt.executeUpdate();
                    messageUtils.sendMessage(player, "2fa.enabled");
                } catch (Exception e) {
                    messageUtils.sendMessage(player, "error.database");
                }
                return true;
            }
            if (botManager.getLinkedVK(player.getUniqueId()) != null) {
                try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement("UPDATE players SET twofa_method = ?, twofa_data = ? WHERE identifier = ?")) {
                    stmt.setString(1, "VK");
                    stmt.setString(2, botManager.getLinkedVK(player.getUniqueId()));
                    String idForVk = plugin.getConfig().getString("auth.method", "uuid").equalsIgnoreCase("nickname")
                            ? player.getName() : player.getUniqueId().toString();
                    stmt.setString(3, idForVk);
                    stmt.executeUpdate();
                    messageUtils.sendMessage(player, "2fa.enabled");
                } catch (Exception e) {
                    messageUtils.sendMessage(player, "error.database");
                }
                return true;
            }
            if (authManager.getPlayerState(player.getUniqueId()) != PlayerState.PENDING_2FA) {
                messageUtils.sendMessage(player, "2fa.no_pending");
                return true;
            }
            try (java.sql.Connection conn = plugin.getDatabaseManager().getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(
                         "SELECT twofa_method, twofa_data FROM players WHERE identifier = ?")) {
                stmt.setString(1, identifier);
                java.sql.ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    messageUtils.sendMessage(player, "error.internal");
                    return true;
                }
                String method = rs.getString("twofa_method");
                String twofaData = rs.getString("twofa_data");
                boolean valid = false;
                if (method.equals("TOTP")) {
                    valid = totpUtils.verifyCode(twofaData, args[1]);
                } else if (method.equals("TELEGRAM") || method.equals("VK")) {
                    valid = botManager.verify2FACode(player, method, args[1]);
                }
                if (valid) {
                    authManager.setPlayerState(player.getUniqueId(), PlayerState.AUTHENTICATED);
                    messageUtils.sendMessage(player, "2fa.verified");
                    authManager.teleportToMainWorld(player);
                } else {
                    messageUtils.sendMessage(player, "2fa.invalid_code");
                }
            } catch (java.sql.SQLException e) {
                messageUtils.sendMessage(player, "error.database");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("enable");
            completions.add("disable");
            completions.add("verify");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("enable")) {
            completions.add("TOTP");
            completions.add("TELEGRAM");
            completions.add("VK");
        }
        return completions;
    }
}
