
package com.httydcraft.authcraft.core.account.factory;

// region Imports
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.account.AccountFactory;
import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.httydcraft.authcraft.api.crypto.HashedPassword;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserInfo;
import com.httydcraft.authcraft.api.type.IdentifierType;
import com.httydcraft.authcraft.core.link.user.LinkUserTemplate;
// endregion

/**
 * Abstract factory template for creating accounts.
 * This class provides a template method for creating accounts, including
 * assigning cryptographic providers and linking users.
 */
public abstract class AccountFactoryTemplate implements AccountFactory {

    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    // region Account Creation

    /**
     * Creates an account using the provided parameters and returns it.
     *
     * @param id              the unique identifier of the user
     * @param identifierType  the type of identifier
     * @param uuid            the universally unique identifier of the user
     * @param name            the name of the user
     * @param cryptoProvider  the cryptographic provider for password hashing
     * @param passwordHash    the hash of the user's password
     * @param lastIp          the last IP address of the user
     * @return a new instance of an account object
     */
    @Override
    public Account createAccount(String id, IdentifierType identifierType, UUID uuid, String name,
                                 CryptoProvider cryptoProvider, String passwordHash, String lastIp) {
        // region Preconditions and Validation
        Preconditions.checkNotNull(id, "ID cannot be null");
        Preconditions.checkArgument(!id.isEmpty(), "ID cannot be empty");
        Preconditions.checkNotNull(identifierType, "IdentifierType cannot be null");
        Preconditions.checkNotNull(uuid, "UUID cannot be null");
        Preconditions.checkNotNull(name, "Name cannot be null");
        Preconditions.checkNotNull(cryptoProvider, "CryptoProvider cannot be null");
        Preconditions.checkNotNull(passwordHash, "PasswordHash cannot be null");
        Preconditions.checkNotNull(lastIp, "Last IP cannot be null");
        logger.atInfo().log("Creating account for ID: %s, Name: %s", id, name);
        // endregion

        // region Account Initialization
        Account account = newAccount(identifierType.fromRawString(id), identifierType, uuid, name);
        account.setCryptoProvider(cryptoProvider);
        account.setPasswordHash(HashedPassword.of(passwordHash, cryptoProvider));
        account.setLastIpAddress(lastIp);
        // endregion

        // region Link User Creation
        Iterable<LinkType> linkTypes = AuthPlugin.instance().getLinkTypeProvider().getLinkTypes();
        linkTypes.forEach(linkType -> account.addLinkUser(createUser(linkType, account)));
        // endregion

        return account;
    }
    // endregion

    // region User Link Creation

    /**
     * Creates a new user link using the specified link type and account.
     *
     * @param linkType the type of link to be created
     * @param account  the account to associate with the link
     * @return a new LinkUser instance
     */
    private LinkUser createUser(LinkType linkType, Account account) {
        logger.atFiner().log("Creating link user for LinkType: %s, Account: %s",
                linkType.getClass().getSimpleName(), account.getName());
        return LinkUserTemplate.of(linkType, account, LinkUserInfo.of(linkType.getDefaultIdentificator()));
    }
    // endregion

    // region Abstract Methods

    /**
     * Abstract method for creating a new account instance.
     *
     * @param id              the unique identifier for the account
     * @param identifierType  the type of identifier
     * @param uniqueId        the universally unique identifier for the account
     * @param name            the name associated with the account
     * @return a new Account instance
     */
    protected abstract Account newAccount(String id, IdentifierType identifierType, UUID uniqueId, String name);
    // endregion
}
