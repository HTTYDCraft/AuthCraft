package com.httydcraft.authcraft.core.server.commands.parameters;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;

// #region Class Documentation
/**
 * Represents a new password for password change or reset operations.
 */
public class NewPassword {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final String newPassword;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code NewPassword}.
     *
     * @param newPassword The new password. Must not be null.
     */
    public NewPassword(String newPassword) {
        this.newPassword = Preconditions.checkNotNull(newPassword, "newPassword must not be null");
        LOGGER.atFine().log("Initialized NewPassword");
    }
    // #endregion

    // #region Getter
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