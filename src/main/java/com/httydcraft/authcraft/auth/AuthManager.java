package com.httydcraft.authcraft.auth;

import com.httydcraft.authcraft.AuthCraft;
import com.httydcraft.authcraft.database.DatabaseManager;
import com.httydcraft.authcraft.utils.UtilsManager;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthManager {
    private final AuthCraft plugin;
    private final DatabaseManager databaseManager;
    private final UtilsManager utilsManager;
    private final ConcurrentHashMap<UUID, Long> authenticatedPlayers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> lockoutTimes = new ConcurrentHashMap<>();

    public AuthManager(AuthCraft plugin, DatabaseManager databaseManager, UtilsManager utilsManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.utilsManager = utilsManager;
    }

    public void register(Player player, String password) {
        UUID uuid = player.getUniqueId();
        if (authenticatedPlayers.containsKey(uuid)) {
            utilsManager.getMessageUtils().send(player, "register.already_authenticated");
            return;
        }
        if (isRegistered(uuid)) {
            utilsManager.getMessageUtils().send(player, "register.already_registered");
            return;
        }
        if (!utilsManager.getPasswordValidator().isValid(password, player.getName())) {
            utilsManager.getMessageUtils().send(player, "register.invalid_password");
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO players (uuid, username, password, role, last_login) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, player.getName());
            stmt.setString(3, hashedPassword);
            stmt.setString(4, "PLAYER");
            stmt.setLong(5, System.currentTimeMillis());
            stmt.executeUpdate();
            authenticatedPlayers.put(uuid, System.currentTimeMillis());
            plugin.removeLimboEffects(player);
            utilsManager.getMessageUtils().send(player, "register.success");
            utilsManager.getAuditLogger().log("Player " + player.getName() + " (" + uuid + ") registered.");
        } catch (SQLException e) {
            utilsManager.getAuditLogger().log("Database error during registration for " + player.getName() + ": " + e.getMessage());
            utilsManager.getMessageUtils().send(player, "error.database");
        }
    }

    public void login(Player player, String password) {
        UUID uuid = player.getUniqueId();
        if (authenticatedPlayers.containsKey(uuid)) {
            utilsManager.getMessageUtils().send(player, "login.already_authenticated");
            return;
        }
        if (lockoutTimes.getOrDefault(uuid, 0L) > System.currentTimeMillis()) {
            utilsManager.getMessageUtils().send(player, "login.too_many_attempts");
            return;
        }

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT password, twofa_method, twofa_data FROM players WHERE uuid = ?");
             ResultSet rs = stmt.executeQuery()) {
            stmt.setString(1, uuid.toString());
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    loginAttempts.remove(uuid);
                    String twoFAMethod = rs.getString("twofa_method");
                    String twoFAData = rs.getString("twofa_data");
                    if (twoFAMethod != null && !twoFAMethod.isEmpty()) {
                        if (twoFAMethod.equals("TOTP")) {
                            utilsManager.getCacheManager().put("2fa_pending:" + uuid, twoFAData);
                            utilsManager.getMessageUtils().send(player, "login.2fa_required");
                        } else if (twoFAMethod.equals("TELEGRAM") || twoFAMethod.equals("VK")) {
                            plugin.getBotManager().requestBotLogin(player, twoFAMethod, twoFAData);
                        }
                    } else {
                        completeLogin(player);
                    }
                } else {
                    handleFailedLogin(player);
                }
            } else {
                utilsManager.getMessageUtils().send(player, "login.invalid_credentials");
            }
        } catch (SQLException e) {
            utilsManager.getAuditLogger().log("Database error during login for " + player.getName() + ": " + e.getMessage());
            utilsManager.getMessageUtils().send(player, "error.database");
        }
    }

    private void handleFailedLogin(Player player) {
        UUID uuid = player.getUniqueId();
        int attempts = loginAttempts.merge(uuid, 1, Integer::sum);
        if (attempts >= plugin.getConfig().getInt("security.max_attempts", 5)) {
            lockoutTimes.put(uuid, System.currentTimeMillis() + plugin.getConfig().getLong("security.block_duration", 900000));
            loginAttempts.remove(uuid);
            utilsManager.getMessageUtils().send(player, "login.too_many_attempts");
            plugin.getBotManager().notifyAdmin("Too many login attempts for " + player.getName() + " (" + uuid + ").");
        } else {
            utilsManager.getMessageUtils().send(player, "login.invalid_credentials");
        }
        utilsManager.getAuditLogger().log("Failed login attempt for " + player.getName() + " (" + uuid + ").");
    }

    public void completeLogin(Player player) {
        UUID uuid = player.getUniqueId();
        authenticatedPlayers.put(uuid, System.currentTimeMillis());
        plugin.restorePlayerState(player);
        plugin.removeLimboEffects(player);
        utilsManager.getMessageUtils().send(player, "login.success");
        utilsManager.getAuditLogger().log("Player " + player.getName() + " (" + uuid + ") logged in.");
    }

    public void changePassword(Player player, String oldPassword, String newPassword) {
        UUID uuid = player.getUniqueId();
        if (!authenticatedPlayers.containsKey(uuid)) {
            utilsManager.getMessageUtils().send(player, "changepassword.not_authenticated");
            return;
        }
        if (!utilsManager.getPasswordValidator().isValid(newPassword, player.getName())) {
            utilsManager.getMessageUtils().send(player, "changepassword.invalid_password");
            return;
        }

        try (Connection conn = databaseManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT password FROM players WHERE uuid = ?");
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && BCrypt.checkpw(oldPassword, rs.getString("password"))) {
                String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
                databaseManager.closeResources(null, stmt, rs);
                stmt = conn.prepareStatement("UPDATE players SET password = ? WHERE uuid = ?");
                stmt.setString(1, newHashedPassword);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
                utilsManager.getMessageUtils().send(player, "changepassword.success");
                utilsManager.getAuditLogger().log("Player " + player.getName() + " (" + uuid + ") changed password.");
            } else {
                utilsManager.getMessageUtils().send(player, "changepassword.invalid_old_password");
            }
            databaseManager.closeResources(null, stmt, rs);
        } catch (SQLException e) {
            utilsManager.getAuditLogger().log("Database error during password change for " + player.getName() + ": " + e.getMessage());
            utilsManager.getMessageUtils().send(player, "error.database");
        }
    }

    public void logout(Player player) {
        UUID uuid = player.getUniqueId();
        if (!authenticatedPlayers.containsKey(uuid)) {
            utilsManager.getMessageUtils().send(player, "logout.not_authenticated");
            return;
        }
        authenticatedPlayers.remove(uuid);
        plugin.storePlayerState(player);
        plugin.applyLimboEffects(player);
        utilsManager.getMessageUtils().send(player, "logout.success");
        utilsManager.getAuditLogger().log("Player " + player.getName() + " (" + uuid + ") logged out.");
    }

    public boolean isRegistered(UUID uuid) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM players WHERE uuid = ?");
             ResultSet rs = stmt.executeQuery()) {
            stmt.setString(1, uuid.toString());
            return rs.next();
        } catch (SQLException e) {
            utilsManager.getAuditLogger().log("Database error checking registration for UUID " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean isAuthenticated(UUID uuid) {
        Long loginTime = authenticatedPlayers.get(uuid);
        if (loginTime == null) {
            return false;
        }
        long timeout = plugin.getConfig().getLong("session.timeout", 1800000);
        return System.currentTimeMillis() - loginTime < timeout;
    }

    public int getAuthenticatedPlayersCount() {
        return authenticatedPlayers.size();
    }

    public int getLockedAccountsCount() {
        return lockoutTimes.size();
    }
}