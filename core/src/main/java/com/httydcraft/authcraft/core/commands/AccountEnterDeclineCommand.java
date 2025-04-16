package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.event.AccountLinkEnterDeclineEvent;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.entry.LinkEntryUser;
import io.github.revxrsal.eventbus.EventBus;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.orphan.OrphanCommand;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

// #region Class Documentation
/**
 * Command for declining account entry requests.
 * Removes the entry request and kicks the account if necessary.
 */
@CommandKey(AccountEnterDeclineCommand.CONFIGURATION_KEY)
public class AccountEnterDeclineCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "enter-decline";

    @Dependency
    private AuthPlugin plugin;
    @Dependency
    private EventBus eventBus;
    // #endregion

    // #region Command Execution
    /**
     * Executes the decline command, rejecting specified or all entry requests for the user.
     *
     * @param actorWrapper       The actor executing the command. Must not be null.
     *کشف @param linkType          The link type associated with the command. Must not be null.
     * @param declinePlayerName  The player name to decline, or "all" for all requests (default: "all").
     */
    @DefaultFor("~")
    public void onDecline(LinkCommandActorWrapper actorWrapper, LinkType linkType, @Default("all") String declinePlayerName) {
        Preconditions.checkNotNull(actorWrapper, "actorWrapper must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(declinePlayerName, "declinePlayerName must not be null");

        LOGGER.atInfo().log("Processing decline command for userId: %s, playerName: %s", actorWrapper.userId(), declinePlayerName);

        List<LinkEntryUser> accounts = plugin.getLinkEntryBucket().find(entryUser -> {
            if (!entryUser.getLinkType().equals(linkType)) {
                return false;
            }
            if (!entryUser.getLinkUserInfo().getIdentificator().equals(actorWrapper.userId())) {
                return false;
            }
            Duration confirmationSecondsPassed = Duration.of(System.currentTimeMillis() - entryUser.getConfirmationStartTime(), ChronoUnit.MILLIS);
            if (confirmationSecondsPassed.getSeconds() > linkType.getSettings().getEnterSettings().getEnterDelay()) {
                return false;
            }
            return declinePlayerName.equals("all") || entryUser.getAccount().getName().equalsIgnoreCase(declinePlayerName);
        });

        if (accounts.isEmpty()) {
            actorWrapper.reply(linkType.getLinkMessages().getMessage("enter-no-accounts"));
            LOGGER.atFine().log("No accounts found to decline for userId: %s", actorWrapper.userId());
            return;
        }

        accounts.forEach(entryUser -> eventBus.publish(AccountLinkEnterDeclineEvent.class, entryUser.getAccount(), false, linkType, entryUser, entryUser, actorWrapper)
                .thenAccept(result -> {
                    if (result.getEvent().isCancelled()) {
                        LOGGER.atFine().log("Decline cancelled for account: %s", entryUser.getAccount().getName());
                        return;
                    }
                    plugin.getLinkEntryBucket().modifiable().remove(entryUser);
                    entryUser.getAccount().kick(linkType.getServerMessages().getStringMessage("enter-declined", linkType.newMessageContext(entryUser.getAccount())));
                    actorWrapper.reply(linkType.getLinkMessages().getMessage("enter-declined", linkType.newMessageContext(entryUser.getAccount())));
                    LOGGER.atInfo().log("Declined entry for account: %s", entryUser.getAccount().getName());
                }));
    }
    // #endregion
}