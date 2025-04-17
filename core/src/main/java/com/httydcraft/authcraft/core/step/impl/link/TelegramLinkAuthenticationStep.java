package com.httydcraft.authcraft.core.step.impl.link;

import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.step.AuthenticationStep;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.core.link.telegram.TelegramLinkType;
import com.httydcraft.authcraft.core.step.creators.AuthenticationStepFactoryTemplate;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;

// #region Class Documentation
/**
 * Authentication step for Telegram account linking.
 * Extends {@link MessengerAuthenticationStep} for Telegram-specific functionality.
 */
public class TelegramLinkAuthenticationStep extends MessengerAuthenticationStep {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String STEP_NAME = "TELEGRAM_LINK";
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code TelegramLinkAuthenticationStep}.
     *
     * @param context The authentication step context. Must not be null.
     */
    public TelegramLinkAuthenticationStep(AuthenticationStepContext context) {
        super(STEP_NAME, context, TelegramLinkType.getInstance(), TelegramLinkType.LINK_USER_FILTER);
        LOGGER.atFine().log("Initialized TelegramLinkAuthenticationStep for account: %s", context.getAccount().getPlayerId());
        SecurityAuditLogger.logSuccess("TelegramLinkAuthenticationStep", context.getAccount().getPlayer().orElse(null), String.format("Initialized TelegramLinkAuthenticationStep for account: %s", context.getAccount().getPlayerId()));
    }
    // #endregion

    // #region Methods
    public void process(ServerPlayer player) {
        SecurityAuditLogger.logSuccess("TelegramLinkAuthenticationStep", player, "Telegram link step triggered");
        if (player == null) {
            SecurityAuditLogger.logFailure("TelegramLinkAuthenticationStep", null, "Player is null on Telegram link step");
            LOGGER.atWarning().log("Auth fail: telegram link step for player %s, reason: player is null in TelegramLinkAuthenticationStep", player != null ? player.getNickname() : "null");
            return;
        }
        Preconditions.checkNotNull(player, "player must not be null");
        Account account = authenticationStepContext.getAccount();
        if (account == null) {
            LOGGER.atWarning().log("Auth fail: telegram link step for player %s, reason: account is null in TelegramLinkAuthenticationStep", player.getNickname());
            SecurityAuditLogger.logFailure("TelegramLinkAuthenticationStep", player, "Account is null on Telegram link step");
            player.sendMessage(AuthPlugin.instance().getConfig().getServerMessages().getMessage("account-not-found"));
            return;
        }
        SecurityAuditLogger.logSuccess("TelegramLinkAuthenticationStep", player, String.format("Telegram link step started for player: %s, account: %s", player.getName(), account.getPlayerId()));
        Messages<ServerComponent> messages = getLinkType().getServerMessages();
        player.sendMessage(messages.getMessage("enter-confirm-need-chat", new ServerMessageContext(account)));
        AuthPlugin.instance()
                .getCore()
                .createTitle(messages.getMessage("enter-confirm-need-title"))
                .subtitle(messages.getMessage("enter-confirm-need-subtitle"))
                .stay(120)
                .send(player);
        LOGGER.atFine().log("Processed Telegram link step for player: %s", player.getNickname());
    }
    // #endregion

    // #region Factory
    /**
     * Factory for creating {@code TelegramLinkAuthenticationStep} instances.
     */
    public static class TelegramLinkAuthenticationStepFactory extends AuthenticationStepFactoryTemplate {
        /**
         * Constructs a new {@code TelegramLinkAuthenticationStepFactory}.
         */
        public TelegramLinkAuthenticationStepFactory() {
            super(STEP_NAME);
            LOGGER.atFine().log("Initialized TelegramLinkAuthenticationStepFactory");
        }

        /**
         * Creates a new authentication step.
         *
         * @param context The authentication step context. Must not be null.
         * @return A new {@link AuthenticationStep}.
         */
        @Override
        public AuthenticationStep createNewAuthenticationStep(AuthenticationStepContext context) {
            SecurityAuditLogger.logSuccess("TelegramLinkAuthenticationStepFactory: step factory event", null, "TelegramLinkAuthenticationStepFactory event triggered");
            if (context == null) {
                SecurityAuditLogger.logFailure("TelegramLinkAuthenticationStepFactory", null, "Context is null on step factory event");
                LOGGER.atWarning().log("Auth fail: telegram link step factory, reason: context is null");
                return null;
            }
            LOGGER.atFine().log("Creating new TelegramLinkAuthenticationStep");
            return new TelegramLinkAuthenticationStep(context);
        }
    }
    // #endregion
}