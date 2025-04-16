package com.httydcraft.authcraft.core.link.user.entry;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.entry.LinkEntryUser;
import com.httydcraft.authcraft.core.link.user.LinkUserTemplate;
import com.httydcraft.authcraft.api.link.user.info.LinkUserInfo;

// #region Class Documentation
/**
 * Base implementation of {@link LinkEntryUser}.
 * Extends {@link LinkUserTemplate} to support link confirmation functionality.
 */
public class BaseLinkEntryUser extends LinkUserTemplate implements LinkEntryUser {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final long confirmationStartTime = System.currentTimeMillis();
    private final LinkUserInfo linkUserInfo;
    private boolean confirmed;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseLinkEntryUser}.
     *
     * @param linkType     The link type. Must not be null.
     * @param account      The associated account. Must not be null.
     * @param linkUserInfo The link user information. May be null.
     */
    public BaseLinkEntryUser(LinkType linkType, Account account, LinkUserInfo linkUserInfo) {
        super(linkType, account, linkUserInfo);
        this.linkUserInfo = linkUserInfo;
        this.confirmed = false;
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(account, "account must not be null");
        LOGGER.atFine().log("Initialized BaseLinkEntryUser for account: %s, linkType: %s",
                account.getPlayerId(), linkType.getName());
    }
    // #endregion

    // #region Confirmation Information
    /**
     * Gets the confirmation start time.
     *
     * @return The confirmation start time in milliseconds.
     */
    public long getConfirmationStartTime() {
        LOGGER.atFine().log("Retrieved confirmation start time: %d", confirmationStartTime);
        return confirmationStartTime;
    }

    /**
     * Gets the link user information.
     *
     * @return The {@link LinkUserInfo}, or {@code null} if not set.
     */
    @Override
    public LinkUserInfo getLinkUserInfo() {
        LOGGER.atFine().log("Retrieved link user info: %s", linkUserInfo != null ? "present" : "null");
        return linkUserInfo;
    }

    /**
     * Checks if the link is confirmed.
     *
     * @return {@code true} if confirmed, {@code false} otherwise.
     */
    @Override
    public boolean isConfirmed() {
        LOGGER.atFine().log("Retrieved confirmation status: %b", confirmed);
        return confirmed;
    }

    /**
     * Sets the confirmation status.
     *
     * @param confirmed The confirmation status.
     */
    @Override
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
        LOGGER.atFine().log("Set confirmation status to: %b", confirmed);
    }
    // #endregion
}