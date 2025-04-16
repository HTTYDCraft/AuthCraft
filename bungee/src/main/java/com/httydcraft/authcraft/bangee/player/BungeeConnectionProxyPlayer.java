package com.httydcraft.authcraft.bangee.player;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.bangee.message.BungeeComponent;
import com.httydcraft.authcraft.api.server.message.SelfHandledServerComponent;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.server.proxy.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

// #region Class Documentation
/**
 * BungeeCord-specific implementation of a server player for pending connections.
 * Implements {@link ServerPlayer} to manage players during the login phase.
 */
public class BungeeConnectionProxyPlayer implements ServerPlayer {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final PendingConnection pendingConnection;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BungeeConnectionProxyPlayer}.
     *
     * @param pendingConnection The BungeeCord pending connection. Must not be null.
     */
    public BungeeConnectionProxyPlayer(@NotNull PendingConnection pendingConnection) {
        this.pendingConnection = Preconditions.checkNotNull(pendingConnection, "pendingConnection must not be null");
        LOGGER.atInfo().log("Initialized BungeeConnectionProxyPlayer for %s", pendingConnection.getName());
    }
    // #endregion

    // #region ServerPlayer Implementation
    /**
     * Checks if the player has the specified permission.
     *
     * @param permission The permission to check. Must not be null.
     * @return {@code false}, as pending connections cannot have permissions.
     */
    @Override
    public boolean hasPermission(@NotNull String permission) {
        Preconditions.checkNotNull(permission, "permission must not be null");
        LOGGER.atFine().log("Checked permission %s for pending connection: false", permission);
        return false;
    }

    /**
     * Disconnects the player with a reason.
     *
     * @param component The disconnect reason component. Must not be null.
     */
    @Override
    public void disconnect(@NotNull ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        LOGGER.atFine().log("Disconnecting pending connection: %s", pendingConnection.getName());
        if (component.safeAs(SelfHandledServerComponent.class).isPresent()) {
            disconnect(AuthPlugin.instance().getCore().componentJson(component.jsonText()));
            return;
        }
        pendingConnection.disconnect(component.as(BungeeComponent.class).components());
    }

    /**
     * Sends a message to the player (no-op for pending connections).
     *
     * @param component The message component to send. Must not be null.
     */
    @Override
    public void sendMessage(@NotNull ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        LOGGER.atFine().log("Skipping message send for pending connection: %s", pendingConnection.getName());
    }

    /**
     * Gets the player's nickname.
     *
     * @return The player's name.
     */
    @Override
    public String getNickname() {
        String result = pendingConnection.getName();
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
        UUID result = pendingConnection.getUniqueId();
        LOGGER.atFine().log("Retrieved UUID: %s", result);
        return result;
    }

    /**
     * Gets the player's IP address.
     *
     * @return The IP address.
     */
    @Override
    public String getPlayerIp() {
        String result = pendingConnection.getAddress().getAddress().getHostAddress();
        LOGGER.atFine().log("Retrieved IP address: %s", result);
        return result;
    }

    /**
     * Gets the player's current server (empty for pending connections).
     *
     * @return An empty optional.
     */
    @Override
    public Optional<ProxyServer> getCurrentServer() {
        LOGGER.atFine().log("No current server for pending connection, returning empty");
        return Optional.empty();
    }

    /**
     * Gets the underlying BungeeCord pending connection.
     *
     * @param <T> The type to cast to.
     * @return The real pending connection object.
     */
    @Override
    public <T> T getRealPlayer() {
        LOGGER.atFine().log("Retrieved real pending connection for %s", pendingConnection.getName());
        return (T) pendingConnection;
    }
    // #endregion
}