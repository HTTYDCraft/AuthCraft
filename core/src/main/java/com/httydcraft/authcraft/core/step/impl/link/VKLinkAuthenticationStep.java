package com.httydcraft.authcraft.core.step.impl.link;

import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.step.AuthenticationStep;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.core.link.vk.VKLinkType;
import com.httydcraft.authcraft.core.step.creators.AuthenticationStepFactoryTemplate;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;

// #region Class Documentation
/**
 * Authentication step for VK account linking.
 * Extends {@link MessengerAuthenticationStep} for VK-specific functionality.
 */
public class VKLinkAuthenticationStep extends MessengerAuthenticationStep {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String STEP_NAME = "VK_LINK";
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VKLinkAuthenticationStep}.
     *
     * @param context The authentication step context. Must not be null.
     */
    public VKLinkAuthenticationStep(AuthenticationStepContext context) {
        super(STEP_NAME, context, VKLinkType.getInstance(), VKLinkType.LINK_USER_FILTER);
        LOGGER.atFine().log("Initialized VKLinkAuthenticationStep for account: %s", context.getAccount().getPlayerId());
        SecurityAuditLogger.logSuccess("VKLinkAuthenticationStep", context.getAccount().getPlayer().orElse(null), String.format("Initialized VKLinkAuthenticationStep for account: %s", context.getAccount().getPlayerId()));
    }
    // #endregion

    // #region Factory
    /**
     * Factory for creating {@code VKLinkAuthenticationStep} instances.
     */
    public static class VKLinkAuthenticationStepFactory extends AuthenticationStepFactoryTemplate {
        /**
         * Constructs a new {@code VKLinkAuthenticationStepFactory}.
         */
        public VKLinkAuthenticationStepFactory() {
            super(STEP_NAME);
            LOGGER.atFine().log("Initialized VKLinkAuthenticationStepFactory");
            SecurityAuditLogger.logSuccess("VKLinkAuthenticationStepFactory: step factory event", null, "VKLinkAuthenticationStepFactory event triggered");
        }

        /**
         * Creates a new authentication step.
         *
         * @param context The authentication step context. Must not be null.
         * @return A new {@link AuthenticationStep}.
         */
        @Override
        public AuthenticationStep createNewAuthenticationStep(AuthenticationStepContext context) {
            SecurityAuditLogger.logSuccess("VKLinkAuthenticationStepFactory: step factory event", null, "VKLinkAuthenticationStepFactory event triggered");
            if (context == null) {
                SecurityAuditLogger.logFailure("VKLinkAuthenticationStepFactory", null, "Context is null on step factory event");
                LOGGER.atWarning().log("Auth fail: vk link step factory, reason: context is null");
                return null;
            }
            LOGGER.atFine().log("Creating new VKLinkAuthenticationStep");
            return new VKLinkAuthenticationStep(context);
        }
    }
    // #endregion

    public void process(ServerPlayer player) {
        Preconditions.checkNotNull(player, "player must not be null");
        Account account = authenticationStepContext.getAccount();
        SecurityAuditLogger.logSuccess("VKLinkAuthenticationStep", player, String.format("Step started for player: %s, account: %s", player.getName(), account != null ? account.getPlayerId() : "null"));
        if (player == null) {
            SecurityAuditLogger.logFailure("VKLinkAuthenticationStep", null, "Player is null on VK link step");
            LOGGER.atWarning().log("Auth fail: vk link step for player %s, reason: player is null in VKLinkAuthenticationStep", player.getNickname());
            return;
        }
        if (account == null) {
            SecurityAuditLogger.logFailure("VKLinkAuthenticationStep", player, "Account is null on VK link step");
            LOGGER.atWarning().log("Auth fail: vk link step for player %s, reason: account is null in VKLinkAuthenticationStep", player.getNickname());
            player.sendMessage(AuthPlugin.instance().getConfig().getServerMessages().getMessage("account-not-found"));
            return;
        }
        SecurityAuditLogger.logSuccess("VKLinkAuthenticationStep", player, String.format("VK link step started for player: %s, account: %s", player.getName(), account.getPlayerId()));
        try {
            Messages<ServerComponent> messages = VKLinkType.getInstance().getServerMessages();
            player.sendMessage(messages.getMessage("enter-confirm-need-chat", new ServerMessageContext(account)));
            AuthPlugin.instance()
                    .getCore()
                    .createTitle(messages.getMessage("enter-confirm-need-title"))
                    .subtitle(messages.getMessage("enter-confirm-need-subtitle"))
                    .stay(120)
                    .send(player);
            SecurityAuditLogger.logSuccess("VKLinkAuthenticationStep", player, "VK confirmation prompt sent to player: " + player.getName());
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("VKLinkAuthenticationStep", player, "VKLinkAuthenticationStep failed for player: " + (player != null ? player.getName() : "null") + ", error: " + ex.getMessage());
            throw ex;
        }
        LOGGER.atFine().log("Processed VK link step for player: %s", player.getNickname());
    }
}