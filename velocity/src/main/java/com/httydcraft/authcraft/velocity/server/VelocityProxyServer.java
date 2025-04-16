package com.httydcraft.authcraft.velocity.server;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.velocity.player.VelocityServerPlayer;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.server.proxy.ProxyServer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitywired.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import java.util.List;

// #region Class Documentation
/**
 * Velocity-specific implementation of a proxy server.
 * Implements {@link ProxyServer} to manage server connections and players.
 */
public class VelocityProxyServer implements ProxyServer {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final RegisteredServer registeredServer;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VelocityProxyServer}.
     *
     * @param registeredServer The Velocity registered server. Must not be null.
     */
    public VelocityProxyServer(RegisteredServer registeredServer) {
        this.registeredServer = Preconditions.checkNotNull(registeredServer, "registeredServer must not be null");
        LOGGER.atInfo().log("Initialized VelocityProxyServer for %s", registeredServer.getServerInfo().getName());
    }
    // #endregion

    // #region ProxyServer Implementation
    /**
     * Gets the server name.
     *
     * @return The server name.
     */
    @Override
    public String getServerName() {
        String result = registeredServer.getServerInfo().getName();
        LOGGER.atFine().log("Retrieved server name: %s", result);
        return result;
    }

    /**
     * Sends players to this server.
     *
     * @param players The players to send. Must not be null.
     */
    @Override
    public void sendPlayer(ServerPlayer... players) {
        Preconditions.checkNotNull(players, "players must not be null");
        for (ServerPlayer player : players) {
            Preconditions.checkNotNull(player, "player in players array must not be null");
            Player proxyPlayer = player.as(VelocityServerPlayer.class).getPlayer();
            String currentServerName = proxyPlayer.getCurrentServer()
                    .map(ServerConnection::getServerInfo)
                    .map(ServerInfo::getName)
                    .orElse(null);
            if (registeredServer.getServerInfo().getName().equals(currentServerName)) {
                LOGGER.atFine().log("Player %s already on server %s, skipping", proxyPlayer.getUsername(), currentServerName);
                continue;
            }
            LOGGER.atFine().log("Sending player %s to server %s", proxyPlayer.getUsername(), registeredServer.getServerInfo().getName());
            proxyPlayer.createConnectionRequest(registeredServer).connect();
        }
    }

    /**
     * Gets the list of players connected to this server.
     *
     * @return An immutable list of {@link ServerPlayer}.
     */
    @Override
    public List<ServerPlayer> getPlayers() {
        List<ServerPlayer> result = registeredServer.getPlayersConnected()
                .stream()
                .map(AuthPlugin.instance().getCore()::wrapPlayer)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableList.toImmutableList());
        LOGGER.atFine().log("Retrieved %d players from server %s", result.size(), registeredServer.getServerInfo().getName());
        return result;
    }

    /**
     * Gets the number of players connected to this server.
     *
     * @return The player count.
     */
    @Override
    public int getPlayersCount() {
        int result = registeredServer.getPlayersConnected().size();
        LOGGER.atFine().log("Retrieved player count: %d for server %s", result, registeredServer.getServerInfo().getName());
        return result;
    }

    /**
     * Checks if the server exists.
     *
     * @return {@code true} if the server exists, {@code false} otherwise.
     */
    @Override
    public boolean isExists() {
        boolean result = registeredServer != null;
        LOGGER.atFine().log("Checked server existence: %b", result);
        return result;
    }
    // #endregion

    // #region Utility Methods
    /**
     * Gets the underlying Velocity registered server.
     *
     * @return The {@link RegisteredServer}.
     */
    public RegisteredServer getServer() {
        LOGGER.atFine().log("Retrieved registered server");
        return registeredServer;
    }
    // #endregion
}