package com.httydcraft.authcraft.core.database;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.core.account.AuthAccountAdapter;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.impl.UserStringIdentificator;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

// #region Class Documentation
/**
 * Proxy for accessing account data in the database.
 * Provides asynchronous methods for querying and updating accounts.
 */
public class AuthAccountDatabaseProxy implements AccountDatabase {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final DatabaseHelper databaseHelper;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AuthAccountDatabaseProxy}.
     *
     * @param databaseHelper The database helper instance. Must not be null.
     */
    public AuthAccountDatabaseProxy(DatabaseHelper databaseHelper) {
        this.databaseHelper = Preconditions.checkNotNull(databaseHelper, "databaseHelper must not be null");
        LOGGER.atFine().log("Initialized AuthAccountDatabaseProxy");
    }
    // #endregion

    // #region Account Queries
    /**
     * Gets an account by its ID.
     *
     * @param id The account ID. Must not be null.
     * @return A {@link CompletableFuture} containing the {@link Account}, or null if not found.
     */
    @Override
    public CompletableFuture<Account> getAccount(String id) {
        Preconditions.checkNotNull(id, "id must not be null");
        SecurityAuditLogger.logSuccess("AccountDatabase: getAccount", null, "Account query requested by ID: " + id);
        return CompletableFuture.supplyAsync(() -> {
            try {
                Account account = databaseHelper.getAuthAccountDao()
                        .queryFirstAccountPlayerId(id)
                        .map(AuthAccountAdapter::new)
                        .orElse(null);
                SecurityAuditLogger.logSuccess("AccountDatabase: getAccount", null, "Account queried by ID: " + id + ", found: " + (account != null));
                LOGGER.atFine().log("Queried account by ID: %s, found: %b", id, account != null);
                return account;
            } catch (Exception ex) {
                SecurityAuditLogger.logFailure("AccountDatabase: getAccount", null, "Failed to query account by ID: " + id + ", error: " + ex.getMessage());
                throw ex;
            }
        });
    }

    /**
     * Gets an account by player name.
     *
     * @param playerName The player name. Must not be null.
     * @return A {@link CompletableFuture} containing the {@link Account}, or null if not found.
     */
    @Override
    public CompletableFuture<Account> getAccountFromName(String playerName) {
        Preconditions.checkNotNull(playerName, "playerName must not be null");
        SecurityAuditLogger.logSuccess("AccountDatabase: getAccountFromName", null, "Account query requested by name: " + playerName);
        return CompletableFuture.supplyAsync(() -> {
            try {
                Account account = databaseHelper.getAuthAccountDao()
                        .queryFirstAccountPlayerName(playerName)
                        .map(AuthAccountAdapter::new)
                        .orElse(null);
                SecurityAuditLogger.logSuccess("AccountDatabase: getAccountFromName", null, "Account queried by name: " + playerName + ", found: " + (account != null));
                LOGGER.atFine().log("Queried account by name: %s, found: %b", playerName, account != null);
                return account;
            } catch (Exception ex) {
                SecurityAuditLogger.logFailure("AccountDatabase: getAccountFromName", null, "Failed to query account by name: " + playerName + ", error: " + ex.getMessage());
                throw ex;
            }
        });
    }

    /**
     * Gets accounts linked to a VK ID.
     *
     * @param id The VK ID. Must not be null.
     * @return A {@link CompletableFuture} containing a collection of {@link Account}s.
     */
    @Override
    public CompletableFuture<Collection<Account>> getAccountsByVKID(Integer id) {
        Preconditions.checkNotNull(id, "id must not be null");
        return getAccountsFromLinkIdentificator(new UserStringIdentificator(id.toString()));
    }

    /**
     * Gets accounts by a link identifier.
     *
     * @param identificator The link identifier. Must not be null.
     * @return A {@link CompletableFuture} containing a collection of {@link Account}s.
     */
    @Override
    public CompletableFuture<Collection<Account>> getAccountsFromLinkIdentificator(LinkUserIdentificator identificator) {
        Preconditions.checkNotNull(identificator, "identificator must not be null");
        return CompletableFuture.supplyAsync(() -> {
            Collection<Account> accounts = databaseHelper.getAuthAccountDao()
                    .queryAccounts(identificator)
                    .stream()
                    .map(AuthAccountAdapter::new)
                    .collect(Collectors.toList());
            LOGGER.atFine().log("Queried accounts by identifier: %s, found: %d", identificator.asString(), accounts.size());
            return accounts;
        });
    }

    /**
     * Gets all accounts in the database.
     *
     * @return A {@link CompletableFuture} containing a collection of all {@link Account}s.
     */
    @Override
    public CompletableFuture<Collection<Account>> getAllAccounts() {
        return CompletableFuture.supplyAsync(() -> {
            Collection<Account> accounts = databaseHelper.getAuthAccountDao()
                    .queryAllAccounts()
                    .stream()
                    .map(AuthAccountAdapter::new)
                    .collect(Collectors.toList());
            LOGGER.atFine().log("Queried all accounts, found: %d", accounts.size());
            return accounts;
        });
    }

    /**
     * Gets all accounts with links.
     *
     * @return A {@link CompletableFuture} containing a collection of linked {@link Account}s.
     */
    @Override
    public CompletableFuture<Collection<Account>> getAllLinkedAccounts() {
        SecurityAuditLogger.logSuccess("AccountDatabase: getAllLinkedAccounts", null, "All linked accounts query requested");
        return CompletableFuture.supplyAsync(() -> {
            try {
                Collection<Account> accounts = databaseHelper.getAuthAccountDao()
                        .queryAllLinkedAccounts()
                        .stream()
                        .map(AuthAccountAdapter::new)
                        .collect(Collectors.toList());
                SecurityAuditLogger.logSuccess("AccountDatabase: getAllLinkedAccounts", null, "All linked accounts queried, count: " + accounts.size());
                LOGGER.atFine().log("Queried all linked accounts, count: %d", accounts.size());
                return accounts;
            } catch (Exception ex) {
                SecurityAuditLogger.logFailure("AccountDatabase: getAllLinkedAccounts", null, "Failed to query all linked accounts, error: " + ex.getMessage());
                throw ex;
            }
        });
    }

    /**
     * Gets all accounts linked to a specific link type.
     *
     * @param linkType The link type. Must not be null.
     * @return A {@link CompletableFuture} containing a collection of linked {@link Account}s.
     */
    @Override
    public CompletableFuture<Collection<Account>> getAllLinkedAccounts(LinkType linkType) {
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        SecurityAuditLogger.logSuccess("AccountDatabase: getAllLinkedAccountsByType", null, "Linked accounts query requested for type: " + linkType.getName());
        return CompletableFuture.supplyAsync(() -> {
            try {
                Collection<Account> accounts = databaseHelper.getAuthAccountDao()
                        .queryAllLinkedAccounts(linkType)
                        .stream()
                        .map(AuthAccountAdapter::new)
                        .collect(Collectors.toList());
                SecurityAuditLogger.logSuccess("AccountDatabase: getAllLinkedAccountsByType", null, "Linked accounts queried for type: " + linkType.getName() + ", count: " + accounts.size());
                LOGGER.atFine().log("Queried linked accounts for type: %s, found: %d", linkType.getName(), accounts.size());
                return accounts;
            } catch (Exception ex) {
                SecurityAuditLogger.logFailure("AccountDatabase: getAllLinkedAccountsByType", null, "Failed to query linked accounts for type: " + linkType.getName() + ", error: " + ex.getMessage());
                throw ex;
            }
        });
    }
    // #endregion

    // #region Account Updates
    /**
     * Saves or updates an account.
     *
     * @param account The account to save or update. Must not be null.
     * @return A {@link CompletableFuture} containing the saved or updated {@link Account}.
     */
    @Override
    public CompletableFuture<Account> saveOrUpdateAccount(Account account) {
        Preconditions.checkNotNull(account, "account must not be null");
        return CompletableFuture.supplyAsync(() -> {
            Account savedAccount = new AuthAccountAdapter(databaseHelper.getAuthAccountDao().createOrUpdateAccount(account));
            SecurityAuditLogger.logSuccess("AccountDatabase: saveOrUpdateAccount", null, "Account saved or updated: " + account.getPlayerId());
            LOGGER.atFine().log("Saved or updated account: %s", account.getPlayerId());
            return savedAccount;
        });
    }

    /**
     * Updates the links for an account.
     *
     * @param account The account to update links for. Must not be null.
     * @return A {@link CompletableFuture} representing the completion of the operation.
     */
    @Override
    public CompletableFuture<Void> updateAccountLinks(Account account) {
        Preconditions.checkNotNull(account, "account must not be null");
        SecurityAuditLogger.logSuccess("AccountDatabase: updateAccountLinks", null, "Update account links requested for: " + account.getPlayerId());
        return CompletableFuture.supplyAsync(() -> {
            try {
                databaseHelper.getAccountLinkDao().updateAccountLinks(account);
                SecurityAuditLogger.logSuccess("AccountDatabase: updateAccountLinks", null, "Updated account links for: " + account.getPlayerId());
                LOGGER.atFine().log("Updated links for account: %s", account.getPlayerId());
                return null;
            } catch (Exception ex) {
                SecurityAuditLogger.logFailure("AccountDatabase: updateAccountLinks", null, "Failed to update account links for: " + account.getPlayerId() + ", error: " + ex.getMessage());
                throw ex;
            }
        });
    }

    /**
     * Deletes an account by ID.
     *
     * @param id The account ID. Must not be null.
     * @return A {@link CompletableFuture} representing the completion of the operation.
     */
    @Override
    public CompletableFuture<Void> deleteAccount(String id) {
        Preconditions.checkNotNull(id, "id must not be null");
        return CompletableFuture.supplyAsync(() -> {
            databaseHelper.getAuthAccountDao().deleteAccountById(id);
            LOGGER.atFine().log("Deleted account by ID: %s", id);
            return null;
        });
    }
    // #endregion

    // #region Status
    /**
     * Checks if the database is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    @Override
    public boolean isEnabled() {
        boolean isEnabled = databaseHelper.isEnabled();
        LOGGER.atFine().log("Checked database enabled status: %b", isEnabled);
        return isEnabled;
    }
    // #endregion
}