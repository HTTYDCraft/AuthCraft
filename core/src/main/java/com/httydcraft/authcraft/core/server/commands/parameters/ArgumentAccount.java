package com.httydcraft.authcraft.core.server.commands.parameters;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;

import java.util.concurrent.CompletableFuture;

// #region Class Documentation
/**
 * Represents an account argument for command parameters.
 * Wraps a {@link CompletableFuture} to resolve an {@link Account}.
 */
public class ArgumentAccount {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final CompletableFuture<Account> account;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code ArgumentAccount}.
     *
     * @param account The future resolving to an account. Must not be null.
     */
    public ArgumentAccount(CompletableFuture<Account> account) {
        this.account = Preconditions.checkNotNull(account, "account must not be null");
        LOGGER.atFine().log("Initialized ArgumentAccount");
    }
    // #endregion

    // #region Getter
    /**
     * Gets the future resolving to the account.
     *
     * @return The {@link CompletableFuture} for the {@link Account}.
     */
    public CompletableFuture<Account> future() {
        LOGGER.atFine().log("Retrieved account future");
        return account;
    }
    // #endregion
}