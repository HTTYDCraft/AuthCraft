package com.httydcraft.authcraft.config;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.AuthPlugin;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

// #region Class Documentation
/**
 * Spigot-specific plugin configuration.
 * Extends {@link SpongeConfiguratePluginConfig} to provide configuration for Spigot.
 */
public class SpigotPluginConfig extends SpongeConfiguratePluginConfig {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final File configFile;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code SpigotPluginConfig}.
     *
     * @param proxyPlugin The AuthCraft plugin instance. Must not be null.
     */
    public SpigotPluginConfig(AuthPlugin proxyPlugin) {
        super(Preconditions.checkNotNull(proxyPlugin, "proxyPlugin must not be null"));
        this.configFile = new File(proxyPlugin.getDataFolder(), "config.yml");
        LOGGER.atInfo().log("Initialized SpigotPluginConfig");
    }
    // #endregion

    // #region Configuration Creation
    /**
     * Creates a configuration section for the plugin.
     *
     * @param plugin The AuthCraft plugin instance. Must not be null.
     * @return The configuration section.
     */
    @Override
    protected ConfigurationSectionHolder createConfiguration(AuthPlugin plugin) {
        Preconditions.checkNotNull(plugin, "plugin must not be null");
        LOGGER.atFine().log("Creating configuration from file: %s", configFile.getPath());
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        return new BukkitConfigurationHolder(config);
    }
    // #endregion

    // #region Helper Classes
    /**
     * Holder for Bukkit configuration sections.
     */
    private static class BukkitConfigurationHolder implements ConfigurationSectionHolder {
        private final FileConfiguration config;

        public BukkitConfigurationHolder(FileConfiguration config) {
            this.config = config;
        }

        @Override
        public Object get(String path) {
            return config.get(path);
        }

        @Override
        public void set(String path, Object value) {
            config.set(path, value);
        }

        // Реализация других методов ConfigurationSectionHolder по необходимости
    }
    // #endregion
}