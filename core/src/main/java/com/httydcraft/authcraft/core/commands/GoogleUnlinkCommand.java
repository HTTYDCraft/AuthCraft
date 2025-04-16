package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.event.AccountUnlinkEvent;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.commands.annotation.ConfigurationArgumentError;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.core.link.google.GoogleLinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.core.server.commands.annotations.GoogleUse;
import io.github.revxrsal.eventbus.EventBus;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.orphan.OrphanCommand;

// #region Class Documentation
/**
 * Command for unlinking a Google authenticator from an account.
 * Publishes an event to handle the unlink process and updates the account.
 */
@CommandKey(GoogleUnlinkCommand.CONFIGURATION_KEY)
public class GoogleUnlinkCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "google-remove";

    @Dependency
    private PluginConfig config;
    @Dependency
    private AccountDatabase accountDatabase;
    @Dependency
    private EventBus eventBus;
    // #endregion

    // #region Command Execution
    /**
     * Executes the Google unlink command, removing the Google authenticator association.
     *
     * @param actorWrapper The actor executing the command. Must not be null.
     * @param linkType     The link type (Google). Must not be null.
     * @param account      The account to unlink. Must not be null.
     */
    @GoogleUse
    @ConfigurationArgumentError("google-unlink-not-enough-arguments")
    @DefaultFor("~")
    @CommandCooldown(CommandCooldown.DEFAULT_VALUE)
    public void unlink(LinkCommandActorWrapper actorWrapper, LinkType linkType, Account account) {
        Preconditions.checkNotNull(actorWrapper, "actorWrapper must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(account, "account must not be null");

        LOGGER.atInfo().log("Processing Google unlink command for account: %s", account.getName());
        LinkUser linkUser = account.findFirstLinkUserOrNew(GoogleLinkType.LINK_USER_FILTER, GoogleLinkType.getInstance());

        if (linkUser.isIdentifierDefaultOrNull()) {
            actorWrapper.reply(linkType.getLinkMessages().getStringMessage("google-unlink-not-have-google", linkType.newMessageContext(account)));
            LOGGER.atFine().log("No Google authenticator linked for account: %s", account.getName());
            return;
        }

        actorWrapper.reply(linkType.getLinkMessages().getStringMessage("google-unlinked", linkType.newMessageContext(account)));
        eventBus.publish(AccountUnlinkEvent.class, account, false, GoogleLinkType.getInstance(), linkUser, linkUser.getLinkUserInfo().getIdentificator(), actorWrapper)
                .thenAccept(result -> {
                    if (result.getEvent().isCancelled()) {
                        LOGGER.atFine().log("Google unlink cancelled for account: %s", account.getName());
                        return;
                    }
                    linkUser.getLinkUserInfo().setIdentificator(GoogleLinkType.getInstance().getDefaultIdentificator());
                    accountDatabase.updateAccountLinks(account);
                    LOGGER.atInfo().log("Google authenticator unlinked for account: %s", account.getName());
                });
    }
    // #endregion
}