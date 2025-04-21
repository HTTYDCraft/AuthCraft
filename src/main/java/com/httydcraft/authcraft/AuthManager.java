package com.httydcraft.authcraft;

import com.httydcraft.authcraft.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthManager {
    private final AuthCraft plugin;
    private final DatabaseManager databaseManager;
    private final MessageUtils messageUtils;
    private final PasswordValidator passwordValidator;
    private final CloudflareWarpChecker warpChecker;
    private final Map<UUID, PlayerState> playerStates;
    private final boolean useNickname;

    public AuthManager(AuthCraft plugin, DatabaseManager databaseManager, UtilsManager utilsManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.messageUtils = utilsManager.getMessageUtils();
        this.passwordValidator = utilsManager.getPasswordValidator();
        this.warpChecker = utilsManager.getCloudflareWarpChecker();
        this.playerStates = new HashMap<>();
        this.useNickname = plugin.getConfig().getString("auth.method", "uuid").equalsIgnoreCase("nickname");
    }

    public void register(Player player, String password) {
        if (playerStates.get(player.getUniqueId()) == PlayerState.AUTHENTICATED) {
            messageUtils.sendMessage(player, "register.already_authenticated");
            return;
        }
        String identifier = useNickname ? player.getName() : player.getUniqueId().toString();
        if (isRegistered(identifier)) {
            messageUtils.sendMessage(player, "register.already_registered");
            return;
        }
        if (!passwordValidator.isValidPassword(password, player.getName())) {
            messageUtils.sendMessage(player, "register.invalid_password");
            return;
        }
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO players (identifier, username, password, role, last_login) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, identifier);
            stmt.setString(2, player.getName());
            stmt.setString(3, hashedPassword);
            stmt.setString(4, "player");
            stmt.setLong(5, System.currentTimeMillis());
            stmt.executeUpdate();
            playerStates.put(player.getUniqueId(), PlayerState.AUTHENTICATED);
            messageUtils.sendMessage(player, "register.success");
            teleportToMainWorld(player);
            plugin.getRoleManager().assignRole(player, "player");
        } catch (SQLException e) {
            messageUtils.sendMessage(player, "error.database");
        }
    }

    public void login(Player player, String password) {
        if (playerStates.get(player.getUniqueId()) == PlayerState.AUTHENTICATED) {
            messageUtils.sendMessage(player, "login.already_authenticated");
            return;
        }
        String identifier = useNickname ? player.getName() : player.getUniqueId().toString();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT password, twofa_method, twofa_data FROM players WHERE identifier = ?")) {
            stmt.setString(1, identifier);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                messageUtils.sendMessage(player, "login.invalid_credentials");
                return;
            }
            String hashedPassword = rs.getString("password");
            if (!BCrypt.checkpw(password, hashedPassword)) {
                messageUtils.sendMessage(player, "login.invalid_credentials");
                return;
            }
            String twofaMethod = rs.getString("twofa_method");
            String twofaData = rs.getString("twofa_data");
            if (twofaMethod != null && !twofaMethod.isEmpty()) {
                playerStates.put(player.getUniqueId(), PlayerState.PENDING_2FA);
                messageUtils.sendMessage(player, "login.2fa_required");
                if (twofaMethod.equals("TELEGRAM") || twofaMethod.equals("VK")) {
                    plugin.getBotManager().send2FACode(player, twofaMethod, twofaData);
                }
            } else {
                playerStates.put(player.getUniqueId(), PlayerState.AUTHENTICATED);
                messageUtils.sendMessage(player, "login.success");
                teleportToMainWorld(player);
                updateLastLogin(identifier);
            }
        } catch (SQLException e) {
            messageUtils.sendMessage(player, "error.database");
        }
    }

    public void changePassword(Player player, String oldPassword, String newPassword) {
        if (playerStates.get(player.getUniqueId()) != PlayerState.AUTHENTICATED) {
            messageUtils.sendMessage(player, "changepassword.not_authenticated");
            return;
        }
        String identifier = useNickname ? player.getName() : player.getUniqueId().toString();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT password FROM players WHERE identifier = ?")) {
            stmt.setString(1, identifier);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next() || !BCrypt.checkpw(oldPassword, rs.getString("password"))) {
                messageUtils.sendMessage(player, "changepassword.invalid_old_password");
                return;
            }
            if (!passwordValidator.isValidPassword(newPassword, player.getName())) {
                messageUtils.sendMessage(player, "changepassword.invalid_password");
                return;
            }
            try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE players SET password = ? WHERE identifier = ?")) {
                updateStmt.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
                updateStmt.setString(2, identifier);
                updateStmt.executeUpdate();
                messageUtils.sendMessage(player, "changepassword.success");
            }
        } catch (SQLException e) {
            messageUtils.sendMessage(player, "error.database");
        }
    }

    public void logout(Player player) {
        if (playerStates.get(player.getUniqueId()) != PlayerState.AUTHENTICATED) {
            messageUtils.sendMessage(player, "logout.not_authenticated");
            return;
        }
        playerStates.put(player.getUniqueId(), PlayerState.UNAUTHENTICATED);
        messageUtils.sendMessage(player, "logout.success");
        player.teleport(Bukkit.getWorld("limbo").getSpawnLocation());
    }

    public boolean isRegistered(String identifier) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM players WHERE identifier = ?")) {
            stmt.setString(1, identifier);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public PlayerState getPlayerState(UUID uuid) {
        return playerStates.getOrDefault(uuid, PlayerState.UNAUTHENTICATED);
    }

    public void setPlayerState(UUID uuid, PlayerState state) {
        playerStates.put(uuid, state);
    }

    public boolean checkWarp(Player player) {
        String mode = plugin.getConfig().getString("cloudflare_warp.mode", "any").toLowerCase();
        if (mode.equals("any")) {
            return true;
        }
        boolean isUsingWarp = warpChecker.isUsingWarp(player.getAddress().getAddress().getHostAddress());
        if (mode.equals("required") && !isUsingWarp) {
            messageUtils.sendMessage(player, "cloudflare_warp.required");
            return false;
        }
        if (mode.equals("disabled") && isUsingWarp) {
            messageUtils.sendMessage(player, "cloudflare_warp.disabled");
            return false;
        }
        return true;
    }

    void teleportToMainWorld(Player player) {
        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
    }

    private void updateLastLogin(String identifier) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE players SET last_login = ? WHERE identifier = ?")) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, identifier);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Silent fail
        }
    }

    // Вызывается ботом при успешном подтверждении входа
    public void approveLogin(UUID playerId) {
        // Логика разблокировки входа, например:
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            // Снять лимбо/разрешить авторизацию
            setPlayerState(playerId, PlayerState.AUTHENTICATED);
            player.sendMessage("§aВход подтверждён через Telegram/VK!");
        }
    }

    // --- Админские методы ---
    public boolean adminResetPassword(String identifier) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE players SET password = NULL WHERE identifier = ?")) {
            stmt.setString(1, identifier);
            int updated = stmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean adminDisable2FA(String identifier) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE players SET twofa_method = NULL, twofa_data = NULL WHERE identifier = ?")) {
            stmt.setString(1, identifier);
            int updated = stmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public AuthCraft getPlugin() {
        return plugin;
    }
}
