package com.httydcraft.authcraft.core.task;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.link.user.entry.LinkEntryUser;
import com.httydcraft.authcraft.api.model.AuthenticationTask;
import com.httydcraft.authcraft.api.model.PlayerIdSupplier;
import com.httydcraft.authcraft.api.server.scheduler.ServerScheduler;
import com.httydcraft.authcraft.core.config.message.server.ServerMessageContext;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

// #region Class Documentation
/**
 * Task for handling authentication timeouts.
 * Kicks players who exceed the authentication time limit.
 */
public class AuthenticationTimeoutTask implements AuthenticationTask {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ServerScheduler scheduler;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AuthenticationTimeoutTask}.
     *
     * @param plugin The AuthPlugin instance. Must not be null.
     */
    public AuthenticationTimeoutTask(AuthPlugin plugin) {
        Preconditions.checkNotNull(plugin, "plugin must not be null");
        this.scheduler = plugin.getCore().schedule(() -> {
            long now = System.currentTimeMillis();
            long authTimeoutMillis = plugin.getConfig().getAuthTime();

            Collection<String> playerIds = plugin.getAuthenticatingAccountBucket().getAccountIdEntries();
            for (String playerId : playerIds) {
                PlayerIdSupplier playerIdSupplier = PlayerIdSupplier.of(playerId);
                Account account = plugin.getAuthenticatingAccountBucket().getAuthenticatingAccountNullable(playerIdSupplier);
                int accountEnterElapsedMillis = (int) (now - plugin.getAuthenticatingAccountBucket().getEnterTimestampOrZero(playerIdSupplier));

                for (LinkEntryUser entryUser : plugin.getLinkEntryBucket().find(user -> user.getAccount().getPlayerId().equals(account.getPlayerId())))
                    if (entryUser != null)
                        try {
                            authTimeoutMillis += entryUser.getLinkType().getSettings().getEnterSettings().getEnterDelay();
                        } catch(UnsupportedOperationException ignored) { // If link type has no settings support
                        }

                if(!account.getPlayer().isPresent())
                    plugin.getAuthenticatingAccountBucket().modifiable().removeIf(state -> state.getPlayerId().equals(playerId));

                if (accountEnterElapsedMillis < authTimeoutMillis)
                    continue;
                account.getPlayer()
                        .ifPresent(
                                player -> player.disconnect(plugin.getConfig().getServerMessages().getMessage("time-left", new ServerMessageContext(account))));
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
    // #endregion

    // #region AuthenticationTask Implementation
    /**
     * Stops the task by canceling the scheduler.
     */
    @Override
    public void stop() {
        scheduler.cancel();
        LOGGER.atInfo().log("Stopped AuthenticationTimeoutTask");
    }
    // #endregion
}