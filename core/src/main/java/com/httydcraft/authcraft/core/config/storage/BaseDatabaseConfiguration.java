package com.httydcraft.authcraft.core.config.storage;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.database.DatabaseSettings;
import com.httydcraft.authcraft.api.config.database.schema.SchemaSettings;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.annotation.ImportantField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.core.config.resolver.RawURLProviderFieldResolverFactory.RawURLProvider;
import com.httydcraft.authcraft.core.config.resolver.RawURLProviderFieldResolverFactory.SqlConnectionUrl;
import com.httydcraft.authcraft.core.config.storage.schema.BaseSchemaSettings;

import java.io.File;

// #region Class Documentation
/**
 * Configuration for database settings.
 * Defines connection URL, credentials, driver settings, and schema configuration.
 */
public class BaseDatabaseConfiguration implements ConfigurationHolder, DatabaseSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();

    @SqlConnectionUrl
    @ImportantField
    @ConfigField("url")
    private RawURLProvider urlProvider;
    @ImportantField
    @ConfigField("download-url")
    private String driverDownloadUrl;
    @ConfigField("username")
    private String username;
    @ConfigField("password")
    private String password;
    @ConfigField("cache-driver-path")
    private File cacheDriverPath = new File(PLUGIN.getFolder(), "database-driver.jar");
    @ConfigField("migration")
    private boolean migration = true;
    @ConfigField("scheme")
    private BaseSchemaSettings schemaSettings = new BaseSchemaSettings();
    // #endregion

    // #region Constructors
    /**
     * Constructs a new {@code BaseDatabaseConfiguration} from a configuration section.
     *
     * @param sectionHolder The configuration section. Must not be null.
     */
    public BaseDatabaseConfiguration(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized BaseDatabaseConfiguration");
    }

    /**
     * Constructs a new {@code BaseDatabaseConfiguration} with explicit values.
     *
     * @param urlProvider        The URL provider. Must not be null.
     * @param driverDownloadUrl  The driver download URL. Must not be null.
     * @param username           The database username.
     * @param password           The database password.
     */
    public BaseDatabaseConfiguration(RawURLProvider urlProvider, String driverDownloadUrl, String username, String password) {
        this.urlProvider = Preconditions.checkNotNull(urlProvider, "urlProvider must not be null");
        this.driverDownloadUrl = Preconditions.checkNotNull(driverDownloadUrl, "driverDownloadUrl must not be null");
        this.username = username;
        this.password = password;
        LOGGER.atInfo().log("Initialized BaseDatabaseConfiguration with explicit values");
    }
    // #endregion

    // #region Getters
    /**
     * Gets the database connection URL.
     *
     * @return The connection URL.
     */
    @Override
    public String getConnectionUrl() {
        String url = urlProvider.url();
        LOGGER.atFine().log("Retrieved connection URL");
        return url;
    }

    /**
     * Gets the database username.
     *
     * @return The username.
     */
    @Override
    public String getUsername() {
        return username;
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
     * Gets the driver download URL.
     *
     * @return The driver download URL.
     */
    @Override
    public String getDriverDownloadUrl() {
        return driverDownloadUrl;
    }

    /**
     * Gets the cache driver path.
     *
     * @return The cache driver file.
     */
    @Override
    public File getCacheDriverPath() {
        return cacheDriverPath;
    }

    /**
     * Checks if database migration is enabled.
     *
     * @return {@code true} if migration is enabled, {@code false} otherwise.
     */
    @Override
    public boolean isMigrationEnabled() {
        return migration;
    }

    /**
     * Gets the schema settings.
     *
     * @return The {@link SchemaSettings}.
     */
    @Override
    public SchemaSettings getSchemaSettings() {
        return schemaSettings;
    }
    // #endregion
}