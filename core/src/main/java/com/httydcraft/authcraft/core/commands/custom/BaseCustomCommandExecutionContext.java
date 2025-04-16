package com.httydcraft.authcraft.core.commands.custom;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.link.command.context.CustomCommandExecutionContext;

// #region Class Documentation
/**
 * Base implementation of a custom command execution context.
 * Tracks the execution text and whether the command was triggered by a button.
 */
public class BaseCustomCommandExecutionContext implements CustomCommandExecutionContext {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final String executionText;
    private boolean isButtonExecution;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseCustomCommandExecutionContext}.
     *
     * @param executionText The text associated with the command execution. Must not be null.
     */
    public BaseCustomCommandExecutionContext(String executionText) {
        this.executionText = Preconditions.checkNotNull(executionText, "executionText must not be null");
        LOGGER.atFine().log("Created BaseCustomCommandExecutionContext with executionText: %s", executionText);
    }
    // #endregion

    // #region Getters and Setters
    /**
     * Gets the execution text.
     *
     * @return The execution text.
     */
    @Override
    public String getExecutionText() {
        return executionText;
    }

    /**
     * Checks if the command was triggered by a button.
     *
     * @return {@code true} if triggered by a button, {@code false} otherwise.
     */
    @Override
    public boolean isButtonExecution() {
        return isButtonExecution;
    }

    /**
     * Sets whether the command was triggered by a button.
     *
     * @param isButtonExecution The button execution status.
     */
    @Override
    public void setButtonExecution(boolean isButtonExecution) {
        this.isButtonExecution = isButtonExecution;
        LOGGER.atFine().log("Set isButtonExecution to %b", isButtonExecution);
    }
    // #endregion
}