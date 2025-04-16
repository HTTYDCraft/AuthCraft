package com.httydcraft.authcraft.core.step.context;

import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;

// #region Class Documentation
/**
 * Basic implementation of an authentication step context.
 * Extends {@link AuthenticationStepContextTemplate} with minimal functionality.
 */
public class BaseAuthenticationStepContext extends AuthenticationStepContextTemplate {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseAuthenticationStepContext}.
     *
     * @param account The account associated with this context. Must not be null.
     */
    public BaseAuthenticationStepContext(Account account) {
        super(account);
        LOGGER.atFine().log("Initialized BaseAuthenticationStepContext for account: %s", account.getPlayerId());
    }
    // #endregion
}