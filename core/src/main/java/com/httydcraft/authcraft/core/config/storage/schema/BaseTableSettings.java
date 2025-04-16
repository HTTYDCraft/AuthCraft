package com.httydcraft.authcraft.core.config.storage.schema;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.database.schema.TableSettings;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// #region Class Documentation
/**
 * Configuration for database table settings.
 * Defines table name and column mappings.
 */
public class BaseTableSettings implements ConfigurationHolder, TableSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("table-name")
    private String tableName;
    private final Map<String, String> columnNames = new HashMap<>();
    // #endregion

    // #region Constructors
    /**
     * Constructs a new {@code BaseTableSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section. Must not be null.
     */
    public BaseTableSettings(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        ConfigurationSectionHolder columnsSectionHolder = sectionHolder.section("columns");
        columnNames.putAll(columnsSectionHolder.keys().stream()
                .collect(Collectors.toMap(Function.identity(), columnsSectionHolder::getString)));
        LOGGER.atInfo().log("Initialized BaseTableSettings for table: %s with %d columns", tableName, columnNames.size());
    }

    /**
     * Constructs a new {@code BaseTableSettings} with a table name.
     *
     * @param tableName The table name. Must not be null.
     */
    public BaseTableSettings(String tableName) {
        this.tableName = Preconditions.checkNotNull(tableName, "tableName must not be null");
        LOGGER.atFine().log("Initialized BaseTableSettings for table: %s", tableName);
    }
    // #endregion

    // #region Getters
    /**
     * Gets the table name.
     *
     * @return The table name.
     */
    @Override
    public String getTableName() {
        return tableName;
    }

    /**
     * Gets the column name for a given key.
     *
     * @param key The column key. Must not be null.
     * @return An {@link Optional} containing the column name, or empty if not found.
     */
    @Override
    public Optional<String> getColumnName(String key) {
        Preconditions.checkNotNull(key, "key must not be null");
        String columnName = columnNames.get(key);
        LOGGER.atFine().log("Retrieved column name for key: %s, found: %b", key, columnName != null);
        return Optional.ofNullable(columnName);
    }
    // #endregion
}