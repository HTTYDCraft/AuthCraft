package com.httydcraft.authcraft.bangee.hooks.nanolimbo;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.core.hooks.nanolimbo.NanoLimboProvider;
import org.jetbrains.annotations.NotNull;
import ua.nanit.limbo.server.data.InfoForwarding;

// #region Class Documentation
/**
 * Provider for NanoLimbo integration in BungeeCord.
 * Implements {@link NanoLimboProvider} to create forwarding configurations.
 */
public class BungeeNanoLimboProvider implements NanoLimboProvider {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ClassLoader classLoader;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BungeeNanoLimboProvider}.
     *
     * @param classLoader The class loader for the NanoLimbo plugin. Must not be null.
     */
    public BungeeNanoLimboProvider(@NotNull ClassLoader classLoader) {
        this.classLoader = Preconditions.checkNotNull(classLoader, "classLoader must not be null");
        LOGGER.atInfo().log("Initialized BungeeNanoLimboProvider");
    }
    // #endregion

    // #region NanoLimboProvider Implementation
    /**
     * Creates a legacy forwarding configuration for NanoLimbo.
     *
     * @return The {@link InfoForwarding} configuration.
     */
    @Override
    public InfoForwarding createForwarding() {
        LOGGER.atFine().log("Creating legacy forwarding configuration");
        return FORWARDING_FACTORY.legacy();
    }

    /**
     * Gets the class loader for the NanoLimbo plugin.
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