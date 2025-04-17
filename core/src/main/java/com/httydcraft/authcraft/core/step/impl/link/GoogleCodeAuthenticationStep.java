package com.httydcraft.authcraft.core.step.impl.link;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.api.link.user.entry.LinkEntryUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserInfo;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.step.AuthenticationStep;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.api.step.MessageableAuthenticationStep;
import com.httydcraft.authcraft.core.config.message.server.ServerMessageContext;
import com.httydcraft.authcraft.core.link.google.GoogleLinkType;
import com.httydcraft.authcraft.core.link.user.entry.BaseLinkEntryUser;
import com.httydcraft.authcraft.core.step.AuthenticationStepTemplate;
import com.httydcraft.authcraft.core.step.creators.AuthenticationStepFactoryTemplate;

// #region Class Documentation
/**
 * Authentication step for Google two-factor authentication code verification.
 * Prompts the user to enter a Google Authenticator code.
 */
public class GoogleCodeAuthenticationStep extends AuthenticationStepTemplate implements MessageableAuthenticationStep {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String STEP_NAME = "GOOGLE_LINK";
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    private final LinkEntryUser entryUser;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code GoogleCodeAuthenticationStep}.
     *
     * @param context The authentication step context. Must not be null.
     */
    public GoogleCodeAuthenticationStep(AuthenticationStepContext context) {
        super(STEP_NAME, context);
        Preconditions.checkNotNull(context, "context must not be null");
        Account account = context.getAccount();
        this.entryUser = new BaseLinkEntryUser(GoogleLinkType.getInstance(), account,
                account.findFirstLinkUserOrNew(GoogleLinkType.LINK_USER_FILTER, GoogleLinkType.getInstance()).getLinkUserInfo());
        LOGGER.atFine().log("Initialized GoogleCodeAuthenticationStep for account: %s", account.getPlayerId());
        SecurityAuditLogger.logSuccess("GoogleCodeAuthenticationStep", account != null ? account.getPlayer().orElse(null) : null, String.format("Initialized GoogleCodeAuthenticationStep for account: %s", account != null ? account.getPlayerId() : "null"));
    }
    // #endregion

    // #region AuthenticationStep Implementation
    /**
     * Determines if the step should proceed to the next step.
     *
     * @return {@code true} if the step can proceed, {@code false} otherwise.
     */
    @Override
    public boolean shouldPassToNextStep() {
        boolean canPass = authenticationStepContext.canPassToNextStep();
        LOGGER.atFine().log("Checked shouldPassToNextStep: %b", canPass);
        return canPass;
    }

    /**
     * Determines if the step should be skipped.
     *
     * @return {@code true} if the step should be skipped, {@code false} otherwise.
     */
    @Override
    public boolean shouldSkip() {
        Account account = authenticationStepContext.getAccount();
        LOGGER.atFine().log("Checking shouldSkip for account: %s", account.getPlayerId());

        if (!PLUGIN.getConfig().getGoogleAuthenticatorSettings().isEnabled()) {
            LOGGER.atFine().log("Google authentication disabled, skipping");
            return true;
        }

        if (PLUGIN.getLinkEntryBucket().find(account.getPlayerId(), GoogleLinkType.getInstance()).isPresent()) {
            LOGGER.atFine().log("Link entry found, skipping");
            return true;
        }

        if (account.isSessionActive(PLUGIN.getConfig().getSessionDurability())) {
            LOGGER.atFine().log("Active session found, skipping");
            return true;
        }

        return account.findFirstLinkUser(GoogleLinkType.LINK_USER_FILTER).map(linkUser -> {
            LinkUserInfo linkUserInfo = linkUser.getLinkUserInfo();

            if (linkUser.isIdentifierDefaultOrNull()) {
                LOGGER.atFine().log("No valid link user identifier, skipping");
                return true;
            }

            if (!linkUserInfo.isConfirmationEnabled() && GoogleLinkType.getInstance().getSettings().getConfirmationSettings().canToggleConfirmation()) {
                LOGGER.atFine().log("Confirmation not enabled, skipping");
                return true;
            }

            PLUGIN.getLinkEntryBucket().modifiable().add(entryUser);
            LOGGER.atFine().log("Added link entry, proceeding");
            return false;
        }).orElse(true);
    }
    // #endregion

    // #region MessageableAuthenticationStep Implementation
    /**
     * Processes the step by sending a message to the player.
     *
     * @param player The player to process the step for. Must not be null.
     */
    @Override
    public void process(ServerPlayer player) {
        Preconditions.checkNotNull(player, "player must not be null");
        Account account = authenticationStepContext.getAccount();
        if (account == null) {
            LOGGER.atWarning().log("Auth fail: google code step for player %s, reason: account is null in GoogleCodeAuthenticationStep", player.getNickname());
            SecurityAuditLogger.logFailure("GoogleCodeAuthenticationStep", player, "Account is null on Google code step");
            player.sendMessage(PLUGIN.getConfig().getServerMessages().getSubMessages("google").getMessage("account-not-found"));
            return;
        }
        SecurityAuditLogger.logSuccess("GoogleCodeAuthenticationStep", player, String.format("Google code step started for player: %s, account: %s", player.getName(), account.getPlayerId()));
        PluginConfig config = AuthPlugin.instance().getConfig();
        Messages<ServerComponent> googleMessages = config.getServerMessages().getSubMessages("google");
        player.sendMessage(googleMessages.getMessage("need-code-chat", new ServerMessageContext(account)));
        AuthPlugin.instance()
                .getCore()
                .createTitle(googleMessages.getMessage("need-code-title"))
                .subtitle(googleMessages.getMessage("need-code-subtitle"))
                .stay(120)
                .send(player);
        LOGGER.atFine().log("Processed Google code step for player: %s", player.getNickname());
        SecurityAuditLogger.logSuccess("GoogleCodeAuthenticationStep", player, String.format("Google code prompt sent to player: %s, account: %s", player.getName(), account.getPlayerId()));
    }
    // #endregion

    // #region Factory
    /**
     * Factory for creating {@code GoogleCodeAuthenticationStep} instances.
     */
    public static class GoogleLinkAuthenticationStepFactory extends AuthenticationStepFactoryTemplate {
        /**
         * Constructs a new {@code GoogleLinkAuthenticationStepFactory}.
         */
        public GoogleLinkAuthenticationStepFactory() {
            super(STEP_NAME);
            LOGGER.atFine().log("Initialized GoogleLinkAuthenticationStepFactory");
        }

        /**
         * Creates a new authentication step.
         *
         * @param context The authentication step context. Must not be null.
         * @return A new {@link AuthenticationStep}.
         */
        @Override
        public AuthenticationStep createNewAuthenticationStep(AuthenticationStepContext context) {
            LOGGER.atFine().log("Creating new GoogleCodeAuthenticationStep");
            return new GoogleCodeAuthenticationStep(context);
        }
    }
    // #endregion
}