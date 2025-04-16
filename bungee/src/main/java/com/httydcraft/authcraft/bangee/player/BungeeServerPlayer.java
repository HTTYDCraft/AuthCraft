package com.httydcraft.authcraft.bangee.player;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.bangee.message.BungeeComponent;
import com.httydcraft.authcraft.bangee.server.BungeeServer;
import com.httydcraft.authcraft.api.server.message.SelfHandledServerComponent;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.server.proxy.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

// #region Class Documentation
/**
 * BungeeCord-specific implementation of a server player.
 * Implements {@link ServerPlayer} to manage connected players in BungeeCord.
 */
public class BungeeServerPlayer implements ServerPlayer {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ProxiedPlayer player;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BungeeServerPlayer}.
     *
     * @param player The BungeeCord player instance. Must not be null.
     */
    public BungeeServerPlayer(@NotNull ProxiedPlayer player) {
        this.player = Preconditions.checkNotNull(player, "player must not be null");
        LOGGER.atInfo().log("Initialized BungeeServerPlayer for %s", player.getName());
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
    public boolean hasPermission(@NotNull String permission) {
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
    public void disconnect(@NotNull ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        LOGGER.atFine().log("Disconnecting player: %s", player.getName());
        if (component.safeAs(SelfHandledServerComponent.class).isPresent()) {
            disconnect(AuthPlugin.instance().getCore().componentJson(component.jsonText()));
            return;
        }
        player.disconnect(component.as(BungeeComponent.class).components());
    }

    /**
     * Sends a message to the player.
     *
     * @param component The message component to send. Must not be null.
     */
    @Override
    public void sendMessage(@NotNull ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        LOGGER.atFine().log("Sending message to player: %s", player.getName());
        if (component.safeAs(SelfHandledServerComponent.class).isPresent()) {
            component.as(SelfHandledServerComponent.class).send(this);
            return;
        }
        player.sendMessage(component.as(BungeeComponent.class).components());
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
     * @return The IP address.
     */
    @Override
    public String getPlayerIp() {
        String result = player.getAddress().getAddress().getHostAddress();
        LOGGER.atFine().log("Retrieved IP address: %s", result);
        return result;
    }

    /**
     * Gets the player's current server.
     *
     * @return An optional {@link ProxyServer} representing the current server.
     */
    @Override
    public Optional<ProxyServer> getCurrentServer() {
        if (player.getServer() == null) {
            LOGGER.atFine().log("No current server for player: %s", player.getName());
            return Optional.empty();
        }
        LOGGER.atFine().log("Retrieved current server for player: %s", player.getName());
        return Optional.of(new BungeeServer(player.getServer().getInfo()));
    }

    /**
     * Gets the underlying BungeeCord player object.
     *
     * @param <T> The type to cast to.
     * @return The real player object.
     */
    @Override
    public <T> T getRealPlayer() {
        LOGGER.atFine().log("Retrieved real player for %s", player.getName());
        return (T) getBungeePlayer();
    }
    // #endregion

    // #region Utility Methods
    /**
     * Gets the BungeeCord player instance.
     *
     * @return The {@link ProxiedPlayer}.
     */
    public ProxiedPlayer getBungeePlayer() {
        return player;
    }
    // #endregion
}