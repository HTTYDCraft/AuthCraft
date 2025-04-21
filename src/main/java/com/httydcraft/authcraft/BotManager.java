package com.httydcraft.authcraft;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BotManager {
    private final AuthCraft plugin;
    private final AuthManager authManager;
    private final AuditLogger auditLogger;
    private final MessageUtils messageUtils;
    private final TelegramBot telegramBot;
    private final VKBot vkBot;
    private final Map<String, String> pendingCodes;
    // Привязка Minecraft UUID <-> Telegram/VK id
    private final Map<UUID, String> linkedTelegram;
    private final Map<UUID, String> linkedVK;
    // Для обратного поиска
    private final Map<String, UUID> telegramToPlayer;
    private final Map<String, UUID> vkToPlayer;

    public BotManager(AuthCraft plugin, AuthManager authManager, UtilsManager utilsManager) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.auditLogger = utilsManager.getAuditLogger();
        this.messageUtils = utilsManager.getMessageUtils();
        this.pendingCodes = new ConcurrentHashMap<>();
        this.telegramBot = new TelegramBot(plugin.getConfig().getString("telegram.token", ""));
        this.vkBot = new VKBot(plugin.getConfig().getString("vk.token", ""));
        this.linkedTelegram = new ConcurrentHashMap<>();
        this.linkedVK = new ConcurrentHashMap<>();
        this.telegramToPlayer = new ConcurrentHashMap<>();
        this.vkToPlayer = new ConcurrentHashMap<>();
        if (!telegramBot.isValid()) {
            auditLogger.log("Telegram bot disabled: Invalid token");
        }
        if (!vkBot.isValid()) {
            auditLogger.log("VK bot disabled: Invalid token");
        }
    }

    public String startTelegram2FA(Player player) {
        if (!telegramBot.isValid()) {
            return null;
        }
        // Генерируем код, который игрок должен отправить боту
        String code = String.valueOf(100000 + new java.util.Random().nextInt(900000));
        pendingCodes.put(player.getUniqueId().toString() + "TELEGRAM", code);
        return code;
    }

    public String startVK2FA(Player player) {
        if (!vkBot.isValid()) {
            return null;
        }
        String code = String.valueOf(100000 + new Random().nextInt(900000));
        pendingCodes.put(player.getUniqueId().toString() + "VK", code);
        messageUtils.sendMessage(player, "§a[2FA] Для привязки VK отправьте этот код боту в ЛС группы.");
        messageUtils.sendMessage(player, "§a[2FA] Код: " + code);
        return code;
    }

    // Метод вызывается из TelegramBot/VKBot, когда бот получает сообщение с кодом
    public boolean tryLinkTelegram(String telegramId, String code) {
        for (Map.Entry<String, String> entry : pendingCodes.entrySet()) {
            if (entry.getKey().endsWith("TELEGRAM") && entry.getValue().equals(code)) {
                UUID playerId = UUID.fromString(entry.getKey().replace("TELEGRAM", ""));
                linkedTelegram.put(playerId, telegramId);
                telegramToPlayer.put(telegramId, playerId);
                pendingCodes.remove(entry.getKey());
                return true;
            }
        }
        return false;
    }

    public boolean tryLinkVK(String vkId, String code) {
        for (Map.Entry<String, String> entry : pendingCodes.entrySet()) {
            if (entry.getKey().endsWith("VK") && entry.getValue().equals(code)) {
                UUID playerId = UUID.fromString(entry.getKey().replace("VK", ""));
                linkedVK.put(playerId, vkId);
                vkToPlayer.put(vkId, playerId);
                pendingCodes.remove(entry.getKey());
                return true;
            }
        }
        return false;
    }

    public String getLinkedTelegram(UUID playerId) {
        return linkedTelegram.get(playerId);
    }

    public String getLinkedVK(UUID playerId) {
        return linkedVK.get(playerId);
    }

    public UUID getPlayerByTelegram(String telegramId) {
        return telegramToPlayer.get(telegramId);
    }

    public UUID getPlayerByVK(String vkId) {
        return vkToPlayer.get(vkId);
    }

    public void send2FACode(Player player, String method, String twofaData) {
        String code = String.valueOf(100000 + new Random().nextInt(900000));
        pendingCodes.put(player.getUniqueId().toString() + method, code);
        if (method.equals("TELEGRAM")) {
            telegramBot.sendMessage(twofaData, "Your 2FA code is: " + code);
        } else if (method.equals("VK")) {
            vkBot.sendMessage(twofaData, "Your 2FA code is: " + code);
        }
    }

    public boolean verify2FACode(Player player, String method, String code) {
        String key = player.getUniqueId().toString() + method;
        String expectedCode = pendingCodes.get(key);
        if (expectedCode != null && expectedCode.equals(code)) {
            pendingCodes.remove(key);
            return true;
        }
        return false;
    }

    // Проверка входа: отправка push-апрува в TG/VK
    public void requestLoginApproval(UUID playerId, String playerName) {
        String tgId = linkedTelegram.get(playerId);
        String vkId = linkedVK.get(playerId);
        String code = String.valueOf(100000 + new Random().nextInt(900000));
        if (tgId != null) {
            telegramBot.sendLoginApproval(tgId, playerName, code);
            pendingCodes.put(playerId.toString() + "TGLOGIN", code);
        }
        if (vkId != null) {
            vkBot.sendLoginApproval(vkId, playerName, code);
            pendingCodes.put(playerId.toString() + "VKLOGIN", code);
        }
    }

    // Проверка ответа на push-апрув
    public boolean approveLogin(UUID playerId, String code, String method) {
        String key = playerId.toString() + method + "LOGIN";
        String expected = pendingCodes.get(key);
        if (expected != null && expected.equals(code)) {
            pendingCodes.remove(key);
            return true;
        }
        return false;
    }

    public TelegramBot getTelegramBot() {
        return telegramBot;
    }

    public void shutdown() {
        // No cleanup needed for simple bot implementation
    }
}
