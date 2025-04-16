package com.httydcraft.authcraft.core.database;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.core.account.AuthAccountAdapter;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.impl.UserStringIdentificator;

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
        return CompletableFuture.supplyAsync(() -> {
            Account account = databaseHelper.getAuthAccountDao()
                    .queryFirstAccountPlayerId(id)
                    .map(AuthAccountAdapter::new)
                    .orElse(null);
            LOGGER.atFine().log("Queried account by ID: %s, found: %b", id, account != null);
            return account;
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
        return CompletableFuture.supplyAsync(() -> {
            Account account = databaseHelper.getAuthAccountDao()
                    .queryFirstAccountPlayerName(playerName)
                    .map(AuthAccountAdapter::new)
                    .orElse(null);
            LOGGER.atFine().log("Queried account by name: %s, found: %b", playerName, account != null);
            return account;
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
        return CompletableFuture.supplyAsync(() -> {
            Collection<Account> accounts = databaseHelper.getAuthAccountDao()
                    .queryAllLinkedAccounts()
                    .stream()
                    .map(AuthAccountAdapter::new)
                    .collect(Collectors.toList());
            LOGGER.atFine().log("Queried all linked accounts, found: %d", accounts.size());
            return accounts;
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
        return CompletableFuture.supplyAsync(() -> {
            Collection<Account> accounts = databaseHelper.getAuthAccountDao()
                    .queryAllLinkedAccounts(linkType)
                    .stream()
                    .map(AuthAccountAdapter::new)
                    .collect(Collectors.toList());
            LOGGER.atFine().log("Queried linked accounts for type: %s, found: %d", linkType.getName(), accounts.size());
            return accounts;
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
        return CompletableFuture.supplyAsync(() -> {
            databaseHelper.getAccountLinkDao().updateAccountLinks(account);
            LOGGER.atFine().log("Updated links for account: %s", account.getPlayerId());
            return null;
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