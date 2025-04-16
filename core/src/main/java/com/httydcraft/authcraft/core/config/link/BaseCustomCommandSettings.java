package com.httydcraft.authcraft.core.config.link;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.config.link.command.LinkCustomCommandSettings;
import com.httydcraft.authcraft.api.link.command.context.CustomCommandExecutionContext;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

import java.util.regex.Pattern;

// #region Class Documentation
/**
 * Configuration settings for a custom command.
 * Defines matching criteria and response for command execution.
 */
public class BaseCustomCommandSettings implements LinkCustomCommandSettings, ConfigurationHolder {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    private final ConfigurationSectionHolder sectionHolder;
    private final String key;
    private final String answer;
    private Pattern matchCommand = null;
    private boolean ignoreButtons = false;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseCustomCommandSettings}.
     *
     * @param key           The command key. Must not be null.
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public BaseCustomCommandSettings(String key, ConfigurationSectionHolder sectionHolder) {
        this.key = Preconditions.checkNotNull(key, "key must not be null");
        this.sectionHolder = Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        this.answer = sectionHolder.getString("answer");
        if (sectionHolder.contains("regex")) {
            this.matchCommand = Pattern.compile(sectionHolder.getString("regex"));
        }
        if (sectionHolder.contains("button-ignore")) {
            this.ignoreButtons = sectionHolder.getBoolean("button-ignore");
        }
        LOGGER.atInfo().log("Initialized BaseCustomCommandSettings for key: %s", key);
    }
    // #endregion

    // #region Command Execution
    /**
     * Determines if the command should execute based on the context.
     *
     * @param context The execution context. Must not be null.
     * @return {@code true} if the command should execute, {@code false} otherwise.
     */
    @Override
    public boolean shouldExecute(CustomCommandExecutionContext context) {
        Preconditions.checkNotNull(context, "context must not be null");

        if (ignoreButtons && context.isButtonExecution()) {
            LOGGER.atFine().log("Command ignored due to button execution for key: %s", key);
            return false;
        }
        if (context.getExecutionText() == null) {
            LOGGER.atFine().log("No execution text for key: %s", key);
            return false;
        }
        boolean shouldExecute;
        if (matchCommand == null) {
            shouldExecute = key.equalsIgnoreCase(context.getExecutionText());
        } else {
            shouldExecute = matchCommand.matcher(context.getExecutionText()).matches();
        }
        LOGGER.atFine().log("Command execution check for key: %s, result: %b", key, shouldExecute);
        return shouldExecute;
    }
    // #endregion

    // #region Getters
    /**
     * Gets the response for the command.
     *
     * @return The answer string.
     */
    @Override
    public String getAnswer() {
        return answer;
    }

    /**
     * Gets the configuration section holder.
     *
     * @return The {@link ConfigurationSectionHolder}.
     */
    @Override
    public ConfigurationSectionHolder getSectionHolder() {
        return sectionHolder;
    }
    // #endregion
}