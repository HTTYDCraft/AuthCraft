package com.httydcraft.authcraft.bangee.server;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.bangee.player.BungeeServerPlayer;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.server.proxy.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

// #region Class Documentation
/**
 * BungeeCord-specific implementation of a proxy server.
 * Implements {@link ProxyServer} to manage server operations and player connections.
 */
public class BungeeServer implements ProxyServer {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ServerInfo bungeeServerInfo;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BungeeServer}.
     *
     * @param bungeeServerInfo The BungeeCord server info. Must not be null.
     */
    public BungeeServer(@NotNull ServerInfo bungeeServerInfo) {
        this.bungeeServerInfo = Preconditions.checkNotNull(bungeeServerInfo, "bungeeServerInfo must not be null");
        LOGGER.atInfo().log("Initialized BungeeServer for server: %s", bungeeServerInfo.getName());
    }
    // #endregion

    // #region ProxyServer Implementation
    /**
     * Gets the name of the server.
     *
     * @return The server name.
     */
    @Override
    public String getServerName() {
        String name = bungeeServerInfo.getName();
        LOGGER.atFine().log("Retrieved server name: %s", name);
        return name;
    }

    /**
     * Sends players to this server.
     *
     * @param players The players to send. Must not be null or contain null elements.
     */
    @Override
    public void sendPlayer(@NotNull ServerPlayer... players) {
        Preconditions.checkNotNull(players, "players must not be null");
        for (ServerPlayer player : players) {
            Preconditions.checkNotNull(player, "player in players array must not be null");
            ProxiedPlayer bungeePlayer = player.getRealPlayer();
            String currentServer = player.getCurrentServer().map(ProxyServer::getServerName).orElse(null);
            if (bungeeServerInfo.getName().equals(currentServer)) {
                LOGGER.atFine().log("Player %s already on server %s, skipping", player.getNickname(), currentServer);
                continue;
            }
            LOGGER.atFine().log("Sending player %s to server: %s", player.getNickname(), bungeeServerInfo.getName());
            bungeePlayer.connect(bungeeServerInfo, (result, exception) -> {
                if (exception != null) {
                    LOGGER.atWarning().withCause(exception).log("Failed to connect player %s to server %s",
                            player.getNickname(), bungeeServerInfo.getName());
                } else {
                    LOGGER.atFine().log("Successfully connected player %s to server %s",
                            player.getNickname(), bungeeServerInfo.getName());
                }
            });
        }
    }

    /**
     * Gets the list of players connected to this server.
     *
     * @return A list of {@link ServerPlayer} instances.
     */
    @Override
    public List<ServerPlayer> getPlayers() {
        List<ServerPlayer> players = bungeeServerInfo.getPlayers().stream()
                .map(BungeeServerPlayer::new)
                .collect(Collectors.toList());
        LOGGER.atFine().log("Retrieved %d players for server: %s", players.size(), bungeeServerInfo.getName());
        return players;
    }

    /**
     * Gets the number of players connected to this server.
     *
     * @return The player count.
     */
    @Override
    public int getPlayersCount() {
        int count = bungeeServerInfo.getPlayers().size();
        LOGGER.atFine().log("Retrieved player count %d for server: %s", count, bungeeServerInfo.getName());
        return count;
    }

    /**
     * Checks if the server exists.
     *
     * @return {@code true} if the server info is not null, {@code false} otherwise.
     */
    @Override
    public boolean isExists() {
        boolean exists = bungeeServerInfo != null;
        LOGGER.atFine().log("Checked server existence for %s: %b",
                bungeeServerInfo != null ? bungeeServerInfo.getName() : "null", exists);
        return exists;
    }
    // #endregion

    // #region Utility Methods
    /**
     * Gets the underlying BungeeCord server info.
     *
     * @return The {@link ServerInfo}.
     */
    public ServerInfo getServerInfo() {
        LOGGER.atFine().log("Retrieved server info for: %s", bungeeServerInfo.getName());
        return bungeeServerInfo;
    }
    // #endregion
}