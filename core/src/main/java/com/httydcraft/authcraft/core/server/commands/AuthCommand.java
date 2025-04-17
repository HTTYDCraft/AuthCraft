package com.httydcraft.authcraft.core.server.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.message.MessageContext;
import com.httydcraft.authcraft.api.crypto.HashInput;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.resource.Resource;
import com.httydcraft.authcraft.api.resource.impl.FolderResource;
import com.httydcraft.authcraft.api.resource.impl.FolderResourceReader;
import com.httydcraft.authcraft.api.server.command.ServerCommandActor;
import com.httydcraft.authcraft.core.server.commands.annotations.Admin;
import com.httydcraft.authcraft.core.server.commands.annotations.Permission;
import com.httydcraft.authcraft.core.server.commands.parameters.ArgumentAccount;
import com.httydcraft.authcraft.core.server.commands.parameters.ArgumentServerPlayer;
import com.httydcraft.authcraft.core.server.commands.parameters.NewPassword;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.core.step.context.BaseAuthenticationStepContext;
import com.httydcraft.authcraft.core.step.impl.EnterServerAuthenticationStep;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import ru.vyarus.yaml.updater.YamlUpdater;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import com.httydcraft.authcraft.core.util.SecurityAuditLogger;

// #region Class Documentation
/**
 * Command class for administrative authentication operations.
 * Provides subcommands for managing accounts, configurations, and forced logins.
 */
