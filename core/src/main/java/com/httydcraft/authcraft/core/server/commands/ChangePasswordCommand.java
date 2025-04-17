package com.httydcraft.authcraft.core.server.commands;

import com.google.common.base.Preconditions;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.crypto.HashInput;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.event.AccountTryChangePasswordEvent;
import com.httydcraft.authcraft.core.server.commands.parameters.DoublePassword;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;
import io.github.revxrsal.eventbus.PostResult;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;

// #region Class Documentation
/**
 * Command for changing a player's password.
 * Verifies the old password and updates to a new one if valid.
 */
@Command({"passchange", "changepass", "changepassword"})
public class ChangePasswordCommand {
    @Dependency
    private AuthPlugin plugin;
    @Dependency
    private PluginConfig config;
    @Dependency
    private AccountDatabase accountStorage;
    // #endregion

    // #region Command Logic
    /**
     * Changes the player's password.
     *
     * @param sender The player issuing the command. Must not be null.
     * @param password The old and new passwords. Must not be null.
     */
    @DefaultFor({"passchange", "changepass", "changepassword"})
    @CommandCooldown(CommandCooldown.DEFAULT_VALUE)
    public void changePlayerPassword(ServerPlayer sender, DoublePassword password) {
        Preconditions.checkNotNull(sender, "sender must not be null");
        Preconditions.checkNotNull(password, "password must not be null");
        SecurityAuditLogger.logSuccess("ChangePasswordCommand", sender, String.format("Password change command started for player: %s", sender.getName()));

        String id = config.getActiveIdentifierType().getId(sender);
        accountStorage.getAccount(id).thenAcceptAsync(account -> {
            if (account == null || !account.isRegistered()) {
                sender.sendMessage(config.getServerMessages().getMessage("account-not-found"));
                SecurityAuditLogger.logFailure("ChangePasswordCommand", sender, "Account not found or not registered");
                return;
            }

            boolean isWrongPassword = !account.getCryptoProvider()
                    .matches(HashInput.of(password.getOldPassword()), account.getPasswordHash());
            PostResult<AccountTryChangePasswordEvent> tryChangePasswordEventPostResult = plugin.getEventBus()
                    .publish(AccountTryChangePasswordEvent.class, account, false, !isWrongPassword)
                    .join();

            if (tryChangePasswordEventPostResult.getEvent().isCancelled()) {
                SecurityAuditLogger.logFailure("ChangePasswordCommand", sender, "Change cancelled by event");
                return;
            }

            if (isWrongPassword) {
                sender.sendMessage(config.getServerMessages().getMessage("wrong-old-password"));
                SecurityAuditLogger.logFailure("ChangePasswordCommand", sender, "Wrong old password");
                return;
            }

            if (!account.getCryptoProvider().getIdentifier().equals(config.getActiveHashType().getIdentifier())) {
                account.setCryptoProvider(config.getActiveHashType());
                // Можно добавить отдельный лог для смены hash type, если требуется
            }

            account.setPasswordHash(account.getCryptoProvider().hash(HashInput.of(password.getNewPassword())));
            accountStorage.saveOrUpdateAccount(account);
            sender.sendMessage(config.getServerMessages().getMessage("change-success"));
            SecurityAuditLogger.logSuccess("ChangePasswordCommand", sender, "Password changed successfully for account: " + account.getPlayerId());
        });
    }
    // #endregion
}