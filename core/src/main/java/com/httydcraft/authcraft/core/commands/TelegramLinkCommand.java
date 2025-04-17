package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.commands.MessageableCommandActor;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.link.telegram.TelegramLinkType;
import com.httydcraft.authcraft.core.link.user.confirmation.BaseLinkConfirmationUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.model.PlayerIdSupplier;
import com.httydcraft.authcraft.core.server.commands.annotations.TelegramUse;
import com.httydcraft.authcraft.api.type.LinkConfirmationType;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.orphan.OrphanCommand;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;

// #region Class Documentation
/**
 * Command for linking a Telegram account to a player's game account.
 * Generates a confirmation code and initiates the linking process.
 */
@CommandKey(MessengerLinkCommandTemplate.CONFIGURATION_KEY)
public class TelegramLinkCommand extends MessengerLinkCommandTemplate implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String LINK_NAME = "TELEGRAM";

    @Dependency
    private AuthPlugin plugin;
    @Dependency
    private PluginConfig config;
    @Dependency
    private AccountDatabase accountDatabase;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code TelegramLinkCommand} with the specified messages.
     *
     * @param messages The messages configuration for this command. Must not be null.
     */
    public TelegramLinkCommand(Messages<?> messages) {
        super(messages, TelegramLinkType.getInstance());
        LOGGER.atInfo().log("Initialized TelegramLinkCommand");
    }
    // #endregion

    // #region Command Execution
    /**
     * Executes the Telegram link command, initiating the account linking process.
     *
     * @param commandActor        The actor executing the command. Must not be null.
     * @param idSupplier         The supplier of the player's ID. Must not be null.
     * @param linkUserIdentificator The optional user identificator for linking.
     */
    @TelegramUse
    @DefaultFor("~")
    @CommandCooldown(CommandCooldown.DEFAULT_VALUE)
    public void telegramLink(MessageableCommandActor commandActor, PlayerIdSupplier idSupplier, @Optional LinkUserIdentificator linkUserIdentificator) {
        Preconditions.checkNotNull(commandActor, "commandActor must not be null");
        Preconditions.checkNotNull(idSupplier, "idSupplier must not be null");

        String accountId = idSupplier.getPlayerId();
        LOGGER.atInfo().log("Processing Telegram link command for accountId: %s", accountId);

        accountDatabase.getAccountFromName(accountId).thenAccept(account -> {
            if (isInvalidAccount(account, commandActor, TelegramLinkType.LINK_USER_FILTER)) {
                LOGGER.atFine().log("Invalid account for accountId: %s", accountId);
                return;
            }
            String code = generateCode(() -> config.getTelegramSettings().getConfirmationSettings().generateCode());
            LOGGER.atFine().log("Generated confirmation code for accountId: %s", accountId);

            LinkConfirmationType linkConfirmationType = getLinkConfirmationType(commandActor);
            long timeoutTimestamp = System.currentTimeMillis() + TelegramLinkType.getInstance().getSettings().getConfirmationSettings().getRemoveDelay().getMillis();
            try {
                sendLinkConfirmation(commandActor, linkConfirmationType.bindLinkConfirmationUser(
                        new BaseLinkConfirmationUser(linkConfirmationType, timeoutTimestamp, TelegramLinkType.getInstance(), account, code), linkUserIdentificator));
                LOGGER.atInfo().log("Sent link confirmation for accountId: %s", accountId);
                SecurityAuditLogger.logSuccess("TelegramLinkCommand", null, "Sent link confirmation for accountId: " + accountId);
            } catch (Exception ex) {
                SecurityAuditLogger.logFailure("TelegramLinkCommand", null, "Failed to send link confirmation for accountId: " + accountId + ", error: " + ex.getMessage());
                throw ex;
            }
        });
    }
    // #endregion
}