package com.httydcraft.authcraft.utils;

import com.httydcraft.authcraft.AuthCraft;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageUtils {
    private final AuthCraft plugin;
    private YamlConfiguration messages;

    public MessageUtils(AuthCraft plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlerConfiguration.loadConfiguration(file);
    }

    public String getMessage(String key) {
        return messages.getString(key, "Message not found: " + key);
    }

    public void send(CommandSender sender, String key) {
        String message = getMessage(key);
        if (!message.isEmpty()) {
            sender.sendMessage(message);
        }
    }
}