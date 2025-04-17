package com.httydcraft.authcraft.core.step.impl;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.core.step.AuthenticationStepTemplate;
import com.httydcraft.authcraft.core.step.creators.AuthenticationStepFactoryTemplate;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;

import java.util.Optional;

// #region Class Documentation
/**
 * Authentication step for directing a player to the game server in AuthCraft.
 * Finalizes authentication and updates account session details.
 */
public class EnterServerAuthenticationStep extends AuthenticationStepTemplate {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String STEP_NAME = "ENTER_SERVER";
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code EnterServerAuthenticationStep}.
     *
     * @param context The authentication step context. Must not be null.
     */
    public EnterServerAuthenticationStep(AuthenticationStepContext context) {
        super(STEP_NAME, context);
        Preconditions.checkNotNull(context, "context must not be null");
        LOGGER.atFine().log("Initialized EnterServerAuthenticationStep for account: %s", context.getAccount().getPlayerId());
        LOGGER.atFine().log("EnterServerAuthenticationStep initialized");
    }
    // #endregion

    // #region AuthenticationStep Implementation
    /**
     * Determines if the step should proceed to the next step.
     *
     * @return {@code true} always, as this step auto-proceeds.
     */
    @Override
    public boolean shouldPassToNextStep() {
        LOGGER.atFine().log("Checked shouldPassToNextStep: true");
        return true;
    }

    /**
     * Determines if the step should be skipped.
     *
     * @return {@code true} always, as this step is executed and then skipped.
     */
    @Override
    public boolean shouldSkip() {
        LOGGER.atFine().log("Executing enterServer and skipping");
        enterServer();
        return true;
    }
    // #endregion

    // #region Helper Methods
    /**
     * Directs the player to the game server and updates account details.
     */
    public void enterServer() {
        Account account = authenticationStepContext.getAccount();
        ServerPlayer player = account != null ? account.getPlayer().orElse(null) : null;
        SecurityAuditLogger.logSuccess("EnterServerAuthenticationStep", player, String.format("Step started for account: %s, player: %s", account != null ? account.getPlayerId() : "null", player != null ? player.getName() : "null"));
        if (account == null) {
            SecurityAuditLogger.logFailure("EnterServerAuthenticationStep", null, "Account is null");
            LOGGER.atWarning().log("Auth fail: enterServer step, reason: account is null in EnterServerAuthenticationStep");
            return;
        }
        LOGGER.atFine().log("Processing enterServer for account: %s", account.getPlayerId());

        PLUGIN.getAuthenticatingAccountBucket().removeAuthenticatingAccount(account);
        account.setLastSessionStartTimestamp(System.currentTimeMillis());
        account.getPlayer().map(ServerPlayer::getPlayerIp).ifPresent(account::setLastIpAddress);
        PLUGIN.getAccountDatabase().saveOrUpdateAccount(account);

        if (!account.getPlayer().isPresent()) {
            SecurityAuditLogger.logFailure("EnterServerAuthenticationStep", null, String.format("No player found for account: %s", account.getPlayerId()));
            LOGGER.atFine().log("No player found for account: %s", account.getPlayerId());
            return;
        }

        ServerPlayer presentPlayer = account.getPlayer().get();
        try {
            PLUGIN.getConfig().findServerInfo(PLUGIN.getConfig().getGameServers()).asProxyServer().sendPlayer(presentPlayer);
            LOGGER.atInfo().log("Sent player %s to game server", presentPlayer.getNickname());
            SecurityAuditLogger.logSuccess("EnterServerAuthenticationStep", presentPlayer, "Player sent to game server: " + presentPlayer.getName());
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("EnterServerAuthenticationStep", presentPlayer, "Failed to send player: " + (presentPlayer != null ? presentPlayer.getName() : "null") + ", error: " + ex.getMessage());
            throw ex;
        }
    }
    // #endregion

    // #region Factory
    /**
     * Factory for creating {@code EnterServerAuthenticationStep} instances.
     */
    public static class EnterServerAuthenticationStepFactory extends AuthenticationStepFactoryTemplate {
        /**
         * Constructs a new {@code EnterServerAuthenticationStepFactory}.
         */
        public EnterServerAuthenticationStepFactory() {
            super(STEP_NAME);
            LOGGER.atFine().log("Initialized EnterServerAuthenticationStepFactory");
        }

        /**
         * Creates a new authentication step.
         *
         * @param context The authentication step context. Must not be null.
         * @return A new {@link EnterServerAuthenticationStep}.
         */
        @Override
        public EnterServerAuthenticationStep createNewAuthenticationStep(AuthenticationStepContext context) {
            if (context == null) {
                SecurityAuditLogger.logFailure("EnterServerAuthenticationStepFactory", null, "Context is null on step factory event");
                LOGGER.atWarning().log("Auth fail: enterServer step factory, reason: context is null");
                return null;
            }
            SecurityAuditLogger.logSuccess("EnterServerAuthenticationStepFactory", null, String.format("Step factory event triggered for player: %s", context.getAccount() != null && context.getAccount().getPlayer().isPresent() ? context.getAccount().getPlayer().get().getName() : "null"));
            LOGGER.atFine().log("Creating new EnterServerAuthenticationStep");
            return new EnterServerAuthenticationStep(context);
        }
    }
    // #endregion
}