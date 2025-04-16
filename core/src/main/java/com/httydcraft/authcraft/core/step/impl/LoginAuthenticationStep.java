package com.httydcraft.authcraft.core.step.impl;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.api.step.MessageableAuthenticationStep;
import com.httydcraft.authcraft.core.config.message.server.ServerMessageContext;
import com.httydcraft.authcraft.core.step.AuthenticationStepTemplate;
import com.httydcraft.authcraft.core.step.creators.AuthenticationStepFactoryTemplate;

// #region Class Documentation
/**
 * Authentication step for player login.
 * Prompts the player to enter their login credentials.
 */
public class LoginAuthenticationStep extends AuthenticationStepTemplate implements MessageableAuthenticationStep {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String STEP_NAME = "LOGIN";
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code LoginAuthenticationStep}.
     *
     * @param context The authentication step context. Must not be null.
     */
    public LoginAuthenticationStep(AuthenticationStepContext context) {
        super(STEP_NAME, context);
        Preconditions.checkNotNull(context, "context must not be null");
        LOGGER.atFine().log("Initialized LoginAuthenticationStep for account: %s", context.getAccount().getPlayerId());
    }
    // #endregion

    // #region AuthenticationStep Implementation
    /**
     * Determines if the step should proceed to the next step.
     *
     * @return {@code true} if the step can proceed, {@code false} otherwise.
     */
    @Override
    public boolean shouldPassToNextStep() {
        boolean canPass = authenticationStepContext.canPassToNextStep();
        LOGGER.atFine().log("Checked shouldPassToNextStep: %b", canPass);
        return canPass;
    }

    /**
     * Determines if the step should be skipped.
     *
     * @return {@code true} if the step should be skipped, {@code false} otherwise.
     */
    @Override
    public boolean shouldSkip() {
        boolean skip = !PLUGIN.getAuthenticatingAccountBucket().isAuthenticating(authenticationStepContext.getAccount()) ||
                authenticationStepContext.getAccount().isSessionActive(PLUGIN.getConfig().getSessionDurability());
        LOGGER.atFine().log("Checked shouldSkip: %b", skip);
        return skip;
    }
    // #endregion

    // #region MessageableAuthenticationStep Implementation
    /**
     * Processes the step by sending a login prompt to the player.
     *
     * @param player The player to process the step for. Must not be null.
     */
    @Override
    public void process(ServerPlayer player) {
        Preconditions.checkNotNull(player, "player must not be null");
        Account account = authenticationStepContext.getAccount();
        PluginConfig config = PLUGIN.getConfig();
        player.sendMessage(config.getServerMessages().getMessage("login-chat", new ServerMessageContext(account)));
        AuthPlugin.instance()
                .getCore()
                .createTitle(config.getServerMessages().getMessage("login-title"))
                .subtitle(config.getServerMessages().getMessage("login-subtitle"))
                .stay(120)
                .send(player);
        LOGGER.atFine().log("Processed login step for player: %s", player.getNickname());
    }
    // #endregion

    // #region Factory
    /**
     * Factory for creating {@code LoginAuthenticationStep} instances.
     */
    public static class LoginAuthenticationStepFactory extends AuthenticationStepFactoryTemplate {
        /**
         * Constructs a new {@code LoginAuthenticationStepFactory}.
         */
        public LoginAuthenticationStepFactory() {
            super(STEP_NAME);
            LOGGER.atFine().log("Initialized LoginAuthenticationStepFactory");
        }

        /**
         * Creates a new authentication step.
         *
         * @param context The authentication step context. Must not be null.
         * @return A new {@link LoginAuthenticationStep}.
         */
        @Override
        public LoginAuthenticationStep createNewAuthenticationStep(AuthenticationStepContext context) {
            LOGGER.atFine().log("Creating new LoginAuthenticationStep");
            return new LoginAuthenticationStep(context);
        }
    }
    // #endregion
}