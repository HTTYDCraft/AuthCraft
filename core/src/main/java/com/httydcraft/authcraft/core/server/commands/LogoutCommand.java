package com.httydcraft.authcraft.core.server.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
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

// #region Class Documentation
/**
 * Command for logging out a player.
 * Terminates the player's session and re-authenticates.
 */
@Command("logout")
public class LogoutCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
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
        LOGGER.atFine().log("Executing logout for player: %s", player.getNickname());

        String id = config.getActiveIdentifierType().getId(player);
        if (authenticatingAccountBucket.isAuthenticating(player)) {
            player.sendMessage(config.getServerMessages().getMessage("already-logged-out"));
            LOGGER.atFine().log("Player %s already in authentication", player.getNickname());
            return;
        }

        eventBus.publish(PlayerLogoutEvent.class, player, false).thenAccept(result -> {
            if (result.getEvent().isCancelled()) {
                LOGGER.atFine().log("Logout cancelled for player: %s", player.getNickname());
                return;
            }

            accountStorage.getAccount(id).thenAccept(account -> {
                account.logout(config.getSessionDurability());
                accountStorage.saveOrUpdateAccount(account);
                authenticatingAccountBucket.addAuthenticatingAccount(account);
                account.nextAuthenticationStep(
                        AuthPlugin.instance().getAuthenticationContextFactoryBucket().createContext(account));
                player.sendMessage(config.getServerMessages().getMessage("logout-success"));
                config.findServerInfo(config.getAuthServers()).asProxyServer().sendPlayer(player);
                LOGGER.atInfo().log("Logged out player: %s", player.getNickname());
            });
        });
    }
    // #endregion
}