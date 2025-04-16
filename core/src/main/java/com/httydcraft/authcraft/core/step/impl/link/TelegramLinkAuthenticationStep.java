package com.httydcraft.authcraft.core.step.impl.link;

import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.step.AuthenticationStep;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.core.link.telegram.TelegramLinkType;
import com.httydcraft.authcraft.core.step.creators.AuthenticationStepFactoryTemplate;

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
            LOGGER.atFine().log("Creating new TelegramLinkAuthenticationStep");
            return new TelegramLinkAuthenticationStep(context);
        }
    }
    // #endregion
}