package com.httydcraft.authcraft.core.step;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.step.AuthenticationStep;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;

// #region Class Documentation
/**
 * Abstract template for authentication steps.
 * Provides common functionality for step name and context management.
 */
public abstract class AuthenticationStepTemplate implements AuthenticationStep {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    protected final String stepName;
    protected final AuthenticationStepContext authenticationStepContext;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AuthenticationStepTemplate}.
     *
     * @param stepName The name of the authentication step. Must not be null.
     * @param authenticationStepContext The authentication step context. May be null for certain steps.
     */
    public AuthenticationStepTemplate(String stepName, AuthenticationStepContext authenticationStepContext) {
        this.stepName = Preconditions.checkNotNull(stepName, "stepName must not be null");
        this.authenticationStepContext = authenticationStepContext;
        LOGGER.atFine().log("Initialized AuthenticationStepTemplate with step name: %s", stepName);
    }
    // #endregion

    // #region AuthenticationStep Implementation
    /**
     * Gets the name of the authentication step.
     *
     * @return The step name.
     */
    @Override
    public String getStepName() {
        LOGGER.atFine().log("Retrieved step name: %s", stepName);
        return stepName;
    }

    /**
     * Gets the authentication step context.
     *
     * @return The {@link AuthenticationStepContext}, or null if not applicable.
     */
    @Override
    public AuthenticationStepContext getAuthenticationStepContext() {
        LOGGER.atFine().log("Retrieved authentication step context");
        return authenticationStepContext;
    }
    // #endregion
}