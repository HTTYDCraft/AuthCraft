package com.httydcraft.authcraft.core.link.vk;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.link.LinkSettings;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.core.config.message.link.context.LinkPlaceholderContext;
import com.httydcraft.authcraft.core.config.message.vk.VKMessagePlaceholderContext;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.impl.UserNumberIdentificator;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.multimessenger.vk.MessengerVk;

import java.util.function.Predicate;

// #region Class Documentation
/**
 * Implementation of {@link LinkType} and {@link MessengerVk} for VK integration.
 * Provides VK-specific messaging and configuration.
 */
public class VKLinkType implements LinkType, MessengerVk {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final VKLinkType INSTANCE = new VKLinkType();
    public static final Predicate<LinkUser> LINK_USER_FILTER = linkUser -> linkUser.getLinkType() == getInstance();
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    private static final LinkUserIdentificator DEFAULT_IDENTIFICATOR = new UserNumberIdentificator(-1L);
    // #endregion

    // #region Constructor
    /**
     * Private constructor to enforce singleton pattern.
     */
    private VKLinkType() {
        LOGGER.atFine().log("Initialized VKLinkType singleton");
    }
    // #endregion

    // #region Singleton Access
    /**
     * Gets the singleton instance of {@code VKLinkType}.
     *
     * @return The {@link VKLinkType} instance.
     */
    public static VKLinkType getInstance() {
        LOGGER.atFine().log("Retrieved VKLinkType instance");
        return INSTANCE;
    }
    // #endregion

    // #region Messaging
    /**
     * Gets the server messages for VK.
     *
     * @return The {@link Messages} containing {@link ServerComponent}s.
     */
    @Override
    public Messages<ServerComponent> getServerMessages() {
        Messages<ServerComponent> messages = PLUGIN.getConfig().getServerMessages().getSubMessages("vk");
        LOGGER.atFine().log("Retrieved VK server messages");
        return messages;
    }

    /**
     * Gets the link messages for VK.
     *
     * @return The {@link Messages} containing string messages.
     */
    @Override
    public Messages<String> getLinkMessages() {
        Messages<String> messages = PLUGIN.getConfig().getVKSettings().getMessages();
        LOGGER.atFine().log("Retrieved VK link messages");
        return messages;
    }
    // #endregion

    // #region Configuration
    /**
     * Gets the VK link settings.
     *
     * @return The {@link LinkSettings}.
     */
    @Override
    public LinkSettings getSettings() {
        LinkSettings settings = PLUGIN.getConfig().getVKSettings();
        LOGGER.atFine().log("Retrieved VK link settings");
        return settings;
    }
    // #endregion

    // #region Identification
    /**
     * Gets the default user identifier for VK.
     *
     * @return The {@link LinkUserIdentificator}.
     */
    @Override
    public LinkUserIdentificator getDefaultIdentificator() {
        LOGGER.atFine().log("Retrieved default VK identifier: %s", DEFAULT_IDENTIFICATOR);
        return DEFAULT_IDENTIFICATOR;
    }
    // #endregion

    // #region Message Context
    /**
     * Creates a new message context for VK.
     *
     * @param account The account to create context for. Must not be null.
     * @return The {@link LinkPlaceholderContext}.
     */
    @Override
    public LinkPlaceholderContext newMessageContext(Account account) {
        Preconditions.checkNotNull(account, "account must not be null");
        LinkPlaceholderContext context = new VKMessagePlaceholderContext(account);
        LOGGER.atFine().log("Created VK message context for account: %s", account.getPlayerId());
        return context;
    }
    // #endregion
}