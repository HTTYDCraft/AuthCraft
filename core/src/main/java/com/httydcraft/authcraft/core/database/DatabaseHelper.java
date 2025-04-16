package com.httydcraft.authcraft.core.database;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.database.DatabaseSettings;
import com.httydcraft.authcraft.api.config.database.schema.SchemaSettings;
import com.httydcraft.authcraft.core.config.storage.schema.BaseTableSettings;
import com.httydcraft.authcraft.core.database.dao.AccountLinkDao;
import com.httydcraft.authcraft.core.database.dao.AuthAccountDao;
import com.httydcraft.authcraft.core.database.migration.MigrationCoordinator;
import com.httydcraft.authcraft.core.database.migration.Migrations;
import com.httydcraft.authcraft.core.database.model.AccountLink;
import com.httydcraft.authcraft.core.database.model.AuthAccount;
import com.httydcraft.authcraft.core.database.persister.CryptoProviderPersister;
import com.httydcraft.authcraft.core.database.type.IdentityPostgresDatabaseType;
import com.httydcraft.authcraft.core.util.DownloadUtil;
import com.httydcraft.authcraft.core.util.DriverUtil;
import com.httydcraft.authcraft.core.util.HashUtils;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.jdbc.db.DatabaseTypeUtils;
import com.j256.ormlite.jdbc.db.PostgresDatabaseType;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.support.ConnectionSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

// #region Class Documentation
/**
 * Helper class for managing database connections and migrations.
 * Initializes database connection, DAOs, and applies migrations if enabled.
 */
