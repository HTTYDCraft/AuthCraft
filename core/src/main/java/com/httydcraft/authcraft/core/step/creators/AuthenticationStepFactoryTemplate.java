package com.httydcraft.authcraft.core.step.creators;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.factory.AuthenticationStepFactory;

// #region Class Documentation
/**
 * Abstract template for authentication step factories.
 * Provides a base for creating authentication steps with a specified name.
 */
public abstract class AuthenticationStepFactoryTemplate implements AuthenticationStepFactory {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    protected final String authenticationStepName;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AuthenticationStepFactoryTemplate}.
     *
     * @param authenticationStepName The name of the authentication step. Must not be null.
     */
    public AuthenticationStepFactoryTemplate(String authenticationStepName) {
        this.authenticationStepName = Preconditions.checkNotNull(authenticationStepName, "authenticationStepName must not be null");
        LOGGER.atFine().log("Initialized AuthenticationStepFactoryTemplate with step name: %s", authenticationStepName);
    }
    // #endregion

    // #region AuthenticationStepFactory Implementation
    /**
     * Gets the name of the authentication step.
     *
     * @return The step name.
     */
    @Override
    public String getAuthenticationStepName() {
        LOGGER.atFine().log("Retrieved authentication step name: %s", authenticationStepName);
        return authenticationStepName;
    }
    // #endregion
}