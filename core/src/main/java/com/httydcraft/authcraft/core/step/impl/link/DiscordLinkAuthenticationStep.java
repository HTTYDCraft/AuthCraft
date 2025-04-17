package com.httydcraft.authcraft.core.step.impl.link;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.info.LinkUserInfo;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.core.config.message.server.ServerMessageContext;
import com.httydcraft.multimessenger.core.keyboard.Keyboard;
import com.httydcraft.multimessenger.discord.message.DiscordMessage;
import com.httydcraft.authcraft.core.hooks.DiscordHook;
import com.httydcraft.authcraft.core.link.discord.DiscordLinkType;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;

// #region Class Documentation
/**
 * Authentication step for Discord account linking.
 * Sends a confirmation message to the user via Discord.
 */
public class DiscordLinkAuthenticationStep extends MessengerAuthenticationStep {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String STEP_NAME = "DISCORD_LINK";
    private final DiscordHook discordHook = AuthPlugin.instance().getHook(DiscordHook.class);

    // #region Constructor
    /**
     * Constructs a new {@code DiscordLinkAuthenticationStep}.
     *
     * @param context The authentication step context. Must not be null.
     */
    public DiscordLinkAuthenticationStep(AuthenticationStepContext context) {
        super(STEP_NAME, context, DiscordLinkType.getInstance(), DiscordLinkType.LINK_USER_FILTER);
        LOGGER.atFine().log("Initialized DiscordLinkAuthenticationStep for account: %s", context.getAccount().getPlayerId());
        SecurityAuditLogger.logSuccess("DiscordLinkAuthenticationStep", context.getAccount().getPlayer().orElse(null), String.format("Initialized DiscordLinkAuthenticationStep for account: %s", context.getAccount().getPlayerId()));
        // Discord audit log
        // LinkUserInfo не доступен напрямую из context, поэтому Discord-аудит будет только в sendConfirmationMessage
    }
    // #endregion

    // #region MessengerAuthenticationStep Implementation
    /**
     * Sends a confirmation message to the user via Discord.
     *
     * @param account The account to send the message to. Must not be null.
     * @param linkType The link type (Discord). Must not be null.
     * @param linkUserInfo The user info for the link. Must not be null.
     */
    @Override
    protected void sendConfirmationMessage(Account account, LinkType linkType, LinkUserInfo linkUserInfo) {
        Preconditions.checkNotNull(account, "account must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(linkUserInfo, "linkUserInfo must not be null");
        LOGGER.atFine().log("Sending Discord confirmation message for account: %s", account.getPlayerId());
        SecurityAuditLogger.logSuccess("DiscordLinkAuthenticationStep", account.getPlayer().orElse(null), String.format("Sending Discord confirmation message for account: %s, Discord ID: %s", account.getPlayerId(), linkUserInfo.getIdentificator().asString()));
        // Discord audit log
        String discordId = linkUserInfo.getIdentificator().asString();
        SecurityAuditLogger.logSuccessDiscord("SendConfirmation", discordId, discordId, String.format("Sent confirmation message for account: %s", account.getPlayerId()));

        Keyboard keyboard = linkType.getSettings().getKeyboards().createKeyboard("confirmation", "%name%", account.getName());
        linkType.newMessageBuilder(linkType.getSettings().getMessages().getMessage("enter-message", linkType.newMessageContext(account)))
                .keyboard(keyboard)
                .build()
                .as(DiscordMessage.class)
                .send(builder -> discordHook.getJDA()
                        .openPrivateChannelById(linkUserInfo.getIdentificator().asNumber())
                        .queue(channel -> channel.sendMessage(builder.build()).queue()));
        LOGGER.atFine().log("Sent Discord confirmation message for account: %s", account.getPlayerId());
        SecurityAuditLogger.logSuccess("DiscordLinkAuthenticationStep", account.getPlayer().orElse(null), String.format("Discord confirmation message sent for account: %s, Discord ID: %s", account.getPlayerId(), linkUserInfo.getIdentificator().asString()));
    }
    // #endregion

    public void process(ServerPlayer player) {
        Preconditions.checkNotNull(player, "player must not be null");
        Account account = authenticationStepContext.getAccount();
        if (account == null) {
            LOGGER.atWarning().log("Auth fail: discord link step for player %s, reason: account is null in DiscordLinkAuthenticationStep", player.getNickname());
            SecurityAuditLogger.logFailure("DiscordLinkAuthenticationStep", player, "Account is null on Discord link step");
            player.sendMessage(AuthPlugin.instance().getConfig().getServerMessages().getMessage("account-not-found"));
            return;
        }
        SecurityAuditLogger.logSuccess("DiscordLinkAuthenticationStep", player, String.format("Discord link step started for player: %s, account: %s", player.getName(), account.getPlayerId()));
        Messages<ServerComponent> messages = getLinkType().getServerMessages();
        player.sendMessage(messages.getMessage("enter-confirm-need-chat", new ServerMessageContext(account)));
        AuthPlugin.instance()
                .getCore()
                .createTitle(messages.getMessage("enter-confirm-need-title"))
                .subtitle(messages.getMessage("enter-confirm-need-subtitle"))
                .stay(120)
                .send(player);
        LOGGER.atFine().log("Processed Discord link step for player: %s", player.getNickname());
    }

    // #region Factory
    /**
     * Factory for creating {@code DiscordLinkAuthenticationStep} instances.
     */
    public static class DiscordLinkAuthenticationStepFactory extends com.httydcraft.authcraft.core.step.creators.AuthenticationStepFactoryTemplate {
        /**
         * Constructs a new {@code DiscordLinkAuthenticationStepFactory}.
         */
        public DiscordLinkAuthenticationStepFactory() {
            super(STEP_NAME);
            LOGGER.atFine().log("Initialized DiscordLinkAuthenticationStepFactory");
        }
    }
    // #endregion
}