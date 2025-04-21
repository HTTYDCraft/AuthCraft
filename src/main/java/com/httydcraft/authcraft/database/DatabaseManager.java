package com.httydcraft.authcraft.database;

import com.httydcraft.authcraft.AuthCraft;
import com.httydcraft.authcraft.AuditLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private final AuthCraft plugin;
    private final AuditLogger auditLogger;
    private final HikariDataSource dataSource;
    private final Database database;

    public DatabaseManager(AuthCraft plugin) {
        this.plugin = plugin;
        this.auditLogger = plugin.getUtilsManager().getAuditLogger();
        String dbType = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
        if (dbType.equals("postgresql")) {
            this.database = new PostgreSQLDatabase(plugin, this);
        } else {
            this.database = new SQLiteDatabase(plugin, this);
        }
        HikariConfig config = database.configureHikari();
        this.dataSource = new HikariDataSource(config);
        try {
            database.initializeTables();
            database.backup();
        } catch (SQLException e) {
            auditLogger.log("Failed to initialize or backup database: " + e.getMessage());
            throw new IllegalStateException("Could not initialize database", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
            auditLogger.log("Closed database connection pool");
        }
    }

    public void backup() throws SQLException {
        database.backup();
    }
}
