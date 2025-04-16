package com.httydcraft.authcraft.core.step.impl.link;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.info.LinkUserInfo;
import com.httydcraft.authcraft.api.step.AuthenticationStep;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.multimessenger.core.keyboard.Keyboard;
import com.httydcraft.multimessenger.discord.message.DiscordMessage;
import com.httydcraft.authcraft.core.hooks.DiscordHook;
import com.httydcraft.authcraft.core.link.discord.DiscordLinkType;
import com.httydcraft.authcraft.core.step.creators.AuthenticationStepFactoryTemplate;

// #region Class Documentation
/**
 * Authentication step for Discord account linking.
 * Sends a confirmation message to the user via Discord.
 */
public class DiscordLinkAuthenticationStep extends MessengerAuthenticationStep {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String STEP_NAME = "DISCORD_LINK";
    private final DiscordHook discordHook = AuthPlugin.instance().getHook(DiscordHook.class);
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code DiscordLinkAuthenticationStep}.
     *
     * @param context The authentication step context. Must not be null.
     */
    public DiscordLinkAuthenticationStep(AuthenticationStepContext context) {
        super(STEP_NAME, context, DiscordLinkType.getInstance(), DiscordLinkType.LINK_USER_FILTER);
        LOGGER.atFine().log("Initialized DiscordLinkAuthenticationStep for account: %s", context.getAccount().getPlayerId());
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

        Keyboard keyboard = linkType.getSettings().getKeyboards().createKeyboard("confirmation", "%name%", account.getName());
        linkType.newMessageBuilder(linkType.getSettings().getMessages().getMessage("enter-message", linkType.newMessageContext(account)))
                .keyboard(keyboard)
                .build()
                .as(DiscordMessage.class)
                .send(builder -> discordHook.getJDA()
                        .openPrivateChannelById(linkUserInfo.getIdentificator().asNumber())
                        .queue(channel -> channel.sendMessage(builder.build()).queue()));
        LOGGER.atFine().log("Sent Discord confirmation message for account: %s", account.getPlayerId());
    }
    // #endregion

    // #region Factory
    /**
     * Factory for creating {@code DiscordLinkAuthenticationStep} instances.
     */
    public static class DiscordLinkAuthenticationStepFactory extends AuthenticationStepFactoryTemplate {
        /**
         * Constructs a new {@code DiscordLinkAuthenticationStepFactory}.
         */
        public DiscordLinkAuthenticationStepFactory() {
            super(STEP_NAME);
            LOGGER.atFine().log("Initialized DiscordLinkAuthenticationStepFactory");
        }

        /**
         * Creates a new authentication step.
         *
         * @param context The authentication step context. Must not be null.
         * @return A new {@link AuthenticationStep}.
         */
        @Override
        public AuthenticationStep createNewAuthenticationStep(AuthenticationStepContext context) {
            LOGGER.atFine().log("Creating new DiscordLinkAuthenticationStep");
            return new DiscordLinkAuthenticationStep(context);
        }
    }
    // #endregion
}