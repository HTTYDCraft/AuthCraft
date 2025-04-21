package com.httydcraft.authcraft.database;

import com.httydcraft.authcraft.AuthCraft;
import com.httydcraft.authcraft.utils.AuditLogger;
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
    private final File dbFile;

    public SQLiteDatabase(AuthCraft plugin) {
        this.plugin = plugin;
        this.auditLogger = plugin.getUtilsManager().getAuditLogger();
        String dbFileName = plugin.getConfig().getString("database.sqlite.file", "authcraft.db");
        if (dbFileName == null || dbFileName.isEmpty()) {
            throw new IllegalStateException("SQLite database file name is missing in configuration");
        }
        this.dbFile = new File(plugin.getDataFolder(), dbFileName);
        ensureDbFile();
    }

    private void ensureDbFile() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            throw new IllegalStateException("Could not create plugin data folder");
        }
        if (!dbFile.exists()) {
            try {
                if (dbFile.createNewFile()) {
                    auditLogger.log("Created new SQLite database file: " + dbFile.getAbsolutePath());
                }
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
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(10000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        auditLogger.log("Configured HikariCP for SQLite");
        return config;
    }

    @Override
    public void initializeTables() throws SQLException {
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid TEXT PRIMARY KEY, " +
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
