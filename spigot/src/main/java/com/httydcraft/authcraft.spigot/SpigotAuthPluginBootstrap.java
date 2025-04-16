package com.httydcraft.authcraft;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.adventure.SpigotAudienceProvider;
import com.httydcraft.authcraft.commands.SpigotCommandRegistry;
import com.httydcraft.authcraft.hooks.SpigotVkPluginHook;
import com.httydcraft.authcraft.listener.AuthenticationListener;
import com.httydcraft.authcraft.listener.VkDispatchListener;
import com.httydcraft.authcraft.management.BaseLibraryManagement;
import com.alessiodp.libby.BukkitLibraryManager;
import com.httydcraft.multimessenger.vk.message.VkMessage;
import com.httydcraft.multimessenger.vk.provider.VkApiProvider;
import net.kyori.adventure.platform.AudienceProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

// #region Class Documentation
/**
 * Bootstrap class for the AuthCraft plugin on Spigot.
 * Initializes the plugin, registers listeners, commands, and hooks.
 */
public class SpigotAuthPluginBootstrap extends JavaPlugin {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static SpigotAuthPluginBootstrap instance;
    private final AudienceProvider audienceProvider;
    private final ServerCore core;
    private BaseAuthPlugin authPlugin;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code SpigotAuthPluginBootstrap}.
     */
    public SpigotAuthPluginBootstrap() {
        instance = this;
        this.core = new SpigotServerCore(this);
        this.audienceProvider = new SpigotAudienceProvider(this);
        LOGGER.atInfo().log("Initialized SpigotAuthPluginBootstrap");
    }
    // #endregion

    // #region Lifecycle Methods
    /**
     * Called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        LOGGER.atInfo().log("Enabling SpigotAuthPluginBootstrap");
        this.authPlugin = new BaseAuthPlugin(
                audienceProvider,
                getDescription().getVersion(),
                getDataFolder(),
                core,
                new BaseLibraryManagement(new BukkitLibraryManager(this))
        );
        initializeListener();
        initializeCommand();
        if (authPlugin.getConfig().getVKSettings().isEnabled()) {
            initializeVk();
        }
    }
    // #endregion

    // #region Static Methods
    /**
     * Gets the singleton instance of the bootstrap.
     *
     * @return The {@code SpigotAuthPluginBootstrap} instance.
     */
    public static SpigotAuthPluginBootstrap getInstance() {
        Preconditions.checkState(instance != null, "Bootstrap not initialized");
        return instance;
    }
    // #endregion

    // #region Initialization Methods
    /**
     * Initializes VK integration if enabled.
     */
    private void initializeVk() {
        LOGGER.atInfo().log("Initializing VK integration");
        authPlugin.putHook(VkPluginHook.class, new SpigotVkPluginHook());
        VkMessage.setDefaultApiProvider(VkApiProvider.of(
                Bukkit.getPluginManager().getPlugin("VkPlugin").getVkApiProvider().getActor(),
                Bukkit.getPluginManager().getPlugin("VkPlugin").getVkApiProvider().getVkApiClient()
        ));
        getServer().getPluginManager().registerEvents(new VkDispatchListener(), this);
        new VKCommandRegistry();
    }

    /**
     * Initializes listeners for authentication events.
     */
    private void initializeListener() {
        LOGGER.atInfo().log("Initializing listeners");
        getServer().getPluginManager().registerEvents(new AuthenticationListener(authPlugin), this);
    }

    /**
     * Initializes command registration.
     */
    private void initializeCommand() {
        LOGGER.atInfo().log("Initializing commands");
        new SpigotCommandRegistry(this, authPlugin);
    }
    // #endregion

    // #region Getters
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