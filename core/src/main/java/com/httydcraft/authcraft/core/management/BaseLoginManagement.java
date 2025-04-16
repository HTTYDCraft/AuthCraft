package com.httydcraft.authcraft.core.management;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.account.AccountFactory;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.message.MessageContext;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.event.AccountJoinEvent;
import com.httydcraft.authcraft.api.event.AccountSessionEnterEvent;
import com.httydcraft.authcraft.api.factory.AuthenticationStepFactory;
import com.httydcraft.authcraft.api.management.LoginManagement;
import com.httydcraft.authcraft.api.server.CoreServer;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.core.config.message.server.ServerMessageContext;
import com.httydcraft.authcraft.core.step.impl.NullAuthenticationStep.NullAuthenticationStepFactory;
import io.github.revxrsal.eventbus.PostResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// #region Class Documentation
/**
 * Base implementation of {@link LoginManagement}.
 * Handles player login and disconnection logic, including authentication steps and session management.
 */
public class BaseLoginManagement implements LoginManagement {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final AuthPlugin plugin;
    private final CoreServer core;
    private final PluginConfig config;
    private final AccountFactory accountFactory;
    private final AccountDatabase accountDatabase;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseLoginManagement}.
     *
     * @param plugin The AuthPlugin instance. Must not be null.
     */
    public BaseLoginManagement(AuthPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin must not be null");
        this.core = plugin.getCore();
        this.config = plugin.getConfig();
        this.accountFactory = plugin.getAccountFactory();
        this.accountDatabase = plugin.getAccountDatabase();
        LOGGER.atFine().log("Initialized BaseLoginManagement");
    }
    // #endregion

    // #region Login Handling
    /**
     * Processes a player login attempt.
     *
     * @param player The player attempting to log in. Must not be null.
     * @return A {@link CompletableFuture} containing the {@link Account}, or {@code null} if login is rejected.
     */
    @Override
    public CompletableFuture<Account> onLogin(ServerPlayer player) {
        Preconditions.checkNotNull(player, "player must not be null");
        String nickname = player.getNickname();
        LOGGER.atFine().log("Processing login for player: %s", nickname);

        if (!config.getNamePattern().matcher(nickname).matches()) {
            player.disconnect(config.getServerMessages().getMessage("illegal-name-chars"));
            LOGGER.atInfo().log("Rejected login for player %s due to illegal name characters", nickname);
            return CompletableFuture.completedFuture(null);
        }

        if (config.getMaxLoginPerIP() != 0 &&
                core.getPlayers().stream()
                        .filter(onlinePlayer -> onlinePlayer.getPlayerIp().equals(player.getPlayerIp()))
                        .count() > config.getMaxLoginPerIP()) {
            player.disconnect(config.getServerMessages().getMessage("limit-ip-reached"));
            LOGGER.atInfo().log("Rejected login for player %s due to IP limit", nickname);
            return CompletableFuture.completedFuture(null);
        }

        String id = config.getActiveIdentifierType().getId(player);
        return accountDatabase.getAccount(id).thenCompose(account -> {
            if (config.isNameCaseCheckEnabled() && account != null && !account.getName().equals(nickname)) {
                player.disconnect(config.getServerMessages()
                        .getMessage("check-name-case-failed",
                                MessageContext.of("%correct%", account.getName(), "%failed%", nickname)));
                LOGGER.atInfo().log("Rejected login for player %s due to case mismatch", nickname);
                return CompletableFuture.completedFuture(account);
            }

            if (account == null) {
                Account newAccount = accountFactory.createAccount(id, config.getActiveIdentifierType(),
                        player.getUniqueId(), nickname, config.getActiveHashType(), null, player.getPlayerIp());
                AuthenticationStepContext context = plugin.getAuthenticationContextFactoryBucket().createContext(newAccount);
                plugin.getAuthenticatingAccountBucket().addAuthenticatingAccount(newAccount);
                newAccount.nextAuthenticationStep(context);
                LOGGER.atInfo().log("Created new account for player: %s", nickname);
                return CompletableFuture.completedFuture(null);
            }

            AuthenticationStepFactory authenticationStepCreator = plugin.getAuthenticationStepFactoryBucket()
                    .findFirst(stepCreator -> stepCreator.getAuthenticationStepName()
                            .equals(config.getAuthenticationSteps().stream().findFirst().orElse("NULL")))
                    .orElse(new NullAuthenticationStepFactory());
            AuthenticationStepContext context = plugin.getAuthenticationContextFactoryBucket()
                    .createContext(authenticationStepCreator.getAuthenticationStepName(), account);

            return plugin.getEventBus().publish(AccountJoinEvent.class, account, false).thenApplyAsync(event -> {
                if (event.getEvent().isCancelled()) {
                    LOGGER.atFine().log("Login cancelled for account: %s", account.getPlayerId());
                    return account;
                }

                if (account.isSessionActive(config.getSessionDurability()) &&
                        account.getLastIpAddress().equals(player.getPlayerIp())) {
                    PostResult<AccountSessionEnterEvent> sessionEventResult = plugin.getEventBus()
                            .publish(AccountSessionEnterEvent.class, account, false)
                            .join();
                    if (sessionEventResult.getEvent().isCancelled()) {
                        LOGGER.atFine().log("Session enter cancelled for account: %s", account.getPlayerId());
                        return account;
                    }
                    player.sendMessage(config.getServerMessages().getMessage("autoconnect", new ServerMessageContext(account)));
                    if (config.getJoinDelay() == 0) {
                        account.nextAuthenticationStep(context);
                    } else {
                        core.schedule(() -> account.nextAuthenticationStep(context), config.getJoinDelay(), TimeUnit.MILLISECONDS);
                    }
                    LOGGER.atInfo().log("Auto-connected account: %s", account.getPlayerId());
                    return account;
                }

                if (plugin.getAuthenticatingAccountBucket().isAuthenticating(account)) {
                    LOGGER.atSevere().log("Duplicate authentication attempt for account: %s", account.getPlayerId());
                    throw new IllegalStateException("Cannot have two authenticating accounts at the same time!");
                }

                plugin.getAuthenticatingAccountBucket().addAuthenticatingAccount(account);
                account.nextAuthenticationStep(context);
                LOGGER.atFine().log("Started authentication for account: %s", account.getPlayerId());
                return account;
            });
        });
    }
    // #endregion

    // #region Disconnect Handling
    /**
     * Processes a player disconnection.
     *
     * @param player The disconnecting player. Must not be null.
     */
    @Override
    public void onDisconnect(ServerPlayer player) {
        Preconditions.checkNotNull(player, "player must not be null");
        String id = config.getActiveIdentifierType().getId(player);
        LOGGER.atFine().log("Processing disconnect for player: %s", player.getNickname());

        plugin.getLinkEntryBucket().removeLinkUsers(entryUser ->
                entryUser.getAccount().getPlayerId().equals(id));
        long loginDuration = System.currentTimeMillis() -
                plugin.getAuthenticatingAccountBucket().getEnterTimestampOrZero(player);

        if (plugin.getAuthenticatingAccountBucket().isAuthenticating(player)) {
            LOGGER.atFine().log("Player %s disconnected during authentication", player.getNickname());
            return;
        }

        accountDatabase.getAccount(id).thenAccept(account -> {
            account.setLastQuitTimestamp(System.currentTimeMillis());
            accountDatabase.saveOrUpdateAccount(account);
            LOGGER.atFine().log("Updated last quit timestamp for account: %s", account.getPlayerId());
        });
    }
    // #endregion
}