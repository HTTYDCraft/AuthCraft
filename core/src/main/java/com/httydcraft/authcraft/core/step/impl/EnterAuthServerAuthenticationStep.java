package com.httydcraft.authcraft.core.step.impl;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.server.ConfigurationServer;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.server.proxy.ProxyServer;
import com.httydcraft.authcraft.api.step.AuthenticationStep;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.core.step.AuthenticationStepTemplate;
import com.httydcraft.authcraft.core.step.creators.AuthenticationStepFactoryTemplate;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

// #region Class Documentation
/**
 * Authentication step for connecting a player to an authentication server.
 * Ensures the player is directed to the correct server for authentication.
 */
public class EnterAuthServerAuthenticationStep extends AuthenticationStepTemplate {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String STEP_NAME = "ENTER_AUTH_SERVER";
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code EnterAuthServerAuthenticationStep}.
     *
     * @param context The authentication step context. Must not be null.
     */
    public EnterAuthServerAuthenticationStep(AuthenticationStepContext context) {
        super(STEP_NAME, context);
        Preconditions.checkNotNull(context, "context must not be null");
        LOGGER.atFine().log("Initialized EnterAuthServerAuthenticationStep for account: %s", context.getAccount().getPlayerId());
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
     * @return {@code true} if the step should be skipped, {@code false} otherwise.
     */
    @Override
    public boolean shouldSkip() {
        LOGGER.atFine().log("Checking shouldSkip for account: %s", authenticationStepContext.getAccount().getPlayerId());
        if (!PLUGIN.getAuthenticatingAccountBucket().isAuthenticating(authenticationStepContext.getAccount()) ||
                authenticationStepContext.getAccount().isSessionActive(PLUGIN.getConfig().getSessionDurability())) {
            LOGGER.atFine().log("Account not authenticating or session active, skipping");
            return true;
        }
        tryToConnect(true);
        LOGGER.atFine().log("Attempted connection, skipping");
        return true;
    }
    // #endregion

    // #region Helper Methods
    /**
     * Attempts to connect the player to the authentication server.
     *
     * @param shouldTryAgain Whether to retry if the initial attempt fails.
     */
    private void tryToConnect(boolean shouldTryAgain) {
        Optional<ServerPlayer> playerOptional = authenticationStepContext.getAccount().getPlayer();
        if (!playerOptional.isPresent()) {
            LOGGER.atFine().log("No player found, aborting connection attempt");
            return;
        }
        ServerPlayer player = playerOptional.get();
        ConfigurationServer foundServer = PLUGIN.getConfig().findServerInfo(PLUGIN.getConfig().getAuthServers());
        Optional<ProxyServer> currentServerOptional = player.getCurrentServer();
        if (!currentServerOptional.isPresent()) {
            if (shouldTryAgain) {
                PLUGIN.getCore().schedule(() -> tryToConnect(false), 3, TimeUnit.SECONDS);
                LOGGER.atFine().log("No current server, scheduling retry for player: %s", player.getNickname());
            }
            return;
        }
        if (currentServerOptional.get().getServerName().equals(foundServer.getId())) {
            LOGGER.atFine().log("Player %s already on auth server", player.getNickname());
            return;
        }
        foundServer.asProxyServer().sendPlayer(player);
        LOGGER.atFine().log("Sent player %s to auth server", player.getNickname());
    }
    // #endregion

    // #region Factory
    /**
     * Factory for creating {@code EnterAuthServerAuthenticationStep} instances.
     */
    public static class EnterAuthServerAuthenticationStepFactory extends AuthenticationStepFactoryTemplate {
        /**
         * Constructs a new {@code EnterAuthServerAuthenticationStepFactory}.
         */
        public EnterAuthServerAuthenticationStepFactory() {
            super(STEP_NAME);
            LOGGER.atFine().log("Initialized EnterAuthServerAuthenticationStepFactory");
        }

        /**
         * Creates a new authentication step.
         *
         * @param context The authentication step context. Must not be null.
         * @return A new {@link AuthenticationStep}.
         */
        @Override
        public AuthenticationStep createNewAuthenticationStep(AuthenticationStepContext context) {
            LOGGER.atFine().log("Creating new EnterAuthServerAuthenticationStep");
            return new EnterAuthServerAuthenticationStep(context);
        }
    }
    // #endregion
}