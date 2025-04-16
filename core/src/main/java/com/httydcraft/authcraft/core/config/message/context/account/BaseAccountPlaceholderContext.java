package com.httydcraft.authcraft.core.config.message.context.account;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.core.config.message.context.placeholder.MessagePlaceholderContext;
import com.httydcraft.authcraft.core.config.message.context.placeholder.PlaceholderProvider;

// #region Class Documentation
/**
 * Base context for account-related placeholders.
 * Provides placeholders for account name and last IP address.
 */
public class BaseAccountPlaceholderContext extends MessagePlaceholderContext {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    protected final Account account;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseAccountPlaceholderContext}.
     *
     * @param account The account to provide placeholders for. Must not be null.
     */
    public BaseAccountPlaceholderContext(Account account) {
        this.account = Preconditions.checkNotNull(account, "account must not be null");
        registerPlaceholderProvider(PlaceholderProvider.of(account.getName(), "%name%", "%nick%", "%account_name%", "%account_nick%", "%correct%"));
        registerPlaceholderProvider(PlaceholderProvider.of(account.getLastIpAddress(), "%account_ip%", "%account_last_ip%"));
        LOGGER.atFine().log("Initialized BaseAccountPlaceholderContext for account: %s", account.getName());
    }
    // #endregion
}