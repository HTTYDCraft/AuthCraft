package com.httydcraft.authcraft.bot;

import com.httydcraft.authcraft.AuthCraft;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Message;
import org.bukkit.entity.Player;

import java.util.UUID;

public class VKBot {
    private final AuthCraft plugin;
    private final BotManager botManager;
    private final VkApiClient vk;
    private final GroupActor actor;
    private final String adminId;

    public VKBot(AuthCraft plugin, BotManager botManager, String token, String adminId) {
        this.plugin = plugin;
        this.botManager = botManager;
        this.vk = new VkApiClient(new HttpTransportClient());
        this.actor = new GroupActor(0, token);
        this.adminId = adminId;
        plugin.getUtilsManager().getAuditLogger().log("VK bot initialized. Polling requires VK Bots Long Poll API setup.");
    }

    public void sendAdminMessage(String message) {
        try {
            vk.messages().send(actor)
                    .userId(Integer.parseInt(adminId))
                    .message(message)
                    .randomId((int) (Math.random() * 1000000))
                    .execute();
        } catch (ClientException | ApiException e) {
            plugin.getUtilsManager().getAuditLogger().log("Failed to send VK admin message: " + e.getMessage());
        }
    }

    public void sendLoginRequest(Player player, String chatId, String loginId) {
        try {
            vk.messages().send(actor)
                    .userId(Integer.parseInt(chatId))
                    .message("Login request from " + player.getName() + " (" + player.getUniqueId() + "). Reply with 'approve' or 'deny'.")
                    .randomId((int) (Math.random() * 1000000))
                    .execute();
            plugin.getUtilsManager().getAuditLogger().log("Sent VK login request to chat ID " + chatId + " for " + player.getName());
        } catch (ClientException | ApiException e) {
            plugin.getUtilsManager().getAuditLogger().log("Failed to send VK login request for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void handleMessage(Message message) {
        String chatId = message.getUserId().toString();
        String text = message.getText().toLowerCase();
        if (text.matches("\\d{6}")) {
            botManager.verifyLinkCode(text, chatId, "VK");
        } else if (text.equals("approve") || text.equals("deny")) {
            plugin.getUtilsManager().getAuditLogger().log("Received VK response: " + text + " from chat ID " + chatId + " (polling not fully implemented).");
        }
    }

    public void shutdown() {
        plugin.getUtilsManager().getAuditLogger().log("VK bot shutdown.");
    }
}