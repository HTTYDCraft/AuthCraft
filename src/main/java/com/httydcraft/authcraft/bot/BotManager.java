package com.httydcraft.authcraft.bot;

import com.httydcraft.authcraft.AuthCraft;
import com.httydcraft.authcraft.utils.AuditLogger;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BotManager {
    private final AuthCraft plugin;
    private final AuditLogger auditLogger;
    private final TelegramBot telegramBot;
    private final VKBot vkBot;
    private final ConcurrentHashMap<String, UUID> linkCodes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> pendingLogins = new ConcurrentHashMap<>();

    public BotManager(AuthCraft plugin, AuditLogger auditLogger) {
        this.plugin = plugin;
        this.auditLogger = auditLogger;

        String telegramToken = plugin.getConfig().getString("telegram.token", "");
        String vkToken = plugin.getConfig().getString("vk.token", "");

        telegramBot = telegramToken.isEmpty() ? null : new TelegramBot(plugin, this, telegramToken, plugin.getConfig().getString("telegram.admin_id", ""));
        vkBot = vkToken.isEmpty() ? null : new VKBot(plugin, this, vkToken, plugin.getConfig().getString("vk.admin_id", ""));
    }

    public String generateLinkCode(UUID playerId) {
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        linkCodes.put(code, playerId);
        return code;
    }

    public void verifyLinkCode(String code, String chatId, String method) {
        UUID playerId = linkCodes.remove(code);
        if (playerId == null) {
            notifyAdmin("Invalid or expired link code: " + code);
            return;
        }

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE players SET twofa_data = ? WHERE uuid = ?")) {
            stmt.setString(1, chatId);
            stmt.setString(2, playerId.toString());
            stmt.executeUpdate();
            auditLogger.log("Player UUID " + playerId + " linked " + method + " chat ID " + chatId);
        } catch (SQLException e) {
            auditLogger.log("Database error linking " + method + " for UUID " + playerId + ": " + e.getMessage());
        }
    }

    public void requestBotLogin(Player player, String method, String chatId) {
        String loginId = UUID.randomUUID().toString();
        pendingLogins.put(player.getUniqueId(), loginId);
        if (method.equals("TELEGRAM") && telegramBot != null) {
            telegramBot.sendLoginRequest(player, chatId, loginId);
        } else if (method.equals("VK") && vkBot != null) {
            vkBot.sendLoginRequest(player, chatId, loginId);
        } else {
            auditLogger.log("Failed to send " + method + " login request for " + player.getName() + ": Bot not initialized.");
        }
    }

    public void handleLoginResponse(UUID playerId, String loginId, boolean approved) {
        if (!pendingLogins.getOrDefault(playerId, "").equals(loginId)) {
            auditLogger.log("Invalid or expired login ID for player UUID " + playerId);
            return;
        }

        pendingLogins.remove(playerId);
        Player player = plugin.getServer().getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            auditLogger.log("Player UUID " + playerId + " is offline during login response.");
            return;
        }

        if (approved) {
            plugin.getAuthManager().completeLogin(player);
        } else {
            plugin.getUtilsManager().getMessageUtils().send(player, "login.invalid_credentials");
            auditLogger.log("Login denied for player " + player.getName() + " (" + playerId + ") via bot.");
        }
    }

    public void notifyAdmin(String message) {
        if (telegramBot != null) {
            telegramBot.sendAdminMessage(message);
        }
        if (vkBot != null) {
            vkBot.sendAdminMessage(message);
        }
    }

    public void shutdown() {
        if (telegramBot != null) {
            telegramBot.shutdown();
        }
        if (vkBot != null) {
            vkBot.shutdown();
        }
    }
}