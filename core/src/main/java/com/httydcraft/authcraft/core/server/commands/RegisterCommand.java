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
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;
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
        Preconditions.checkNotNull(password, "password must not be null");
        SecurityAuditLogger.logSuccess("RegisterCommand", player, String.format("Register command started for player: %s", player.getName()));

        if (account == null) {
            SecurityAuditLogger.logFailure("RegisterCommand", player, "Account not found for registration");
            player.sendMessage(config.getServerMessages().getMessage("account-not-found"));
            return;
        }

        RegisterCommandImplementation impl = new RegisterCommandImplementation(plugin);
        try {
            impl.performRegister(player, account, password);
            SecurityAuditLogger.logSuccess("RegisterCommand", player, "Registration completed for account: " + account.getPlayerId());
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("RegisterCommand", player, "Registration failed for account: " + account.getPlayerId() + ", error: " + ex.getMessage());
            throw ex;
        }
    }
    // #endregion
}