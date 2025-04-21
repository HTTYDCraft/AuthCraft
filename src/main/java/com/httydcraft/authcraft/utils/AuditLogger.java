package com.httydcraft.authcraft.utils;

import com.httydcraft.authcraft.AuthCraft;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AuditLogger {
    private final AuthCraft plugin;
    private final File logFile;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AuditLogger(AuthCraft plugin) {
        this.plugin = plugin;
        this.logFile = new File(plugin.getDataFolder(), "audit.log");
    }

    public void log(String message) {
        String logEntry = String.format("[%s] %s%n", dateFormat.format(new Date()), message);
        plugin.getLogger().info(message);
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(logEntry);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write to audit log: " + e.getMessage());
        }
    }
}