package com.httydcraft.authcraft.velocity.hooks.nanolimbo;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.velocity.VelocityAuthPluginBootstrap;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.hook.LimboPluginHook;
import com.httydcraft.authcraft.velocity.server.VelocityProxyServer;
import com.httydcraft.authcraft.core.hooks.nanolimbo.NanoLimboProvider;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import java.net.InetSocketAddress;

// #region Class Documentation
/**
 * Hook for integrating NanoLimbo with Velocity.
 * Implements {@link LimboPluginHook} to manage limbo servers for authentication.
 */
public class VelocityNanoLimboPluginHook implements LimboPluginHook {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final int[] limboPorts;
    private final ProxyServer proxyServer;
    private NanoLimboProvider provider;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VelocityNanoLimboPluginHook}.
     *
     * @param limboPorts The stream of available limbo ports. Must not be null.
     * @param proxyServer The Velocity proxy server instance. Must not be null.
     */
    public VelocityNanoLimboPluginHook(IntStream limboPorts, ProxyServer proxyServer) {
        this.limboPorts = Preconditions.checkNotNull(limboPorts, "limboPorts must not be null").toArray();
        this.proxyServer = Preconditions.checkNotNull(proxyServer, "proxyServer must not be null");
        if (canHook()) {
            this.provider = new VelocityNanoLimboProvider(proxyServer);
            proxyServer.getEventManager().register(VelocityAuthPluginBootstrap.getInstance(), this);
            LOGGER.atInfo().log("Initialized VelocityNanoLimboPluginHook with provider");
        } else {
            LOGGER.atWarning().log("NanoLimbo plugin not loaded, hook disabled");
        }
    }
    // #endregion

    // #region LimboPluginHook Implementation
    /**
     * Creates a new limbo server with the specified name.
     *
     * @param serverName The name of the server. Must not be null.
     * @return The created {@link com.httydcraft.authcraft.api.server.proxy.ProxyServer}.
     * @throws IllegalStateException If no available port is found.
     */
    @Override
    public com.httydcraft.authcraft.api.server.proxy.ProxyServer createServer(String serverName) {
        Preconditions.checkNotNull(serverName, "serverName must not be null");
        if (provider == null) {
            LOGGER.atSevere().log("Cannot create server: NanoLimbo provider not initialized");
            throw new IllegalStateException("NanoLimbo provider not initialized");
        }
        LOGGER.atFine().log("Creating limbo server: %s", serverName);
        InetSocketAddress address = provider.findAvailableAddress(limboPorts)
                .orElseThrow(() -> new IllegalStateException("Cannot find available port for limbo server!"));
        provider.createAndStartLimbo(address);
        return new VelocityProxyServer(proxyServer.registerServer(new ServerInfo(serverName, address)));
    }

    /**
     * Checks if the NanoLimbo plugin is available for hooking.
     *
     * @return {@code true} if the plugin is loaded, {@code false} otherwise.
     */
    @Override
    public boolean canHook() {
        boolean result = proxyServer.getPluginManager().isLoaded("nanolimbovelocity");
        LOGGER.atFine().log("Checked NanoLimbo hook availability: %b", result);
        return result;
    }
    // #endregion

    // #region Event Handlers
    /**
     * Handles the initial server selection for a player.
     *
     * @param event The player choose initial server event. Must not be null.
     */
    @Subscribe
    public void onServerChoose(PlayerChooseInitialServerEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        PluginConfig config = AuthPlugin.instance().getConfig();
        LOGGER.atFine().log("Handling server choose event for player");
        event.setInitialServer(config.findServerInfo(config.getAuthServers())
                .asProxyServer()
                .as(VelocityProxyServer.class)
                .getServer());
    }
    // #endregion
}