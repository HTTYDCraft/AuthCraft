package com.httydcraft.authcraft.core.task;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.model.AuthenticationTask;
import com.httydcraft.authcraft.api.model.PlayerIdSupplier;
import com.httydcraft.authcraft.api.server.scheduler.ServerScheduler;

import java.util.Date;
import java.util.concurrent.TimeUnit;

// #region Class Documentation
/**
 * Task for handling authentication timeouts.
 * Kicks players who exceed the authentication time limit.
 */
public class AuthenticationTimeoutTask implements AuthenticationTask {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ServerScheduler proxyScheduler;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AuthenticationTimeoutTask}.
     *
     * @param plugin The AuthPlugin instance. Must not be null.
     */
    public AuthenticationTimeoutTask(AuthPlugin plugin) {
        Preconditions.checkNotNull(plugin, "plugin must not be null");
        this.proxyScheduler = plugin.getCore().schedule(() -> {
            long now = System.currentTimeMillis();
            for (String accountPlayerId : plugin.getAuthenticatingAccountBucket().getAccountIdEntries()) {
                Account account = plugin.getAuthenticatingAccountBucket()
                        .getAuthenticatingAccountNullable(PlayerIdSupplier.of(accountPlayerId));
                if (account == null) {
                    LOGGER.atFine().log("No account found for player ID: %s", accountPlayerId);
                    continue;
                }
                if (now >= account.getAuthenticationTimeout()) {
                    account.getPlayer().ifPresent(player -> {
                        player.kick(plugin.getConfig().getBossBarSettings()
                                .getMessages().getMessage("timeout", new Date(account.getAuthenticationTimeout())));
                        LOGGER.atFine().log("Kicked player ID: %s due to authentication timeout", accountPlayerId);
                    });
                    plugin.getAuthenticatingAccountBucket().removeAuthenticatingAccount(account);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
        LOGGER.atInfo().log("Initialized AuthenticationTimeoutTask");
    }
    // #endregion

    // #region AuthenticationTask Implementation
    /**
     * Stops the task by canceling the scheduler.
     */
    @Override
    public void stop() {
        proxyScheduler.cancel();
        LOGGER.atInfo().log("Stopped AuthenticationTimeoutTask");
    }
    // #endregion
}