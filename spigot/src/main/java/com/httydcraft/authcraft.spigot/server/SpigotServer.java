package com.httydcraft.authcraft.server;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.AuthPlugin;
import com.httydcraft.authcraft.player.SpigotServerPlayer;
import com.httydcraft.authcraft.server.player.ServerPlayer;
import com.httydcraft.authcraft.server.proxy.ProxyServer;
import org.bukkit.Bukkit;

import java.util.List;

// #region Class Documentation
/**
 * Spigot-specific implementation of a server.
 * Implements {@link ProxyServer} to manage players on the Minecraft server.
 */
public class SpigotServer implements ProxyServer {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #endregion

    // #region ProxyServer Implementation
    /**
     * Gets the server name.
     *
     * @return The server name.
     */
    @Override
    public String getServerName() {
        String result = Bukkit.getServer().getName();
        LOGGER.atFine().log("Retrieved server name: %s", result);
        return result;
    }

    /**
     * Sends players to this server (no-op in Spigot, players are already on the server).
     *
     * @param players The players to send. Must not be null.
     */
    @Override
    public void sendPlayer(ServerPlayer... players) {
        Preconditions.checkNotNull(players, "players must not be null");
        for (ServerPlayer player : players) {
            Preconditions.checkNotNull(player, "player in players array must not be null");
            LOGGER.atFine().log("Send player %s to server (no-op in Spigot)", player.getNickname());
        }
    }

    /**
     * Gets the list of players connected to this server.
     *
     * @return An immutable list of {@link ServerPlayer}.
     */
    @Override
    public List<ServerPlayer> getPlayers() {
        List<ServerPlayer> result = Bukkit.getOnlinePlayers().stream()
                .map(SpigotServerPlayer::new)
                .collect(ImmutableList.toImmutableList());
        LOGGER.atFine().log("Retrieved %d players from server", result.size());
        return result;
    }

    /**
     * Gets the number of players connected to this server.
     *
     * @return The player count.
     */
    @Override
    public int getPlayersCount() {
        int result = Bukkit.getOnlinePlayers().size();
        LOGGER.atFine().log("Retrieved player count: %d", result);
        return result;
    }

    /**
     * Checks if the server exists.
     *
     * @return {@code true} always, as the server exists in Spigot context.
     */
    @Override
    public boolean isExists() {
        LOGGER.atFine().log("Checked server existence: true");
        return true;
    }
    // #endregion
}