@Command({"authadmin", "adminauth", "auth"})
@Permission("auth.admin")
@Admin
public class AuthCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    @Dependency
    private AuthPlugin plugin;
    @Dependency
    private PluginConfig config;
    @Dependency
    private AccountDatabase accountDatabase;
    // #endregion

    // #region Default Command
    /**
     * Displays authentication system information.
     * Includes registered accounts, authenticating accounts, and plugin version.
     *
     * @param commandActor The command issuer. Must not be null.
     */
    @DefaultFor({"authadmin", "adminauth", "auth"})
    public void accountInfos(ServerCommandActor commandActor) {
        Preconditions.checkNotNull(commandActor, "commandActor must not be null");
        LOGGER.atFine().log("Executing accountInfos command for actor: %s");

        accountDatabase.getAllAccounts().thenAccept(accounts -> {
            commandActor.reply(config.getServerMessages().getMessage("info-registered",
                    MessageContext.of("%players%", Integer.toString(accounts.size()))));
            commandActor.reply(config.getServerMessages().getMessage("info-auth",
                    MessageContext.of("%players%",
                            Integer.toString(plugin.getAuthenticatingAccountBucket().getAccountIdEntries().size()))));
            commandActor.reply(config.getServerMessages().getMessage("info-version",
                    MessageContext.of("%version%", plugin.getVersion())));
            LOGGER.atFine().log("Sent account info to actor: %s");
        });
    }
    // #endregion

    // #region Subcommands
    /**
     * Forces a player to enter the server.
     *
     * @param commandActor The command issuer. Must not be null.
     * @param proxyPlayer The player to force-enter. Must not be null.
     */
    @Subcommand({"force", "forcejoin", "fjoin"})
    public void forceEnter(ServerCommandActor commandActor, ArgumentServerPlayer proxyPlayer) {
        Preconditions.checkNotNull(commandActor, "commandActor must not be null");
        Preconditions.checkNotNull(proxyPlayer, "proxyPlayer must not be null");
        LOGGER.atFine().log("Executing forceEnter for player: %s", proxyPlayer.getNickname());
        SecurityAuditLogger.logAdminAction("ForceEnter command executed", commandActor.getName(), commandActor.getUuid(), proxyPlayer.getNickname(), "Attempting force enter");

        String id = config.getActiveIdentifierType().getId(proxyPlayer);
        accountDatabase.getAccount(id).thenAccept(account -> {
            if (account == null || !account.isRegistered()) {
                LOGGER.atWarning().log("Auth fail: player %s [%s], reason: account not found", proxyPlayer.getNickname(), proxyPlayer.getPlayerIp());
                SecurityAuditLogger.logFailure("ForceEnter", commandActor, "Account not found or not registered for nickname: " + proxyPlayer.getNickname());
                commandActor.reply(config.getServerMessages().getMessage("account-not-found"));
                return;
            }
            AuthenticationStepContext context = new BaseAuthenticationStepContext(account);
            EnterServerAuthenticationStep enterServerAuthenticationStep = new EnterServerAuthenticationStep(context);
            enterServerAuthenticationStep.enterServer();
            commandActor.reply(config.getServerMessages().getMessage("force-connect-success"));
            LOGGER.atInfo().log("Forced enter for player: %s", proxyPlayer.getNickname());
            SecurityAuditLogger.logSuccess("ForceEnter", commandActor, "Forced enter for player: " + proxyPlayer.getNickname());
        });
    }

    /**
     * Changes an account's password.
     *
     * @param actor The command issuer. Must not be null.
     * @param account The account to modify. Must not be null.
     * @param newPlayerPassword The new password. Must not be null.
     */
    @Subcommand({"changepassword", "changepass"})
    public void changePassword(ServerCommandActor actor, ArgumentAccount account, NewPassword newPlayerPassword) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(account, "account must not be null");
        Preconditions.checkNotNull(newPlayerPassword, "newPlayerPassword must not be null");
        LOGGER.atFine().log("Executing changePassword command");
        SecurityAuditLogger.logAdminAction("ChangePassword command executed", actor.getName(), actor.getUuid(), account.getPlayerId(), "Attempting change password");

        account.future().thenAccept(foundAccount -> {
            foundAccount.setPasswordHash(foundAccount.getCryptoProvider()
                    .hash(HashInput.of(newPlayerPassword.getNewPassword())));
            accountDatabase.saveOrUpdateAccount(foundAccount);
            actor.reply(config.getServerMessages().getMessage("auth-change-success"));
            LOGGER.atInfo().log("Changed password for account: %s", foundAccount.getPlayerId());
            SecurityAuditLogger.logSuccess("ChangePassword", actor, "Changed password for account: " + foundAccount.getPlayerId());
        });
    }

    /**
     * Resets or deletes an account.
     *
     * @param actor The command issuer. Must not be null.
     * @param account The account to reset. Must not be null.
     */
    @Subcommand({"reset", "resetaccount", "deleteaccount"})
    public void resetAccount(ServerCommandActor actor, ArgumentAccount account) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(account, "account must not be null");
        SecurityAuditLogger.logAdminAction("Delete account command executed", actor.getName(), actor.getUuid(), account.getPlayerId(), "Attempting delete");

        String id = config.getActiveIdentifierType().getId(account.getPlayerId());
        accountDatabase.getAccount(id).thenAccept(foundAccount -> {
            if (foundAccount == null || !foundAccount.isRegistered()) {
                SecurityAuditLogger.logFailure("DeleteAccount", actor, "Account not found or not registered for nickname: " + account.getPlayerId());
                actor.reply(config.getServerMessages().getMessage("account-not-found"));
                return;
            }

            try {
                accountDatabase.deleteAccount(foundAccount.getPlayerId());
                actor.reply(config.getServerMessages().getMessage("auth-delete-success"));
                SecurityAuditLogger.logSuccess("DeleteAccount", actor, "Account deleted for nickname: " + account.getPlayerId());
            } catch (Exception ex) {
                SecurityAuditLogger.logFailure("DeleteAccount", actor, "Failed to delete account: " + account.getPlayerId() + ", error: " + ex.getMessage());
                throw ex;
            }
        });
    }

    /**
     * Reloads the plugin configuration.
     *
     * @param actor The command issuer. Must not be null.
     */
    @Subcommand("reload")
    public void reload(ServerCommandActor actor) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        LOGGER.atFine().log("Executing reload command");
        SecurityAuditLogger.logAdminAction("Reload command executed", actor.getName(), actor.getUuid(), null, "Attempting reload");

        try {
            plugin.getConfig().reload();
            actor.reply(config.getServerMessages().getMessage("auth-reloaded"));
            LOGGER.atInfo().log("Reloaded configuration");
            SecurityAuditLogger.logSuccess("Reload", actor, "Reloaded plugin configuration");
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("Reload", actor, "Failed to reload configuration: " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * Migrates configuration files to the latest version.
     *
     * @param actor The command issuer. Must not be null.
     * @throws IOException If an I/O error occurs.
     * @throws URISyntaxException If a URI syntax error occurs.
     */
    @Subcommand("migrateconfig")
    public void migrateConfig(ServerCommandActor actor) throws IOException, URISyntaxException {
        Preconditions.checkNotNull(actor, "actor must not be null");
        LOGGER.atFine().log("Executing migrateConfig command");
        SecurityAuditLogger.logAdminAction("MigrateConfig command executed", actor.getName(), actor.getUuid(), null, "Attempting migrate config");

        try {
            FolderResource folderResource = new FolderResourceReader(plugin.getClass().getClassLoader(), "configurations").read();
            for (Resource resource : folderResource.getResources()) {
                String realConfigurationName = resource.getName().substring(folderResource.getName().length() + 1);
                File resourceConfiguration = new File(plugin.getFolder(), realConfigurationName);
                if (!resourceConfiguration.exists()) {
                    continue;
                }
                YamlUpdater.create(resourceConfiguration, resource.getStream()).backup(true).update();
                LOGGER.atFine().log("Migrated configuration: %s", realConfigurationName);
            }
            actor.reply(config.getServerMessages().getMessage("config-migrated"));
            LOGGER.atInfo().log("Completed configuration migration");
            SecurityAuditLogger.logSuccess("MigrateConfig", actor, "Migrated configuration files");
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("MigrateConfig", actor, "Failed to migrate configuration: " + ex.getMessage());
            throw ex;
        }
    }
    // #endregion
}