package com.httydcraft.authcraft.velocity.adventure;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

// #region Class Documentation
/**
 * Provides audience-based messaging for Velocity proxy using Kyori Adventure.
 * Implements {@link AudienceProvider} to manage audiences for players, console, and servers.
 */
public class VelocityAudienceProvider implements AudienceProvider {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ProxyServer server;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VelocityAudienceProvider}.
     *
     * @param server The Velocity proxy server instance. Must not be null.
     */
    public VelocityAudienceProvider(ProxyServer server) {
        this.server = Preconditions.checkNotNull(server, "server must not be null");
        LOGGER.atInfo().log("Initialized VelocityAudienceProvider");
    }
    // #endregion

    // #region Audience Methods
    /**
     * Returns an audience containing all connected players and the console.
     *
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience all() {
        LOGGER.atFine().log("Retrieved audience for all");
        return server.filterAudience(audience -> true);
    }

    /**
     * Returns an audience for the console.
     *
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience console() {
        LOGGER.atFine().log("Retrieved console audience");
        return server.filterAudience(audience -> audience instanceof ConsoleCommandSource);
    }

    /**
     * Returns an audience containing all connected players.
     *
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience players() {
        LOGGER.atFine().log("Retrieved players audience");
        return server.filterAudience(audience -> audience instanceof Player);
    }

    /**
     * Returns an audience for a specific player by their UUID.
     *
     * @param playerId The player's UUID. Must not be null.
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience player(@NotNull UUID playerId) {
        Preconditions.checkNotNull(playerId, "playerId must not be null");
        LOGGER.atFine().log("Retrieved audience for player UUID: %s", playerId);
        return server.filterAudience(audience -> {
            Optional<UUID> uniqueId = audience.get(Identity.UUID);
            return uniqueId.map(uuid -> uuid.equals(playerId)).orElse(false);
        });
    }

    /**
     * Returns an audience for players with the specified permission.
     *
     * @param permission The permission to check. Must not be null.
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience permission(@NotNull String permission) {
        Preconditions.checkNotNull(permission, "permission must not be null");
        LOGGER.atFine().log("Retrieved audience for permission: %s", permission);
        return server.filterAudience(audience -> {
            if (!(audience instanceof Player)) {
                return false;
            }
            Player player = (Player) audience;
            return player.hasPermission(permission);
        });
    }

    /**
     * Returns an audience for players in a specific world.
     * Currently unsupported in Velocity, always returns an empty audience.
     *
     * @param world The world key. Must not be null.
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience world(@NotNull Key world) {
        Preconditions.checkNotNull(world, "world must not be null");
        LOGGER.atFine().log("Retrieved world audience (unsupported): %s", world);
        return server.filterAudience(audience -> false);
    }

    /**
     * Returns an audience for players connected to a specific server.
     *
     * @param serverName The server name. Must not be null.
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience server(@NotNull String serverName) {
        Preconditions.checkNotNull(serverName, "serverName must not be null");
        LOGGER.atFine().log("Retrieved audience for server: %s", serverName);
        return server.filterAudience(audience -> {
            if (!(audience instanceof Player)) {
                return false;
            }
            Player player = (Player) audience;
            return player.getCurrentServer()
                    .map(ServerConnection::getServerInfo)
                    .map(ServerInfo::getName)
                    .map(name -> name.equals(serverName))
                    .orElse(false);
        });
    }
    // #endregion

    // #region Utility Methods
    /**
     * Returns the component flattener for text rendering.
     *
     * @return A non-null {@link ComponentFlattener}.
     */
    @Override
    public @NotNull ComponentFlattener flattener() {
        LOGGER.atFine().log("Retrieved component flattener");
        return ComponentFlattener.basic();
    }

    /**
     * Closes the audience provider.
     * No-op in this implementation.
     */
    @Override
    public void close() {
        LOGGER.atFine().log("Audience provider closed (no-op)");
    }
    // #endregion
}