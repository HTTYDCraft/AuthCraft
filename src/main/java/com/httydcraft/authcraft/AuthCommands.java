package com.httydcraft.authcraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class AuthCommands implements CommandExecutor {
    private final AuthCraft plugin;
    private final AuthManager authManager;
    private final MessageUtils messageUtils;

    public AuthCommands(AuthCraft plugin, AuthManager authManager, UtilsManager utilsManager) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.messageUtils = utilsManager.getMessageUtils();
    }

    public void register() {
        plugin.getCommand("register").setExecutor(this);
        plugin.getCommand("login").setExecutor(this);
        plugin.getCommand("changepassword").setExecutor(this);
        plugin.getCommand("logout").setExecutor(this);
        plugin.getCommand("authcraft").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            messageUtils.sendMessage(sender, "command.player_only");
            return true;
        }
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("register")) {
            if (args.length != 2) {
                messageUtils.sendMessage(player, "register.usage");
                return true;
            }
            if (!args[0].equals(args[1])) {
                messageUtils.sendMessage(player, "register.password_mismatch");
                return true;
            }
            authManager.register(player, args[0]);
        } else if (command.getName().equalsIgnoreCase("login")) {
            if (args.length != 1) {
                messageUtils.sendMessage(player, "login.usage");
                return true;
            }
            authManager.login(player, args[0]);
        } else if (command.getName().equalsIgnoreCase("changepassword")) {
            if (args.length != 2) {
                messageUtils.sendMessage(player, "changepassword.usage");
                return true;
            }
            authManager.changePassword(player, args[0], args[1]);
        } else if (command.getName().equalsIgnoreCase("logout")) {
            if (args.length != 0) {
                messageUtils.sendMessage(player, "logout.usage");
                return true;
            }
            authManager.logout(player);
        } else if (command.getName().equalsIgnoreCase("authcraft")) {
            if (!player.hasPermission("authcraft.admin")) {
                messageUtils.sendMessage(player, "no_permission");
                return true;
            }
            if (args.length != 1 || !args[0].matches("backup|reload|stats")) {
                messageUtils.sendMessage(player, "authcraft.usage");
                return true;
            }
            if (args[0].equals("backup")) {
                try {
                    plugin.getDatabaseManager().backup();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                messageUtils.sendMessage(player, "authcraft.backup_success");
            } else if (args[0].equals("reload")) {
                plugin.reloadConfig();
                messageUtils.sendMessage(player, "authcraft.reload_success");
            } else {
                messageUtils.sendMessage(player, "authcraft.stats");
            }
        }
        return true;
    }
}
