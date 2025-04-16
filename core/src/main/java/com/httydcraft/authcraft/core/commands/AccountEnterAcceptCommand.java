package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.event.AccountLinkEnterAcceptEvent;
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
 * Command for accepting account entry requests.
 * Confirms the entry and advances the authentication step.
 */
@CommandKey(AccountEnterAcceptCommand.CONFIGURATION_KEY)
public class AccountEnterAcceptCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "enter-accept";

    @Dependency
    private AuthPlugin plugin;
    @Dependency
    private EventBus eventBus;
    // #endregion

    // #region Command Execution
    /**
     * Executes the accept command, confirming specified or all entry requests for the user.
     *
     * @param actorWrapper      The actor executing the command. Must not be null.
     * @param linkType         The link type associated with the command. Must not be null.
     * @param acceptPlayerName The player name to accept, or "all" for all requests (default: "all").
     */
    @DefaultFor("~")
    public void onAccept(LinkCommandActorWrapper actorWrapper, LinkType linkType, @Default("all") String acceptPlayerName) {
        Preconditions.checkNotNull(actorWrapper, "actorWrapper must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(acceptPlayerName, "acceptPlayerName must not be null");

        LOGGER.atInfo().log("Processing accept command for userId: %s, playerName: %s", actorWrapper.userId(), acceptPlayerName);

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
            return acceptPlayerName.equals("all") || entryUser.getAccount().getName().equalsIgnoreCase(acceptPlayerName);
        });

        if (accounts.isEmpty()) {
            actorWrapper.reply(linkType.getLinkMessages().getMessage("enter-no-accounts"));
            LOGGER.atFine().log("No accounts found to accept for userId: %s", actorWrapper.userId());
            return;
        }

        accounts.forEach(entryUser -> eventBus.publish(AccountLinkEnterAcceptEvent.class, entryUser.getAccount(), false, linkType, entryUser, entryUser, actorWrapper)
                .thenAccept(result -> {
                    if (result.getEvent().isCancelled()) {
                        LOGGER.atFine().log("Accept cancelled for account: %s", entryUser.getAccount().getName());
                        return;
                    }
                    entryUser.setConfirmed(true);
                    Account account = entryUser.getAccount();
                    account.getPlayer().ifPresent(player -> player.sendMessage(
                            linkType.getServerMessages().getStringMessage("enter-confirmed", linkType.newMessageContext(account))));
                    account.nextAuthenticationStep(plugin.getAuthenticationContextFactoryBucket().createContext(account));
                    plugin.getLinkEntryBucket().modifiable().remove(entryUser);
                    actorWrapper.reply(linkType.getLinkMessages().getMessage("enter-accepted", linkType.newMessageContext(account)));
                    LOGGER.atInfo().log("Accepted entry for account: %s", account.getName());
                }));
    }
    // #endregion
}