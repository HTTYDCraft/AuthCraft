package com.httydcraft.authcraft.core.server.commands.parameters;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.server.proxy.ProxyServer;

import java.util.Optional;
import java.util.UUID;

// #region Class Documentation
/**
 * Wrapper for a {@link ServerPlayer} used as a command parameter.
 * Delegates all operations to the underlying player instance.
 */
public class ArgumentServerPlayer implements ServerPlayer {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ServerPlayer player;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code ArgumentServerPlayer}.
     *
     * @param player The underlying player instance. Must not be null.
     */
    public ArgumentServerPlayer(ServerPlayer player) {
        this.player = Preconditions.checkNotNull(player, "player must not be null");
        LOGGER.atFine().log("Initialized ArgumentServerPlayer for player: %s", player.getNickname());
    }
    // #endregion

    // #region Player Operations
    /**
     * Checks if the player has a specific permission.
     *
     * @param permission The permission to check. Must not be null.
     * @return {@code true} if the player has the permission, {@code false} otherwise.
     */
    @Override
    public boolean hasPermission(String permission) {
        Preconditions.checkNotNull(permission, "permission must not be null");
        boolean hasPermission = player.hasPermission(permission);
        LOGGER.atFine().log("Checked permission %s for player %s: %b", permission, player.getNickname(), hasPermission);
        return hasPermission;
    }

    /**
     * Disconnects the player with a reason.
     *
     * @param reason The reason for disconnection. Must not be null.
     */
    @Override
    public void disconnect(String reason) {
        Preconditions.checkNotNull(reason, "reason must not be null");
        player.disconnect(reason);
        LOGGER.atInfo().log("Disconnected player %s with reason: %s", player.getNickname(), reason);
    }

    /**
     * Disconnects the player with a component message.
     *
     * @param component The component message. Must not be null.
     */
    @Override
    public void disconnect(ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        player.disconnect(component);
        LOGGER.atInfo().log("Disconnected player %s with component", player.getNickname());
    }

    /**
     * Sends a text message to the player.
     *
     * @param message The message to send. Must not be null.
     */
    @Override
    public void sendMessage(String message) {
        Preconditions.checkNotNull(message, "message must not be null");
        player.sendMessage(message);
        LOGGER.atFine().log("Sent message to player %s: %s", player.getNickname(), message);
    }

    /**
     * Sends a component message to the player.
     *
     * @param component The component to send. Must not be null.
     */
    @Override
    public void sendMessage(ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        player.sendMessage(component);
        LOGGER.atFine().log("Sent component to player %s", player.getNickname());
    }

    /**
     * Gets the player's nickname.
     *
     * @return The player's nickname.
     */
    @Override
    public String getNickname() {
        String nickname = player.getNickname();
        LOGGER.atFine().log("Retrieved nickname for player: %s", nickname);
        return nickname;
    }

    /**
     * Gets the player's unique ID.
     *
     * @return The player's {@link UUID}.
     */
    @Override
    public UUID getUniqueId() {
        UUID uuid = player.getUniqueId();
        LOGGER.atFine().log("Retrieved UUID for player: %s", uuid);
        return uuid;
    }

    /**
     * Gets the player's IP address.
     *
     * @return The player's IP address.
     */
    @Override
    public String getPlayerIp() {
        String ip = player.getPlayerIp();
        LOGGER.atFine().log("Retrieved IP for player %s: %s", player.getNickname(), ip);
        return ip;
    }

    /**
     * Gets the player's current server, if any.
     *
     * @return An {@link Optional} containing the {@link ProxyServer}, or empty if none.
     */
    @Override
    public Optional<ProxyServer> getCurrentServer() {
        Optional<ProxyServer> server = player.getCurrentServer();
        LOGGER.atFine().log("Retrieved current server for player %s: %s", player.getNickname(),
                server.map(s -> s.toString()).orElse("none"));
        return server;
    }

    /**
     * Gets the real player object.
     *
     * @param <T> The type of the real player.
     * @return The real player instance.
     */
    @Override
    public <T> T getRealPlayer() {
        T realPlayer = player.getRealPlayer();
        LOGGER.atFine().log("Retrieved real player for %s", player.getNickname());
        return realPlayer;
    }
    // #endregion
}