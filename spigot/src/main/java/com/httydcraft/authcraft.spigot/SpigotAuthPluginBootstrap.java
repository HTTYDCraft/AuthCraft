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
import org.bukkit.configuration.file.FileConfiguration;
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
    private CloudflareWarpChecker warpChecker;
    private SpigotLuckPermsHook luckPermsHook;
    private SpigotWorldEditHook worldEditHook;
    private AntiBotManager antiBotManager;
    private SpigotLimboManager limboManager;

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
        FileConfiguration config = getConfig();
        saveDefaultConfig();
        this.warpChecker = new CloudflareWarpChecker(this, config);
        this.luckPermsHook = new SpigotLuckPermsHook();
        if (!luckPermsHook.available()) {
            LOGGER.atWarning().log("LuckPerms not found! Role/group management disabled.");
            this.luckPermsHook = null;
        }
        this.worldEditHook = new SpigotWorldEditHook();
        if (!worldEditHook.available()) {
            LOGGER.atWarning().log("WorldEdit not found! Region protection disabled.");
            this.worldEditHook = null;
        }
        this.antiBotManager = new AntiBotManager();
        // LimboManager init (10x10x10 регион, точка 0,80,0)
        com.sk89q.worldedit.regions.CuboidRegion region = new com.sk89q.worldedit.regions.CuboidRegion(
                Bukkit.getWorlds().get(0),
                new com.sk89q.worldedit.math.BlockVector3(-5, 75, -5),
                new com.sk89q.worldedit.math.BlockVector3(5, 85, 5)
        );
        org.bukkit.Location spawn = new org.bukkit.Location(Bukkit.getWorlds().get(0), 0, 80, 0);
        limboManager = new SpigotLimboManager(this, spawn, worldEditHook, region);
        // Регистрируем как сервис Bukkit
        getServer().getServicesManager().register(SpigotLimboManager.class, limboManager, this, org.bukkit.plugin.ServicePriority.Normal);
        initializeListener();
        initializeCommand();
        if (authPlugin.getConfig().getVKSettings().isEnabled()) {
            initializeVk();
        }
        if (authPlugin.getConfig().getTelegramSettings().isEnabled()) {
            initializeTelegram();
        }
        if (authPlugin.getConfig().getDiscordSettings().isEnabled()) {
            initializeDiscord();
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
     * Initializes Telegram integration if enabled.
     */
    private void initializeTelegram() {
        LOGGER.atInfo().log("Initializing Telegram integration");
        authPlugin.putHook(com.httydcraft.authcraft.core.hooks.TelegramPluginHook.class,
                new com.httydcraft.authcraft.spigot.hooks.SpigotTelegramPluginHook());
        // Регистрация TelegramUpdatesListener
        com.pengrad.telegrambot.TelegramBot bot = ((com.httydcraft.authcraft.spigot.hooks.SpigotTelegramPluginHook)
                authPlugin.getHook(com.httydcraft.authcraft.core.hooks.TelegramPluginHook.class)).getTelegramBot();
        bot.setUpdatesListener(new com.httydcraft.authcraft.spigot.listener.TelegramUpdatesListenerImpl());
        // Регистрация команд
        new com.httydcraft.authcraft.spigot.commands.TelegramCommandRegistry();
    }

    /**
     * Initializes Discord integration if enabled.
     */
    private void initializeDiscord() {
        LOGGER.atInfo().log("Initializing Discord integration");
        com.httydcraft.authcraft.spigot.hooks.SpigotDiscordHook discordHook = new com.httydcraft.authcraft.spigot.hooks.SpigotDiscordHook();
        authPlugin.putHook(com.httydcraft.authcraft.core.hooks.DiscordHook.class, discordHook);
        // Регистрация DiscordListener
        if (discordHook.getJDA() != null) {
            discordHook.getJDA().addEventListener(new com.httydcraft.authcraft.spigot.listener.DiscordListener());
        }
        // Регистрация команд
        new com.httydcraft.authcraft.spigot.commands.DiscordCommandRegistry();
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

    /**
     * Gets the CloudflareWarpChecker instance.
     *
     * @return The {@link CloudflareWarpChecker}.
     */
    public CloudflareWarpChecker getWarpChecker() {
        return warpChecker;
    }

    /**
     * Gets the LuckPerms hook instance.
     *
     * @return The {@link SpigotLuckPermsHook}.
     */
    public SpigotLuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    /**
     * Gets the WorldEdit hook instance.
     *
     * @return The {@link SpigotWorldEditHook}.
     */
    public SpigotWorldEditHook getWorldEditHook() {
        return worldEditHook;
    }

    /**
     * Gets the AntiBotManager instance.
     *
     * @return The {@link AntiBotManager}.
     */
    public AntiBotManager getAntiBotManager() {
        return antiBotManager;
    }

    /**
     * Gets the LimboManager instance.
     *
     * @return The {@link SpigotLimboManager}.
     */
    public SpigotLimboManager getLimboManager() {
        return limboManager;
    }
    // #endregion
}