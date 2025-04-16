package com.httydcraft.authcraft.core.commands.parameter;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.link.user.confirmation.LinkConfirmationUser;

// #region Class Documentation
/**
 * Context for messenger link commands, holding the confirmation code and user data.
 */
public class MessengerLinkContext {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final LinkConfirmationUser confirmationUser;
    private final String linkCode;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code MessengerLinkContext}.
     *
     * @param linkCode        The confirmation code. Must not be null.
     * @param confirmationUser The confirmation user data. Must not be null.
     */
    public MessengerLinkContext(String linkCode, LinkConfirmationUser confirmationUser) {
        this.linkCode = Preconditions.checkNotNull(linkCode, "linkCode must not be null");
        this.confirmationUser = Preconditions.checkNotNull(confirmationUser, "confirmationUser must not be null");
        LOGGER.atFine().log("Created MessengerLinkContext with code: %s", linkCode);
    }
    // #endregion

    // #region Getters
    /**
     * Gets the confirmation code.
     *
     * @return The confirmation code.
     */
    public String getLinkCode() {
        return linkCode;
    }

    /**
     * Gets the confirmation user data.
     *
     * @return The {@link LinkConfirmationUser}.
     */
    public LinkConfirmationUser getConfirmationUser() {
        return confirmationUser;
    }
    // #endregion
}