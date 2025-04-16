package com.httydcraft.authcraft.core.step.impl;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.step.AuthenticationStep;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.api.step.MessageableAuthenticationStep;
import com.httydcraft.authcraft.core.config.message.server.ServerMessageContext;
import com.httydcraft.authcraft.core.step.AuthenticationStepTemplate;
import com.httydcraft.authcraft.core.step.creators.AuthenticationStepFactoryTemplate;

// #region Class Documentation
/**
 * Authentication step for player registration.
 * Prompts the player to register a new account.
 */
public class RegisterAuthenticationStep extends AuthenticationStepTemplate implements MessageableAuthenticationStep {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String STEP_NAME = "REGISTER";
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code RegisterAuthenticationStep}.
     *
     * @param context The authentication step context. Must not be null.
     */
    public RegisterAuthenticationStep(AuthenticationStepContext context) {
        super(STEP_NAME, context);
        Preconditions.checkNotNull(context, "context must not be null");
        LOGGER.atFine().log("Initialized RegisterAuthenticationStep for account: %s", context.getAccount().getPlayerId());
    }
    // #endregion

    // #region AuthenticationStep Implementation
    /**
     * Determines if the step should proceed to the next step.
     *
     * @return {@code true} if the account is registered, {@code false} otherwise.
     */
    @Override
    public boolean shouldPassToNextStep() {
        boolean isRegistered = authenticationStepContext.getAccount().isRegistered();
        LOGGER.atFine().log("Checked shouldPassToNextStep: %b", isRegistered);
        if (isRegistered) {
            Account account = authenticationStepContext.getAccount();
            PLUGIN.getAuthenticatingAccountBucket().removeAuthenticatingAccount(account);
            account.setLastSessionStartTimestamp(System.currentTimeMillis());
            account.getPlayer().map(ServerPlayer::getPlayerIp).ifPresent(account::setLastIpAddress);
            LOGGER.atFine().log("Account %s registered, updated session details", account.getPlayerId());
        }
        return isRegistered;
    }

    /**
     * Determines if the step should be skipped.
     *
     * @return {@code true} if the account is already registered, {@code false} otherwise.
     */
    @Override
    public boolean shouldSkip() {
        boolean skip = authenticationStepContext.getAccount().isRegistered();
        LOGGER.atFine().log("Checked shouldSkip: %b", skip);
        return skip;
    }
    // #endregion

    // #region MessageableAuthenticationStep Implementation
    /**
     * Processes the step by sending a registration prompt to the player.
     *
     * @param player The player to process the step for. Must not be null.
     */
    @Override
    public void process(ServerPlayer player) {
        Preconditions.checkNotNull(player, "player must not be null");
        Account account = authenticationStepContext.getAccount();
        PluginConfig config = AuthPlugin.instance().getConfig();
        player.sendMessage(config.getServerMessages().getMessage("register-chat", new ServerMessageContext(account)));
        AuthPlugin.instance()
                .getCore()
                .createTitle(config.getServerMessages().getMessage("register-title"))
                .subtitle(config.getServerMessages().getMessage("register-subtitle"))
                .stay(120)
                .send(player);
        LOGGER.atFine().log("Processed registration step for player: %s", player.getNickname());
    }
    // #endregion

    // #region Factory
    /**
     * Factory for creating {@code RegisterAuthenticationStep} instances.
     */
    public static class RegisterAuthenticationStepFactory extends AuthenticationStepFactoryTemplate {
        /**
         * Constructs a new {@code RegisterAuthenticationStepFactory}.
         */
        public RegisterAuthenticationStepFactory() {
            super(STEP_NAME);
            LOGGER.atFine().log("Initialized RegisterAuthenticationStepFactory");
        }

        /**
         * Creates a new authentication step.
         *
         * @param context The authentication step context. Must not be null.
         * @return A new {@link AuthenticationStep}.
         */
        @Override
        public AuthenticationStep createNewAuthenticationStep(AuthenticationStepContext context) {
            LOGGER.atFine().log("Creating new RegisterAuthenticationStep");
            return new RegisterAuthenticationStep(context);
        }
    }
    // #endregion
}