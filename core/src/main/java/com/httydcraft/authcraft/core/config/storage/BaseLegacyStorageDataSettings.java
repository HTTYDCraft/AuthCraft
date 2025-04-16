package com.httydcraft.authcraft.core.config.storage;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.database.LegacyStorageDataSettings;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.annotation.ImportantField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

// #region Class Documentation
/**
 * Configuration for legacy storage data settings.
 * Defines database connection parameters for legacy systems.
 */
public class BaseLegacyStorageDataSettings implements ConfigurationHolder, LegacyStorageDataSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ImportantField
    @ConfigField("host")
    private String host = null;
    @ImportantField
    @ConfigField("database")
    private String database = null;
    @ImportantField
    @ConfigField("username")
    private String user = null;
    @ImportantField
    @ConfigField("password")
    private String password = null;
    @ImportantField
    @ConfigField("port")
    private int port = 0;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseLegacyStorageDataSettings} from a configuration section.
     *
     * @param configurationSection The configuration section. Must not be null.
     */
    public BaseLegacyStorageDataSettings(ConfigurationSectionHolder configurationSection) {
        Preconditions.checkNotNull(configurationSection, "configurationSection must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(configurationSection, this);
        LOGGER.atInfo().log("Initialized BaseLegacyStorageDataSettings for host: %s, database: %s", host, database);
    }
    // #endregion

    // #region Getters
    /**
     * Gets the database host.
     *
     * @return The host.
     */
    @Override
    public String getHost() {
        return host;
    }

    /**
     * Gets the database name.
     *
     * @return The database name.
     */
    @Override
    public String getDatabase() {
        return database;
    }

    /**
     * Gets the database username.
     *
     * @return The username.
     */
    @Override
    public String getUser() {
        return user;
    }

    /**
     * Gets the database password.
     *
     * @return The password.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Gets the database port.
     *
     * @return The port.
     */
    @Override
    public int getPort() {
        return port;
    }
    // #endregion
}