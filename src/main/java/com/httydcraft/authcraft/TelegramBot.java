package com.httydcraft.authcraft;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.util.UUID;

public class TelegramBot extends TelegramLongPollingBot {
    private final String token;
    private boolean valid;
    private static TelegramBot instance;

    public TelegramBot(String token) {
        this.token = token;
        this.valid = token != null && !token.isEmpty();
        if (valid) {
            try {
                TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
                api.registerBot(this);
            } catch (TelegramApiException e) {
                valid = false;
            }
        }
        instance = this;
    }

    @Override
    public String getBotUsername() {
        return "AuthCraftBot";
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            AuthCraft plugin = AuthCraft.getInstance();
            BotManager botManager = plugin.getBotManager();
            // Привязка Telegram
            if (botManager.tryLinkTelegram(chatId, text)) {
                sendMessage(chatId, "✅ Ваш Telegram успешно привязан к аккаунту Minecraft!");
                return;
            }
            // Подтверждение входа
            UUID playerId = botManager.getPlayerByTelegram(chatId);
            if (playerId != null && botManager.approveLogin(playerId, text, "TG")) {
                sendMessage(chatId, "✅ Вход в аккаунт Minecraft разрешён!");
                plugin.getAuthManager().approveLogin(playerId);
            }
        }
    }

    public boolean isValid() {
        return valid;
    }

    public String requestChatId(String playerName) {
        return "123456789"; // Simplified: Assume admin provides chat ID in config
    }

    public void sendMessage(String chatId, String text) {
        if (!valid) {
            return;
        }
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            // Silent fail
        }
    }

    // Отправка push-апрува
    public void sendLoginApproval(String chatId, String playerName, String code) {
        String text = "⚠️ Попытка входа в аккаунт Minecraft: " + playerName + "\n" +
                "Если это вы, отправьте этот код в ответ: " + code + "\n" +
                "Если не вы — проигнорируйте сообщение.";
        sendMessage(chatId, text);
    }

    public static TelegramBot getInstance() {
        return instance;
    }
}
