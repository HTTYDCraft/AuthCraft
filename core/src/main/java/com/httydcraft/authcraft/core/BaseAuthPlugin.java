package com.httydcraft.authcraft.core;

import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.bucket.AuthenticatingAccountBucket;
import com.httydcraft.authcraft.api.bucket.LinkConfirmationBucket;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.server.CoreServer;
import com.httydcraft.authcraft.core.server.commands.ServerCommandsRegistry;
import io.github.revxrsal.eventbus.EventBus;

// #region Class Documentation
/**
 * Base class for the AuthCraft plugin.
 * Initializes core components and manages plugin lifecycle.
 */
public abstract class BaseAuthPlugin {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    protected PluginConfig config;
    protected CoreServer core;
    protected AccountDatabase accountDatabase;
    protected AuthenticatingAccountBucket authenticatingAccountBucket;
    protected LinkConfirmationBucket linkConfirmationBucket;
    protected EventBus eventBus;
    protected ServerCommandsRegistry commandsRegistry;
    // #endregion

    // #region Lifecycle Methods
    /**
     * Initializes the plugin and its components.
     */
    public void onEnable() {
        LOGGER.atInfo().log("Enabling AuthCraft plugin");
        initializeConfig();
        initializeCore();
        initializeDatabase();
        initializeBuckets();
        initializeEventBus();
        initializeCommands();
        LOGGER.atInfo().log("AuthCraft plugin enabled successfully");
    }

    /**
     * Disables the plugin and cleans up resources.
     */
    public void onDisable() {
        LOGGER.atInfo().log("Disabling AuthCraft plugin");
        if (accountDatabase != null) {
            accountDatabase.close();
            LOGGER.atFine().log("Closed account database");
        }
        if (commandsRegistry != null) {
            commandsRegistry = null;
            LOGGER.atFine().log("Cleared commands registry");
        }
        LOGGER.atInfo().log("AuthCraft plugin disabled");
    }
    // #endregion

    // #region Initialization Methods
    /**
     * Initializes the plugin configuration.
     */
    protected abstract void initializeConfig();

    /**
     * Initializes the core server component.
     */
    protected abstract void initializeCore();

    /**
     * Initializes the account database.
     */
    protected abstract void initializeDatabase();

    /**
     * Initializes the authenticating account and link confirmation buckets.
     */
    protected void initializeBuckets() {
        this.authenticatingAccountBucket = new AuthenticatingAccountBucket();
        this.linkConfirmationBucket = new LinkConfirmationBucket();
        LOGGER.atFine().log("Initialized authenticating account and link confirmation buckets");
    }

    /**
     * Initializes the event bus.
     */
    protected abstract void initializeEventBus();

    /**
     * Initializes the command registry.
     */
    protected abstract void initializeCommands();
    // #endregion

    // #region Getters
    /**
     * Gets the plugin configuration.
     *
     * @return The {@link PluginConfig}.
     */
    public PluginConfig getConfig() {
        LOGGER.atFine().log("Retrieved plugin configuration");
        return config;
    }

    /**
     * Gets the core server component.
     *
     * @return The {@link CoreServer}.
     */
    public CoreServer getCore() {
        LOGGER.atFine().log("Retrieved core server");
        return core;
    }

    /**
     * Gets the account database.
     *
     * @return The {@link AccountDatabase}.
     */
    public AccountDatabase getAccountDatabase() {
        LOGGER.atFine().log("Retrieved account database");
        return accountDatabase;
    }

    /**
     * Gets the authenticating account bucket.
     *
     * @return The {@link AuthenticatingAccountBucket}.
     */
    public AuthenticatingAccountBucket getAuthenticatingAccountBucket() {
        LOGGER.atFine().log("Retrieved authenticating account bucket");
        return authenticatingAccountBucket;
    }

    /**
     * Gets the link confirmation bucket.
     *
     * @return The {@link LinkConfirmationBucket}.
     */
    public LinkConfirmationBucket getLinkConfirmationBucket() {
        LOGGER.atFine().log("Retrieved link confirmation bucket");
        return linkConfirmationBucket;
    }

    /**
     * Gets the event bus.
     *
     * @return The {@link EventBus}.
     */
    public EventBus getEventBus() {
        LOGGER.atFine().log("Retrieved event bus");
        return eventBus;
    }
    // #endregion
}