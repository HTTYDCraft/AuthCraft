package com.httydcraft.authcraft.core.config.message.server;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.message.MessageContext;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.api.config.message.server.ServerMessages;
import com.httydcraft.authcraft.core.config.message.MessagesTemplate;
import com.httydcraft.authcraft.core.server.adventure.BaseComponentDeserializer;
import com.httydcraft.authcraft.api.server.message.SelfHandledServerComponent;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// #region Class Documentation
/**
 * Configuration for server messages.
 * Manages server component messages with caching and deserialization.
 */
public class BaseServerMessages extends MessagesTemplate<ServerComponent> implements ServerMessages {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    private static final Pattern INNER_DESERIALIZER_PATTERN = Pattern.compile("\\[([^]\\s]+)]");
    private final Map<String, ServerComponent> componentCachedMessages = new HashMap<>();

    @ConfigField("deserializer")
    private BaseComponentDeserializer deserializer = BaseComponentDeserializer.LEGACY_AMPERSAND;
    // #endregion

    // #region Constructors
    /**
     * Constructs a new {@code BaseServerMessages}.
     *
     * @param configurationSection The configuration section. Must not be null.
     */
    public BaseServerMessages(ConfigurationSectionHolder configurationSection) {
        super(DEFAULT_DELIMITER, null);
        initializeMessages(Preconditions.checkNotNull(configurationSection, "configurationSection must not be null"));
        PLUGIN.getConfigurationProcessor().resolve(configurationSection, this);
        LOGGER.atInfo().log("Initialized BaseServerMessages");
    }

    /**
     * Constructs a new {@code BaseServerMessages} with a specific deserializer.
     *
     * @param configurationSection The configuration section. Must not be null.
     * @param deserializer         The component deserializer. Must not be null.
     */
    public BaseServerMessages(ConfigurationSectionHolder configurationSection, BaseComponentDeserializer deserializer) {
        super(DEFAULT_DELIMITER, null);
        this.deserializer = Preconditions.checkNotNull(deserializer, "deserializer must not be null");
        initializeMessages(Preconditions.checkNotNull(configurationSection, "configurationSection must not be null"));
        LOGGER.atInfo().log("Initialized BaseServerMessages with custom deserializer");
    }
    // #endregion

    // #region Message Retrieval
    /**
     * Gets a message for the given key with context applied.
     *
     * @param key     The message key. Must not be null.
     * @param context The message context. Must not be null.
     * @return The {@link ServerComponent} message, or a null component if not found.
     */
    @Override
    public ServerComponent getMessage(String key, MessageContext context) {
        Preconditions.checkNotNull(key, "key must not be null");
        Preconditions.checkNotNull(context, "context must not be null");
        String message = getStringMessage(key);
        if (message == null) {
            LOGGER.atFine().log("No message found for key: %s", key);
            return SelfHandledServerComponent.NULL_COMPONENT;
        }
        ServerComponent component = fromText(context.apply(message));
        LOGGER.atFine().log("Retrieved message for key: %s", key);
        return component;
    }

    /**
     * Gets a cached message for the given key.
     *
     * @param key The message key. Must not be null.
     * @return The {@link ServerComponent} message, or a null component if not found.
     */
    @Override
    public ServerComponent getMessage(String key) {
        Preconditions.checkNotNull(key, "key must not be null");
        String message = getStringMessage(key);
        if (message == null) {
            LOGGER.atFine().log("No message found for key: %s", key);
            return SelfHandledServerComponent.NULL_COMPONENT;
        }
        if (componentCachedMessages.containsKey(key)) {
            LOGGER.atFine().log("Retrieved cached message for key: %s", key);
            return componentCachedMessages.get(key);
        }
        ServerComponent result = fromText(message);
        componentCachedMessages.put(key, result);
        LOGGER.atFine().log("Cached new message for key: %s", key);
        return result;
    }

    /**
     * Converts text to a server component.
     *
     * @param text The text to convert. May be null.
     * @return The {@link ServerComponent}, or a null component if text is null.
     */
    @Override
    public ServerComponent fromText(String text) {
        if (text == null) {
            LOGGER.atFine().log("Null text provided for deserialization");
            return SelfHandledServerComponent.NULL_COMPONENT;
        }
        Matcher innerDeserializerMatcher = INNER_DESERIALIZER_PATTERN.matcher(text.trim());
        if (innerDeserializerMatcher.find()) {
            String deserializerName = innerDeserializerMatcher.group(1);
            String cleanedText = text.replaceFirst(INNER_DESERIALIZER_PATTERN.pattern(), "");
            ServerComponent component = BaseComponentDeserializer.findWithName(deserializerName)
                    .map(foundDeserializer -> foundDeserializer.deserialize(cleanedText))
                    .orElseGet(() -> deserializer.deserialize(text));
            LOGGER.atFine().log("Deserialized text with inner deserializer: %s", deserializerName);
            return component;
        }
        ServerComponent component = deserializer.deserialize(text);
        LOGGER.atFine().log("Deserialized text with default deserializer");
        return component;
    }

    /**
     * Creates a new messages instance for a configuration section.
     *
     * @param configurationSection The configuration section. Must not be null.
     * @return A new {@link Messages} instance.
     */
    @Override
    protected Messages<ServerComponent> createMessages(ConfigurationSectionHolder configurationSection) {
        return new BaseServerMessages(configurationSection, deserializer);
    }
    // #endregion

    // #region Getter
    /**
     * Gets the component deserializer.
     *
     * @return The {@link BaseComponentDeserializer}.
     */
    @Override
    public BaseComponentDeserializer getDeserializer() {
        return deserializer;
    }
    // #endregion
}