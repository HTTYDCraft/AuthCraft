package com.httydcraft.authcraft.core.config.message.link;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.config.message.MessageContext;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.core.config.message.MessagesTemplate;

// #region Class Documentation
/**
 * Configuration for link messages.
 * Handles message retrieval and placeholder application with a custom delimiter.
 */
public class LinkMessages extends MessagesTemplate<String> {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final String delimiter;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code LinkMessages}.
     *
     * @param configurationSection The configuration section. Must not be null.
     * @param delimiter            The delimiter for joining messages. Must not be null.
     */
    public LinkMessages(ConfigurationSectionHolder configurationSection, String delimiter) {
        super(Preconditions.checkNotNull(delimiter, "delimiter must not be null"));
        this.delimiter = delimiter;
        initializeMessages(Preconditions.checkNotNull(configurationSection, "configurationSection must not be null"));
        LOGGER.atInfo().log("Initialized LinkMessages with delimiter: %s", delimiter);
    }
    // #endregion

    // #region Message Retrieval
    /**
     * Gets a message for the given key and applies the context.
     *
     * @param key     The message key. Must not be null.
     * @param context The message context. Must not be null.
     * @return The processed message.
     */
    @Override
    public String getMessage(String key, MessageContext context) {
        Preconditions.checkNotNull(key, "key must not be null");
        Preconditions.checkNotNull(context, "context must not be null");
        String message = context.apply(getMessage(key));
        LOGGER.atFine().log("Retrieved message for key: %s", key);
        return message;
    }

    /**
     * Creates a new messages instance for a configuration section.
     *
     * @param configurationSection The configuration section. Must not be null.
     * @return A new {@link Messages} instance.
     */
    @Override
    protected Messages<String> createMessages(ConfigurationSectionHolder configurationSection) {
        return new LinkMessages(configurationSection, delimiter);
    }

    /**
     * Converts text to the message type.
     *
     * @param text The text to convert. Must not be null.
     * @return The converted text.
     */
    @Override
    public String fromText(String text) {
        Preconditions.checkNotNull(text, "text must not be null");
        return text;
    }
    // #endregion
}