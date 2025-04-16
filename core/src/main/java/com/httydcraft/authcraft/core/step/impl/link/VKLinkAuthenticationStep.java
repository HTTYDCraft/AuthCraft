package com.httydcraft.authcraft.core.step.impl.link;

import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.step.AuthenticationStep;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.core.link.vk.VKLinkType;
import com.httydcraft.authcraft.core.step.creators.AuthenticationStepFactoryTemplate;

// #region Class Documentation
/**
 * Authentication step for VK account linking.
 * Extends {@link MessengerAuthenticationStep} for VK-specific functionality.
 */
public class VKLinkAuthenticationStep extends MessengerAuthenticationStep {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String STEP_NAME = "VK_LINK";
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VKLinkAuthenticationStep}.
     *
     * @param context The authentication step context. Must not be null.
     */
    public VKLinkAuthenticationStep(AuthenticationStepContext context) {
        super(STEP_NAME, context, VKLinkType.getInstance(), VKLinkType.LINK_USER_FILTER);
        LOGGER.atFine().log("Initialized VKLinkAuthenticationStep for account: %s", context.getAccount().getPlayerId());
    }
    // #endregion

    // #region Factory
    /**
     * Factory for creating {@code VKLinkAuthenticationStep} instances.
     */
    public static class VKLinkAuthenticationStepFactory extends AuthenticationStepFactoryTemplate {
        /**
         * Constructs a new {@code VKLinkAuthenticationStepFactory}.
         */
        public VKLinkAuthenticationStepFactory() {
            super(STEP_NAME);
            LOGGER.atFine().log("Initialized VKLinkAuthenticationStepFactory");
        }

        /**
         * Creates a new authentication step.
         *
         * @param context The authentication step context. Must not be null.
         * @return A new {@link AuthenticationStep}.
         */
        @Override
        public AuthenticationStep createNewAuthenticationStep(AuthenticationStepContext context) {
            LOGGER.atFine().log("Creating new VKLinkAuthenticationStep");
            return new VKLinkAuthenticationStep(context);
        }
    }
    // #endregion
}