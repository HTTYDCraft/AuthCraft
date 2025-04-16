package com.httydcraft.authcraft.core.config.discord;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.link.command.LinkCommandPaths;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.core.config.factory.ConfigurationHolderMapResolverFactory.ConfigurationHolderMap;

// #region Class Documentation
/**
 * Configuration for Discord command paths.
 * Maps command paths to their settings.
 */
public class DiscordCommandPaths implements ConfigurationHolder, LinkCommandPaths {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("self")
    private ConfigurationHolderMap<DiscordCommandSettings> defaultCommands = new ConfigurationHolderMap<>();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code DiscordCommandPaths} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public DiscordCommandPaths(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized DiscordCommandPaths with %d commands", defaultCommands.size());
    }
    // #endregion

    // #region Command Path Retrieval
    /**
     * Gets the command settings for a given path.
     *
     * @param commandPath The command path.
     * @return The {@link DiscordCommandSettings}, or {@code null} if not found.
     */
    @Override
    public DiscordCommandSettings getCommandPath(String commandPath) {
        DiscordCommandSettings settings = defaultCommands.getOrDefault(commandPath, null);
        LOGGER.atFine().log("Retrieved command path %s: %s", commandPath, settings != null ? "found" : "not found");
        return settings;
    }
    // #endregion
}