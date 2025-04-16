package com.httydcraft.authcraft.core.link.user.confirmation;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.confirmation.LinkConfirmationUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.type.LinkConfirmationType;

// #region Class Documentation
/**
 * Base implementation of {@link LinkConfirmationUser}.
 * Manages confirmation details for linking a user.
 */
public class BaseLinkConfirmationUser implements LinkConfirmationUser {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final LinkConfirmationType linkConfirmationType;
    private final long linkTimeoutTimestamp;
    private final LinkType linkType;
    private final Account target;
    private final String code;
    private LinkUserIdentificator linkUserIdentificator;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseLinkConfirmationUser}.
     *
     * @param linkConfirmationType The confirmation type. Must not be null.
     * @param linkTimeoutTimestamp The timeout timestamp.
     * @param linkType             The link type. Must not be null.
     * @param target               The target account. Must not be null.
     * @param code                 The confirmation code. Must not be null.
     */
    public BaseLinkConfirmationUser(LinkConfirmationType linkConfirmationType, long linkTimeoutTimestamp,
                                    LinkType linkType, Account target, String code) {
        this.linkConfirmationType = Preconditions.checkNotNull(linkConfirmationType, "linkConfirmationType must not be null");
        this.linkTimeoutTimestamp = linkTimeoutTimestamp;
        this.linkType = Preconditions.checkNotNull(linkType, "linkType must not be null");
        this.target = Preconditions.checkNotNull(target, "target must not be null");
        this.code = Preconditions.checkNotNull(code, "code must not be null");
        LOGGER.atFine().log("Initialized BaseLinkConfirmationUser for account: %s, linkType: %s",
                target.getPlayerId(), linkType.getName());
    }
    // #endregion

    // #region Confirmation Details
    /**
     * Gets the confirmation code.
     *
     * @return The confirmation code.
     */
    @Override
    public String getConfirmationCode() {
        LOGGER.atFine().log("Retrieved confirmation code");
        return code;
    }

    /**
     * Gets the link confirmation type.
     *
     * @return The {@link LinkConfirmationType}.
     */
    @Override
    public LinkConfirmationType getLinkConfirmationType() {
        LOGGER.atFine().log("Retrieved link confirmation type: %s", linkConfirmationType);
        return linkConfirmationType;
    }

    /**
     * Gets the target account for linking.
     *
     * @return The {@link Account}.
     */
    @Override
    public Account getLinkTarget() {
        LOGGER.atFine().log("Retrieved link target: %s", target.getPlayerId());
        return target;
    }

    /**
     * Gets the link timeout timestamp.
     *
     * @return The timeout timestamp in milliseconds.
     */
    @Override
    public long getLinkTimeoutTimestamp() {
        LOGGER.atFine().log("Retrieved link timeout timestamp: %d", linkTimeoutTimestamp);
        return linkTimeoutTimestamp;
    }

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
     * Gets the user identifier for the link.
     *
     * @return The {@link LinkUserIdentificator}, or {@code null} if not set.
     */
    @Override
    public LinkUserIdentificator getLinkUserIdentificator() {
        LOGGER.atFine().log("Retrieved link user identifier: %s", linkUserIdentificator != null ? linkUserIdentificator : "null");
        return linkUserIdentificator;
    }

    /**
     * Sets the user identifier for the link.
     *
     * @param linkUserIdentificator The user identifier to set.
     */
    @Override
    public void setLinkUserIdentificator(LinkUserIdentificator linkUserIdentificator) {
        this.linkUserIdentificator = linkUserIdentificator;
        LOGGER.atFine().log("Set link user identifier: %s", linkUserIdentificator != null ? linkUserIdentificator : "null");
    }
    // #endregion
}