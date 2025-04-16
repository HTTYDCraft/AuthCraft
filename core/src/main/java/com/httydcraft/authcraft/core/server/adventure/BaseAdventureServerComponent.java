package com.httydcraft.authcraft.core.server.adventure;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.server.message.AdventureServerComponent;
import com.httydcraft.authcraft.api.server.message.SelfHandledServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

// #region Class Documentation
/**
 * Base implementation of {@link AdventureServerComponent} and {@link SelfHandledServerComponent}.
 * Represents a text component using the Adventure API for messaging.
 */
public class BaseAdventureServerComponent implements SelfHandledServerComponent, AdventureServerComponent {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final Component component;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseAdventureServerComponent}.
     *
     * @param component The Adventure component. Must not be null.
     */
    public BaseAdventureServerComponent(Component component) {
        this.component = Preconditions.checkNotNull(component, "component must not be null");
        LOGGER.atFine().log("Initialized BaseAdventureServerComponent");
    }
    // #endregion

    // #region Text Serialization
    /**
     * Gets the component as JSON text.
     *
     * @return The JSON representation of the component.
     */
    @Override
    public String jsonText() {
        String json = GsonComponentSerializer.gson().serialize(component);
        LOGGER.atFine().log("Serialized component to JSON");
        return json;
    }

    /**
     * Gets the component as legacy text.
     *
     * @return The legacy text representation of the component.
     */
    @Override
    public String legacyText() {
        String legacy = LegacyComponentSerializer.legacySection().serialize(component);
        LOGGER.atFine().log("Serialized component to legacy text");
        return legacy;
    }

    /**
     * Gets the component as plain text.
     *
     * @return The plain text representation of the component.
     */
    @Override
    public String plainText() {
        String plain = PlainTextComponentSerializer.plainText().serialize(component);
        LOGGER.atFine().log("Serialized component to plain text");
        return plain;
    }
    // #endregion

    // #region Messaging
    /**
     * Sends the component to a player.
     *
     * @param player The player to send the component to. Must not be null.
     */
    @Override
    public void send(ServerPlayer player) {
        Preconditions.checkNotNull(player, "player must not be null");
        getAudience(player).sendMessage(component);
        LOGGER.atFine().log("Sent component to player: %s", player.getNickname());
    }
    // #endregion

    // #region Component Access
    /**
     * Gets the underlying Adventure component.
     *
     * @return The {@link Component}.
     */
    @Override
    public Component component() {
        LOGGER.atFine().log("Retrieved Adventure component");
        return component;
    }

    /**
     * Gets the underlying Adventure component (alias for {@link #component()}).
     *
     * @return The {@link Component}.
     */
    public Component getComponent() {
        LOGGER.atFine().log("Retrieved Adventure component via getComponent");
        return component;
    }
    // #endregion

    // #region Helper Methods
    /**
     * Gets the audience for a player.
     *
     * @param player The player to get the audience for. Must not be null.
     * @return The {@link Audience}.
     */
    private Audience getAudience(ServerPlayer player) {
        Preconditions.checkNotNull(player, "player must not be null");
        Audience audience = AuthPlugin.instance().getCore().getAudience(player);
        LOGGER.atFine().log("Retrieved audience for player: %s", player.getNickname());
        return audience;
    }
    // #endregion
}