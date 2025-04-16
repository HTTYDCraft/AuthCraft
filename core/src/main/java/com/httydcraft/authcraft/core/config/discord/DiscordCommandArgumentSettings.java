package com.httydcraft.authcraft.core.config.discord;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

// #region Class Documentation
/**
 * Configuration settings for Discord command arguments.
 * Defines the name and description of a command argument.
 */
public class DiscordCommandArgumentSettings implements ConfigurationHolder {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("name")
    private String name;
    @ConfigField("description")
    private String description;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code DiscordCommandArgumentSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public DiscordCommandArgumentSettings(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized DiscordCommandArgumentSettings with name: %s", name);
    }
    // #endregion

    // #region Getters
    /**
     * Gets the name of the command argument.
     *
     * @return The argument name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description of the command argument.
     *
     * @return The argument description.
     */
    public String getDescription() {
        return description;
    }
    // #endregion
}