public class DatabaseHelper {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String ID_FIELD_KEY = "id";
    private final MigrationCoordinator<AuthAccount, Long> authAccountMigrationCoordinator = new MigrationCoordinator<>();
    private final MigrationCoordinator<AccountLink, Long> accountLinkMigrationCoordinator = new MigrationCoordinator<>();
    private boolean enabled = false;
    private ConnectionSource connectionSource;
    private AuthAccountDao authAccountDao;
    private AccountLinkDao accountLinkDao;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code DatabaseHelper}.
     *
     * @param plugin      The AuthPlugin instance. Must not be null.
     * @param classLoader The class loader for loading drivers. Must not be null.
     */
    public DatabaseHelper(AuthPlugin plugin, ClassLoader classLoader) {
        Preconditions.checkNotNull(plugin, "plugin must not be null");
        Preconditions.checkNotNull(classLoader, "classLoader must not be null");

        DatabaseSettings databaseConfiguration = plugin.getConfig().getDatabaseConfiguration();
        SchemaSettings schemaSettings = databaseConfiguration.getSchemaSettings();

        // Configure database types
        modifyDatabaseType(databaseTypes -> {
            databaseTypes.removeIf(databaseType -> databaseType instanceof PostgresDatabaseType);
            databaseTypes.add(new IdentityPostgresDatabaseType());
            LOGGER.atFine().log("Modified database types to include IdentityPostgresDatabaseType");
        });

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Logger.setGlobalLogLevel(Level.WARNING);
                initializeDatabase(databaseConfiguration, schemaSettings, classLoader);
                enabled = true;
                LOGGER.atInfo().log("DatabaseHelper initialized successfully");
            } catch (SQLException | IOException e) {
                LOGGER.atSevere().withCause(e).log("Failed to initialize database");
            }
        });
    }
    // #endregion

    // #region Database Initialization
    /**
     * Initializes the database connection, DAOs, and migrations.
     *
     * @param databaseConfiguration The database settings. Must not be null.
     * @param schemaSettings        The schema settings. Must not be null.
     * @param classLoader           The class loader for drivers. Must not be null.
     * @throws SQLException If a database error occurs.
     * @throws IOException  If a file operation fails.
     */
    private void initializeDatabase(DatabaseSettings databaseConfiguration, SchemaSettings schemaSettings, ClassLoader classLoader)
            throws SQLException, IOException {
        Preconditions.checkNotNull(databaseConfiguration, "databaseConfiguration must not be null");
        Preconditions.checkNotNull(schemaSettings, "schemaSettings must not be null");
        Preconditions.checkNotNull(classLoader, "classLoader must not be null");

        // Download and load driver if necessary
        File cacheDriverFile = databaseConfiguration.getCacheDriverPath();
        URL downloadUrl = new URL(databaseConfiguration.getDriverDownloadUrl());
        String cacheDriverCheckSum = HashUtils.getFileCheckSum(cacheDriverFile, HashUtils.getMD5());
        if (!cacheDriverFile.exists() || (cacheDriverCheckSum != null && !DownloadUtil.checkSum(HashUtils.mapToMd5URL(downloadUrl), cacheDriverCheckSum))) {
            DownloadUtil.downloadFile(downloadUrl, cacheDriverFile);
            LOGGER.atInfo().log("Downloaded database driver to %s", cacheDriverFile.getAbsolutePath());
        }
        DriverUtil.loadDriver(cacheDriverFile, classLoader);
        LOGGER.atFine().log("Loaded database driver");

        // Register data persisters
        DataPersisterManager.registerDataPersisters(new CryptoProviderPersister());
        LOGGER.atFine().log("Registered CryptoProviderPersister");

        // Initialize connection source
        this.connectionSource = new JdbcPooledConnectionSource(
                databaseConfiguration.getConnectionUrl(),
                databaseConfiguration.getUsername(),
                databaseConfiguration.getPassword());
        LOGGER.atFine().log("Initialized JdbcPooledConnectionSource");

        // Initialize DAOs
        this.accountLinkDao = new AccountLinkDao(
                connectionSource,
                schemaSettings.getTableSettings("link").orElse(new BaseTableSettings("auth_links")),
                this);
        this.authAccountDao = new AuthAccountDao(
                connectionSource,
                schemaSettings.getTableSettings("auth").orElse(new BaseTableSettings("mc_auth_accounts")),
                this);
        LOGGER.atFine().log("Initialized DAOs");

        // Configure migrations
        authAccountMigrationCoordinator.add(Migrations.HASH_ITERATION_COLUMN_MIGRATOR);
        authAccountMigrationCoordinator.add(Migrations.LEGACY_MC_AUTH_TO_NEW_MIGRATOR);
        accountLinkMigrationCoordinator.add(Migrations.AUTH_1_5_0_LINKS_MIGRATOR);
        accountLinkMigrationCoordinator.add(Migrations.AUTH_1_6_0_LINKS_MIGRATOR);
        LOGGER.atFine().log("Configured migration coordinators");

        // Apply migrations if enabled
        if (databaseConfiguration.isMigrationEnabled()) {
            authAccountMigrationCoordinator.migrate(connectionSource, authAccountDao);
            accountLinkMigrationCoordinator.migrate(connectionSource, accountLinkDao);
            LOGGER.atInfo().log("Applied database migrations");
        }
    }
    // #endregion

    // #region Database Type Modification
    /**
     * Modifies the list of registered database types using reflection.
     *
     * @param consumer The consumer to modify the database types. Must not be null.
     */
    private void modifyDatabaseType(Consumer<List<DatabaseType>> consumer) {
        Preconditions.checkNotNull(consumer, "consumer must not be null");
        try {
            Field field = DatabaseTypeUtils.class.getDeclaredField("databaseTypes");
            field.setAccessible(true);
            List<DatabaseType> databaseTypes = (List<DatabaseType>) field.get(null);
            consumer.accept(databaseTypes);
            field.setAccessible(false);
            LOGGER.atFine().log("Modified database types");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.atWarning().withCause(e).log("Failed to modify database types");
        }
    }
    // #endregion

    // #region Getters
    /**
     * Checks if the database is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the database connection source.
     *
     * @return The {@link ConnectionSource}, or null if not initialized.
     */
    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }

    /**
     * Gets the migration coordinator for auth accounts.
     *
     * @return The {@link MigrationCoordinator} for {@link AuthAccount}.
     */
    public MigrationCoordinator<AuthAccount, Long> getAuthAccountMigrationCoordinator() {
        return authAccountMigrationCoordinator;
    }

    /**
     * Gets the migration coordinator for account links.
     *
     * @return The {@link MigrationCoordinator} for {@link AccountLink}.
     */
    public MigrationCoordinator<AccountLink, Long> getAccountLinkMigrationCoordinator() {
        return accountLinkMigrationCoordinator;
    }

    /**
     * Gets the DAO for auth accounts.
     *
     * @return The {@link AuthAccountDao}, or null if not initialized.
     */
    public AuthAccountDao getAuthAccountDao() {
        return authAccountDao;
    }

    /**
     * Gets the DAO for account links.
     *
     * @return The {@link AccountLinkDao}, or null if not initialized.
     */
    public AccountLinkDao getAccountLinkDao() {
        return accountLinkDao;
    }
    // #endregion
}