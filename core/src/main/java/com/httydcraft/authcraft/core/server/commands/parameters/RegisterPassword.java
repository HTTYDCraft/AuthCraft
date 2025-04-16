package com.httydcraft.authcraft.core.server.commands.parameters;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;

// #region Class Documentation
/**
 * Represents a password for account registration.
 */
public class RegisterPassword {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final String password;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code RegisterPassword}.
     *
     * @param password The registration password. Must not be null.
     */
    public RegisterPassword(String password) {
        this.password = Preconditions.checkNotNull(password, "password must not be null");
        LOGGER.atFine().log("Initialized RegisterPassword");
    }
    // #endregion

    // #region Getter
    /**
     * Gets the registration password.
     *
     * @return The password.
     */
    public String getPassword() {
        LOGGER.atFine().log("Retrieved registration password");
        return password;
    }
    // #endregion
}