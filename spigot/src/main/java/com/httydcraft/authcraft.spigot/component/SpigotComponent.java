package com.httydcraft.authcraft.component;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.server.message.AdventureServerComponent;
import com.httydcraft.authcraft.server.message.ServerComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

// #region Class Documentation
/**
 * Spigot-specific implementation of a server component.
 * Implements {@link ServerComponent} and {@link AdventureServerComponent} to handle text serialization.
 */
public class SpigotComponent implements ServerComponent, AdventureServerComponent {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final Component component;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code SpigotComponent}.
     *
     * @param component The Adventure component to wrap. Must not be null.
     */
    public SpigotComponent(Component component) {
        this.component = Preconditions.checkNotNull(component, "component must not be null");
        LOGGER.atInfo().log("Initialized SpigotComponent");
    }
    // #endregion

    // #region Serialization Methods
    /**
     * Serializes the component to JSON format.
     *
     * @return The JSON string representation.
     */
    @Override
    public String jsonText() {
        String result = GsonComponentSerializer.gson().serialize(component);
        LOGGER.atFine().log("Serialized component to JSON: %s", result);
        return result;
    }

    /**
     * Serializes the component to legacy text format.
     *
     * @return The legacy text representation.
     */
    @Override
    public String legacyText() {
        String result = LegacyComponentSerializer.legacyAmpersand().serialize(component);
        LOGGER.atFine().log("Serialized component to legacy text: %s", result);
        return result;
    }

    /**
     * Serializes the component to plain text format.
     *
     * @return The plain text representation.
     */
    @Override
    public String plainText() {
        String result = PlainTextComponentSerializer.plainText().serialize(component);
        LOGGER.atFine().log("Serialized component to plain text: %s", result);
        return result;
    }

    /**
     * Returns the underlying Adventure component.
     *
     * @return The {@link Component}.
     */
    @Override
    public Component component() {
        LOGGER.atFine().log("Retrieved Adventure component");
        return component;
    }
    // #endregion
}