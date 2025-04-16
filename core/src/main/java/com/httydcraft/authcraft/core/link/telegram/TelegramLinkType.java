package com.httydcraft.authcraft.core.link.telegram;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.link.LinkSettings;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.core.config.message.link.context.LinkPlaceholderContext;
import com.httydcraft.authcraft.core.config.message.telegram.TelegramMessagePlaceholderContext;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.impl.UserNumberIdentificator;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.multimessenger.telegram.MessengerTelegram;

import java.util.function.Predicate;

// #region Class Documentation
/**
 * Implementation of {@link LinkType} and {@link MessengerTelegram} for Telegram integration.
 * Provides Telegram-specific messaging and configuration.
 */
public class TelegramLinkType implements LinkType, MessengerTelegram {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final TelegramLinkType INSTANCE = new TelegramLinkType();
    public static final Predicate<LinkUser> LINK_USER_FILTER = linkUser -> linkUser.getLinkType() == getInstance();
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    private static final LinkUserIdentificator DEFAULT_IDENTIFICATOR = new UserNumberIdentificator(-1L);
    // #endregion

    // #region Constructor
    /**
     * Private constructor to enforce singleton pattern.
     */
    private TelegramLinkType() {
        LOGGER.atFine().log("Initialized TelegramLinkType singleton");
    }
    // #endregion

    // #region Singleton Access
    /**
     * Gets the singleton instance of {@code TelegramLinkType}.
     *
     * @return The {@link TelegramLinkType} instance.
     */
    public static TelegramLinkType getInstance() {
        LOGGER.atFine().log("Retrieved TelegramLinkType instance");
        return INSTANCE;
    }
    // #endregion

    // #region Messaging
    /**
     * Gets the server messages for Telegram.
     *
     * @return The {@link Messages} containing {@link ServerComponent}s.
     */
    @Override
    public Messages<ServerComponent> getServerMessages() {
        Messages<ServerComponent> messages = PLUGIN.getConfig().getServerMessages().getSubMessages("telegram");
        LOGGER.atFine().log("Retrieved Telegram server messages");
        return messages;
    }

    /**
     * Gets the link messages for Telegram.
     *
     * @return The {@link Messages} containing string messages.
     */
    @Override
    public Messages<String> getLinkMessages() {
        Messages<String> messages = PLUGIN.getConfig().getTelegramSettings().getMessages();
        LOGGER.atFine().log("Retrieved Telegram link messages");
        return messages;
    }
    // #endregion

    // #region Configuration
    /**
     * Gets the Telegram link settings.
     *
     * @return The {@link LinkSettings}.
     */
    @Override
    public LinkSettings getSettings() {
        LinkSettings settings = PLUGIN.getConfig().getTelegramSettings();
        LOGGER.atFine().log("Retrieved Telegram link settings");
        return settings;
    }
    // #endregion

    // #region Identification
    /**
     * Gets the default user identifier for Telegram.
     *
     * @return The {@link LinkUserIdentificator}.
     */
    @Override
    public LinkUserIdentificator getDefaultIdentificator() {
        LOGGER.atFine().log("Retrieved default Telegram identifier: %s", DEFAULT_IDENTIFICATOR);
        return DEFAULT_IDENTIFICATOR;
    }
    // #endregion

    // #region Message Context
    /**
     * Creates a new message context for Telegram.
     *
     * @param account The account to create context for. Must not be null.
     * @return The {@link LinkPlaceholderContext}.
     */
    @Override
    public LinkPlaceholderContext newMessageContext(Account account) {
        Preconditions.checkNotNull(account, "account must not be null");
        LinkPlaceholderContext context = new TelegramMessagePlaceholderContext(account);
        LOGGER.atFine().log("Created Telegram message context for account: %s", account.getPlayerId());
        return context;
    }
    // #endregion
}