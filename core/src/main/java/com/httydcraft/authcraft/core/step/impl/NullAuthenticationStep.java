package com.httydcraft.authcraft.core.step.impl;

import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.core.step.AuthenticationStepTemplate;
import com.httydcraft.authcraft.core.step.creators.AuthenticationStepFactoryTemplate;

// #region Class Documentation
/**
 * Null authentication step that does nothing.
 * Used as a placeholder or default step when no action is required.
 */
public class NullAuthenticationStep extends AuthenticationStepTemplate {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final String STEP_NAME = "NULL";
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code NullAuthenticationStep}.
     */
    public NullAuthenticationStep() {
        super(STEP_NAME, null);
        LOGGER.atFine().log("Initialized NullAuthenticationStep");
    }
    // #endregion

    // #region AuthenticationStep Implementation
    /**
     * Determines if the step should proceed to the next step.
     *
     * @return {@code true} always, as this step auto-proceeds.
     */
    @Override
    public boolean shouldPassToNextStep() {
        LOGGER.atFine().log("Checked shouldPassToNextStep: true");
        return true;
    }

    /**
     * Determines if the step should be skipped.
     *
     * @return {@code true} always, as this step is always skipped.
     */
    @Override
    public boolean shouldSkip() {
        LOGGER.atFine().log("Checked shouldSkip: true");
        return true;
    }
    // #endregion

    // #region Factory
    /**
     * Factory for creating {@code NullAuthenticationStep} instances.
     */
    public static class NullAuthenticationStepFactory extends AuthenticationStepFactoryTemplate {
        /**
         * Constructs a new {@code NullAuthenticationStepFactory}.
         */
        public NullAuthenticationStepFactory() {
            super(STEP_NAME);
            LOGGER.atFine().log("Initialized NullAuthenticationStepFactory");
        }

        /**
         * Creates a new authentication step.
         *
         * @param context The authentication step context (ignored).
         * @return A new {@link NullAuthenticationStep}.
         */
        @Override
        public NullAuthenticationStep createNewAuthenticationStep(AuthenticationStepContext context) {
            LOGGER.atFine().log("Creating new NullAuthenticationStep");
            return new NullAuthenticationStep();
        }
    }
    // #endregion
}