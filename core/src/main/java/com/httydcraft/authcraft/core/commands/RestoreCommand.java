package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.crypto.HashInput;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.commands.annotation.ConfigurationArgumentError;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.orphan.OrphanCommand;

// #region Class Documentation
/**
 * Command for restoring an account by generating a new password and logging out the user.
 * Updates the account with the new password and notifies the user.
 */
@CommandKey(RestoreCommand.CONFIGURATION_KEY)
public class RestoreCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "restore";

    @Dependency
    private PluginConfig config;
    @Dependency
    private AccountDatabase accountDatabase;
    // #endregion

    // #region Command Execution
    /**
     * Executes the restore command, generating a new password and updating the account.
     *
     * @param actorWrapper The actor executing the command. Must not be null.
     * @param linkType     The link type associated with the command. Must not be null.
     * @param account      The account to restore. Must not be null.
     */
    @ConfigurationArgumentError("restore-not-enough-arguments")
    @DefaultFor("~")
    @CommandCooldown(CommandCooldown.DEFAULT_VALUE)
    public void onRestore(LinkCommandActorWrapper actorWrapper, LinkType linkType, Account account) {
        Preconditions.checkNotNull(actorWrapper, "actorWrapper must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(account, "account must not be null");

        LOGGER.atInfo().log("Processing restore command for account: %s", account.getName());
        SecurityAuditLogger.logSuccess("RestoreCommand", account.getPlayer().orElse(null), String.format("Restore command started for account: %s, linkType: %s", account.getName(), linkType));
        try {
            String generatedPassword = linkType.getSettings().getRestoreSettings().generateCode();
            account.setPasswordHash(account.getCryptoProvider().hash(HashInput.of(generatedPassword)));
            account.logout(config.getSessionDurability());
            account.kick(linkType.getServerMessages().getStringMessage("kicked", linkType.newMessageContext(account)));
            actorWrapper.reply(linkType.getLinkMessages().getMessage("restored", linkType.newMessageContext(account))
                    .replaceAll("(?i)%password%", generatedPassword));
            accountDatabase.saveOrUpdateAccount(account);
            LOGGER.atInfo().log("Restored account: %s with new password", account.getName());
            SecurityAuditLogger.logSuccess("RestoreCommand", account.getPlayer().orElse(null), "Restored account with new password: " + account.getName());
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("RestoreCommand", account.getPlayer().orElse(null), "Failed to restore account: " + (account != null ? account.getName() : "null") + ", error: " + ex.getMessage());
            throw ex;
        }
    }
    // #endregion
}