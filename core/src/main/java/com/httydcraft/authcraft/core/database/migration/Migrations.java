package com.httydcraft.authcraft.core.database.migration;

import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.database.migration.ColumnDropMigrator;
import com.httydcraft.authcraft.api.database.migration.ConditionalBatchTaskMigrator;
import com.httydcraft.authcraft.api.database.migration.ConditionalMigrator;
import com.httydcraft.authcraft.api.database.migration.Migrator;
import com.httydcraft.authcraft.core.database.model.AccountLink;
import com.httydcraft.authcraft.core.database.model.AuthAccount;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// #region Class Documentation
/**
 * Defines database migration tasks for authentication data.
 * Includes migrators for legacy tables and column adjustments.
 */
public class Migrations {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Legacy Auth Migrator
    /**
     * Migrates legacy auth table to the new mc_auth_accounts table.
     */
    public static final ConditionalMigrator<AuthAccount, Long> LEGACY_MC_AUTH_TO_NEW_MIGRATOR = new ConditionalMigrator<AuthAccount, Long>() {
        private final String migrationQuery = "INSERT INTO mc_auth_accounts SELECT null,id, id_type, hash_type, last_ip, uuid, name, password, last_quit, " +
                "last_session_start FROM auth;";

        /**
         * Checks if migration is needed.
         *
         * @param connectionSource The database connection source.
         * @param dao             The DAO for auth accounts.
         * @return {@code true} if the legacy table exists and the new table is empty.
         * @throws SQLException If a database error occurs.
         */
        @Override
        public boolean shouldMigrate(ConnectionSource connectionSource, Dao<? extends AuthAccount, Long> dao) throws SQLException {
            boolean shouldMigrate = connectionSource.getReadOnlyConnection(dao.getTableName()).isTableExists("auth") && dao.queryForFirst() == null;
            LOGGER.atFine().log("Checked LEGACY_MC_AUTH_TO_NEW migration needed: %b", shouldMigrate);
            return shouldMigrate;
        }

        /**
         * Executes the migration query.
         *
         * @param connectionSource The database connection source.
         * @param dao             The DAO for auth accounts.
         * @throws SQLException If a database error occurs.
         */
        @Override
        public void migrate(ConnectionSource connectionSource, Dao<? extends AuthAccount, Long> dao) throws SQLException {
            dao.executeRawNoArgs(migrationQuery);
            LOGGER.atInfo().log("Executed LEGACY_MC_AUTH_TO_NEW migration");
        }
    };
    // #endregion

    // #region Auth 1.5.0 Links Migrator
    /**
     * Migrates auth links from version 1.5.0.
     */
    public static final ConditionalMigrator<AccountLink, Long> AUTH_1_5_0_LINKS_MIGRATOR = new ConditionalBatchTaskMigrator<AccountLink, Long>() {
        private final List<String> migrationQueries = Arrays.asList(
                "INSERT INTO auth_links (link_type, link_user_id, link_enabled, account_id) SELECT 'VK', vkId, 1, mc_auth_accounts.id FROM " +
                        "auth JOIN mc_auth_accounts ON auth.id = mc_auth_accounts.player_id WHERE vkId IS NOT NULL AND vkId != -1;",
                "INSERT INTO auth_links (link_type, link_user_id, link_enabled, account_id) SELECT 'GOOGLE', google_key, 1, mc_auth_accounts.id FROM " +
                        "auth JOIN mc_auth_accounts ON auth.id = mc_auth_accounts.player_id WHERE google_key IS NOT NULL;");

        /**
         * Checks if migration is needed.
         *
         * @param connectionSource The database connection source.
         * @param dao             The DAO for account links.
         * @return {@code true} if the legacy table exists with specific columns and the new table is empty.
         * @throws SQLException If a database error occurs.
         */
        @Override
        public boolean shouldMigrate(ConnectionSource connectionSource, Dao<? extends AccountLink, Long> dao) throws SQLException {
            DatabaseConnection readOnlyConnection = connectionSource.getReadOnlyConnection(dao.getTableName());
            boolean shouldMigrate = readOnlyConnection.isTableExists("auth") &&
                    readOnlyConnection.getUnderlyingConnection().getMetaData().getColumns(null, null, "auth", "vk_confirm_enabled").next() &&
                    dao.queryForFirst() == null;
            LOGGER.atFine().log("Checked AUTH_1_5_0_LINKS migration needed: %b", shouldMigrate);
            return shouldMigrate;
        }

        /**
         * Gets the migration queries.
         *
         * @return An unmodifiable collection of migration queries.
         */
        @Override
        public Collection<String> getMigrationQueries() {
            return Collections.unmodifiableCollection(migrationQueries);
        }
    };
    // #endregion

