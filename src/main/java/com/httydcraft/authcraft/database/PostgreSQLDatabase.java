package com.httydcraft.authcraft.database;

import com.httydcraft.authcraft.AuthCraft;
import com.httydcraft.authcraft.AuditLogger;
import com.zaxxer.hikari.HikariConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQLDatabase implements Database {
    private final AuthCraft plugin;
    private final AuditLogger auditLogger;
    private final DatabaseManager databaseManager;

    public PostgreSQLDatabase(AuthCraft plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.auditLogger = plugin.getUtilsManager().getAuditLogger();
        this.databaseManager = databaseManager;
    }

    @Override
    public HikariConfig configureHikari() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        String host = plugin.getConfig().getString("database.postgresql.host", "localhost");
        int port = plugin.getConfig().getInt("database.postgresql.port", 5432);
        String database = plugin.getConfig().getString("database.postgresql.database", "authcraft");
        String username = plugin.getConfig().getString("database.postgresql.username", "authcraft");
        String password = plugin.getConfig().getString("database.postgresql.password", "password");
        int poolSize = plugin.getConfig().getInt("database.postgresql.pool_size", 10);
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", host, port, database));
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(10000);
        auditLogger.log("Configured HikariCP for PostgreSQL");
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
            auditLogger.log("Initialized PostgreSQL players table");
        } catch (SQLException e) {
            auditLogger.log("Failed to initialize PostgreSQL tables: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void backup() throws SQLException {
        auditLogger.log("PostgreSQL backup not implemented");
        throw new SQLException("PostgreSQL backup not supported");
    }
}
