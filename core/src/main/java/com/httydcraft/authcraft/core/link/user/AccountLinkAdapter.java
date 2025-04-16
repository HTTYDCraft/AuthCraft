package com.httydcraft.authcraft.core.link.user;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.core.database.model.AccountLink;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.LinkUserInfo;

// #region Class Documentation
/**
 * Adapter for converting an {@link AccountLink} to a {@link LinkUser}.
 * Extends {@link LinkUserTemplate} to provide link-specific initialization.
 */
public class AccountLinkAdapter extends LinkUserTemplate {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AccountLinkAdapter}.
     *
     * @param accountLink The account link to adapt. Must not be null.
     * @param account     The associated account. Must not be null.
     */
    public AccountLinkAdapter(AccountLink accountLink, Account account) {
        super(findLinkType(accountLink), account, LinkUserInfo.of(
                LinkUserIdentificator.ofParsed(accountLink.getLinkUserId()),
                accountLink.isLinkEnabled()));
        Preconditions.checkNotNull(accountLink, "accountLink must not be null");
        Preconditions.checkNotNull(account, "account must not be null");
        LOGGER.atFine().log("Initialized AccountLinkAdapter for account: %s, linkType: %s",
                account.getPlayerId(), accountLink.getLinkType());
    }
    // #endregion

    // #region Helper Methods
    /**
     * Finds the link type for an account link.
     *
     * @param accountLink The account link. Must not be null.
     * @return The {@link LinkType}.
     * @throws IllegalArgumentException If the link type is not found.
     */
    private static LinkType findLinkType(AccountLink accountLink) {
        Preconditions.checkNotNull(accountLink, "accountLink must not be null");
        LinkType linkType = AuthPlugin.instance()
                .getLinkTypeProvider()
                .getLinkType(accountLink.getLinkType())
                .orElseThrow(() -> {
                    LOGGER.atSevere().log("Link type not found: %s", accountLink.getLinkType());
                    return new IllegalArgumentException("Link type " + accountLink.getLinkType() + " not exists!");
                });
        LOGGER.atFine().log("Found link type: %s", linkType.getName());
        return linkType;
    }
    // #endregion
}