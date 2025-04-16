package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.crypto.HashInput;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.event.AccountTryChangePasswordEvent;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.commands.annotation.ConfigurationArgumentError;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.core.server.commands.parameters.NewPassword;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.orphan.OrphanCommand;

// #region Class Documentation
/**
 * Command for changing an account's password.
 * Publishes an event to handle the change and updates the account.
 */
@CommandKey(ChangePasswordCommand.CONFIGURATION_KEY)
public class ChangePasswordCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "change-pass";

    @Dependency
    private AuthPlugin plugin;
    @Dependency
    private AccountDatabase accountStorage;
    // #endregion

    // #region Command Execution
    /**
     * Executes the password change command, updating the account with a new password.
     *
     * @param actorWrapper The actor executing the command. Must not be null.
     * @param linkType     The link type associated with the command. Must not be null.
     * @param account      The account to change the password for. Must not be null.
     * @param newPassword  The new password. Must not be null.
     */
    @ConfigurationArgumentError("changepass-not-enough-arguments")
    @DefaultFor("~")
    @CommandCooldown(CommandCooldown.DEFAULT_VALUE)
    public void onPasswordChange(LinkCommandActorWrapper actorWrapper, LinkType linkType, Account account, NewPassword newPassword) {
        Preconditions.checkNotNull(actorWrapper, "actorWrapper must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(account, "account must not be null");
        Preconditions.checkNotNull(newPassword, "newPassword must not be null");

        LOGGER.atInfo().log("Processing password change for account: %s", account.getName());
        plugin.getEventBus().publish(AccountTryChangePasswordEvent.class, account, false, true).thenAccept(tryChangePasswordEventPostResult -> {
            if (tryChangePasswordEventPostResult.getEvent().isCancelled()) {
                LOGGER.atFine().log("Password change cancelled for account: %s", account.getName());
                return;
            }
            account.setPasswordHash(account.getCryptoProvider().hash(HashInput.of(newPassword.getNewPassword())));
            accountStorage.saveOrUpdateAccount(account);
            actorWrapper.reply(linkType.getLinkMessages()
                    .getStringMessage("changepass-success", linkType.newMessageContext(account))
                    .replaceAll("(?i)%password%", newPassword.getNewPassword()));
            LOGGER.atInfo().log("Password changed for account: %s", account.getName());
        });
    }
    // #endregion
}