package com.httydcraft.authcraft.velocity.hooks.nanolimbo;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.core.hooks.nanolimbo.NanoLimboProvider;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.config.PlayerInfoForwarding;
import com.velocitywired.proxy.config.VelocityConfiguration;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.server.data.InfoForwarding;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

// #region Class Documentation
/**
 * Provider for creating and managing NanoLimbo instances in Velocity.
 * Implements {@link NanoLimboProvider} to handle limbo server creation and forwarding configuration.
 */
public class VelocityNanoLimboProvider implements NanoLimboProvider {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ClassLoader classLoader;
    private final ProxyServer proxyServer;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VelocityNanoLimboProvider}.
     *
     * @param proxyServer The Velocity proxy server instance. Must not be null.
     */
    public VelocityNanoLimboProvider(ProxyServer proxyServer) {
        this.proxyServer = Preconditions.checkNotNull(proxyServer, "proxyServer must not be null");
        this.classLoader = NanoLimbo.class.getClassLoader();
        LOGGER.atInfo().log("Initialized VelocityNanoLimboProvider");
    }
    // #endregion

    // #region NanoLimboProvider Implementation
    /**
     * Creates and starts a new limbo server at the specified address.
     *
     * @param address The socket address for the limbo server. Must not be null.
     */
    @Override
    public void createAndStartLimbo(SocketAddress address) {
        Preconditions.checkNotNull(address, "address must not be null");
        LOGGER.atFine().log("Creating and starting limbo server at address: %s", address);
        NanoLimboProvider.super.createAndStartLimbo(address);
    }

    /**
     * Creates the forwarding configuration for the limbo server.
     *
     * @return The {@link InfoForwarding} configuration.
     */
    @Override
    public InfoForwarding createForwarding() {
        LOGGER.atFine().log("Creating forwarding configuration");
        VelocityConfiguration velocityConfiguration = (VelocityConfiguration) proxyServer.getConfiguration();
        PlayerInfoForwarding forwardingMode = velocityConfiguration.getPlayerInfoForwardingMode();
        switch (forwardingMode) {
            case NONE:
                LOGGER.atFine().log("Created NONE forwarding configuration");
                return FORWARDING_FACTORY.none();
            case LEGACY:
                LOGGER.atFine().log("Created LEGACY forwarding configuration");
                return FORWARDING_FACTORY.legacy();
            case MODERN:
                LOGGER.atFine().log("Created MODERN forwarding configuration");
                return FORWARDING_FACTORY.modern(velocityConfiguration.getForwardingSecret());
            case BUNGEEGUARD:
                LOGGER.atFine().log("Created BUNGEEGUARD forwarding configuration");
                return FORWARDING_FACTORY.bungeeGuard(
                        Collections.singleton(new String(velocityConfiguration.getForwardingSecret(), StandardCharsets.UTF_8)));
            default:
                LOGGER.atWarning().log("Unknown forwarding mode: %s, defaulting to NONE", forwardingMode);
                return FORWARDING_FACTORY.none();
        }
    }

    /**
     * Returns the class loader for NanoLimbo.
     *
     * @return The {@link ClassLoader}.
     */
    @Override
    public ClassLoader classLoader() {
        LOGGER.atFine().log("Retrieved class loader");
        return classLoader;
    }
    // #endregion
}