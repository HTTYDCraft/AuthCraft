package com.httydcraft.authcraft.bot;

import com.httydcraft.authcraft.AuthCraft;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TelegramBot extends TelegramLongPollingBot {
    private final AuthCraft plugin;
    private final BotManager botManager;
    private final String botToken;
    private final String adminId;

    public TelegramBot(AuthCraft plugin, BotManager botManager, String botToken, String adminId) {
        this.plugin = plugin;
        this.botManager = botManager;
        this.botToken = botToken;
        this.adminId = adminId;
        plugin.getUtilsManager().getAuditLogger().log("Telegram bot initialized.");
    }

    @Override
    public String getBotUsername() {
        return "AuthCraftBot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();
            botManager.verifyLinkCode(text, chatId, "TELEGRAM");
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String[] parts = callbackData.split(":");
            if (parts.length == 3 && parts[0].startsWith("login")) {
                UUID playerId = UUID.fromString(parts[1]);
                String loginId = parts[2];
                boolean approved = parts[0].equals("login_approve");
                botManager.handleLoginResponse(playerId, loginId, approved);
            }
        }
    }

    public void sendAdminMessage(String message) {
        SendMessage msg = new SendMessage();
        msg.setChatId(adminId);
        msg.setText(message);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            plugin.getUtilsManager().getAuditLogger().log("Failed to send Telegram admin message: " + e.getMessage());
        }
    }

    public void sendLoginRequest(Player player, String chatId, String loginId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("Login request from " + player.getName() + " (" + player.getUniqueId() + "). Approve or deny?");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton approveButton = new InlineKeyboardButton();
        approveButton.setText("Approve");
        approveButton.setCallbackData("login_approve:" + player.getUniqueId() + ":" + loginId);

        InlineKeyboardButton denyButton = new InlineKeyboardButton();
        denyButton.setText("Deny");
        denyButton.setCallbackData("login_deny:" + player.getUniqueId() + ":" + loginId);

        row.add(approveButton);
        row.add(denyButton);
        rows.add(row);
        markup.setKeyboard(rows);

        msg.setReplyMarkup(markup);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            plugin.getUtilsManager().getAuditLogger().log("Failed to send Telegram login request for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void shutdown() {
        plugin.getUtilsManager().getAuditLogger().log("Telegram bot shutdown.");
    }
}