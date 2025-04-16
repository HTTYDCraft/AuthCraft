package com.httydcraft.authcraft.core.config.server;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.server.ConfigurationServer;
import com.httydcraft.authcraft.api.server.proxy.ProxyServer;

// #region Class Documentation
/**
 * Configuration for a server.
 * Defines server ID and maximum player count.
 */
public class BaseConfigurationServer implements ConfigurationServer {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final String id;
    private final int maxPlayers;
    // #endregion

    // #region Constructors
    /**
     * Constructs a new {@code BaseConfigurationServer} from a string format.
     *
     * @param stringFormat The format string (id:maxPlayers). Must not be null.
     */
    public BaseConfigurationServer(String stringFormat) {
        Preconditions.checkNotNull(stringFormat, "stringFormat must not be null");
        String[] args = stringFormat.split(":");
        this.id = Preconditions.checkNotNull(args[0], "Server ID must not be null");
        this.maxPlayers = args.length >= 2 ? Integer.parseInt(args[1]) : Integer.MAX_VALUE;
        LOGGER.atInfo().log("Initialized BaseConfigurationServer with ID: %s, maxPlayers: %d", id, maxPlayers);
    }

    /**
     * Constructs a new {@code BaseConfigurationServer} with explicit values.
     *
     * @param id         The server ID. Must not be null.
     * @param maxPlayers The maximum number of players.
     */
    public BaseConfigurationServer(String id, int maxPlayers) {
        this.id = Preconditions.checkNotNull(id, "id must not be null");
        this.maxPlayers = maxPlayers;
        LOGGER.atInfo().log("Initialized BaseConfigurationServer with ID: %s, maxPlayers: %d", id, maxPlayers);
    }
    // #endregion

    // #region Getters
    /**
     * Converts this configuration to a proxy server.
     *
     * @return The {@link ProxyServer}.
     * @throws NullPointerException if the server does not exist.
     */
    @Override
    public ProxyServer asProxyServer() {
        ProxyServer server = AuthPlugin.instance().getCore().serverFromName(id)
                .orElseThrow(() -> {
                    LOGGER.atSevere().log("Server with name %s does not exist", id);
                    return new NullPointerException("Server with name " + id + " not exists!");
                });
        LOGGER.atFine().log("Converted to ProxyServer: %s", id);
        return server;
    }

    /**
     * Gets the server ID.
     *
     * @return The server ID.
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Gets the maximum number of players.
     *
     * @return The maximum player count.
     */
    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }
    // #endregion
}