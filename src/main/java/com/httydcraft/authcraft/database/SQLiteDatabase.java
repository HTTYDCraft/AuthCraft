package com.httydcraft.authcraft.database;

import com.httydcraft.authcraft.AuthCraft;
import com.httydcraft.authcraft.AuditLogger;
import com.zaxxer.hikari.HikariConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDatabase implements Database {
    private final AuthCraft plugin;
    private final AuditLogger auditLogger;
    private final DatabaseManager databaseManager;
    private final File dbFile;

    public SQLiteDatabase(AuthCraft plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.auditLogger = plugin.getUtilsManager().getAuditLogger();
        this.databaseManager = databaseManager;
        String dbFileName = plugin.getConfig().getString("database.sqlite.file", "authcraft.db");
        this.dbFile = new File(plugin.getDataFolder(), dbFileName);
        ensureDbFile();
    }

    private void ensureDbFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
                auditLogger.log("Created new SQLite database file: " + dbFile.getAbsolutePath());
            } catch (IOException e) {
                auditLogger.log("Failed to create SQLite database file: " + e.getMessage());
                throw new IllegalStateException("Could not create SQLite database file", e);
            }
        }
    }

    @Override
    public HikariConfig configureHikari() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
        config.setIdleTimeout(0); // Disable idleTimeout for fixed-size pool
        config.setConnectionTimeout(10000);
        auditLogger.log("Configured HikariCP for SQLite");
        return config;
    }

    @Override
    public void initializeTables() throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS players (" +
                    "identifier TEXT PRIMARY KEY, " +
                    "username TEXT NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "twofa_method TEXT, " +
                    "twofa_data TEXT, " +
                    "role TEXT NOT NULL, " +
                    "last_login BIGINT NOT NULL)");
            auditLogger.log("Initialized SQLite players table");
        } catch (SQLException e) {
            auditLogger.log("Failed to initialize SQLite tables: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void backup() throws SQLException {
        File backupFile = new File(plugin.getDataFolder(), "authcraft_backup_" + System.currentTimeMillis() + ".db");
        try {
            Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            auditLogger.log("Created SQLite backup: " + backupFile.getAbsolutePath());
        } catch (IOException e) {
            auditLogger.log("Failed to create SQLite backup: " + e.getMessage());
            throw new SQLException("Backup failed", e);
        }
    }
}