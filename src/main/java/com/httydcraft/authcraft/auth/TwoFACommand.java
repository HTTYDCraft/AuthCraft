package com.httydcraft.authcraft.auth;

import com.httydcraft.authcraft.AuthCraft;
import com.httydcraft.authcraft.bot.BotManager;
import com.httydcraft.authcraft.utils.TOTPUtils;
import com.httydcraft.authcraft.utils.UtilsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TwoFACommand implements CommandExecutor {
    private final AuthCraft plugin;
    private final AuthManager authManager;
    private final BotManager botManager;
    private final UtilsManager utilsManager;

    public TwoFACommand(AuthCraft plugin, AuthManager authManager, BotManager botManager, UtilsManager utilsManager) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.botManager = botManager;
        this.utilsManager = utilsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            utilsManager.getMessageUtils().send(sender, "command.player_only");
            return true;
        }

        Player player = (Player) sender;
        if (!authManager.isAuthenticated(player.getUniqueId()) && utilsManager.getCacheManager().get("2fa_pending:" + player.getUniqueId().toString()) == null) {
            utilsManager.getMessageUtils().send(player, "2fa.not_authenticated");
            return true;
        }

        if (args.length < 1) {
            utilsManager.getMessageUtils().send(player, "2fa.usage");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "enable":
                if (args.length != 2) {
                    utilsManager.getMessageUtils().send(player, "2fa.usage");
                    return true;
                }
                String method = args[1].toUpperCase();
                if (!method.equals("TOTP") && !method.equals("TELEGRAM") && !method.equals("VK")) {
                    utilsManager.getMessageUtils().send(player, "2fa.usage");
                    return true;
                }
                enable2FA(player, method);
                break;
            case "disable":
                disable2FA(player);
                break;
            case "verify":
                if (args.length != 2) {
                    utilsManager.getMessageUtils().send(player, "2fa.verify_usage");
                    return true;
                }
                verifyTOTP(player, args[1]);
                break;
            default:
                utilsManager.getMessageUtils().send(player, "2fa.usage");
        }
        return true;
    }

    private void enable2FA(Player player, String method) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT twofa_method FROM players WHERE uuid = ?");
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getString("twofa_method") != null) {
                utilsManager.getMessageUtils().send(player, "2fa.already_enabled");
                return;
            }
            plugin.getDatabaseManager().closeResources(null, stmt, rs);

            String twoFAData = "";
            if (method.equals("TOTP")) {
                String secret = TOTPUtils.generateSecret();
                String issuer = plugin.getConfig().getString("2fa.issuer", "AuthCraft");
                String qrCodeUrl = TOTPUtils.getQRCode(player.getName(), issuer, secret);
                twoFAData = utilsManager.getCryptManager().encrypt(secret);
                utilsManager.getMessageUtils().send(player, "2fa.enabled");
                player.sendMessage("Scan this QR code with your authenticator app: " + qrCodeUrl);
            } else if (method.equals("TELEGRAM") || method.equals("VK")) {
                String linkCode = botManager.generateLinkCode(player.getUniqueId());
                utilsManager.getMessageUtils().send(player, "Send this code to the " + method + " bot: " + linkCode);
            }

            stmt = conn.prepareStatement("UPDATE players SET twofa_method = ?, twofa_data = ? WHERE uuid = ?");
            stmt.setString(1, method);
            stmt.setString(2, twoFAData);
            stmt.setString(3, player.getUniqueId().toString());
            stmt.executeUpdate();
            utilsManager.getAuditLogger().log("Player " + player.getName() + " (" + player.getUniqueId() + ") enabled " + method + " 2FA.");
        } catch (SQLException e) {
            utilsManager.getAuditLogger().log("Database error enabling 2FA for " + player.getName() + ": " + e.getMessage());
            utilsManager.getMessageUtils().send(player, "error.database");
        }
    }

    private void disable2FA(Player player) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT twofa_method FROM players WHERE uuid = ?");
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next() || rs.getString("twofa_method") == null) {
                utilsManager.getMessageUtils().send(player, "2fa.not_enabled");
                return;
            }
            plugin.getDatabaseManager().closeResources(null, stmt, rs);

            stmt = conn.prepareStatement("UPDATE players SET twofa_method = NULL, twofa_data = NULL WHERE uuid = ?");
            stmt.setString(1, player.getUniqueId().toString());
            stmt.executeUpdate();
            utilsManager.getMessageUtils().send(player, "2fa.disabled");
            utilsManager.getAuditLogger().log("Player " + player.getName() + " (" + player.getUniqueId() + ") disabled 2FA.");
        } catch (SQLException e) {
            utilsManager.getAuditLogger().log("Database error disabling 2FA for " + player.getName() + ": " + e.getMessage());
            utilsManager.getMessageUtils().send(player, "error.database");
        }
    }

    private void verifyTOTP(Player player, String code) {
        String encryptedSecret = (String) utilsManager.getCacheManager().get("2fa_pending:" + player.getUniqueId().toString());
        if (encryptedSecret == null) {
            utilsManager.getMessageUtils().send(player, "2fa.no_pending");
            return;
        }

        String secret = utilsManager.getCryptManager().decrypt(encryptedSecret);
        if (secret == null) {
            utilsManager.getMessageUtils().send(player, "error.internal");
            return;
        }

        if (TOTPUtils.verifyCode(secret, code)) {
            utilsManager.getCacheManager().remove("2fa_pending:" + player.getUniqueId().toString());
            authManager.completeLogin(player);
            utilsManager.getMessageUtils().send(player, "2fa.verified");
            utilsManager.getAuditLogger().log("Player " + player.getName() + " (" + player.getUniqueId() + ") verified TOTP 2FA.");
        } else {
            utilsManager.getMessageUtils().send(player, "2fa.invalid_code");
            utilsManager.getAuditLogger().log("Invalid TOTP code for player " + player.getName() + " (" + player.getUniqueId() + ").");
        }
    }
}