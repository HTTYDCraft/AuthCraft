package com.httydcraft.authcraft.core.config;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.configuration.configurate.holder.ConfigurationNodeHolder;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.resource.impl.FolderResourceReader;

// #region Class Documentation
/**
 * Base configuration class for the AuthCraft plugin.
 * Extends {@link SpongeConfiguratePluginConfig} to provide configuration loading.
 */
public class BasePluginConfig extends SpongeConfiguratePluginConfig {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #region Constructor
    /**
     * Constructs a new {@code BasePluginConfig}.
     *
     * @param proxyPlugin The AuthPlugin instance. Must not be null.
     */
    public BasePluginConfig(AuthPlugin proxyPlugin) {
        super(proxyPlugin);
        LOGGER.atInfo().log("Initialized BasePluginConfig");
    }
    // #endregion

    // #region Configuration Loading
    /**
     * Creates a configuration section from the plugin's folder and resources.
     *
     * @param proxyPlugin The AuthPlugin instance. Must not be null.
     * @return The configuration section.
     */
    @Override
    protected ConfigurationSectionHolder createConfiguration(AuthPlugin proxyPlugin) {
        Preconditions.checkNotNull(proxyPlugin, "proxyPlugin must not be null");
        LOGGER.atFine().log("Creating configuration for AuthPlugin");

        ConfigurationSectionHolder holder = new ConfigurationNodeHolder(
                loadConfiguration(proxyPlugin.getFolder(),
                        new FolderResourceReader(proxyPlugin.getClass().getClassLoader(), "configurations").read()));
        LOGGER.atInfo().log("Created configuration section");
        return holder;
    }
    // #endregion
}