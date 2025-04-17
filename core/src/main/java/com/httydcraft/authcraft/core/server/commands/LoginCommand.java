package com.httydcraft.authcraft.core.server.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.core.server.commands.annotations.AuthenticationStepCommand;
import com.httydcraft.authcraft.core.server.commands.impl.LoginCommandImplementation;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.core.step.impl.LoginAuthenticationStep;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;

// #region Class Documentation
/**
 * Command for player login.
 * Delegates to {@link LoginCommandImplementation} for authentication logic.
 */
@Command({"l", "login"})
public class LoginCommand {
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
     * Performs a login attempt.
     *
     * @param player The player logging in. Must not be null.
     * @param account The account to authenticate. Must not be null.
     * @param password The password to verify. Must not be null.
     */
    @AuthenticationStepCommand(stepName = LoginAuthenticationStep.STEP_NAME)
    @DefaultFor({"l", "login"})
    public void login(ServerPlayer player, Account account, String password) {
        Preconditions.checkNotNull(player, "player must not be null");
        Preconditions.checkNotNull(account, "account must not be null");
        Preconditions.checkNotNull(password, "password must not be null");
        SecurityAuditLogger.logSuccess("Login command executed", player, "Attempting login");

        LoginCommandImplementation impl = new LoginCommandImplementation(plugin);
        impl.performLogin(player, account, password);
        SecurityAuditLogger.logSuccess("Delegated login to implementation", player, "Delegated for account: " + account.getPlayerId());
    }
    // #endregion
}