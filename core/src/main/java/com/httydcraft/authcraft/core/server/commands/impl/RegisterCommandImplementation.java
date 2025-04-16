package com.httydcraft.authcraft.core.server.commands.impl;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.crypto.HashInput;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.event.AccountRegisterEvent;
import com.httydcraft.authcraft.core.server.commands.annotations.AuthenticationAccount;
import com.httydcraft.authcraft.core.server.commands.parameters.RegisterPassword;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.step.AuthenticationStep;

// #region Class Documentation
/**
 * Implementation of the register command logic.
 * Handles account registration and persists account data.
 */
public class RegisterCommandImplementation {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final AuthPlugin plugin;
    private final PluginConfig config;
    private final AccountDatabase accountStorage;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code RegisterCommandImplementation}.
     *
     * @param plugin The AuthPlugin instance. Must not be null.
     */
    public RegisterCommandImplementation(AuthPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin must not be null");
        this.config = plugin.getConfig();
        this.accountStorage = plugin.getAccountDatabase();
        LOGGER.atFine().log("Initialized RegisterCommandImplementation");
    }
    // #endregion

    // #region Command Logic
    /**
     * Performs a registration attempt for a player.
     *
     * @param player The player registering. Must not be null.
     * @param account The account to register. Must not be null.
     * @param password The registration password. Must not be null.
     */
    public void performRegister(ServerPlayer player, @AuthenticationAccount Account account, RegisterPassword password) {
        Preconditions.checkNotNull(player, "player must not be null");
        Preconditions.checkNotNull(account, "account must not be null");
        Preconditions.checkNotNull(password, "password must not be null");

        AuthenticationStep currentAuthenticationStep = account.getCurrentAuthenticationStep();
        LOGGER.atFine().log("Processing registration for account: %s", account.getPlayerId());

        plugin.getEventBus().publish(AccountRegisterEvent.class, account, false).thenAccept(registerEventPostResult -> {
            if (registerEventPostResult.getEvent().isCancelled()) {
                LOGGER.atFine().log("Registration cancelled for account: %s", account.getPlayerId());
                return;
            }

            currentAuthenticationStep.getAuthenticationStepContext().setCanPassToNextStep(true);

            if (!account.getCryptoProvider().getIdentifier().equals(config.getActiveHashType().getIdentifier())) {
                account.setCryptoProvider(config.getActiveHashType());
                LOGGER.atInfo().log("Updated hash type for account: %s", account.getPlayerId());
            }

            account.setPasswordHash(account.getCryptoProvider().hash(HashInput.of(password.getPassword())));
            accountStorage.saveOrUpdateAccount(account);

            account.nextAuthenticationStep(plugin.getAuthenticationContextFactoryBucket().createContext(account));
            player.sendMessage(config.getServerMessages().getMessage("register-success"));
            LOGGER.atInfo().log("Registration successful for account: %s", account.getPlayerId());
        });
    }
    // #endregion
}