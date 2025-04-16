package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.event.AccountUnlinkEvent;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.commands.annotation.ConfigurationArgumentError;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import io.github.revxrsal.eventbus.EventBus;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.orphan.OrphanCommand;

// #region Class Documentation
/**
 * Command for unlinking a player's account from a specified link type.
 * Publishes an event to handle the unlink process and updates the account.
 */
@CommandKey(UnlinkCommand.CONFIGURATION_KEY)
public class UnlinkCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "unlink";

    @Dependency
    private AccountDatabase accountDatabase;
    @Dependency
    private EventBus eventBus;
    // #endregion

    // #region Command Execution
    /**
     * Executes the unlink command, removing the association between an account and a link type.
     *
     * @param actorWrapper The actor executing the command. Must not be null.
     * @param linkType     The link type to unlink. Must not be null.
     * @param account      The account to unlink. Must not be null.
     */
    @ConfigurationArgumentError("unlink-not-enough-arguments")
    @DefaultFor("~")
    public void onUnlink(LinkCommandActorWrapper actorWrapper, LinkType linkType, Account account) {
        Preconditions.checkNotNull(actorWrapper, "actorWrapper must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(account, "account must not be null");

        LOGGER.atInfo().log("Processing unlink command for account: %s, linkType: %s", account.getName(), linkType);
        LinkUser linkUser = account.findFirstLinkUserOrNew(user -> user.getLinkType().equals(linkType), linkType);
        eventBus.publish(AccountUnlinkEvent.class, account, false, linkType, linkUser, linkUser.getLinkUserInfo().getIdentificator(), actorWrapper)
                .thenAccept(result -> {
                    if (result.getEvent().isCancelled()) {
                        LOGGER.atFine().log("Unlink cancelled for account: %s", account.getName());
                        return;
                    }
                    linkUser.getLinkUserInfo().setIdentificator(linkType.getDefaultIdentificator());
                    accountDatabase.updateAccountLinks(account);
                    actorWrapper.reply(linkType.getLinkMessages().getMessage("unlinked", linkType.newMessageContext(account)));
                    LOGGER.atInfo().log("Unlinked account: %s from linkType: %s", account.getName(), linkType);
                });
    }
    // #endregion
}