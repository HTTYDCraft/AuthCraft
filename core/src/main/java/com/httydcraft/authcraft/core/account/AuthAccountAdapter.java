
package com.httydcraft.authcraft.core.account;

// region Imports
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.httydcraft.authcraft.api.crypto.HashedPassword;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.type.IdentifierType;
import com.httydcraft.authcraft.core.database.model.AccountLink;
import com.httydcraft.authcraft.core.database.model.AuthAccount;
import com.httydcraft.authcraft.core.link.user.AccountLinkAdapter;
import com.google.common.collect.ImmutableList;
// endregion

/**
 * Adapter class for {@link AuthAccount} to adapt it as {@link Account}.
 * It provides a concrete implementation of the account using the existing {@link AuthAccount}.
 */
public class AuthAccountAdapter extends AccountTemplate {

    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    private final List<LinkUser> linkUsers;
    private final AuthAccount authAccount;

    // region Constructor

    /**
     * Constructs an AuthAccountAdapter.
     *
     * @param authAccount The AuthAccount instance to be adapted
     * @param accountLinks The linked social {@link AccountLink} collection
     */
    public AuthAccountAdapter(AuthAccount authAccount, Collection<AccountLink> accountLinks) {
        Preconditions.checkNotNull(authAccount, "AuthAccount cannot be null");
        Preconditions.checkNotNull(accountLinks, "AccountLinks cannot be null");

        this.authAccount = authAccount;
        this.linkUsers = accountLinks.stream()
                .map(accountLink -> new AccountLinkAdapter(accountLink, this))
                .collect(Collectors.toList());

        logger.atInfo().log("AuthAccountAdapter created for account ID: %s", authAccount.getPlayerId());
    }

    /**
     * Constructs an AuthAccountAdapter querying links from the database.
     *
     * @param authAccount The AuthAccount instance to be adapted
     */
    public AuthAccountAdapter(AuthAccount authAccount) {
        this(authAccount, authAccount.getLinks());
    }
    // endregion

    // region Account Details

    @Override
    public long getDatabaseId() {
        return authAccount.getId();
    }

    @Override
    public String getPlayerId() {
        return authAccount.getPlayerId();
    }

    @Override
    public IdentifierType getIdentifierType() {
        return authAccount.getPlayerIdType();
    }

    @Override
    public CryptoProvider getCryptoProvider() {
        return authAccount.getHashType();
    }

    @Override
    public void setCryptoProvider(CryptoProvider cryptoProvider) {
        Preconditions.checkNotNull(cryptoProvider, "CryptoProvider cannot be null");
        authAccount.setHashType(cryptoProvider);
    }

    @Override
    public UUID getUniqueId() {
        return authAccount.getUniqueId();
    }

    @Override
    public String getName() {
        return authAccount.getPlayerName();
    }

    @Override
    public HashedPassword getPasswordHash() {
        return HashedPassword.of(authAccount.getPasswordHash(), null, authAccount.getHashType());
    }

    @Override
    public void setPasswordHash(HashedPassword hashedPassword) {
        Preconditions.checkNotNull(hashedPassword, "HashedPassword cannot be null");
        authAccount.setPasswordHash(hashedPassword.getHash());
    }

    @Override
    public List<LinkUser> getLinkUsers() {
        return ImmutableList.copyOf(linkUsers);
    }

    @Override
    public void addLinkUser(LinkUser linkUser) {
        Preconditions.checkNotNull(linkUser, "LinkUser cannot be null");
        if (linkUsers.stream().noneMatch(existingUser -> existingUser.getLinkType().equals(linkUser.getLinkType()))) {
            linkUsers.add(linkUser);
            logger.atInfo().log("LinkUser added for account ID: %s", authAccount.getPlayerId());
        } else {
            logger.atInfo().log("LinkUser already exists for account ID: %s", authAccount.getPlayerId());
        }
    }

    @Override
    public Optional<LinkUser> findFirstLinkUser(Predicate<LinkUser> filter) {
        Preconditions.checkNotNull(filter, "LinkUser filter cannot be null");
        return linkUsers.stream().filter(filter).findFirst();
    }
    // endregion

    // region Session Management

    @Override
    public long getLastQuitTimestamp() {
        return authAccount.getLastQuitTimestamp();
    }

    @Override
    public void setLastQuitTimestamp(long timestamp) {
        authAccount.setLastQuitTimestamp(timestamp);
    }

    @Override
    public String getLastIpAddress() {
        return authAccount.getLastIp();
    }

    @Override
    public void setLastIpAddress(String hostString) {
        Preconditions.checkNotNull(hostString, "IP address cannot be null");
        authAccount.setLastIp(hostString);
    }

    @Override
    public long getLastSessionStartTimestamp() {
        return authAccount.getLastSessionStartTimestamp();
    }

    @Override
    public void setLastSessionStartTimestamp(long currentTimeMillis) {
        authAccount.setLastSessionStartTimestamp(currentTimeMillis);
    }
    // endregion

    // region Comparison

    @Override
    public int compareTo(AccountTemplate accountTemplate) {
        Preconditions.checkNotNull(accountTemplate, "AccountTemplate cannot be null");
        return accountTemplate.getName().compareTo(getName());
    }
    // endregion
}