    // #region Auth 1.6.0 Links Migrator
    /**
     * Migrates auth links from version 1.6.0.
     */
    public static final Migrator<AccountLink, Long> AUTH_1_6_0_LINKS_MIGRATOR = new ConditionalBatchTaskMigrator<AccountLink, Long>() {
        private final List<String> migrationQueries = Arrays.asList(
                "INSERT INTO auth_links (link_type, link_user_id, link_enabled, account_id) SELECT 'VK', vkId, vk_confirmation_enabled, mc_auth_accounts.id " +
                        "FROM auth JOIN mc_auth_accounts ON auth.id = mc_auth_accounts.player_id WHERE vkId IS NOT NULL AND vkId != -1;",
                "INSERT INTO auth_links (link_type, link_user_id, link_enabled, account_id) SELECT 'GOOGLE', google_key, 1, mc_auth_accounts.id FROM auth " +
                        "JOIN mc_auth_accounts ON auth.id = mc_auth_accounts.player_id WHERE google_key IS NOT NULL;",
                "INSERT INTO auth_links (link_type, link_user_id, link_enabled, account_id) SELECT 'TELEGRAM', telegram_id, telegram_confirmation_enabled, " +
                        "mc_auth_accounts.id FROM auth JOIN mc_auth_accounts ON auth.id = mc_auth_accounts.player_id WHERE telegram_id IS NOT NULL AND " +
                        "telegram_id != -1;");

        /**
         * Checks if migration is needed.
         *
         * @param connectionSource The database connection source.
         * @param dao             The DAO for account links.
         * @return {@code true} if the legacy table exists with specific columns and the new table is empty.
         * @throws SQLException If a database error occurs.
         */
        @Override
        public boolean shouldMigrate(ConnectionSource connectionSource, Dao<? extends AccountLink, Long> dao) throws SQLException {
            DatabaseConnection readOnlyConnection = connectionSource.getReadOnlyConnection(dao.getTableName());
            boolean shouldMigrate = readOnlyConnection.isTableExists("auth") &&
                    readOnlyConnection.getUnderlyingConnection().getMetaData().getColumns(null, null, "auth", "vk_confirmation_enabled").next() &&
                    dao.queryForFirst() == null;
            LOGGER.atFine().log("Checked AUTH_1_6_0_LINKS migration needed: %b", shouldMigrate);
            return shouldMigrate;
        }

        /**
         * Gets the migration queries.
         *
         * @return An unmodifiable collection of migration queries.
         */
        @Override
        public Collection<String> getMigrationQueries() {
            return Collections.unmodifiableCollection(migrationQueries);
        }
    };
    // #endregion

    // #region Hash Iteration Column Migrator
    /**
     * Drops the hash_iteration_count column.
     */
    public static final ColumnDropMigrator<AuthAccount, Long> HASH_ITERATION_COLUMN_MIGRATOR = ColumnDropMigrator.of("hash_iteration_count");
    // #endregion

    // #region Constructor
    /**
     * Private constructor to prevent instantiation.
     */
    private Migrations() {
        LOGGER.atFine().log("Initialized Migrations class");
    }
    // #endregion
}