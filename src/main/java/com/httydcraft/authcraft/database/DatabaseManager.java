package com.httydcraft.authcraft.database;

import com.httydcraft.authcraft.AuthCraft;
import com.httydcraft.authcraft.utils.AuditLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private final AuthCraft plugin;
    private final AuditLogger auditLogger;
    private final HikariDataSource dataSource;
    private final Database database;

    public DatabaseManager(AuthCraft plugin, AuditLogger auditLogger) {
        this.plugin = plugin;
        this.auditLogger = auditLogger;
        String dbType = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();

        try {
            if (dbType.equals("postgresql")) {
                database = new PostgreSQLDatabase(plugin);
            } else {
                database = new SQLiteDatabase(plugin);
            }

            HikariConfig hikariConfig = database.configureHikari();
            this.dataSource = new HikariDataSource(hikariConfig);

            database.initializeTables();
            auditLogger.log("Database initialized successfully.");
        } catch (Exception e) {
            auditLogger.log("Failed to initialize database: " + e.getMessage());
            throw new IllegalStateException("Could not initialize database", e);
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            auditLogger.log("Failed to get database connection: " + e.getMessage());
            throw e;
        }
    }

    public void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                auditLogger.log("Failed to close ResultSet: " + e.getMessage());
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                auditLogger.log("Failed to close PreparedStatement: " + e.getMessage());
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                auditLogger.log("Failed to close Connection: " + e.getMessage());
            }
        }
    }

    public void backupDatabase() {
        try {
            database.backup();
            auditLogger.log("Database backup completed successfully.");
        } catch (SQLException e) {
            auditLogger.log("Database backup failed: " + e.getMessage());
        }
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
            auditLogger.log("Database connection pool closed.");
        }
    }
}
