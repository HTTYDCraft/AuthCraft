package com.httydcraft.authcraft.core.link.discord;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.link.DiscordSettings;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.core.config.message.discord.DiscordMessagePlaceholderContext;
import com.httydcraft.authcraft.core.config.message.link.context.LinkPlaceholderContext;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.impl.UserNumberIdentificator;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.multimessenger.discord.MessengerDiscord;

import java.util.function.Predicate;

// #region Class Documentation
/**
 * Implementation of {@link LinkType} and {@link MessengerDiscord} for Discord integration.
 * Provides Discord-specific messaging and configuration.
 */
public class DiscordLinkType implements LinkType, MessengerDiscord {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final DiscordLinkType INSTANCE = new DiscordLinkType();
    public static final Predicate<LinkUser> LINK_USER_FILTER = linkUser -> linkUser.getLinkType() == getInstance();
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    private static final LinkUserIdentificator DEFAULT_IDENTIFICATOR = new UserNumberIdentificator(-1L);
    // #endregion

    // #region Constructor
    /**
     * Private constructor to enforce singleton pattern.
     */
    private DiscordLinkType() {
        LOGGER.atFine().log("Initialized DiscordLinkType singleton");
    }
    // #endregion

    // #region Singleton Access
    /**
     * Gets the singleton instance of {@code DiscordLinkType}.
     *
     * @return The {@link DiscordLinkType} instance.
     */
    public static DiscordLinkType getInstance() {
        LOGGER.atFine().log("Retrieved DiscordLinkType instance");
        return INSTANCE;
    }
    // #endregion

    // #region Messaging
    /**
     * Gets the server messages for Discord.
     *
     * @return The {@link Messages} containing {@link ServerComponent}s.
     */
    @Override
    public Messages<ServerComponent> getServerMessages() {
        Messages<ServerComponent> messages = PLUGIN.getConfig().getServerMessages().getSubMessages("discord");
        LOGGER.atFine().log("Retrieved Discord server messages");
        return messages;
    }

    /**
     * Gets the link messages for Discord.
     *
     * @return The {@link Messages} containing string messages.
     */
    @Override
    public Messages<String> getLinkMessages() {
        Messages<String> messages = PLUGIN.getConfig().getDiscordSettings().getMessages();
        LOGGER.atFine().log("Retrieved Discord link messages");
        return messages;
    }
    // #endregion

    // #region Configuration
    /**
     * Gets the Discord link settings.
     *
     * @return The {@link DiscordSettings}.
     */
    @Override
    public DiscordSettings getSettings() {
        DiscordSettings settings = PLUGIN.getConfig().getDiscordSettings();
        LOGGER.atFine().log("Retrieved Discord link settings");
        return settings;
    }
    // #endregion

    // #region Identification
    /**
     * Gets the default user identifier for Discord.
     *
     * @return The {@link LinkUserIdentificator}.
     */
    @Override
    public LinkUserIdentificator getDefaultIdentificator() {
        LOGGER.atFine().log("Retrieved default Discord identifier: %s", DEFAULT_IDENTIFICATOR);
        return DEFAULT_IDENTIFICATOR;
    }
    // #endregion

    // #region Message Context
    /**
     * Creates a new message context for Discord.
     *
     * @param account The account to create context for. Must not be null.
     * @return The {@link LinkPlaceholderContext}.
     */
    @Override
    public LinkPlaceholderContext newMessageContext(Account account) {
        Preconditions.checkNotNull(account, "account must not be null");
        LinkPlaceholderContext context = new DiscordMessagePlaceholderContext(account);
        LOGGER.atFine().log("Created Discord message context for account: %s", account.getPlayerId());
        return context;
    }
    // #endregion
}