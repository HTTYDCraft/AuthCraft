package com.httydcraft.authcraft;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageUtils {
    private final AuthCraft plugin;
    private FileConfiguration messages;

    public MessageUtils(AuthCraft plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        String language = plugin.getConfig().getString("language", "en").toLowerCase();
        File messagesFile = new File(plugin.getDataFolder(), "messages_" + language + ".yml");
        if (!messagesFile.exists()) {
            messagesFile = new File(plugin.getDataFolder(), "messages_en.yml");
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void sendMessage(CommandSender sender, String key) {
        String message = messages.getString(key, "Message not found: " + key);
        sender.sendMessage(colorize(message));
    }

    public String getMessage(String key) {
        return colorize(messages.getString(key, "Message not found: " + key));
    }

    private String colorize(String message) {
        return message.replace("&", "§");
    }
}
