package com.httydcraft.authcraft.core.config.discord;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.core.config.link.BaseCommandPath;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.core.config.factory.ConfigurationHolderMapResolverFactory.ConfigurationHolderMap;

import java.util.Map;

// #region Class Documentation
/**
 * Configuration settings for a Discord command.
 * Extends base command path settings with argument mappings.
 */
public class DiscordCommandSettings extends BaseCommandPath {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("arguments")
    private ConfigurationHolderMap<DiscordCommandArgumentSettings> arguments = new ConfigurationHolderMap<>();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code DiscordCommandSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public DiscordCommandSettings(ConfigurationSectionHolder sectionHolder) {
        super(sectionHolder);
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized DiscordCommandSettings with %d arguments", arguments.size());
    }
    // #endregion

    // #region Getter
    /**
     * Gets the arguments for the command.
     *
     * @return A map of argument names to {@link DiscordCommandArgumentSettings}.
     */
    public Map<String, DiscordCommandArgumentSettings> getArguments() {
        return arguments;
    }
    // #endregion
}