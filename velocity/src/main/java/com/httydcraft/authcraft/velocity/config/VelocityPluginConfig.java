package com.httydcraft.authcraft.velocity.config;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.core.config.SpongeConfiguratePluginConfig;

// #region Class Documentation
/**
 * Velocity-specific plugin configuration.
 * Extends {@link SpongeConfiguratePluginConfig} to provide configuration for Velocity.
 */
public class VelocityPluginConfig extends SpongeConfiguratePluginConfig {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VelocityPluginConfig}.
     *
     * @param proxyPlugin The AuthCraft plugin instance. Must not be null.
     */
    public VelocityPluginConfig(AuthPlugin proxyPlugin) {
        super(Preconditions.checkNotNull(proxyPlugin, "proxyPlugin must not be null"));
        LOGGER.atInfo().log("Initialized VelocityPluginConfig");
    }
    // #endregion

    // #region Configuration Creation
    /**
     * Creates a configuration section for the plugin.
     * Currently returns null as a placeholder for future implementation.
     *
     * @param plugin The AuthCraft plugin instance. Must not be null.
     * @return The configuration section, or null.
     */
    @Override
    protected ConfigurationSectionHolder createConfiguration(AuthPlugin plugin) {
        Preconditions.checkNotNull(plugin, "plugin must not be null");
        LOGGER.atWarning().log("Configuration creation not implemented, returning null");
        return null;
    }
    // #endregion
}