package com.httydcraft.authcraft.core.step.context;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;

// #region Class Documentation
/**
 * Abstract template for authentication step contexts.
 * Provides common functionality for managing accounts and step progression.
 */
public abstract class AuthenticationStepContextTemplate implements AuthenticationStepContext {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    protected final Account account;
    protected boolean canPassToNextStep;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AuthenticationStepContextTemplate}.
     *
     * @param account The account associated with this context. Must not be null.
     */
    public AuthenticationStepContextTemplate(Account account) {
        this.account = Preconditions.checkNotNull(account, "account must not be null");
        this.canPassToNextStep = false;
        LOGGER.atFine().log("Initialized AuthenticationStepContextTemplate for account: %s", account.getPlayerId());
    }
    // #endregion

    // #region AuthenticationStepContext Implementation
    /**
     * Gets the account associated with this context.
     *
     * @return The {@link Account}.
     */
    @Override
    public Account getAccount() {
        LOGGER.atFine().log("Retrieved account: %s", account.getPlayerId());
        return account;
    }

    /**
     * Checks if the context allows passing to the next authentication step.
     *
     * @return {@code true} if the step can proceed, {@code false} otherwise.
     */
    @Override
    public boolean canPassToNextStep() {
        LOGGER.atFine().log("Checked canPassToNextStep: %b", canPassToNextStep);
        return canPassToNextStep;
    }

    /**
     * Sets whether the context allows passing to the next authentication step.
     *
     * @param canPass {@code true} to allow progression, {@code false} otherwise.
     */
    @Override
    public void setCanPassToNextStep(boolean canPass) {
        this.canPassToNextStep = canPass;
        LOGGER.atFine().log("Set canPassToNextStep to: %b", canPass);
    }
    // #endregion
}