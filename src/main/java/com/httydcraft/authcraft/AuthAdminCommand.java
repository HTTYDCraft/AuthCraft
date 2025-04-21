package com.httydcraft.authcraft;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuthAdminCommand implements CommandExecutor {
    private final AuthManager authManager;
    private final MessageUtils messageUtils;

    public AuthAdminCommand(AuthManager authManager, MessageUtils messageUtils) {
        this.authManager = authManager;
        this.messageUtils = messageUtils;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("authcraft.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /authadmin <resetpw|disable2fa> <player>");
            return true;
        }
        String action = args[0].toLowerCase();
        String targetName = args[1];
        Bukkit.getScheduler().runTaskAsynchronously(authManager.getPlugin(), () -> {
            String identifier = targetName;
            if (!authManager.isRegistered(identifier)) {
                sender.sendMessage("§cPlayer not found in database.");
                return;
            }
            if (action.equals("resetpw")) {
                boolean ok = authManager.adminResetPassword(identifier);
                if (ok) {
                    sender.sendMessage("§aPassword for " + targetName + " has been reset. The player must register again.");
                } else {
                    sender.sendMessage("§cFailed to reset password.");
                }
            } else if (action.equals("disable2fa")) {
                boolean ok = authManager.adminDisable2FA(identifier);
                if (ok) {
                    sender.sendMessage("§a2FA for " + targetName + " has been disabled.");
                } else {
                    sender.sendMessage("§cFailed to disable 2FA.");
                }
            } else {
                sender.sendMessage("§cUnknown action. Use resetpw or disable2fa.");
            }
        });
        return true;
    }
}
