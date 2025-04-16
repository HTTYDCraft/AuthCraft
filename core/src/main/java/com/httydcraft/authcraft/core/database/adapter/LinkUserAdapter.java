package com.httydcraft.authcraft.core.database.adapter;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.core.database.model.AccountLink;
import com.httydcraft.authcraft.core.database.model.AuthAccount;
import com.httydcraft.authcraft.api.link.user.LinkUser;

// #region Class Documentation
/**
 * Adapter for converting a {@link LinkUser} to an {@link AccountLink}.
 * Extends {@link AccountLink} to provide specific initialization logic.
 */
public class LinkUserAdapter extends AccountLink {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code LinkUserAdapter}.
     *
     * @param linkUser The link user to adapt. Must not be null.
     * @param account  The associated account. Must not be null.
     */
    public LinkUserAdapter(LinkUser linkUser, AuthAccount account) {
        super(
                Preconditions.checkNotNull(linkUser, "linkUser must not be null").getLinkType().getName(),
                linkUser.getLinkUserInfo() != null ? linkUser.getLinkUserInfo().getIdentificator().asString() : null,
                linkUser.getLinkUserInfo() != null && linkUser.getLinkUserInfo().isConfirmationEnabled(),
                Preconditions.checkNotNull(account, "account must not be null")
        );
        LOGGER.atFine().log("Created LinkUserAdapter for linkType: %s, accountId: %d", linkUser.getLinkType().getName(), account.getId());
    }
    // #endregion
}