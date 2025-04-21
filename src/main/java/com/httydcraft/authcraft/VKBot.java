package com.httydcraft.authcraft;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import java.util.UUID;

public class VKBot {
    private final String token;
    private final VkApiClient vkClient;
    private final GroupActor actor;
    private boolean valid;

    public VKBot(String token) {
        this.token = token;
        this.vkClient = new VkApiClient(HttpTransportClient.getInstance());
        this.actor = new GroupActor(0, token);
        this.valid = token != null && !token.isEmpty();
    }

    public boolean isValid() {
        return valid;
    }

    public String requestUserId(String playerName) {
        return "123456789"; // Simplified: Assume admin provides user ID
    }

    public void sendMessage(String userId, String text) {
        if (!valid) {
            return;
        }
        try {
            vkClient.messages().send(actor)
                    .userId(Integer.parseInt(userId))
                    .message(text)
                    .randomId((int) System.currentTimeMillis())
                    .execute();
        } catch (ApiException | ClientException e) {
            // Silent fail
        }
    }

    // Отправка push-апрува
    public void sendLoginApproval(String vkId, String playerName, String code) {
        String text = "⚠️ Попытка входа в аккаунт Minecraft: " + playerName + "\n" +
                "Если это вы, отправьте этот код в ответ: " + code + "\n" +
                "Если не вы — проигнорируйте сообщение.";
        sendMessage(vkId, text);
    }

    // Добавить обработку входящих сообщений для привязки VK
    public void onMessageReceived(String vkId, String text) {
        AuthCraft plugin = AuthCraft.getInstance(); // Получить singleton или передать через конструктор
        BotManager botManager = plugin.getBotManager();
        // Привязка VK
        if (botManager.tryLinkVK(vkId, text)) {
            sendMessage(vkId, "✅ Ваш VK успешно привязан к аккаунту Minecraft!");
            return;
        }
        // Подтверждение входа
        UUID playerId = botManager.getPlayerByVK(vkId);
        if (playerId != null && botManager.approveLogin(playerId, text, "VK")) {
            sendMessage(vkId, "✅ Вход в аккаунт Minecraft разрешён!");
            plugin.getAuthManager().approveLogin(playerId);
        }
    }
}
