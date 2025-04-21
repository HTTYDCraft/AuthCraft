package com.httydcraft.authcraft;

import com.httydcraft.authcraft.auth.AuthCommands;
import com.httydcraft.authcraft.auth.AuthManager;
import com.httydcraft.authcraft.auth.TwoFACommand;
import com.httydcraft.authcraft.bot.BotManager;
import com.httydcraft.authcraft.database.DatabaseManager;
import com.httydcraft.authcraft.listener.EventListener;
import com.httydcraft.authcraft.utils.*;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthCraft extends JavaPlugin {
    private DatabaseManager databaseManager;
    private AuthManager authManager;
    private BotManager botManager;
    private UtilsManager utilsManager;
    private ConnectionLimiter connectionLimiter;
    private MojangAuth mojangAuth;
    private String limboWorldName;
    private final ConcurrentHashMap<UUID, PlayerState> playerStates = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }

        utilsManager = new UtilsManager(this);
        databaseManager = new DatabaseManager(this, utilsManager.getAuditLogger());
        authManager = new AuthManager(this, databaseManager, utilsManager);
        botManager = new BotManager(this, utilsManager.getAuditLogger());
        connectionLimiter = new ConnectionLimiter(getConfig().getInt("connection_limit.max_per_ip", 3));
        mojangAuth = new MojangAuth();

        limboWorldName = getConfig().getString("limbo.world", "limbo");
        createLimboWorld();

        registerCommands();
        getServer().getPluginManager().registerEvents(new EventListener(this, authManager, connectionLimiter, mojangAuth, utilsManager), this);

        utilsManager.getAuditLogger().log("AuthCraft enabled successfully.");
    }

    @Override
    public void onDisable() {
        databaseManager.close();
        botManager.shutdown();
        utilsManager.getAuditLogger().log("AuthCraft disabled.");
    }

    private void createLimboWorld() {
        WorldCreator creator = new WorldCreator(limboWorldName);
        creator.generator(new VoidChunkGenerator());
        creator.environment(World.Environment.NORMAL);
        creator.createWorld();
    }

    private void registerCommands() {
        AuthCommands authCommands = new AuthCommands(this, authManager, utilsManager);
        getCommand("register").setExecutor(authCommands);
        getCommand("login").setExecutor(authCommands);
        getCommand("changepassword").setExecutor(authCommands);
        getCommand("logout").setExecutor(authCommands);
        getCommand("authcraft").setExecutor(authCommands);
        getCommand("2fa").setExecutor(new TwoFACommand(this, authManager, botManager, utilsManager));
    }

    public void applyLimboEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, false, false));
        player.teleport(getServer().getWorld(limboWorldName).getSpawnLocation());
    }

    public void removeLimboEffects(Player player) {
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOW);
    }

    public void storePlayerState(Player player) {
        playerStates.put(player.getUniqueId(), new PlayerState(player));
    }

    public void restorePlayerState(Player player) {
        PlayerState state = playerStates.remove(player.getUniqueId());
        if (state != null) {
            state.restore(player);
        }
    }

    public void reload() {
        reloadConfig();
        utilsManager.getMessageUtils().reload();
        databaseManager.close();
        databaseManager = new DatabaseManager(this, utilsManager.getAuditLogger());
        authManager = new AuthManager(this, databaseManager, utilsManager);
        botManager.shutdown();
        botManager = new BotManager(this, utilsManager.getAuditLogger());
        connectionLimiter = new ConnectionLimiter(getConfig().getInt("connection_limit.max_per_ip", 3));
        utilsManager.getAuditLogger().log("Configuration and managers reloaded.");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public BotManager getBotManager() {
        return botManager;
    }

    public UtilsManager getUtilsManager() {
        return utilsManager;
    }

    public ConnectionLimiter getConnectionLimiter() {
        return connectionLimiter;
    }

    public MojangAuth getMojangAuth() {
        return mojangAuth;
    }

    public String getLimboWorldName() {
        return limboWorldName;
    }
}