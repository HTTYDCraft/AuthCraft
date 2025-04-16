package com.httydcraft.authcraft.core.task;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.model.AuthenticationTask;
import com.httydcraft.authcraft.api.model.PlayerIdSupplier;
import com.httydcraft.authcraft.api.server.scheduler.ServerScheduler;
import com.httydcraft.authcraft.api.step.AuthenticationStep;
import com.httydcraft.authcraft.api.step.MessageableAuthenticationStep;

import java.util.concurrent.TimeUnit;

// #region Class Documentation
/**
 * Task for sending authentication messages to players.
 * Periodically processes messageable authentication steps.
 */
public class AuthenticationMessageSendTask implements AuthenticationTask {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ServerScheduler proxyScheduler;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AuthenticationMessageSendTask}.
     *
     * @param plugin The AuthPlugin instance. Must not be null.
     */
    public AuthenticationMessageSendTask(AuthPlugin plugin) {
        Preconditions.checkNotNull(plugin, "plugin must not be null");
        this.proxyScheduler = plugin.getCore().schedule(() -> {
            for (String accountPlayerId : plugin.getAuthenticatingAccountBucket().getAccountIdEntries()) {
                Account account = plugin.getAuthenticatingAccountBucket().getAuthenticatingAccountNullable(PlayerIdSupplier.of(accountPlayerId));
                if (account == null) {
                    LOGGER.atFine().log("No account found for player ID: %s", accountPlayerId);
                    continue;
                }
                AuthenticationStep authenticationStep = account.getCurrentAuthenticationStep();
                if (!(authenticationStep instanceof MessageableAuthenticationStep)) {
                    LOGGER.atFine().log("Step %s is not messageable for account: %s", authenticationStep.getStepName(), accountPlayerId);
                    continue;
                }
                account.getPlayer().ifPresent(player -> {
                    ((MessageableAuthenticationStep) authenticationStep).process(player);
                    LOGGER.atFine().log("Processed message for player: %s, step: %s", player.getNickname(), authenticationStep.getStepName());
                });
            }
        }, 0, plugin.getConfig().getMessagesDelay(), TimeUnit.SECONDS);
        LOGGER.atFine().log("Initialized AuthenticationMessageSendTask with delay: %d seconds", plugin.getConfig().getMessagesDelay());
    }
    // #endregion

    // #region AuthenticationTask Implementation
    /**
     * Stops the task by canceling the scheduler.
     */
    @Override
    public void stop() {
        proxyScheduler.cancel();
        LOGGER.atInfo().log("Stopped AuthenticationMessageSendTask");
    }
    // #endregion
}