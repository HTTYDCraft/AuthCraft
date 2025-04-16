package com.httydcraft.authcraft.core.config.link;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.link.command.LinkCommandPathSettings;
import com.httydcraft.authcraft.api.config.link.command.LinkCommandPaths;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.core.config.factory.ConfigurationHolderMapResolverFactory.ConfigurationHolderMap;

// #region Class Documentation
/**
 * Configuration for multiple command paths.
 * Maps command keys to their settings.
 */
public class BaseCommandPaths implements ConfigurationHolder, LinkCommandPaths {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("self")
    private ConfigurationHolderMap<BaseCommandPath> defaultCommands = new ConfigurationHolderMap<>();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseCommandPaths} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public BaseCommandPaths(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized BaseCommandPaths with %d commands", defaultCommands.size());
    }
    // #endregion

    // #region Command Path Retrieval
    /**
     * Gets the command path settings for a given key.
     *
     * @param commandKey The command key.
     * @return The {@link BaseCommandPath}, or {@code null} if not found.
     */
    public BaseCommandPath getPath(String commandKey) {
        BaseCommandPath path = defaultCommands.getOrDefault(commandKey, null);
        LOGGER.atFine().log("Retrieved command path %s: %s", commandKey, path != null ? "found" : "not found");
        return path;
    }

    /**
     * Gets the command path settings for a given path.
     *
     * @param commandPath The command path.
     * @return The {@link LinkCommandPathSettings}, or {@code null} if not found.
     */
    @Override
    public LinkCommandPathSettings getCommandPath(String commandPath) {
        return getPath(commandPath);
    }
    // #endregion
}