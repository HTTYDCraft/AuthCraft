package com.httydcraft.authcraft.api.database;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;

public interface AccountDatabase {
    CompletableFuture<Account> getAccount(String id);

    CompletableFuture<Account> getAccountFromName(String playerName);

    @Deprecated
    CompletableFuture<Collection<Account>> getAccountsByVKID(Integer id);

    CompletableFuture<Collection<Account>> getAccountsFromLinkIdentificator(LinkUserIdentificator identificator);

    CompletableFuture<Collection<Account>> getAllAccounts();

    CompletableFuture<Collection<Account>> getAllLinkedAccounts();

    CompletableFuture<Collection<Account>> getAllLinkedAccounts(LinkType linkType);

    CompletableFuture<Account> saveOrUpdateAccount(Account account);

    CompletableFuture<Void> updateAccountLinks(Account account);

    CompletableFuture<Void> deleteAccount(String id);

    boolean isEnabled();
}