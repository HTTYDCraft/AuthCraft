package com.httydcraft.authcraft.core.database.adapter;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.core.database.model.AuthAccount;

// #region Class Documentation
/**
 * Adapter for converting an {@link Account} to an {@link AuthAccount}.
 * Extends {@link AuthAccount} to provide specific initialization logic.
 */
public class AccountAdapter extends AuthAccount {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AccountAdapter}.
     *
     * @param account The account to adapt. Must not be null.
     */
    public AccountAdapter(Account account) {
        super(
                account.getDatabaseId(),
                Preconditions.checkNotNull(account, "account must not be null").getPlayerId(),
                account.getIdentifierType(),
                account.getCryptoProvider(),
                account.getLastIpAddress(),
                account.getUniqueId(),
                account.getName(),
                account.getPasswordHash() != null ? account.getPasswordHash().getHash() : null,
                account.getLastQuitTimestamp(),
                account.getLastSessionStartTimestamp()
        );
        LOGGER.atFine().log("Created AccountAdapter for playerId: %s", account.getPlayerId());
    }
    // #endregion
}