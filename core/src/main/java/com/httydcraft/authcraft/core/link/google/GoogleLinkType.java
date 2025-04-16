package com.httydcraft.authcraft.core.link.google;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.link.LinkSettings;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.core.config.message.link.context.LinkPlaceholderContext;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.multimessenger.core.button.Button.ButtonBuilder;
import com.httydcraft.multimessenger.core.button.ButtonAction.ButtonActionBuilder;
import com.httydcraft.multimessenger.core.button.ButtonColor.ButtonColorBuilder;
import com.httydcraft.multimessenger.core.keyboard.Keyboard.KeyboardBuilder;
import com.httydcraft.multimessenger.core.message.Message.MessageBuilder;

import java.util.function.Predicate;

// #region Class Documentation
/**
 * Implementation of {@link LinkType} for Google integration.
 * Provides Google-specific configuration and messaging, with limited support for messenger features.
 */
public class GoogleLinkType implements LinkType {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final GoogleLinkType INSTANCE = new GoogleLinkType();
    public static final Predicate<LinkUser> LINK_USER_FILTER = linkUser -> linkUser.getLinkType() == getInstance();
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    private static final LinkUserIdentificator DEFAULT_IDENTIFICATOR = LinkUserIdentificator.of(null);
    private static final String CANNOT_CREATE_BUILDER_ERROR = "Cannot create builder with GoogleLinkType!";
    private static final String UNSUPPORTED_ERROR = "This method is not supported by Google link type";
    // #endregion

    // #region Constructor
    /**
     * Private constructor to enforce singleton pattern.
     */
    private GoogleLinkType() {
        LOGGER.atFine().log("Initialized GoogleLinkType singleton");
    }
    // #endregion

    // #region Singleton Access
    /**
     * Gets the singleton instance of {@code GoogleLinkType}.
     *
     * @return The {@link GoogleLinkType} instance.
     */
    public static GoogleLinkType getInstance() {
        LOGGER.atFine().log("Retrieved GoogleLinkType instance");
        return INSTANCE;
    }
    // #endregion

    // #region Builder Methods
    /**
     * Attempts to create a new message builder.
     *
     * @param text The message text. Must not be null.
     * @return Never returns; always throws {@link UnsupportedOperationException}.
     * @throws UnsupportedOperationException Always thrown as this operation is not supported.
     */
    @Override
    public MessageBuilder newMessageBuilder(String text) {
        Preconditions.checkNotNull(text, "text must not be null");
        LOGGER.atWarning().log("Attempted to create message builder for GoogleLinkType");
        throw new UnsupportedOperationException(CANNOT_CREATE_BUILDER_ERROR);
    }

    /**
     * Attempts to create a new button builder.
     *
     * @param label The button label. Must not be null.
     * @return Never returns; always throws {@link UnsupportedOperationException}.
     * @throws UnsupportedOperationException Always thrown as this operation is not supported.
     */
    @Override
    public ButtonBuilder newButtonBuilder(String label) {
        Preconditions.checkNotNull(label, "label must not be null");
        LOGGER.atWarning().log("Attempted to create button builder for GoogleLinkType");
        throw new UnsupportedOperationException(CANNOT_CREATE_BUILDER_ERROR);
    }

    /**
     * Attempts to create a new keyboard builder.
     *
     * @return Never returns; always throws {@link UnsupportedOperationException}.
     * @throws UnsupportedOperationException Always thrown as this operation is not supported.
     */
    @Override
    public KeyboardBuilder newKeyboardBuilder() {
        LOGGER.atWarning().log("Attempted to create keyboard builder for GoogleLinkType");
        throw new UnsupportedOperationException(CANNOT_CREATE_BUILDER_ERROR);
    }

    /**
     * Attempts to create a new button color builder.
     *
     * @return Never returns; always throws {@link UnsupportedOperationException}.
     * @throws UnsupportedOperationException Always thrown as this operation is not supported.
     */
    @Override
    public ButtonColorBuilder newButtonColorBuilder() {
        LOGGER.atWarning().log("Attempted to create button color builder for GoogleLinkType");
        throw new UnsupportedOperationException(CANNOT_CREATE_BUILDER_ERROR);
    }

    /**
     * Attempts to create a new button action builder.
     *
     * @return Never returns; always throws {@link UnsupportedOperationException}.
     * @throws UnsupportedOperationException Always thrown as this operation is not supported.
     */
    @Override
    public ButtonActionBuilder newButtonActionBuilder() {
        LOGGER.atWarning().log("Attempted to create button action builder for GoogleLinkType");
        throw new UnsupportedOperationException(CANNOT_CREATE_BUILDER_ERROR);
    }
    // #endregion

    // #region Messaging
    /**
     * Gets the server messages for Google.
     *
     * @return The {@link Messages} containing {@link ServerComponent}s.
     */
    @Override
    public Messages<ServerComponent> getServerMessages() {
        Messages<ServerComponent> messages = PLUGIN.getConfig().getServerMessages().getSubMessages("google");
        LOGGER.atFine().log("Retrieved Google server messages");
        return messages;
    }

    /**
     * Attempts to get the link messages for Google.
     *
     * @return Never returns; always throws {@link UnsupportedOperationException}.
     * @throws UnsupportedOperationException Always thrown as this operation is not supported.
     */
    @Override
    public Messages<String> getLinkMessages() {
        LOGGER.atWarning().log("Attempted to retrieve link messages for GoogleLinkType");
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR);
    }
    // #endregion

    // #region Configuration
    /**
     * Attempts to get the Google link settings.
     *
     * @return Never returns; always throws {@link UnsupportedOperationException}.
     * @throws UnsupportedOperationException Always thrown as this operation is not supported.
     */
    @Override
    public LinkSettings getSettings() {
        LOGGER.atWarning().log("Attempted to retrieve settings for GoogleLinkType");
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR);
    }
    // #endregion

    // #region Identification
    /**
     * Gets the default user identifier for Google.
     *
     * @return The {@link LinkUserIdentificator}.
     */
    @Override
    public LinkUserIdentificator getDefaultIdentificator() {
        LOGGER.atFine().log("Retrieved default Google identifier: %s", DEFAULT_IDENTIFICATOR);
        return DEFAULT_IDENTIFICATOR;
    }

    /**
     * Gets the name of the link type.
     *
     * @return The name ("GOOGLE").
     */
    @Override
    public String getName() {
        LOGGER.atFine().log("Retrieved Google link type name: GOOGLE");
        return "GOOGLE";
    }
    // #endregion

    // #region Message Context
    /**
     * Attempts to create a new message context for Google.
     *
     * @param account The account to create context for. Must not be null.
     * @return Never returns; always throws {@link UnsupportedOperationException}.
     * @throws UnsupportedOperationException Always thrown as this operation is not supported.
     */
    @Override
    public LinkPlaceholderContext newMessageContext(Account account) {
        Preconditions.checkNotNull(account, "account must not be null");
        LOGGER.atWarning().log("Attempted to create message context for GoogleLinkType");
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR);
    }
    // #endregion
}