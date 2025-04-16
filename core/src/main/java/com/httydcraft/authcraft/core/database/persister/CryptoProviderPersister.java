package com.httydcraft.authcraft.core.database.persister;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;

// #region Class Documentation
/**
 * Persister for {@link CryptoProvider} objects in the database.
 * Handles conversion between Java objects and SQL data.
 */
public class CryptoProviderPersister extends BaseDataType {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final int DEFAULT_WIDTH = 100;
    private static final CryptoProviderPersister SINGLETON = new CryptoProviderPersister();
    // #endregion

    // #region Singleton
    /**
     * Gets the singleton instance of the persister.
     *
     * @return The singleton {@link CryptoProviderPersister}.
     */
    public static CryptoProviderPersister getSingleton() {
        return SINGLETON;
    }
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code CryptoProviderPersister}.
     */
    public CryptoProviderPersister() {
        super(SqlType.STRING, new Class<?>[]{CryptoProvider.class});
        LOGGER.atFine().log("Initialized CryptoProviderPersister");
    }
    // #endregion

    // #region Data Conversion
    /**
     * Converts a database result to a SQL argument.
     *
     * @param fieldType The field type.
     * @param results   The database results. Must not be null.
     * @param columnPos The column position.
     * @return The string value from the database.
     * @throws SQLException If a database error occurs.
     */
    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        Preconditions.checkNotNull(results, "results must not be null");
        String value = results.getString(columnPos);
        LOGGER.atFine().log("Converted database result to SQL arg at column %d: %s", columnPos, value);
        return value;
    }

    /**
     * Converts a Java object to a SQL argument.
     *
     * @param fieldType The field type.
     * @param javaObject The Java object (CryptoProvider). Must not be null.
     * @return The identifier string.
     */
    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        CryptoProvider cryptoProvider = (CryptoProvider) Preconditions.checkNotNull(javaObject, "javaObject must not be null");
        String identifier = cryptoProvider.getIdentifier();
        LOGGER.atFine().log("Converted Java object to SQL arg: %s", identifier);
        return identifier;
    }

    /**
     * Converts a SQL argument to a Java object.
     *
     * @param fieldType The field type. Must not be null.
     * @param sqlArg    The SQL argument.
     * @param columnPos The column position.
     * @return The {@link CryptoProvider} instance.
     * @throws SQLException If the crypto provider cannot be found.
     */
    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        Preconditions.checkNotNull(fieldType, "fieldType must not be null");
        if (sqlArg == null) {
            LOGGER.atFine().log("SQL arg is null at column %d", columnPos);
            return null;
        }
        String identifier = (String) sqlArg;
        CryptoProvider provider = AuthPlugin.instance()
                .getCryptoProviderBucket()
                .findFirstByValue(CryptoProvider::getIdentifier, identifier)
                .orElseThrow(() -> {
                    SQLException e = new SQLException("Cannot get crypto provider value of '" + identifier + "' for field " + fieldType);
                    LOGGER.atSevere().withCause(e).log("Failed to find crypto provider for identifier: %s", identifier);
                    return e;
                });
        LOGGER.atFine().log("Converted SQL arg to Java object: %s", identifier);
        return provider;
    }

    /**
     * Parses a default string value.
     *
     * @param fieldType   The field type.
     * @param defaultStr  The default string. Must not be null.
     * @return The default string.
     */
    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) {
        Preconditions.checkNotNull(defaultStr, "defaultStr must not be null");
        LOGGER.atFine().log("Parsed default string: %s", defaultStr);
        return defaultStr;
    }
    // #endregion

    // #region Configuration
    /**
     * Gets the default width for the field.
     *
     * @return The default width (100).
     */
    @Override
    public int getDefaultWidth() {
        return DEFAULT_WIDTH;
    }
    // #endregion
}