package com.httydcraft.authcraft.database;

import com.httydcraft.authcraft.AuthCraft;
import com.httydcraft.authcraft.utils.AuditLogger;
import com.zaxxer.hikari.HikariConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQLDatabase implements Database {
    private final AuthCraft plugin;
    private final AuditLogger auditLogger;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public PostgreSQLDatabase(AuthCraft plugin) {
        this.plugin = plugin;
        this.auditLogger = plugin.getUtilsManager().getAuditLogger();
        this.host = plugin.getConfig().getString("database.postgresql.host", "localhost");
        this.port = plugin.getConfig().getInt("database.postgresql.port", 5432);
        this.database = plugin.getConfig().getString("database.postgresql.database", "authcraft");
        this.username = plugin.getConfig().getString("database.postgresql.username", "authcraft");
        this.password = plugin.getConfig().getString("database.postgresql.password", "password");

        if (host == null || host.isEmpty() || database == null || database.isEmpty() || username == null || username.isEmpty()) {
            throw new IllegalStateException("Invalid PostgreSQL configuration: host, database, or username is missing");
        }
    }

    @Override
    public HikariConfig configureHikari() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", host, port, database));
        config.setUsername(username);
        config.setPassword(password != null ? password : "");
        config.setMaximumPoolSize(plugin.getConfig().getInt("database.postgresql.pool_size", 10));
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(10000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        auditLogger.log("Configured HikariCP for PostgreSQL");
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
            auditLogger.log("Initialized PostgreSQL players table");
        } catch (SQLException e) {
            auditLogger.log("Failed to initialize PostgreSQL tables: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void backup() throws SQLException {
        String backupCommand = plugin.getConfig().getString("database.postgresql.backup_command",
                "pg_dump -U " + username + " -h " + host + " -p " + port + " " + database + " > authcraft_backup.sql");
        try {
            Process process = Runtime.getRuntime().exec(backupCommand);
            if (process.waitFor() != 0) {
                throw new SQLException("PostgreSQL backup command failed");
            }
            auditLogger.log("Created PostgreSQL backup");
        } catch (Exception e) {
            auditLogger.log("Failed to create PostgreSQL backup: " + e.getMessage());
            throw new SQLException("Backup failed", e);
        }
    }
}
