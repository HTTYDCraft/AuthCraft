package com.httydcraft.authcraft.core.step.impl.link;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.entry.LinkEntryUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserInfo;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.api.step.MessageableAuthenticationStep;
import com.httydcraft.multimessenger.core.identificator.Identificator;
import com.httydcraft.multimessenger.core.keyboard.Keyboard;
import com.httydcraft.authcraft.core.config.message.server.ServerMessageContext;
import com.httydcraft.authcraft.core.link.user.entry.BaseLinkEntryUser;
import com.httydcraft.authcraft.core.step.AuthenticationStepTemplate;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;

import java.util.function.Predicate;

// #region Class Documentation
/**
 * Abstract authentication step for messenger-based confirmation.
 * Handles sending confirmation messages for various messenger platforms.
 */
public class MessengerAuthenticationStep extends AuthenticationStepTemplate implements MessageableAuthenticationStep {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    private final LinkEntryUser linkEntryUser;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code MessengerAuthenticationStep}.
     *
     * @param stepName The name of the authentication step. Must not be null.
     * @param authenticationStepContext The authentication step context. Must not be null.
     * @param linkType The link type for the messenger. Must not be null.
     * @param filter The filter for finding link users. Must not be null.
     */
    public MessengerAuthenticationStep(String stepName, AuthenticationStepContext authenticationStepContext, LinkType linkType, Predicate<LinkUser> filter) {
        super(stepName, authenticationStepContext);
        Preconditions.checkNotNull(stepName, "stepName must not be null");
        Preconditions.checkNotNull(authenticationStepContext, "authenticationStepContext must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(filter, "filter must not be null");
        Account account = authenticationStepContext.getAccount();
        this.linkEntryUser = new BaseLinkEntryUser(linkType, account, account.findFirstLinkUserOrNew(filter, linkType).getLinkUserInfo());
        LOGGER.atFine().log("Initialized MessengerAuthenticationStep for account: %s, step: %s", account.getPlayerId(), stepName);
        SecurityAuditLogger.logSuccess("MessengerAuthenticationStep: step factory event", null, "MessengerAuthenticationStep event triggered");
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
        boolean confirmed = linkEntryUser.isConfirmed();
        LOGGER.atFine().log("Checked shouldPassToNextStep: %b", confirmed);
        return confirmed;
    }

    /**
     * Determines if the step should be skipped.
     *
     * @return {@code true} if the step should be skipped, {@code false} otherwise.
     */
    @Override
    public boolean shouldSkip() {
        Account account = authenticationStepContext.getAccount();
        LinkType linkType = linkEntryUser.getLinkType();
        LOGGER.atFine().log("Checking shouldSkip for account: %s, linkType: %s", account.getPlayerId(), linkType.getClass().getSimpleName());

        if (!linkType.getSettings().isEnabled()) {
            SecurityAuditLogger.logFailure("MessengerAuthenticationStep", account.getPlayer().orElse(null), "Link type disabled, skipping messenger step");
            LOGGER.atFine().log("Link type disabled, skipping");
            return true;
        }

        if (PLUGIN.getLinkEntryBucket().find(account.getPlayerId(), linkType).isPresent()) {
            SecurityAuditLogger.logSuccess("MessengerAuthenticationStep", account.getPlayer().orElse(null), "Link entry found, skipping messenger step");
            LOGGER.atFine().log("Link entry found, skipping");
            return true;
        }

        if (account.isSessionActive(PLUGIN.getConfig().getSessionDurability())) {
            SecurityAuditLogger.logSuccess("MessengerAuthenticationStep", account.getPlayer().orElse(null), "Active session found, skipping messenger step");
            LOGGER.atFine().log("Active session found, skipping");
            return true;
        }

        LinkUser linkUser = account.findFirstLinkUser(user -> user.getLinkType().equals(linkType)).orElse(null);

        if (linkUser == null) {
            SecurityAuditLogger.logFailure("MessengerAuthenticationStep", account.getPlayer().orElse(null), "No link user found, skipping messenger step");
            linkEntryUser.getAccount().getPlayer().ifPresent(player ->
                    player.sendMessage(linkType.getServerMessages().getMessage("not-linked")));
            LOGGER.atFine().log("No link user found, skipping");
            return true;
        }

        LinkUserInfo linkUserInfo = linkUser.getLinkUserInfo();

        if (linkUser.isIdentifierDefaultOrNull()) {
            SecurityAuditLogger.logFailure("MessengerAuthenticationStep", account.getPlayer().orElse(null), "No valid link user identifier, skipping messenger step");
            linkEntryUser.getAccount().getPlayer().ifPresent(player ->
                    player.sendMessage(linkType.getServerMessages().getMessage("not-linked")));
            LOGGER.atFine().log("No valid link user identifier, skipping");
            return true;
        }

        if (linkType.getSettings().getConfirmationSettings().canToggleConfirmation() && !linkUserInfo.isConfirmationEnabled()) {
            SecurityAuditLogger.logFailure("MessengerAuthenticationStep", account.getPlayer().orElse(null), "Confirmation not enabled, skipping messenger step");
            LOGGER.atFine().log("Confirmation not enabled, skipping");
            return true;
        }

        PLUGIN.getLinkEntryBucket().modifiable().add(linkEntryUser);
        sendConfirmationMessage(account, linkType, linkUserInfo);
        LOGGER.atFine().log("Added link entry and sent confirmation message");
        return false;
    }
    // #endregion

    // #region MessengerAuthenticationStep Implementation
    /**
     * Sends a confirmation message to the user via the messenger platform.
     *
     * @param account The account to send the message to. Must not be null.
     * @param linkType The link type for the messenger. Must not be null.
     * @param linkUserInfo The user info for the link. Must not be null.
     */
    protected void sendConfirmationMessage(Account account, LinkType linkType, LinkUserInfo linkUserInfo) {
        Preconditions.checkNotNull(account, "account must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(linkUserInfo, "linkUserInfo must not be null");
        LOGGER.atFine().log("Sending confirmation message for account: %s, linkType: %s", account.getPlayerId(), linkType.getClass().getSimpleName());
        try {
            Keyboard keyboard = linkType.getSettings().getKeyboards().createKeyboard("confirmation", "%name%", account.getName());
            Identificator userIdentificator = linkUserInfo.getIdentificator().isNumber() ? Identificator.of(linkUserInfo.getIdentificator().asNumber()) :
                    Identificator.of(linkUserInfo.getIdentificator().asString());
            linkType.newMessageBuilder(linkType.getSettings().getMessages().getMessage("enter-message", linkType.newMessageContext(account)))
                    .keyboard(keyboard)
                    .build()
                    .send(userIdentificator);
            LOGGER.atFine().log("Sent confirmation message for account: %s", account.getPlayerId());
            SecurityAuditLogger.logSuccess("MessengerAuthenticationStep", account.getPlayer().orElse(null), "Confirmation message sent via messenger");
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("MessengerAuthenticationStep", account.getPlayer().orElse(null), "Failed to send confirmation message: " + ex.getMessage());
            LOGGER.atWarning().log("Failed to send confirmation message for account: %s, error: %s", account.getPlayerId(), ex.getMessage());
        }
    }

    /**
     * Processes the step by sending a message to the player.
     *
     * @param player The player to process the step for. Must not be null.
     */
    @Override
    public void process(ServerPlayer player) {
        Preconditions.checkNotNull(player, "player must not be null");
        Account account = authenticationStepContext.getAccount();
        SecurityAuditLogger.logSuccess("MessengerAuthenticationStep", player, String.format("Step started for player: %s, account: %s", player.getName(), account != null ? account.getPlayerId() : "null"));
        if (account == null) {
            SecurityAuditLogger.logFailure("MessengerAuthenticationStep", player, "Account is null on messenger step");
            player.sendMessage(PLUGIN.getConfig().getServerMessages().getMessage("account-not-found"));
            return;
        }
        try {
            Messages<ServerComponent> messages = linkEntryUser.getLinkType().getServerMessages();
            player.sendMessage(messages.getMessage("enter-confirm-need-chat", new ServerMessageContext(linkEntryUser.getAccount())));
            PLUGIN.getCore()
                    .createTitle(messages.getMessage("enter-confirm-need-title"))
                    .subtitle(messages.getMessage("enter-confirm-need-subtitle"))
                    .stay(120)
                    .send(player);
            SecurityAuditLogger.logSuccess("MessengerAuthenticationStep", player, "Messenger confirmation prompt sent to player: " + player.getName());
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("MessengerAuthenticationStep", player, "MessengerAuthenticationStep failed for player: " + (player != null ? player.getName() : "null") + ", error: " + ex.getMessage());
            throw ex;
        }
        LOGGER.atFine().log("Processed messenger step for player: %s", player.getNickname());
    }
    // #endregion
}