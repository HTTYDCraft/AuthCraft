package com.httydcraft.authcraft.velocity;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.server.CoreServer;
import com.httydcraft.authcraft.core.hooks.VkPluginHook;
import com.httydcraft.authcraft.velocity.adventure.VelocityAudienceProvider;
import com.httydcraft.authcraft.velocity.commands.VelocityCommandRegistry;
import com.httydcraft.authcraft.velocity.hooks.VelocityVkPluginHook;
import com.httydcraft.authcraft.velocity.hooks.nanolimbo.VelocityNanoLimboPluginHook;
import com.httydcraft.authcraft.api.hook.LimboPluginHook;
import com.httydcraft.authcraft.velocity.listener.AuthenticationListener;
import com.httydcraft.authcraft.velocity.listener.VkDispatchListener;
import com.httydcraft.authcraft.core.BaseAuthPlugin;
import com.httydcraft.authcraft.core.management.BaseLibraryManagement;
import com.httydcraft.authcraft.core.command.VKCommandRegistry;
import com.alessiodp.libby.VelocityLibraryManager;
import com.httydcraft.multimessenger.vk.message.VkMessage;
import com.httydcraft.multimessenger.vk.provider.VkApiProvider;
import com.google.inject.Inject;
import com.httydcraft.vk.api.velocity.VelocityVkApiPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.platform.AudienceProvider;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

// #region Class Documentation
/**
 * Bootstrap class for the AuthCraft plugin on Velocity.
 * Initializes the AuthCraft plugin, registers listeners, commands, and hooks.
 */
public class VelocityAuthPluginBootstrap {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static VelocityAuthPluginBootstrap instance;
    private final AudienceProvider audienceProvider;
    private final CoreServer core;
    private final File dataFolder;
    private final ProxyServer proxyServer;
    private final Logger logger;
    private BaseAuthPlugin authPlugin;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VelocityAuthPluginBootstrap}.
     *
     * @param proxyServer The Velocity proxy server instance. Must not be null.
     * @param logger The SLF4J logger instance. Must not be null.
     * @param dataDirectory The plugin data directory. Must not be null.
     */
    @Inject
    public VelocityAuthPluginBootstrap(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;
        this.proxyServer = Preconditions.checkNotNull(proxyServer, "proxyServer must not be null");
        this.logger = Preconditions.checkNotNull(logger, "logger must not be null");
        this.dataFolder = Preconditions.checkNotNull(dataDirectory, "dataDirectory must not be null").toFile();
        this.core = new VelocityProxyCoreServer((ProxyServer) proxyServer);
        this.audienceProvider = new VelocityAudienceProvider(proxyServer);
        LOGGER.atInfo().log("Initialized VelocityAuthPluginBootstrap");
    }
    // #endregion

    // #region Static Methods
    /**
     * Gets the singleton instance of the bootstrap.
     *
     * @return The {@code VelocityAuthPluginBootstrap} instance.
     */
    public static VelocityAuthPluginBootstrap getInstance() {
        Preconditions.checkState(instance != null, "Bootstrap not initialized");
        return instance;
    }
    // #endregion

    // #region Event Handlers
    /**
     * Handles the proxy initialization event.
     *
     * @param event The proxy initialization event. Must not be null.
     */
    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        LOGGER.atInfo().log("Handling proxy initialization event");
        this.authPlugin = new BaseAuthPlugin(
                audienceProvider,
                proxyServer.getPluginManager()
                        .fromInstance(this)
                        .map(PluginContainer::getDescription)
                        .flatMap(PluginDescription::getVersion)
                        .orElse("unknown"),
                dataFolder,
                core,
                new BaseLibraryManagement(new VelocityLibraryManager<>(this, logger, dataFolder.toPath(), proxyServer.getPluginManager()))
        );
        initializeListener();
        initializeCommand();
        initializeLimbo();
        if (authPlugin.getConfig().getVKSettings().isEnabled()) {
            initializeVk();
        }
    }
    // #endregion

    // #region Initialization Methods
    /**
     * Initializes VK integration if enabled.
     */
    private void initializeVk() {
        LOGGER.atInfo().log("Initializing VK integration");
        authPlugin.putHook(VkPluginHook.class, new VelocityVkPluginHook());
        VkMessage.setDefaultApiProvider(VkApiProvider.of(
                VelocityVkApiPlugin.getInstance().getVkApiProvider().getActor(),
                VelocityVkApiPlugin.getInstance().getVkApiProvider().getVkApiClient()
        ));
        proxyServer.getEventManager().register(this, new VkDispatchListener());
        new VKCommandRegistry();
    }

    /**
     * Initializes listeners for authentication events.
     */
    private void initializeListener() {
        LOGGER.atInfo().log("Initializing listeners");
        proxyServer.getEventManager().register(this, new AuthenticationListener(authPlugin));
    }

    /**
     * Initializes command registration.
     */
    private void initializeCommand() {
        LOGGER.atInfo().log("Initializing commands");
        new VelocityCommandRegistry(this, authPlugin);
    }

    /**
     * Initializes limbo server hooks.
     */
    private void initializeLimbo() {
        LOGGER.atInfo().log("Initializing limbo hooks");
        Collection<LimboPluginHook> limboPluginHooks = Collections.singleton(
                new VelocityNanoLimboPluginHook(authPlugin.getConfig().getLimboPortRange(), proxyServer)
        );
        limboPluginHooks.stream()
                .filter(LimboPluginHook::canHook)
                .forEach(hook -> authPlugin.putHook(LimboPluginHook.class, hook));
    }
    // #endregion

    // #region Getters
    /**
     * Gets the Velocity proxy server.
     *
     * @return The {@link ProxyServer}.
     */
    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    /**
     * Gets the AuthCraft plugin instance.
     *
     * @return The {@link AuthPlugin}.
     */
    public AuthPlugin getAuthPlugin() {
        return authPlugin;
    }
    // #endregion
}