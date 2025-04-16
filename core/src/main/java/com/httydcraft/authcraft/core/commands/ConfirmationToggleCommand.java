package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.event.AccountLinkConfirmationToggleEvent;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.commands.annotation.ConfigurationArgumentError;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserInfo;
import io.github.revxrsal.eventbus.EventBus;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.orphan.OrphanCommand;

// #region Class Documentation
/**
 * Command for toggling link confirmation settings for an account.
 * Publishes an event to handle the toggle and updates the account.
 */
@CommandKey(ConfirmationToggleCommand.CONFIGURATION_KEY)
public class ConfirmationToggleCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "confirmation-toggle";

    @Dependency
    private AccountDatabase accountDatabase;
    @Dependency
    private EventBus eventBus;
    // #endregion

    // #region Command Execution
    /**
     * Executes the confirmation toggle command, enabling or disabling confirmation for the account.
     *
     * @param actorWrapper The actor executing the command. Must not be null.
     * @param linkType     The link type associated with the command. Must not be null.
     * @param account      The account to toggle confirmation for. Must not be null.
     */
    @ConfigurationArgumentError("confirmation-no-player")
    @DefaultFor("~")
    @CommandCooldown(CommandCooldown.DEFAULT_VALUE)
    public void onConfirmationToggle(LinkCommandActorWrapper actorWrapper, LinkType linkType, Account account) {
        Preconditions.checkNotNull(actorWrapper, "actorWrapper must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(account, "account must not be null");

        LOGGER.atInfo().log("Processing confirmation toggle for account: %s, linkType: %s", account.getName(), linkType);
        if (!linkType.getSettings().getConfirmationSettings().canToggleConfirmation()) {
            actorWrapper.reply(linkType.getLinkMessages().getMessage("confirmation-toggle-disabled", linkType.newMessageContext(account)));
            LOGGER.atFine().log("Confirmation toggle disabled for linkType: %s", linkType);
            return;
        }

        actorWrapper.reply(linkType.getLinkMessages().getMessage("confirmation-toggled", linkType.newMessageContext(account)));
        LinkUser linkUser = account.findFirstLinkUserOrNew(user -> user.getLinkType().equals(linkType), linkType);
        eventBus.publish(AccountLinkConfirmationToggleEvent.class, account, false, linkType, linkUser, actorWrapper).thenAccept(result -> {
            if (result.getEvent().isCancelled()) {
                LOGGER.atFine().log("Confirmation toggle cancelled for account: %s", account.getName());
                return;
            }
            LinkUserInfo linkUserInfo = linkUser.getLinkUserInfo();
            linkUserInfo.setConfirmationEnabled(!linkUserInfo.isConfirmationEnabled());
            accountDatabase.saveOrUpdateAccount(account);
            LOGGER.atInfo().log("Toggled confirmation for account: %s, enabled: %b", account.getName(), linkUserInfo.isConfirmationEnabled());
        });
    }
    // #endregion
}