package com.httydcraft.authcraft.player;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.AuthPlugin;
import com.httydcraft.authcraft.server.message.AdventureServerComponent;
import com.httydcraft.authcraft.server.message.SelfHandledServerComponent;
import com.httydcraft.authcraft.server.message.ServerComponent;
import com.httydcraft.authcraft.server.player.ServerPlayer;
import com.httydcraft.authcraft.server.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

// #region Class Documentation
/**
 * Spigot-specific implementation of a server player.
 * Implements {@link ServerPlayer} to manage player interactions in Spigot.
 */
public class SpigotServerPlayer implements ServerPlayer {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final Player player;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code SpigotServerPlayer}.
     *
     * @param player The Spigot player instance. Must not be null.
     */
    public SpigotServerPlayer(Player player) {
        this.player = Preconditions.checkNotNull(player, "player must not be null");
        LOGGER.atInfo().log("Initialized SpigotServerPlayer for %s", player.getName());
    }
    // #endregion

    // #region ServerPlayer Implementation
    /**
     * Checks if the player has the specified permission.
     *
     * @param permission The permission to check. Must not be null.
     * @return {@code true} if the player has the permission, {@code false} otherwise.
     */
    @Override
    public boolean hasPermission(String permission) {
        Preconditions.checkNotNull(permission, "permission must not be null");
        boolean result = player.hasPermission(permission);
        LOGGER.atFine().log("Checked permission %s for player %s: %b", permission, player.getName(), result);
        return result;
    }

    /**
     * Disconnects the player with a reason.
     *
     * @param component The disconnect reason component. Must not be null.
     */
    @Override
    public void disconnect(ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        LOGGER.atFine().log("Disconnecting player %s", player.getName());
        if (component.safeAs(SelfHandledServerComponent.class).isPresent()) {
            disconnect(AuthPlugin.instance().getCore().componentJson(component.jsonText()));
            return;
        }
        Optional<Component> optionalComponent = component.safeAs(AdventureServerComponent.class)
                .map(AdventureServerComponent::component);
        if (!optionalComponent.isPresent()) {
            LOGGER.atSevere().log("Cannot retrieve Kyori Component from: %s", component.getClass().getSimpleName());
            throw new UnsupportedOperationException("Cannot retrieve Kyori Component from: " +
                    component.getClass().getSimpleName() + ", " + component);
        }
        player.kick(optionalComponent.get());
    }

    /**
     * Sends a message to the player.
     *
     * @param component The message component to send. Must not be null.
     */
    @Override
    public void sendMessage(ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        LOGGER.atFine().log("Sending message to player %s", player.getName());
        if (component.safeAs(SelfHandledServerComponent.class).isPresent()) {
            component.as(SelfHandledServerComponent.class).send(this);
            return;
        }
        Optional<Component> optionalComponent = component.safeAs(AdventureServerComponent.class)
                .map(AdventureServerComponent::component);
        if (!optionalComponent.isPresent()) {
            LOGGER.atSevere().log("Cannot retrieve Kyori Component from: %s", component.getClass().getSimpleName());
            throw new UnsupportedOperationException("Cannot retrieve Kyori Component from: " +
                    component.getClass().getSimpleName() + ", " + component);
        }
        Bukkit.getServer().adventure().player(player).sendMessage(optionalComponent.get());
    }

    /**
     * Gets the player's nickname.
     *
     * @return The player's name.
     */
    @Override
    public String getNickname() {
        String result = player.getName();
        LOGGER.atFine().log("Retrieved nickname: %s", result);
        return result;
    }

    /**
     * Gets the player's unique ID.
     *
     * @return The player's UUID.
     */
    @Override
    public UUID getUniqueId() {
        UUID result = player.getUniqueId();
        LOGGER.atFine().log("Retrieved UUID: %s", result);
        return result;
    }

    /**
     * Gets the player's IP address.
     *
     * @return The IP address, or null if unavailable.
     */
    @Override
    public String getPlayerIp() {
        String result = Optional.ofNullable(player.getAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse(null);
        LOGGER.atFine().log("Retrieved IP address: %s", result);
        return result;
    }

    /**
     * Gets the player's current server (not applicable in Spigot, returns empty).
     *
     * @return An empty optional.
     */
    @Override
    public Optional<ProxyServer> getCurrentServer() {
        LOGGER.atFine().log("Server not applicable in Spigot, returning empty");
        return Optional.empty();
    }

    /**
     * Gets the underlying Spigot player object.
     *
     * @param <T> The type to cast to.
     * @return The real player object.
     */
    @Override
    public <T> T getRealPlayer() {
        LOGGER.atFine().log("Retrieved real player for %s", player.getName());
        return (T) getPlayer();
    }
    // #endregion

    // #region Utility Methods
    /**
     * Gets the Spigot player instance.
     *
     * @return The {@link Player}.
     */
    public Player getPlayer() {
        return player;
    }
    // #endregion
}