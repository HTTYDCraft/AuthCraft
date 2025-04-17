package com.httydcraft.authcraft.core.config.message;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.config.message.MessageContext;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

import java.util.HashMap;
import java.util.Map;

// #region Class Documentation
/**
 * Abstract template for message configurations.
 * Manages messages and sub-messages with customizable delimiters.
 *
 * @param <T> The type of message output.
 */
public abstract class MessagesTemplate<T> implements Messages<T>, ConfigurationHolder {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String DEFAULT_DELIMITER = "\n";
    private final CharSequence delimiter;
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, Messages<T>> subMessages = new HashMap<>();
    private String nullMessage = "Message with key %s not found!";
    // #endregion

    // #region Constructors
    /**
     * Constructs a new {@code MessagesTemplate} with a custom delimiter and null message.
     *
     * @param delimiter   The delimiter for joining messages. Must not be null.
     * @param nullMessage The default message for missing keys. May be null.
     */
    public MessagesTemplate(CharSequence delimiter, String nullMessage) {
        this.delimiter = Preconditions.checkNotNull(delimiter, "delimiter must not be null");
        this.nullMessage = nullMessage;
        LOGGER.atFine().log("Initialized MessagesTemplate with delimiter: %s", delimiter);
    }

    /**
     * Constructs a new {@code MessagesTemplate} with a custom delimiter.
     *
     * @param delimiter The delimiter for joining messages. Must not be null.
     */
    public MessagesTemplate(CharSequence delimiter) {
        this.delimiter = Preconditions.checkNotNull(delimiter, "delimiter must not be null");
        LOGGER.atFine().log("Initialized MessagesTemplate with delimiter: %s", delimiter);
    }
    // #endregion

    // #region Message Initialization
    /**
     * Initializes messages from a configuration section.
     *
     * @param sectionHolder The configuration section. Must not be null.
     */
    protected void initializeMessages(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        for (String key : sectionHolder.keys()) {
            if (sectionHolder.isList(key)) {
                addMessage(key, String.join(delimiter, sectionHolder.getList(key).toArray(new String[0])));
                LOGGER.atFine().log("Added list message for key: %s", key);
                continue;
            }
            if (sectionHolder.isSection(key)) {
                subMessages.put(key, createMessages(sectionHolder.section(key)));
                LOGGER.atFine().log("Added sub-messages for key: %s", key);
                continue;
            }
            addMessage(key, sectionHolder.getString(key));
            LOGGER.atFine().log("Added single message for key: %s", key);
        }
    }
    // #endregion

    // #region Message Retrieval
    /**
     * Gets sub-messages for a given key.
     *
     * @param key The sub-messages key. Must not be null.
     * @return The {@link Messages} instance, or {@code null} if not found.
     */
    @Override
    public Messages<T> getSubMessages(String key) {
        Preconditions.checkNotNull(key, "key must not be null");
        Messages<T> sub = subMessages.getOrDefault(key, null);
        LOGGER.atFine().log("Retrieved sub-messages for key: %s, found: %b", key, sub != null);
        return sub;
    }

    /**
     * Gets a string message for a given key.
     *
     * @param key The message key. Must not be null.
     * @return The message, or a formatted null message if not found.
     */
    @Override
    public String getStringMessage(String key) {
        Preconditions.checkNotNull(key, "key must not be null");
        if (nullMessage == null) {
            return getStringMessage(key, (String) null);
        }
        String message = getStringMessage(key, String.format(nullMessage, key));
        LOGGER.atFine().log("Retrieved string message for key: %s", key);
        return message;
    }

    /**
     * Gets a string message with a default value.
     *
     * @param key          The message key. Must not be null.
     * @param defaultValue The default value. May be null.
     * @return The message, or the default value if not found.
     */
    @Override
    public String getStringMessage(String key, String defaultValue) {
        Preconditions.checkNotNull(key, "key must not be null");
        String message = messages.getOrDefault(key, defaultValue);
        LOGGER.atFine().log("Retrieved string message for key: %s, default: %s", key, defaultValue);
        return message;
    }

    /**
     * Gets a message with context applied.
     *
     * @param key     The message key. Must not be null.
     * @param context The message context. Must not be null.
     * @return The processed message.
     */
    @Override
    public T getMessage(String key, MessageContext context) {
        Preconditions.checkNotNull(key, "key must not be null");
        Preconditions.checkNotNull(context, "context must not be null");
        T message = fromText(context.apply(getStringMessage(key)));
        LOGGER.atFine().log("Retrieved message for key: %s with context", key);
        return message;
    }

    /**
     * Gets a message without context.
     *
     * @param key The message key. Must not be null.
     * @return The message, or a null message if not found.
     */
    @Override
    public T getMessage(String key) {
        Preconditions.checkNotNull(key, "key must not be null");
        String message = getStringMessage(key, Messages.NULL_STRING);
        T result = fromText(message);
        LOGGER.atFine().log("Retrieved message for key: %s", key);
        return result;
    }
    // #endregion

    // #region Message Management
    /**
     * Adds a message to the configuration.
     *
     * @param path    The message key. Must not be null.
     * @param message The message content. Must not be null.
     */
    public void addMessage(String path, String message) {
        Preconditions.checkNotNull(path, "path must not be null");
        Preconditions.checkNotNull(message, "message must not be null");
        String formattedMessage = formatString(message);
        messages.put(path, formattedMessage);
        LOGGER.atFine().log("Added message for path: %s", path);
    }

    /**
     * Formats a message string (default implementation returns the input).
     *
     * @param message The message to format.
     * @return The formatted message.
     */
    public String formatString(String message) {
        return message;
    }
    // #endregion

    // #region Abstract Method
    /**
     * Creates a new messages instance for a configuration section.
     *
     * @param configurationSection The configuration section. Must not be null.
     * @return A new {@link Messages} instance.
     */
    protected abstract Messages<T> createMessages(ConfigurationSectionHolder configurationSection);
    // #endregion
}