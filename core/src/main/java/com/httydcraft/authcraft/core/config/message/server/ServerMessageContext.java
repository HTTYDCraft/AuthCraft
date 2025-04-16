package com.httydcraft.authcraft.core.config.message.server;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.core.config.message.context.account.BaseAccountPlaceholderContext;

// #region Class Documentation
/**
 * Context for server-specific message placeholders.
 * Extends account placeholder context for server messages.
 */
public class ServerMessageContext extends BaseAccountPlaceholderContext {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code ServerMessageContext}.
     *
     * @param account The account to provide placeholders for. Must not be null.
     */
    public ServerMessageContext(Account account) {
        super(Preconditions.checkNotNull(account, "account must not be null"));
        LOGGER.atFine().log("Initialized ServerMessageContext for account: %s", account.getName());
    }
    // #endregion
}