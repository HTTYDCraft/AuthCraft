package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.commands.annotation.ConfigurationArgumentError;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.core.link.google.GoogleLinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.core.server.commands.annotations.GoogleUse;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.orphan.OrphanCommand;

// #region Class Documentation
/**
 * Command for verifying a Google authenticator code.
 * Advances the authentication step if the code is valid.
 */
@CommandKey(GoogleCodeCommand.CONFIGURATION_KEY)
public class GoogleCodeCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "google-code";

    @Dependency
    private AuthPlugin plugin;
    @Dependency
    private PluginConfig config;
    @Dependency
    private AccountDatabase accountStorage;
    // #endregion

    // #region Command Execution
    /**
     * Executes the Google code verification command, checking the provided code.
     *
     * @param actorWrapper The actor executing the command. Must not be null.
     * @param linkType     The link type (Google). Must not be null.
     * @param account      The account to verify. Must not be null.
     * @param code         The Google authenticator code. Must not be null.
     */
    @GoogleUse
    @ConfigurationArgumentError("google-code-not-enough-arguments")
    @DefaultFor("~")
    public void googleCode(LinkCommandActorWrapper actorWrapper, LinkType linkType, Account account, Integer code) {
        Preconditions.checkNotNull(actorWrapper, "actorWrapper must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(account, "account must not be null");
        Preconditions.checkNotNull(code, "code must not be null");

        LOGGER.atInfo().log("Processing Google code verification for account: %s", account.getName());
        LinkUser linkUser = account.findFirstLinkUserOrNew(GoogleLinkType.LINK_USER_FILTER, GoogleLinkType.getInstance());
        String linkUserKey = linkUser.getLinkUserInfo().getIdentificator().asString();

        if (linkUserKey == null || linkUser.isIdentifierDefaultOrNull()) {
            actorWrapper.reply(linkType.getLinkMessages().getStringMessage("google-code-account-not-have-google", linkType.newMessageContext(account)));
            LOGGER.atFine().log("No Google authenticator linked for account: %s", account.getName());
            return;
        }

        if (!plugin.getLinkEntryBucket().find(account.getPlayerId(), GoogleLinkType.getInstance()).isPresent()) {
            actorWrapper.reply(linkType.getLinkMessages().getStringMessage("google-code-not-need-enter", linkType.newMessageContext(account)));
            LOGGER.atFine().log("No Google authentication required for account: %s", account.getName());
            return;
        }

        if (plugin.getGoogleAuthenticator().authorize(linkUser.getLinkUserInfo().getIdentificator().asString(), code)) {
            actorWrapper.reply(linkType.getLinkMessages().getStringMessage("google-code-valid", linkType.newMessageContext(account)));
            account.getCurrentAuthenticationStep().getAuthenticationStepContext().setCanPassToNextStep(true);
            account.nextAuthenticationStep(plugin.getAuthenticationContextFactoryBucket().createContext(account));
            LOGGER.atInfo().log("Google code verified for account: %s", account.getName());
            return;
        }

        actorWrapper.reply(linkType.getLinkMessages().getStringMessage("google-code-not-valid", linkType.newMessageContext(account)));
        LOGGER.atWarning().log("Invalid Google code for account: %s", account.getName());
    }
    // #endregion
}