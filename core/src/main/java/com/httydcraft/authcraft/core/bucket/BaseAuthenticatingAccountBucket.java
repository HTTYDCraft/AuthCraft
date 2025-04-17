/* BaseAuthenticatingAccountBucket.java */
/**
 * A bucket specifically designed for managing AuthenticatingAccountStates.
 * Works with authentication plugins and accounts.
 */
package com.httydcraft.authcraft.core.bucket;

import com.google.common.base.Preconditions;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.bucket.AuthenticatingAccountBucket;
import com.httydcraft.authcraft.api.bucket.AuthenticatingAccountBucket.AuthenticatingAccountState;
import com.httydcraft.authcraft.api.event.AccountStateClearEvent;
import com.httydcraft.authcraft.api.model.PlayerIdSupplier;
import com.google.common.flogger.GoogleLogger;

public class BaseAuthenticatingAccountBucket extends BaseListBucket<AuthenticatingAccountState> implements AuthenticatingAccountBucket {

    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
    private final AuthPlugin plugin;

    /**
     * Constructs a bucket for managing authenticating account states.
     *
     * @param plugin The authentication plugin
     */
    public BaseAuthenticatingAccountBucket(AuthPlugin plugin) {
        this.plugin = plugin;
        logger.atInfo().log("BaseAuthenticatingAccountBucket instantiated");
    }

    @Override
    public void addAuthenticatingAccount(Account account) {
        Preconditions.checkNotNull(account, "Account cannot be null");
        if (hasByValue(AuthenticatingAccountState::getPlayerId, account.getPlayerId()))
            return;
        modifiable().add(new BaseAuthenticatingAccountState(account, System.currentTimeMillis()));
        logger.atInfo().log("Authenticating account added: %s", account);
    }

    @Override
    public void removeAuthenticatingAccount(PlayerIdSupplier playerIdSupplier) {
        Preconditions.checkNotNull(playerIdSupplier, "PlayerIdSupplier cannot be null");
        plugin.getEventBus().post(AccountStateClearEvent.class, getAuthenticatingAccount(playerIdSupplier));
        modifiable().removeIf(accountState -> accountState.getPlayerId().equals(playerIdSupplier.getPlayerId()));
        logger.atInfo().log("Authenticating account removed: %s", playerIdSupplier.getPlayerId());
    }

    public static class BaseAuthenticatingAccountState implements AuthenticatingAccountState {

        private final Account account;
        private final long enterTimestamp;

        /**
         * Constructs an AuthenticatingAccountState.
         *
         * @param account The associated account
         * @param enterTimestamp The timestamp when account entered authentication
         */
        public BaseAuthenticatingAccountState(Account account, long enterTimestamp) {
            this.account = account;
            this.enterTimestamp = enterTimestamp;
        }

        @Override
        public Account getAccount() {
            return account;
        }

        @Override
        public long getEnterTimestamp() {
            return enterTimestamp;
        }

    }

}