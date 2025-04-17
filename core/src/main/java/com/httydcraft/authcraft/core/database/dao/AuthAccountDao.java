package com.httydcraft.authcraft.core.database.dao;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.account.AccountFactory;
import com.httydcraft.authcraft.api.config.database.schema.TableSettings;
import com.httydcraft.authcraft.core.database.DatabaseHelper;
import com.httydcraft.authcraft.core.database.adapter.AccountAdapter;
import com.httydcraft.authcraft.core.database.model.AccountLink;
import com.httydcraft.authcraft.core.database.model.AuthAccount;
import com.httydcraft.authcraft.core.database.persister.CryptoProviderPersister;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// #region Class Documentation
/**
 * DAO for managing {@link AuthAccount} entities in the database.
 * Provides methods for querying, creating, updating, and deleting accounts.
 */
public class AuthAccountDao extends BaseDaoImpl<AuthAccount, Long> {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final String PLAYER_ID_CONFIGURATION_KEY = "playerId";
    private static final String PLAYER_ID_TYPE_CONFIGURATION_KEY = "playerIdType";
    private static final String CRYPTO_PROVIDER_CONFIGURATION_KEY = "cryptoProvider";
    private static final String LAST_IP_CONFIGURATION_KEY = "lastIp";
    private static final String UNIQUE_ID_CONFIGURATION_KEY = "uniqueId";
    private static final String PLAYER_NAME_CONFIGURATION_KEY = "playerName";
    private static final String PASSWORD_HASH_CONFIGURATION_KEY = "passwordHash";
    private static final String LAST_QUIT_TIMESTAMP_CONFIGURATION_KEY = "lastQuitTimestamp";
    private static final String LAST_SESSION_TIMESTAMP_START_CONFIGURATION_KEY = "lastSessionStartTimestamp";
    private static final String LINKS_CONFIGURATION_KEY = "links";
    private static final SupplierExceptionCatcher DEFAULT_EXCEPTION_CATCHER = new SupplierExceptionCatcher();
    private final DatabaseHelper databaseHelper;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AuthAccountDao}.
     *
     * @param connectionSource The database connection source. Must not be null.
     * @param settings        The table settings. Must not be null.
     * @param databaseHelper  The database helper. Must not be null.
     * @throws SQLException If a database error occurs.
     */
    public AuthAccountDao(ConnectionSource connectionSource, TableSettings settings, DatabaseHelper databaseHelper) throws SQLException {
        super(connectionSource, createTableConfig(settings));
        Preconditions.checkNotNull(connectionSource, "connectionSource must not be null");
        Preconditions.checkNotNull(settings, "settings must not be null");
        Preconditions.checkNotNull(databaseHelper, "databaseHelper must not be null");
        TableUtils.createTableIfNotExists(connectionSource, getTableConfig());
        this.databaseHelper = databaseHelper;
        LOGGER.atInfo().log("Initialized AuthAccountDao for table: %s", settings.getTableName());
    }
    // #endregion

    // #region Table Configuration
    /**
     * Creates the table configuration for {@link AuthAccount}.
     *
     * @param settings The table settings. Must not be null.
     * @return The {@link DatabaseTableConfig} for the table.
     */
    private static DatabaseTableConfig<AuthAccount> createTableConfig(TableSettings settings) {
        Preconditions.checkNotNull(settings, "settings must not be null");
        List<DatabaseFieldConfig> fields = new ArrayList<>();

        DatabaseFieldConfig idFieldConfig = new DatabaseFieldConfig("id");
        idFieldConfig.setGeneratedId(true);
        fields.add(idFieldConfig);

        DatabaseFieldConfig playerIdFieldConfig = createFieldConfig(settings, PLAYER_ID_CONFIGURATION_KEY, AuthAccount.PLAYER_ID_FIELD_KEY);
        playerIdFieldConfig.setUnique(true);
        playerIdFieldConfig.setCanBeNull(false);
        fields.add(playerIdFieldConfig);

        DatabaseFieldConfig playerIdTypeFieldConfig = createFieldConfig(settings, PLAYER_ID_TYPE_CONFIGURATION_KEY, AuthAccount.PLAYER_ID_TYPE_FIELD_KEY);
        playerIdTypeFieldConfig.setCanBeNull(false);
        playerIdTypeFieldConfig.setDataType(DataType.ENUM_NAME);
        fields.add(playerIdTypeFieldConfig);

        DatabaseFieldConfig hashTypeFieldConfig = createFieldConfig(settings, CRYPTO_PROVIDER_CONFIGURATION_KEY, AuthAccount.HASH_TYPE_FIELD_KEY);
        hashTypeFieldConfig.setCanBeNull(false);
        hashTypeFieldConfig.setDataPersister(CryptoProviderPersister.getSingleton());
        fields.add(hashTypeFieldConfig);

        fields.add(createFieldConfig(settings, LAST_IP_CONFIGURATION_KEY, AuthAccount.LAST_IP_FIELD_KEY));

        DatabaseFieldConfig uniqueIdFieldConfig = createFieldConfig(settings, UNIQUE_ID_CONFIGURATION_KEY, AuthAccount.UNIQUE_ID_FIELD_KEY);
        uniqueIdFieldConfig.setCanBeNull(false);
        uniqueIdFieldConfig.setDataType(DataType.UUID);
        fields.add(uniqueIdFieldConfig);

        DatabaseFieldConfig playerNameFieldConfig = createFieldConfig(settings, PLAYER_NAME_CONFIGURATION_KEY, AuthAccount.PLAYER_NAME_FIELD_KEY);
        playerNameFieldConfig.setCanBeNull(false);
        fields.add(playerNameFieldConfig);

        fields.add(createFieldConfig(settings, PASSWORD_HASH_CONFIGURATION_KEY, AuthAccount.PASSWORD_HASH_FIELD_KEY));

        DatabaseFieldConfig lastQuitTimestampFieldConfig = createFieldConfig(settings, LAST_QUIT_TIMESTAMP_CONFIGURATION_KEY,
                AuthAccount.LAST_QUIT_TIMESTAMP_FIELD_KEY);
        lastQuitTimestampFieldConfig.setDataType(DataType.LONG);
        fields.add(lastQuitTimestampFieldConfig);

        DatabaseFieldConfig lastSessionStartTimestampFieldConfig = createFieldConfig(settings, LAST_SESSION_TIMESTAMP_START_CONFIGURATION_KEY,
                AuthAccount.LAST_SESSION_TIMESTAMP_START_FIELD_KEY);
        lastSessionStartTimestampFieldConfig.setDataType(DataType.LONG);
        fields.add(lastSessionStartTimestampFieldConfig);

        DatabaseFieldConfig linksFieldConfig = new DatabaseFieldConfig(LINKS_CONFIGURATION_KEY);
        linksFieldConfig.setForeignCollection(true);
        fields.add(linksFieldConfig);

        DatabaseTableConfig<AuthAccount> config = new DatabaseTableConfig<>(AuthAccount.class, settings.getTableName(), fields);
        LOGGER.atFine().log("Created table config for AuthAccount: %s", config.getTableName());
        return config;
    }

    /**
     * Creates a field configuration with the specified key and default value.
     *
     * @param settings        The table settings. Must not be null.
     * @param configurationKey The configuration key. Must not be null.
     * @param defaultValue    The default column name. Must not be null.
     * @return The {@link DatabaseFieldConfig}.
     */
    private static DatabaseFieldConfig createFieldConfig(TableSettings settings, String configurationKey, String defaultValue) {
        Preconditions.checkNotNull(settings, "settings must not be null");
        Preconditions.checkNotNull(configurationKey, "configurationKey must not be null");
        Preconditions.checkNotNull(defaultValue, "defaultValue must not be null");
        DatabaseFieldConfig config = new DatabaseFieldConfig(configurationKey);
        config.setColumnName(settings.getColumnName(configurationKey).orElse(defaultValue));
        LOGGER.atFine().log("Created field config for key: %s, column: %s", configurationKey, config.getColumnName());
        return config;
    }
    // #endregion

    // #region Query Methods
    /**
     * Queries the first account by player ID.
     *
     * @param playerId The player ID. Must not be null.
     * @return An {@link Optional} containing the {@link AuthAccount}, or empty if not found.
     */
    public Optional<AuthAccount> queryFirstAccountPlayerId(String playerId) {
        Preconditions.checkNotNull(playerId, "playerId must not be null");
        Optional<AuthAccount> result = Optional.ofNullable(
                DEFAULT_EXCEPTION_CATCHER.execute(() -> queryBuilder().where().eq(AuthAccount.PLAYER_ID_FIELD_KEY, playerId).queryForFirst()));
        LOGGER.atFine().log("Queried account by playerId: %s, found: %b", playerId, result.isPresent());
        return result;
    }

    /**
     * Queries the first account by player name.
     *
     * @param playerName The player name. Must not be null.
     * @return An {@link Optional} containing the {@link AuthAccount}, or empty if not found.
     */
    public Optional<AuthAccount> queryFirstAccountPlayerName(String playerName) {
        Preconditions.checkNotNull(playerName, "playerName must not be null");
        Optional<AuthAccount> result = Optional.ofNullable(
                DEFAULT_EXCEPTION_CATCHER.execute(() -> queryBuilder().where().eq(AuthAccount.PLAYER_NAME_FIELD_KEY, playerName).queryForFirst()));
        LOGGER.atFine().log("Queried account by playerName: %s, found: %b", playerName, result.isPresent());
        return result;
    }

    /**
     * Queries accounts by link identifier and link type.
     *
     * @param linkUserIdentificator The link user identifier. Must not be null.
     * @param linkType              The link type. Must not be null.
     * @return A collection of matching {@link AuthAccount}s.
     */
    public Collection<AuthAccount> queryAccounts(LinkUserIdentificator linkUserIdentificator, String linkType) {
        Preconditions.checkNotNull(linkUserIdentificator, "linkUserIdentificator must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Collection<AuthAccount> accounts = DEFAULT_EXCEPTION_CATCHER.execute(() -> queryBuilder().where()
                .in(DatabaseHelper.ID_FIELD_KEY, databaseHelper.getAccountLinkDao().queryBuilder(linkUserIdentificator, linkType))
                .query(), Collections.emptyList());
        LOGGER.atFine().log("Queried accounts by identificator: %s, type: %s, found: %d", linkUserIdentificator.asString(), linkType, accounts.size());
        return accounts;
    }

    /**
     * Queries accounts by link identifier.
     *
     * @param linkUserIdentificator The link user identifier. Must not be null.
     * @return A collection of matching {@link AuthAccount}s.
     */
    public Collection<AuthAccount> queryAccounts(LinkUserIdentificator linkUserIdentificator) {
        Preconditions.checkNotNull(linkUserIdentificator, "linkUserIdentificator must not be null");
        Collection<AuthAccount> accounts = DEFAULT_EXCEPTION_CATCHER.execute(
                () -> queryBuilder().where().in(DatabaseHelper.ID_FIELD_KEY, databaseHelper.getAccountLinkDao().queryBuilder(linkUserIdentificator)).query(),
                Collections.emptyList());
        LOGGER.atFine().log("Queried accounts by identificator: %s, found: %d", linkUserIdentificator.asString(), accounts.size());
        return accounts;
    }

    /**
     * Queries all accounts in the database.
     *
     * @return A collection of all {@link AuthAccount}s.
     */
    public Collection<AuthAccount> queryAllAccounts() {
        Collection<AuthAccount> accounts = DEFAULT_EXCEPTION_CATCHER.execute(this::queryForAll, Collections.emptyList());
        LOGGER.atFine().log("Queried all accounts, found: %d", accounts.size());
        return accounts;
    }

    /**
     * Queries all accounts with links.
     *
     * @return A collection of linked {@link AuthAccount}s.
     */
    public Collection<AuthAccount> queryAllLinkedAccounts() {
        Collection<AuthAccount> accounts = DEFAULT_EXCEPTION_CATCHER.execute(() -> queryBuilder().join(databaseHelper.getAccountLinkDao()
                .queryBuilder()
                .selectColumns(AccountLink.ACCOUNT_ID_FIELD_KEY)
                .where()
                .isNotNull(AccountLink.LINK_USER_ID_FIELD_KEY)
                .and()
                .notIn(AccountLink.LINK_USER_ID_FIELD_KEY, AccountFactory.DEFAULT_TELEGRAM_ID, AccountFactory.DEFAULT_VK_ID)
                .queryBuilder()).distinct().query(), Collections.emptyList());
        LOGGER.atFine().log("Queried all linked accounts, found: %d", accounts.size());
        return accounts;
    }

    /**
     * Queries all accounts with links of a specific type.
     *
     * @param linkType The link type. Must not be null.
     * @return A collection of linked {@link AuthAccount}s.
     */
    public Collection<AuthAccount> queryAllLinkedAccounts(LinkType linkType) {
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Collection<AuthAccount> accounts = DEFAULT_EXCEPTION_CATCHER.execute(() -> queryBuilder().join(databaseHelper.getAccountLinkDao()
                .queryBuilder()
                .selectColumns(AccountLink.ACCOUNT_ID_FIELD_KEY)
                .where()
                .isNotNull(AccountLink.LINK_USER_ID_FIELD_KEY)
                .and()
                .notIn(AccountLink.LINK_USER_ID_FIELD_KEY, AccountFactory.DEFAULT_TELEGRAM_ID, AccountFactory.DEFAULT_VK_ID)
                .and().eq(AccountLink.LINK_TYPE_FIELD_KEY, linkType.getName())
                .queryBuilder()).distinct().query(), Collections.emptyList());
        LOGGER.atFine().log("Queried linked accounts for type: %s, found: %d", linkType.getName(), accounts.size());
        return accounts;
    }
    // #endregion

    // #region Update Methods
    /**
     * Creates or updates an account.
     *
     * @param account The account to create or update. Must not be null.
     * @return The created or updated {@link AuthAccount}.
     */
    public AuthAccount createOrUpdateAccount(Account account) {
        Preconditions.checkNotNull(account, "account must not be null");
        SecurityAuditLogger.logSuccess("AccountDatabase: createOrUpdateAccount", null, "Account create/update requested for: " + account.getPlayerId());
        return DEFAULT_EXCEPTION_CATCHER.execute(() -> {
            try {
                Optional<AuthAccount> foundAccount = queryFirstAccountPlayerId(account.getPlayerId());
                if (foundAccount.isPresent()) {
                    AuthAccount authAccount = new AccountAdapter(account);
                    authAccount.setId(foundAccount.get().getId());
                    update(authAccount);
                    SecurityAuditLogger.logSuccess("AccountDatabase: createOrUpdateAccount", null, "Account updated: " + account.getPlayerId());
                    LOGGER.atFine().log("Updated existing account: %s", account.getPlayerId());
                } else {
                    AuthAccount authAccount = new AccountAdapter(account);
                    create(authAccount);
                    SecurityAuditLogger.logSuccess("AccountDatabase: createOrUpdateAccount", null, "Account created: " + account.getPlayerId());
                    LOGGER.atFine().log("Created new account: %s", account.getPlayerId());
                }
                return new AccountAdapter(account);
            } catch (Exception ex) {
                SecurityAuditLogger.logFailure("AccountDatabase: createOrUpdateAccount", null, "Failed to create/update account: " + account.getPlayerId() + ", error: " + ex.getMessage());
                throw ex;
            }
        });
    }

    /**
     * Deletes an account by player ID.
     *
     * @param id The player ID. Must not be null.
     */
    public void deleteAccountById(String id) {
        Preconditions.checkNotNull(id, "id must not be null");
        SecurityAuditLogger.logSuccess("AccountDatabase: deleteAccountById", null, "Account delete requested for: " + id);
        return DEFAULT_EXCEPTION_CATCHER.execute(() -> {
            try {
                DeleteBuilder<AuthAccount, Long> deleteBuilder = deleteBuilder();
                deleteBuilder.where().eq(AuthAccount.PLAYER_ID_FIELD_KEY, id);
                int deleted = deleteBuilder.delete();
                SecurityAuditLogger.logSuccess("AccountDatabase: deleteAccountById", null, "Account deleted by ID: " + id + ", rows affected: " + deleted);
                LOGGER.atFine().log("Deleted account by ID: %s, rows affected: %d", id, deleted);
            } catch (Exception ex) {
                SecurityAuditLogger.logFailure("AccountDatabase: deleteAccountById", null, "Failed to delete account by ID: " + id + ", error: " + ex.getMessage());
                throw ex;
            }
            return null;
        });
    }
    // #endregion
}