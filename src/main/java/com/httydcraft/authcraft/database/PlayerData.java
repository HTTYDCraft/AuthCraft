package com.httydcraft.authcraft.database;

import com.httydcraft.authcraft.AuthCraft;
import com.httydcraft.authcraft.utils.AuditLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerData {
    private final AuthCraft plugin;
    private final AuditLogger auditLogger;
    private final UUID uuid;
    private String username;
    private String password;
    private String twoFAMethod;
    private String twoFAData;
    private String role;
    private long lastLogin;

    public PlayerData(AuthCraft plugin, UUID uuid) {
        this.plugin = plugin;
        this.auditLogger = plugin.getUtilsManager().getAuditLogger();
        this.uuid = uuid;
        load();
    }

    private void load() {
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                username = rs.getString("username");
                password = rs.getString("password");
                twoFAMethod = rs.getString("twofa_method");
                twoFAData = rs.getString("twofa_data");
                role = rs.getString("role");
                lastLogin = rs.getLong("last_login");
            }
        } catch (SQLException e) {
            auditLogger.log("Failed to load player data for UUID " + uuid + ": " + e.getMessage());
        }
    }

    public void save() {
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE players SET username = ?, password = ?, twofa_method = ?, twofa_data = ?, role = ?, last_login = ? WHERE uuid = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, twoFAMethod);
            stmt.setString(4, twoFAData);
            stmt.setString(5, role);
            stmt.setLong(6, lastLogin);
            stmt.setString(7, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            auditLogger.log("Failed to save player data for UUID " + uuid + ": " + e.getMessage());
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTwoFAMethod() {
        return twoFAMethod;
    }

    public void setTwoFAMethod(String twoFAMethod) {
        this.twoFAMethod = twoFAMethod;
    }

    public String getTwoFAData() {
        return twoFAData;
    }

    public void setTwoFAData(String twoFAData) {
        this.twoFAData = twoFAData;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }
}