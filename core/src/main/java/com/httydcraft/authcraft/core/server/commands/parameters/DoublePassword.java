package com.httydcraft.authcraft.core.server.commands.parameters;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;

// #region Class Documentation
/**
 * Represents a pair of old and new passwords for password change operations.
 */
public class DoublePassword {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final String oldPassword;
    private final String newPassword;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code DoublePassword}.
     *
     * @param oldPassword The old password. Must not be null.
     * @param newPassword The new password. Must not be null.
     */
    public DoublePassword(String oldPassword, String newPassword) {
        this.oldPassword = Preconditions.checkNotNull(oldPassword, "oldPassword must not be null");
        this.newPassword = Preconditions.checkNotNull(newPassword, "newPassword must not be null");
        LOGGER.atFine().log("Initialized DoublePassword");
    }
    // #endregion

    // #region Getters
    /**
     * Gets the old password.
     *
     * @return The old password.
     */
    public String getOldPassword() {
        LOGGER.atFine().log("Retrieved old password");
        return oldPassword;
    }

    /**
     * Gets the new password.
     *
     * @return The new password.
     */
    public String getNewPassword() {
        LOGGER.atFine().log("Retrieved new password");
        return newPassword;
    }
    // #endregion
}