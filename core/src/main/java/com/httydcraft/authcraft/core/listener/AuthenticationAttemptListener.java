package com.httydcraft.authcraft.core.listener;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.google.common.collect.Maps;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.message.MessageContext;
import com.httydcraft.authcraft.api.event.AccountStateClearEvent;
import com.httydcraft.authcraft.api.event.AccountTryLoginEvent;
import io.github.revxrsal.eventbus.SubscribeEvent;

import java.util.Map;

// #region Class Documentation
/**
 * Listener for handling authentication attempt events.
 * Tracks login attempts and enforces password attempt limits.
 */
public class AuthenticationAttemptListener {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final Map<String, Integer> loginAttemptCounts = Maps.newHashMap();
    private final AuthPlugin plugin;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AuthenticationAttemptListener}.
     *
     * @param plugin The AuthPlugin instance. Must not be null.
     */
    public AuthenticationAttemptListener(AuthPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin must not be null");
        LOGGER.atFine().log("Initialized AuthenticationAttemptListener");
    }
    // #endregion

    // #region Event Handlers
    /**
     * Handles the {@link AccountTryLoginEvent} to process login attempts.
     * Cancels the event on wrong password, tracks attempts, and disconnects players if the limit is reached.
     *
     * @param event The login attempt event. Must not be null.
     */
    @SubscribeEvent
    public void onLogin(AccountTryLoginEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        Account account = event.getAccount();
        Preconditions.checkNotNull(account, "account must not be null");

        if (event.isRightPassword()) {
            LOGGER.atFine().log("Correct password for account: %s", account.getPlayerId());
            return;
        }

        event.setCancelled(true);
        int maxAttempts = plugin.getConfig().getPasswordAttempts();
        if (maxAttempts < 1) {
            account.getPlayer().ifPresent(player ->
                    player.sendMessage(plugin.getConfig().getServerMessages().getMessage("wrong-password")));
            LOGGER.atFine().log("No attempt limit, sent wrong-password message to account: %s", account.getPlayerId());
            return;
        }

        int loginAttempts = incrementLoginAttemptsAndGet(account);
        account.getPlayer().ifPresent(player -> player.sendMessage(plugin.getConfig()
                .getServerMessages()
                .getMessage("wrong-password",
                        MessageContext.of("%attempts%", Integer.toString(maxAttempts - loginAttempts)))));
        LOGGER.atFine().log("Wrong password for account: %s, attempts: %d", account.getPlayerId(), loginAttempts);

        if (loginAttempts >= maxAttempts) {
            account.getPlayer().ifPresent(player ->
                    player.disconnect(plugin.getConfig().getServerMessages().getMessage("attempts-limit")));
            LOGGER.atInfo().log("Account %s reached attempt limit, disconnected", account.getPlayerId());
        }
    }

    /**
     * Handles the {@link AccountStateClearEvent} to clear login attempt counts.
     *
     * @param event The state clear event. Must not be null.
     */
    @SubscribeEvent
    public void onClear(AccountStateClearEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        event.getAccount()
                .map(Account::getPlayerId)
                .ifPresent(id -> {
                    loginAttemptCounts.remove(id);
                    LOGGER.atFine().log("Cleared login attempts for account: %s", id);
                });
    }
    // #endregion

    // #region Helper Methods
    /**
     * Increments the login attempt count for an account and returns the updated count.
     *
     * @param account The account to track. Must not be null.
     * @return The updated attempt count.
     */
    private int incrementLoginAttemptsAndGet(Account account) {
        Preconditions.checkNotNull(account, "account must not be null");
        String playerId = account.getPlayerId();
        int attempts = loginAttemptCounts.getOrDefault(playerId, 0) + 1;
        loginAttemptCounts.put(playerId, attempts);
        LOGGER.atFine().log("Incremented login attempts for account: %s, new count: %d", playerId, attempts);
        return attempts;
    }
    // #endregion
}