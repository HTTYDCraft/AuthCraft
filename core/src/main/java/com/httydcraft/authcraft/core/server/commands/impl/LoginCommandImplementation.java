package com.httydcraft.authcraft.core.server.commands.impl;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.crypto.HashInput;
import com.httydcraft.authcraft.api.event.AccountTryLoginEvent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.step.AuthenticationStep;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;

// #region Class Documentation
/**
 * Implementation of the login command logic.
 * Handles authentication attempts and updates account credentials if necessary.
 */
public class LoginCommandImplementation {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final AuthPlugin plugin;
    private final PluginConfig config;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code LoginCommandImplementation}.
     *
     * @param plugin The AuthPlugin instance. Must not be null.
     */
    public LoginCommandImplementation(AuthPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin must not be null");
        this.config = plugin.getConfig();
        LOGGER.atFine().log("Initialized LoginCommandImplementation");
    }
    // #endregion

    // #region Command Logic
    /**
     * Performs a login attempt for a player.
     *
     * @param player The player attempting to log in. Must not be null.
     * @param account The account to authenticate. Must not be null.
     * @param password The password to verify. Must not be null.
     */
    public void performLogin(ServerPlayer player, Account account, String password) {
        Preconditions.checkNotNull(player, "player must not be null");
        Preconditions.checkNotNull(password, "password must not be null");

        if (account == null) {
            SecurityAuditLogger.logFailure("Login", player, "Account is null in implementation");
            player.sendMessage(config.getServerMessages().getMessage("account-not-found"));
            return;
        }

        if (!account.isRegistered()) {
            SecurityAuditLogger.logFailure("Login", player, "Account not registered: " + account.getPlayerId());
            player.sendMessage(config.getServerMessages().getMessage("account-not-registered"));
            return;
        }

        AuthenticationStep currentAuthenticationStep = account.getCurrentAuthenticationStep();
        HashInput passwordInput = HashInput.of(password);
        boolean isWrongPassword = !account.getCryptoProvider().matches(passwordInput, account.getPasswordHash());

        if (isWrongPassword) {
            SecurityAuditLogger.logFailure("Login", player, "Wrong password for account: " + account.getPlayerId());
            player.sendMessage(config.getServerMessages().getMessage("wrong-password"));
            return;
        }

        // Успешный вход
        if (!account.getCryptoProvider().getIdentifier().equals(config.getActiveHashType().getIdentifier())) {
            account.setCryptoProvider(config.getActiveHashType());
            // Можно добавить отдельный лог для смены hash type, если требуется
        }
        currentAuthenticationStep.getAuthenticationStepContext().setCanPassToNextStep(true);
        account.nextAuthenticationStep(plugin.getAuthenticationContextFactoryBucket().createContext(account));
        player.sendMessage(config.getServerMessages().getMessage("login-success"));
        SecurityAuditLogger.logSuccess("Login successful", player, "Login successful for account: " + account.getPlayerId());

        plugin.getEventBus().publish(AccountTryLoginEvent.class, account, isWrongPassword, !isWrongPassword)
                .thenAccept(tryLoginEventPostResult -> {
                    if (tryLoginEventPostResult.getEvent().isCancelled()) {
                        LOGGER.atFine().log("Login cancelled for account: %s", account.getPlayerId());
                        return;
                    }

                    LOGGER.atInfo().log("Login successful for account: %s", account.getPlayerId());
                });
    }
    // #endregion
}