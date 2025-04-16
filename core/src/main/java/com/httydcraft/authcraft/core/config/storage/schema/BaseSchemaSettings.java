package com.httydcraft.authcraft.core.config.storage.schema;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.database.schema.SchemaSettings;
import com.httydcraft.authcraft.api.config.database.schema.TableSettings;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.core.config.factory.ConfigurationHolderMapResolverFactory.ConfigurationHolderMap;

import java.util.Optional;

// #region Class Documentation
/**
 * Configuration for database schema settings.
 * Manages table settings for the schema.
 */
public class BaseSchemaSettings implements ConfigurationHolder, SchemaSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("self")
    private ConfigurationHolderMap<BaseTableSettings> tableSettings = new ConfigurationHolderMap<>();
    // #endregion

    // #region Constructors
    /**
     * Constructs a new {@code BaseSchemaSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section. Must not be null.
     */
    public BaseSchemaSettings(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized BaseSchemaSettings with %d table settings", tableSettings.size());
    }

    /**
     * Default constructor for {@code BaseSchemaSettings}.
     */
    public BaseSchemaSettings() {
        LOGGER.atFine().log("Initialized BaseSchemaSettings with default values");
    }
    // #endregion

    // #region Table Settings Retrieval
    /**
     * Gets table settings for a given key.
     *
     * @param key The table key. Must not be null.
     * @return An {@link Optional} containing the {@link TableSettings}, or empty if not found.
     */
    @Override
    public Optional<TableSettings> getTableSettings(String key) {
        Preconditions.checkNotNull(key, "key must not be null");
        TableSettings settings = tableSettings.get(key);
        LOGGER.atFine().log("Retrieved table settings for key: %s, found: %b", key, settings != null);
        return Optional.ofNullable(settings);
    }
    // #endregion
}