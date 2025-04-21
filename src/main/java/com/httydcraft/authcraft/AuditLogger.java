package com.httydcraft.authcraft;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AuditLogger {
    private final AuthCraft plugin;
    private final File logFile;

    public AuditLogger(AuthCraft plugin) {
        this.plugin = plugin;
        this.logFile = new File(plugin.getDataFolder(), "audit.log");
        ensureLogFile();
    }

    private void ensureLogFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create audit log file: " + e.getMessage());
            }
        }
    }

    public void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + message + "\n";
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(logEntry);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to write to audit log: " + e.getMessage());
        }
    }
}
