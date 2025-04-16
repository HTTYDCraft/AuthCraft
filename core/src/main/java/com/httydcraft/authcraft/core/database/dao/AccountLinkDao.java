package com.httydcraft.authcraft.core.database.dao;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.database.schema.TableSettings;
import com.httydcraft.authcraft.core.database.DatabaseHelper;
import com.httydcraft.authcraft.core.database.adapter.AccountAdapter;
import com.httydcraft.authcraft.core.database.adapter.LinkUserAdapter;
import com.httydcraft.authcraft.core.database.model.AccountLink;
import com.httydcraft.authcraft.core.database.model.AuthAccount;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.LinkUserInfo;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// #region Class Documentation
/**
 * DAO for managing {@link AccountLink} entities in the database.
 * Provides methods for updating and querying account links.
 */
public class AccountLinkDao extends BaseDaoImpl<AccountLink, Long> {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final String LINK_TYPE_CONFIGURATION_KEY = "linkType";
    private static final String LINK_USER_ID_CONFIGURATION_KEY = "linkUserId";
    private static final String LINK_ENABLED_CONFIGURATION_KEY = "linkEnabled";
    private static final String ACCOUNT_ID_CONFIGURATION_KEY = "account";
    private static final SupplierExceptionCatcher DEFAULT_EXCEPTION_CATCHER = new SupplierExceptionCatcher();
    private final DatabaseHelper databaseHelper;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AccountLinkDao}.
     *
     * @param connectionSource The database connection source. Must not be null.
     * @param settings        The table settings. Must not be null.
     * @param databaseHelper  The database helper. Must not be null.
     * @throws SQLException If a database error occurs.
     */
    public AccountLinkDao(ConnectionSource connectionSource, TableSettings settings, DatabaseHelper databaseHelper) throws SQLException {
        super(connectionSource, createTableConfig(settings));
        Preconditions.checkNotNull(connectionSource, "connectionSource must not be null");
        Preconditions.checkNotNull(settings, "settings must not be null");
        Preconditions.checkNotNull(databaseHelper, "databaseHelper must not be null");
        TableUtils.createTableIfNotExists(connectionSource, getTableConfig());
        this.databaseHelper = databaseHelper;
        LOGGER.atInfo().log("Initialized AccountLinkDao for table: %s", settings.getTableName());
    }
    // #endregion

    // #region Table Configuration
    /**
     * Creates the table configuration for {@link AccountLink}.
     *
     * @param settings The table settings. Must not be null.
     * @return The {@link DatabaseTableConfig} for the table.
     */
    private static DatabaseTableConfig<AccountLink> createTableConfig(TableSettings settings) {
        Preconditions.checkNotNull(settings, "settings must not be null");
        List<DatabaseFieldConfig> fields = new ArrayList<>();

        DatabaseFieldConfig idFieldConfig = new DatabaseFieldConfig("id");
        idFieldConfig.setGeneratedId(true);
        fields.add(idFieldConfig);

        DatabaseFieldConfig linkTypeFieldConfig = createFieldConfig(settings, LINK_TYPE_CONFIGURATION_KEY, AccountLink.LINK_TYPE_FIELD_KEY);
        linkTypeFieldConfig.setCanBeNull(false);
        linkTypeFieldConfig.setUniqueCombo(true);
        fields.add(linkTypeFieldConfig);

        fields.add(createFieldConfig(settings, LINK_USER_ID_CONFIGURATION_KEY, AccountLink.LINK_USER_ID_FIELD_KEY));

        DatabaseFieldConfig linkEnabledFieldConfig = createFieldConfig(settings, LINK_ENABLED_CONFIGURATION_KEY, AccountLink.LINK_ENABLED_FIELD_KEY);
        linkEnabledFieldConfig.setDataType(DataType.BOOLEAN_INTEGER);
        linkEnabledFieldConfig.setCanBeNull(false);
        linkEnabledFieldConfig.setDefaultValue("true");
        fields.add(linkEnabledFieldConfig);

        DatabaseFieldConfig accountIdFieldConfig = createFieldConfig(settings, ACCOUNT_ID_CONFIGURATION_KEY, AccountLink.ACCOUNT_ID_FIELD_KEY);
        accountIdFieldConfig.setForeign(true);
        accountIdFieldConfig.setUniqueCombo(true);
        fields.add(accountIdFieldConfig);

        DatabaseTableConfig<AccountLink> config = new DatabaseTableConfig<>(AccountLink.class, settings.getTableName(), fields);
        LOGGER.atFine().log("Created table config for AccountLink: %s", config.getTableName());
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

    // #region Update Methods
    /**
     * Updates the links for an account.
     *
     * @param account The account to update links for. Must not be null.
     */
    public void updateAccountLinks(Account account) {
        Preconditions.checkNotNull(account, "account must not be null");
        DEFAULT_EXCEPTION_CATCHER.execute(() -> {
            AuthAccount accountAdapter = new AccountAdapter(account);
            callBatchTasks(() -> {
                for (LinkUser linkUser : account.getLinkUsers()) {
                    String linkTypeName = linkUser.getLinkType().getName();
                    String linkUserId = Optional.ofNullable(linkUser.getLinkUserInfo())
                            .map(LinkUserInfo::getIdentificator)
                            .map(LinkUserIdentificator::asString)
                            .orElse(linkUser.getLinkType().getDefaultIdentificator().asString());
                    boolean linkEnabled = linkUser.getLinkUserInfo() != null && linkUser.getLinkUserInfo().isConfirmationEnabled();

                    AccountLink accountLink = new LinkUserAdapter(linkUser, accountAdapter);

                    AccountLink existingLink = queryBuilder()
                            .where().eq(AccountLink.ACCOUNT_ID_FIELD_KEY, accountAdapter.getId())
                            .and().eq(AccountLink.LINK_TYPE_FIELD_KEY, accountLink.getLinkType())
                            .queryForFirst();

                    if (existingLink != null) {
                        accountLink.setId(existingLink.getId());
                        if (accountLink.equals(existingLink)) {
                            LOGGER.atFine().log("Skipped update for unchanged link: %s, accountId: %d", linkTypeName, accountAdapter.getId());
                            continue;
                        }
                        update(accountLink);
                        LOGGER.atFine().log("Updated link: %s, accountId: %d", linkTypeName, accountAdapter.getId());
                        continue;
                    }

                    AccountLink newAccountLink = new AccountLink(linkTypeName, linkUserId, linkEnabled, accountAdapter);
                    create(newAccountLink);
                    LOGGER.atFine().log("Created new link: %s, accountId: %d", linkTypeName, accountAdapter.getId());
                }
                return null;
            });
            LOGGER.atInfo().log("Completed link updates for account: %s", account.getPlayerId());
            return null;
        });
    }
    // #endregion

    // #region Query Methods
    /**
     * Creates a query builder for accounts by link identifier and link type.
     *
     * @param linkUserIdentificator The link user identifier. Must not be null.
     * @param linkType              The link type. Must not be null.
     * @return The {@link QueryBuilder} for the query.
     */
    public QueryBuilder<AccountLink, Long> queryBuilder(LinkUserIdentificator linkUserIdentificator, String linkType) {
        Preconditions.checkNotNull(linkUserIdentificator, "linkUserIdentificator must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        QueryBuilder<AccountLink, Long> queryBuilder = DEFAULT_EXCEPTION_CATCHER.execute(() -> queryBuilder()
                .selectColumns(AccountLink.ACCOUNT_ID_FIELD_KEY)
                .where()
                .eq(AccountLink.LINK_USER_ID_FIELD_KEY, linkUserIdentificator.asString())
                .and()
                .eq(AccountLink.LINK_TYPE_FIELD_KEY, linkType)
                .queryBuilder());
        LOGGER.atFine().log("Created query builder for identificator: %s, type: %s", linkUserIdentificator.asString(), linkType);
        return queryBuilder;
    }

    /**
     * Creates a query builder for accounts by link identifier.
     *
     * @param linkUserIdentificator The link user identifier. Must not be null.
     * @return The {@link QueryBuilder} for the query.
     */
    public QueryBuilder<AccountLink, Long> queryBuilder(LinkUserIdentificator linkUserIdentificator) {
        Preconditions.checkNotNull(linkUserIdentificator, "linkUserIdentificator must not be null");
        QueryBuilder<AccountLink, Long> queryBuilder = DEFAULT_EXCEPTION_CATCHER.execute(() -> queryBuilder()
                .selectColumns(AccountLink.ACCOUNT_ID_FIELD_KEY)
                .where()
                .eq(AccountLink.LINK_USER_ID_FIELD_KEY, linkUserIdentificator.asString())
                .queryBuilder());
        LOGGER.atFine().log("Created query builder for identificator: %s", linkUserIdentificator.asString());
        return queryBuilder;
    }
    // #endregion
}