package com.httydcraft.authcraft.core.server.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.core.server.commands.annotations.AuthenticationAccount;
import com.httydcraft.authcraft.core.server.commands.annotations.AuthenticationStepCommand;
import com.httydcraft.authcraft.core.server.commands.impl.RegisterCommandImplementation;
import com.httydcraft.authcraft.core.server.commands.parameters.RegisterPassword;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.core.step.impl.RegisterAuthenticationStep;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;

// #region Class Documentation
/**
 * Command for registering a new account.
 * Delegates to {@link RegisterCommandImplementation} for registration logic.
 */
@Command({"reg", "register"})
public class RegisterCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    @Dependency
    private AuthPlugin plugin;
    @Dependency
    private PluginConfig config;
    @Dependency
    private AccountDatabase accountStorage;
    // #endregion

    // #region Command Logic
    /**
     * Registers a new account for a player.
     *
     * @param player The player registering. Must not be null.
     * @param account The account to register. Must not be null.
     * @param password The registration password. Must not be null.
     */
    @AuthenticationStepCommand(stepName = RegisterAuthenticationStep.STEP_NAME)
    @DefaultFor({"reg", "register"})
    public void register(ServerPlayer player, @AuthenticationAccount Account account, RegisterPassword password) {
        Preconditions.checkNotNull(player, "player must not be null");
        Preconditions.checkNotNull(account, "account must not be null");
        Preconditions.checkNotNull(password, "password must not be null");
        LOGGER.atFine().log("Executing register for player: %s", player.getNickname());

        RegisterCommandImplementation impl = new RegisterCommandImplementation(plugin);
        impl.performRegister(player, account, password);
        LOGGER.atFine().log("Delegated registration to implementation for account: %s", account.getPlayerId());
    }
    // #endregion
}