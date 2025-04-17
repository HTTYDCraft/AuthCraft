package com.httydcraft.authcraft.bangee;

import com.alessiodp.libby.BungeeLibraryManager;
import com.httydcraft.authcraft.core.BaseAuthPlugin;
import com.httydcraft.multimessenger.vk.message.VkMessage;
import com.httydcraft.multimessenger.vk.provider.VkApiProvider;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.bangee.commands.BungeeCommandsRegistry;
import com.httydcraft.authcraft.core.command.VKCommandRegistry;
import com.httydcraft.authcraft.api.hook.LimboPluginHook;
import com.httydcraft.authcraft.bangee.hooks.BungeeVkPluginHook;
import com.httydcraft.authcraft.core.hooks.VkPluginHook;
import com.httydcraft.authcraft.bangee.hooks.nanolimbo.BungeeNanoLimboPluginHook;
import com.httydcraft.authcraft.bangee.listener.AuthenticationListener;
import com.httydcraft.authcraft.bangee.listener.VkDispatchListener;
import com.httydcraft.authcraft.core.management.BaseLibraryManagement;
import com.httydcraft.vk.api.bungee.BungeeVkApiPlugin;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;

// #region Class Documentation
/**
 * Bootstrap class for the AuthCraft plugin on BungeeCord.
 * Initializes plugin components, listeners, commands, and hooks.
 */
public class BungeeAuthPluginBootstrap extends Plugin {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static BungeeAuthPluginBootstrap instance;
    private BungeeAudiences bungeeAudiences;
    private BaseAuthPlugin authPlugin;

    // #endregion

    // #region Singleton Access
    /**
     * Gets the singleton instance of the plugin.
     *
     * @return The {@code BungeeAuthPluginBootstrap} instance.
     * @throws UnsupportedOperationException If the plugin is not enabled.
     */
    public static BungeeAuthPluginBootstrap getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("Plugin not enabled!");
        }
        return instance;
    }
    // #endregion

    // #region Plugin Lifecycle
    /**
     * Called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        instance = this;
        bungeeAudiences = BungeeAudiences.create(this);
        authPlugin = new BaseAuthPlugin(
                Preconditions.checkNotNull(bungeeAudiences, "bungeeAudiences must not be null"),
                getDescription().getVersion(),
                getDataFolder(),
                BungeeProxyCoreServer.INSTANCE,
                new BaseLibraryManagement(new BungeeLibraryManager(this))
        );
        LOGGER.atInfo().log("AuthCraft BungeeCord plugin enabled, version: %s", getDescription().getVersion());
        initializeListener();
        initializeCommand();
        initializeLimbo();
        if (authPlugin.getConfig().getVKSettings().isEnabled()) {
            initializeVk();
        }
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        if (bungeeAudiences != null) {
            bungeeAudiences.close();
            LOGGER.atInfo().log("Closed BungeeAudiences");
        }
        instance = null;
        LOGGER.atInfo().log("AuthCraft BungeeCord plugin disabled");
    }
    // #endregion

    // #region Initialization Methods
    /**
     * Initializes event listeners.
     */
    private void initializeListener() {
        getProxy().getPluginManager().registerListener(this, new AuthenticationListener(authPlugin));
        LOGGER.atFine().log("Registered AuthenticationListener");
    }

    /**
     * Initializes command registry.
     */
    private void initializeCommand() {
        new BungeeCommandsRegistry(this, authPlugin);
        LOGGER.atFine().log("Initialized BungeeCommandsRegistry");
    }

    /**
     * Initializes NanoLimbo integration.
     */
    private void initializeLimbo() {
        Collection<LimboPluginHook> limboPluginHooks = Collections.singleton(
                new BungeeNanoLimboPluginHook(authPlugin.getConfig().getLimboPortRange()));
        limboPluginHooks.stream()
                .filter(LimboPluginHook::canHook)
                .forEach(limboPluginHook -> {
                    authPlugin.putHook(LimboPluginHook.class, limboPluginHook);
                    LOGGER.atFine().log("Registered NanoLimbo hook");
                });
    }

    /**
     * Initializes VK integration.
     */
    private void initializeVk() {
        authPlugin.putHook(VkPluginHook.class, new BungeeVkPluginHook());
        VkMessage.setDefaultApiProvider(VkApiProvider.of(
                BungeeVkApiPlugin.getInstance().getVkApiProvider().getActor(),
                BungeeVkApiPlugin.getInstance().getVkApiProvider().getVkApiClient()));
        getProxy().getPluginManager().registerListener(this, new VkDispatchListener());
        new VKCommandRegistry();
        LOGGER.atFine().log("Initialized VK integration");
    }
    // #endregion

    // #region Getter
    /**
     * Gets the BungeeCord Adventure audiences.
     *
     * @return The {@link BungeeAudiences}.
     */
    public BungeeAudiences getBungeeAudiences() {
        LOGGER.atFine().log("Retrieved BungeeAudiences");
        return bungeeAudiences;
    }
    // #endregion
}