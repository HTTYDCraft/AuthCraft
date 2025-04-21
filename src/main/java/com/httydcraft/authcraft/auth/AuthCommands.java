package com.httydcraft.authcraft.auth;

import com.httydcraft.authcraft.AuthCraft;
import com.httydcraft.authcraft.utils.UtilsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthCommands implements CommandExecutor {
    private final AuthCraft plugin;
    private final AuthManager authManager;
    private final UtilsManager utilsManager;

    public AuthCommands(AuthCraft plugin, AuthManager authManager, UtilsManager utilsManager) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.utilsManager = utilsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            utilsManager.getMessageUtils().send(sender, "command.player_only");
            return true;
        }

        Player player = (Player) sender;
        String commandName = command.getName().toLowerCase();

        switch (commandName) {
            case "register":
                if (args.length != 1) {
                    utilsManager.getMessageUtils().send(player, "register.usage");
                    return true;
                }
                authManager.register(player, args[0]);
                break;
            case "login":
                if (args.length != 1) {
                    utilsManager.getMessageUtils().send(player, "login.usage");
                    return true;
                }
                authManager.login(player, args[0]);
                break;
            case "changepassword":
                if (args.length != 2) {
                    utilsManager.getMessageUtils().send(player, "changepassword.usage");
                    return true;
                }
                authManager.changePassword(player, args[0], args[1]);
                break;
            case "logout":
                if (args.length != 0) {
                    utilsManager.getMessageUtils().send(player, "logout.usage");
                    return true;
                }
                authManager.logout(player);
                break;
            case "authcraft":
                if (!player.hasPermission("authcraft.admin")) {
                    utilsManager.getMessageUtils().send(player, "no_permission");
                    return true;
                }
                if (args.length != 1) {
                    utilsManager.getMessageUtils().send(player, "authcraft.usage");
                    return true;
                }
                handleAdminCommand(player, args[0].toLowerCase());
                break;
        }
        return true;
    }

    private void handleAdminCommand(Player player, String subcommand) {
        switch (subcommand) {
            case "backup":
                plugin.getDatabaseManager().backupDatabase();
                utilsManager.getMessageUtils().send(player, "authcraft.backup_success");
                break;
            case "reload":
                plugin.reload();
                utilsManager.getMessageUtils().send(player, "authcraft.reload_success");
                break;
            case "stats":
                displayStats(player);
                break;
            default:
                utilsManager.getMessageUtils().send(player, "authcraft.usage");
        }
    }

    private void displayStats(Player player) {
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM players");
             ResultSet rs = stmt.executeQuery()) {
            int registeredPlayers = rs.next() ? rs.getInt("count") : 0;
            String stats = String.format("=== AuthCraft Statistics ===\nRegistered Players: %d\nActive Sessions: %d\nLocked Accounts: %d",
                    registeredPlayers, authManager.getAuthenticatedPlayersCount(), authManager.getLockedAccountsCount());
            player.sendMessage(stats);
            utilsManager.getAuditLogger().log("Player " + player.getName() + " viewed statistics.");
        } catch (SQLException e) {
            utilsManager.getAuditLogger().log("Database error retrieving stats: " + e.getMessage());
            utilsManager.getMessageUtils().send(player, "error.database");
        }
    }
}