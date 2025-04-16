package com.httydcraft.authcraft.core.link.user;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserInfo;

// #region Class Documentation
/**
 * Template implementation of {@link LinkUser}.
 * Represents a user linked to an account and link type.
 */
public class LinkUserTemplate implements LinkUser {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final LinkType linkType;
    private final Account account;
    private final LinkUserInfo linkUserInfo;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code LinkUserTemplate}.
     *
     * @param linkType     The link type. Must not be null.
     * @param account      The associated account. Must not be null.
     * @param linkUserInfo The link user information. May be null.
     */
    public LinkUserTemplate(LinkType linkType, Account account, LinkUserInfo linkUserInfo) {
        this.linkType = Preconditions.checkNotNull(linkType, "linkType must not be null");
        this.account = Preconditions.checkNotNull(account, "account must not be null");
        this.linkUserInfo = linkUserInfo;
        LOGGER.atFine().log("Initialized LinkUserTemplate for account: %s, linkType: %s", account.getPlayerId(), linkType.getName());
    }
    // #endregion

    // #region Link Information
    /**
     * Gets the link type.
     *
     * @return The {@link LinkType}.
     */
    @Override
    public LinkType getLinkType() {
        LOGGER.atFine().log("Retrieved link type: %s", linkType.getName());
        return linkType;
    }

    /**
     * Gets the associated account.
     *
     * @return The {@link Account}.
     */
    @Override
    public Account getAccount() {
        LOGGER.atFine().log("Retrieved account: %s", account.getPlayerId());
        return account;
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
    // #endregion

    // #region Factory Method
    /**
     * Creates a new {@link LinkUser} instance.
     *
     * @param linkType     The link type. Must not be null.
     * @param account      The associated account. Must not be null.
     * @param linkUserInfo The link user information. May be null.
     * @return The created {@link LinkUser}.
     */
    public static LinkUser of(LinkType linkType, Account account, LinkUserInfo linkUserInfo) {
        LinkUser user = new LinkUserTemplate(linkType, account, linkUserInfo);
        LOGGER.atFine().log("Created LinkUser for account: %s, linkType: %s", account.getPlayerId(), linkType.getName());
        return user;
    }
    // #endregion
}