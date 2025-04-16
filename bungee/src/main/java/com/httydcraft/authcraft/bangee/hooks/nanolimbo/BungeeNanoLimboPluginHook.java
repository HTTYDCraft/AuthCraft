package com.httydcraft.authcraft.bangee.hooks.nanolimbo;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.bangee.BungeeAuthPluginBootstrap;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.hook.LimboPluginHook;
import com.httydcraft.authcraft.bangee.server.BungeeServer;
import com.httydcraft.authcraft.core.hooks.nanolimbo.NanoLimboProvider;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.util.stream.IntStream;

// #region Class Documentation
/**
 * Hook for integrating NanoLimbo with BungeeCord.
 * Implements {@link LimboPluginHook} and {@link Listener} to manage limbo servers and redirect players.
 */
public class BungeeNanoLimboPluginHook implements LimboPluginHook, Listener {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final int[] limboPorts;
    private NanoLimboProvider provider;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BungeeNanoLimboPluginHook}.
     *
     * @param limboPortRange The range of ports for limbo servers. Must not be null.
     */
    public BungeeNanoLimboPluginHook(@NotNull IntStream limboPortRange) {
        this.limboPorts = Preconditions.checkNotNull(limboPortRange, "limboPortRange must not be null").toArray();
        if (canHook()) {
            this.provider = new BungeeNanoLimboProvider(
                    ProxyServer.getInstance().getPluginManager().getPlugin("NanoLimboBungee").getClass().getClassLoader());
            ProxyServer.getInstance().getPluginManager().registerListener(BungeeAuthPluginBootstrap.getInstance(), this);
            LOGGER.atInfo().log("Initialized BungeeNanoLimboPluginHook with %d limbo ports", limboPorts.length);
        } else {
            LOGGER.atWarning().log("NanoLimbo plugin not found, hook disabled");
        }
    }
    // #endregion

    // #region LimboPluginHook Implementation
    /**
     * Creates a new limbo server with the specified name.
     *
     * @param serverName The name of the limbo server. Must not be null.
     * @return The created {@link com.httydcraft.authcraft.api.server.proxy.ProxyServer}.
     * @throws IllegalStateException If no available port is found.
     */
    @Override
    public com.httydcraft.authcraft.api.server.proxy.ProxyServer createServer(@NotNull String serverName) {
        Preconditions.checkNotNull(serverName, "serverName must not be null");
        Preconditions.checkState(provider != null, "NanoLimbo provider not initialized");
        SocketAddress address = provider.findAvailableAddress(limboPorts)
                .orElseThrow(() -> new IllegalStateException("Cannot find available port for limbo server!"));
        LOGGER.atFine().log("Creating limbo server %s on address: %s", serverName, address);
        provider.createAndStartLimbo(address);
        ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(serverName, address, "", false);
        ProxyServer.getInstance().getServers().put(serverInfo.getName(), serverInfo);
        return new BungeeServer(serverInfo);
    }

    /**
     * Checks if the NanoLimbo plugin is available for hooking.
     *
     * @return {@code true} if the plugin is available, {@code false} otherwise.
     */
    @Override
    public boolean canHook() {
        boolean result = ProxyServer.getInstance().getPluginManager().getPlugin("NanoLimboBungee") != null;
        LOGGER.atFine().log("Checked NanoLimbo hook availability: %b", result);
        return result;
    }
    // #endregion

    // #region Event Handlers
    /**
     * Handles the server connect event to redirect players to auth servers on join.
     *
     * @param event The server connect event. Must not be null.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerChoose(@NotNull ServerConnectEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        if (event.getReason() != ServerConnectEvent.Reason.JOIN_PROXY) {
            LOGGER.atFine().log("Skipping ServerConnectEvent, reason is not JOIN_PROXY");
            return;
        }
        PluginConfig config = AuthPlugin.instance().getConfig();
        ServerInfo authServer = config.findServerInfo(config.getAuthServers())
                .asProxyServer().as(BungeeServer.class).getServerInfo();
        LOGGER.atFine().log("Redirecting player %s to auth server: %s", event.getPlayer().getName(), authServer.getName());
        event.setTarget(authServer);
    }
    // #endregion
}