package com.httydcraft.authcraft.core.server.adventure;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.config.message.server.ComponentDeserializer;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Arrays;
import java.util.Optional;

// #region Enum Documentation
/**
 * Enum implementing {@link ComponentDeserializer} for deserializing text into {@link ServerComponent}s.
 * Supports multiple serialization formats using Adventure API.
 */
public enum BaseComponentDeserializer implements ComponentDeserializer {
    // #region Enum Values
    /**
     * Deserializes plain text.
     */
    PLAIN {
        private final PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

        @Override
        public ServerComponent deserialize(String text) {
            Preconditions.checkNotNull(text, "text must not be null");
            ServerComponent component = new BaseAdventureServerComponent(serializer.deserialize(text));
            LOGGER.atFine().log("Deserialized PLAIN text: %s", text);
            return component;
        }
    },
    /**
     * Deserializes JSON text using Gson.
     */
    GSON {
        private final GsonComponentSerializer serializer = GsonComponentSerializer.gson();

        @Override
        public ServerComponent deserialize(String text) {
            Preconditions.checkNotNull(text, "text must not be null");
            ServerComponent component = new BaseAdventureServerComponent(serializer.deserialize(text));
            LOGGER.atFine().log("Deserialized GSON text: %s", text);
            return component;
        }
    },
    /**
     * Deserializes legacy JSON text using Gson with color downsampling.
     */
    GSON_LEGACY {
        private final GsonComponentSerializer serializer = GsonComponentSerializer.colorDownsamplingGson();

        @Override
        public ServerComponent deserialize(String text) {
            Preconditions.checkNotNull(text, "text must not be null");
            ServerComponent component = new BaseAdventureServerComponent(serializer.deserialize(text));
            LOGGER.atFine().log("Deserialized GSON_LEGACY text: %s", text);
            return component;
        }
    },
    /**
     * Deserializes legacy text with ampersand (&) color codes.
     */
    LEGACY_AMPERSAND {
        private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand()
                .toBuilder().hexColors().build();

        @Override
        public ServerComponent deserialize(String text) {
            Preconditions.checkNotNull(text, "text must not be null");
            ServerComponent component = new BaseAdventureServerComponent(serializer.deserialize(text));
            LOGGER.atFine().log("Deserialized LEGACY_AMPERSAND text: %s", text);
            return component;
        }
    },
    /**
     * Deserializes legacy text with section (§) color codes.
     */
    LEGACY_SECTION {
        private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection()
                .toBuilder().hexColors().build();

        @Override
        public ServerComponent deserialize(String text) {
            Preconditions.checkNotNull(text, "text must not be null");
            ServerComponent component = new BaseAdventureServerComponent(serializer.deserialize(text));
            LOGGER.atFine().log("Deserialized LEGACY_SECTION text: %s", text);
            return component;
        }
    },
    /**
     * Deserializes MiniMessage formatted text.
     */
    MINIMESSAGE {
        private final MiniMessage serializer = MiniMessage.miniMessage();

        @Override
        public ServerComponent deserialize(String text) {
            Preconditions.checkNotNull(text, "text must not be null");
            ServerComponent component = new BaseAdventureServerComponent(serializer.deserialize(text));
            LOGGER.atFine().log("Deserialized MINIMESSAGE text: %s", text);
            return component;
        }
    };
    // #endregion

    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #region Utility Methods
    /**
     * Finds a deserializer by name.
     *
     * @param name The name of the deserializer. Must not be null.
     * @return An {@link Optional} containing the deserializer, or empty if not found.
     */
    public static Optional<BaseComponentDeserializer> findWithName(String name) {
        Preconditions.checkNotNull(name, "name must not be null");
        Optional<BaseComponentDeserializer> deserializer = Arrays.stream(values())
                .filter(value -> value.name().equals(name))
                .findFirst();
        LOGGER.atFine().log("Searched for deserializer: %s, found: %b", name, deserializer.isPresent());
        return deserializer;
    }
    // #endregion

    // #region Abstract Method
    /**
     * Deserializes text into a {@link ServerComponent}.
     *
     * @param text The text to deserialize. Must not be null.
     * @return The deserialized {@link ServerComponent}.
     */
    @Override
    public abstract ServerComponent deserialize(String text);
    // #endregion
}