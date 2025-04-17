package com.httydcraft.authcraft.core.server.commands;

import com.google.common.base.Preconditions;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.bucket.AuthenticatingAccountBucket;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.event.PlayerLogoutEvent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import io.github.revxrsal.eventbus.EventBus;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;

// #region Class Documentation
/**
 * Command for logging out a player.
 * Terminates the player's session and re-authenticates.
 */
@Command("logout")
public class LogoutCommand {
    @Dependency
    private AuthenticatingAccountBucket authenticatingAccountBucket;
    @Dependency
    private EventBus eventBus;
    @Dependency
    private PluginConfig config;
    @Dependency
    private AccountDatabase accountStorage;

    // #endregion

    // #region Command Logic
    /**
     * Logs out a player.
     *
     * @param player The player to log out. Must not be null.
     */
    @DefaultFor("logout")
    public void logout(ServerPlayer player) {
        Preconditions.checkNotNull(player, "player must not be null");
        SecurityAuditLogger.logSuccess("Logout command executed", player, "Attempting logout");

        String id = config.getActiveIdentifierType().getId(player);
        if (authenticatingAccountBucket.isAuthenticating(player)) {
            player.sendMessage(config.getServerMessages().getMessage("already-logged-out"));
            SecurityAuditLogger.logFailure("Logout", player, "Already in authentication (already logged out)");
            return;
        }

        eventBus.publish(PlayerLogoutEvent.class, player, false).thenAccept(result -> {
            if (result.getEvent().isCancelled()) {
                SecurityAuditLogger.logFailure("Logout", player, "Logout cancelled by event");
                return;
            }

            accountStorage.getAccount(id).thenAccept(account -> {
                if (account == null || !account.isRegistered()) {
                    player.sendMessage(config.getServerMessages().getMessage("account-not-found"));
                    SecurityAuditLogger.logFailure("Logout", player, "Account not found or not registered");
                    return;
                }
                account.logout(config.getSessionDurability());
                accountStorage.saveOrUpdateAccount(account);
                authenticatingAccountBucket.addAuthenticatingAccount(account);
                account.nextAuthenticationStep(
                        AuthPlugin.instance().getAuthenticationContextFactoryBucket().createContext(account));
                player.sendMessage(config.getServerMessages().getMessage("logout-success"));
                config.findServerInfo(config.getAuthServers()).asProxyServer().sendPlayer(player);
                SecurityAuditLogger.logSuccess("Logout", player, "Logout successful");
            });
        });
    }
    // #endregion
}