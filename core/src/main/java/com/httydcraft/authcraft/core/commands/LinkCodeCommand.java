package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.bucket.LinkConfirmationBucket;
import com.httydcraft.authcraft.api.commands.MessageableCommandActor;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.event.AccountLinkEvent;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.commands.annotation.ConfigurationArgumentError;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.server.command.ServerCommandActor;
import com.httydcraft.authcraft.api.type.LinkConfirmationType;
import com.httydcraft.authcraft.core.commands.parameter.MessengerLinkContext;
import com.httydcraft.authcraft.core.config.message.context.account.BaseAccountPlaceholderContext;
import io.github.revxrsal.eventbus.EventBus;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.orphan.OrphanCommand;

// #region Class Documentation
/**
 * Command for confirming a link using a confirmation code.
 * Validates the link and updates the account with the provided identificator.
 */
@CommandKey(LinkCodeCommand.CONFIGURATION_KEY)
public class LinkCodeCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "code";

    @Dependency
    private PluginConfig config;
    @Dependency
    private EventBus eventBus;
    @Dependency
    private AccountDatabase accountDatabase;
    @Dependency
    private LinkConfirmationBucket linkConfirmationBucket;
    // #endregion

    // #region Command Execution
    /**
     * Executes the link confirmation command, validating and completing the linking process.
     *
     * @param actor                The actor executing the command. Must not be null.
     * @param linkContext          The link context containing the confirmation code and user. Must not be null.
     * @param possibleIdentificator The optional user identificator for linking.
     */
    @ConfigurationArgumentError("confirmation-not-enough-arguments")
    @DefaultFor("~")
    @CommandCooldown(CommandCooldown.DEFAULT_VALUE)
    public void onLink(MessageableCommandActor actor, MessengerLinkContext linkContext, @Optional LinkUserIdentificator possibleIdentificator) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(linkContext, "linkContext must not be null");

        LOGGER.atInfo().log("Processing link confirmation for code: %s");
        LinkConfirmationType linkConfirmationType = getLinkConfirmationType(actor);
        Messages<?> messages = linkConfirmationType.getConfirmationMessages(linkContext.getConfirmationUser());
        LinkType linkType = linkContext.getConfirmationUser().getLinkType();
        LinkUserIdentificator identificator = linkConfirmationType.selectLinkUserIdentificator(linkContext.getConfirmationUser(), possibleIdentificator);

        if (!linkType.getSettings().getLinkConfirmationTypes().contains(linkConfirmationType)) {
            LOGGER.atFine().log("Confirmation type %s not supported for linkType: %s", linkConfirmationType, linkType);
            return;
        }

        accountDatabase.getAccount(linkContext.getConfirmationUser().getLinkTarget().getPlayerId())
                .thenAccept(account -> accountDatabase.getAccountsFromLinkIdentificator(identificator).thenAccept(accounts -> {
                    if (!validateLinkCount(linkType, identificator, accounts.size())) {
                        actor.replyWithMessage(messages.getMessage("link-limit-reached"));
                        LOGGER.atFine().log("Link limit reached for identificator: %s", identificator);
                        return;
                    }
                    LinkUser foundLinkUser = account.findFirstLinkUserOrNew(linkUser -> linkUser.getLinkType().equals(linkType), linkType);
                    eventBus.publish(AccountLinkEvent.class, account, false, linkType, foundLinkUser, identificator, actor).thenAccept(result -> {
                        if (result.getEvent().isCancelled()) {
                            LOGGER.atFine().log("Link event cancelled for account: %s", account.getName());
                            return;
                        }
                        foundLinkUser.getLinkUserInfo().setIdentificator(identificator);
                        accountDatabase.updateAccountLinks(account);
                        actor.replyWithMessage(messages.getMessage("confirmation-success", new BaseAccountPlaceholderContext(account)));
                        linkConfirmationBucket.modifiable().remove(linkContext.getConfirmationUser());
                        LOGGER.atInfo().log("Successfully linked account: %s to identificator: %s", account.getName(), identificator);
                    });
                }));
    }
    // #endregion

    // #region Helper Methods
    /**
     * Validates the number of linked accounts against the maximum allowed.
     *
     * @param linkType            The link type. Must not be null.
     * @param identificator       The user identificator. Must not be null.
     * @param linkedAccountAmount The number of currently linked accounts.
     * @return {@code true} if the link count is valid, {@code false} otherwise.
     */
    private boolean validateLinkCount(LinkType linkType, LinkUserIdentificator identificator, int linkedAccountAmount) {
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(identificator, "identificator must not be null");

        int maxLinkCount = linkType.getSettings().getMaxLinkCount();
        if (maxLinkCount <= 0) {
            return true;
        }
        return linkType.getSettings().isAdministrator(identificator) || maxLinkCount >= linkedAccountAmount;
    }

    /**
     * Resolves the link confirmation type based on the actor.
     *
     * @param actor The actor executing the command. Must not be null.
     * @return The resolved {@link LinkConfirmationType}.
     * @throws IllegalArgumentException if the actor type is unsupported.
     */
    private LinkConfirmationType getLinkConfirmationType(MessageableCommandActor actor) {
        Preconditions.checkNotNull(actor, "actor must not be null");

        if (actor instanceof ServerCommandActor) {
            LOGGER.atFine().log("Resolved confirmation type: FROM_GAME");
            return LinkConfirmationType.FROM_GAME;
        }
        if (actor instanceof LinkCommandActorWrapper) {
            LOGGER.atFine().log("Resolved confirmation type: FROM_LINK");
            return LinkConfirmationType.FROM_LINK;
        }
        LOGGER.atSevere().log("Unsupported actor type: %s", actor.getClass().getName());
        throw new IllegalArgumentException("Cannot resolve confirmation type for actor: " + actor);
    }
    // #endregion
}