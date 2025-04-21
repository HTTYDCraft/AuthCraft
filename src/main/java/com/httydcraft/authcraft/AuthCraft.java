package com.httydcraft.authcraft;

import com.httydcraft.authcraft.AuthAdminCommand;
import com.httydcraft.authcraft.database.DatabaseManager;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

public class AuthCraft extends JavaPlugin {
    private static AuthCraft instance;
    private DatabaseManager databaseManager;
    private AuthManager authManager;
    private BotManager botManager;
    private UtilsManager utilsManager;
    private RoleManager roleManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("messages_en.yml", false);
        saveResource("messages_ru.yml", false);

        utilsManager = new UtilsManager(this);
        databaseManager = new DatabaseManager(this);
        authManager = new AuthManager(this, databaseManager, utilsManager);
        botManager = new BotManager(this, authManager, utilsManager);
        roleManager = new RoleManager(this, utilsManager.getAuditLogger());

        new AuthCommands(this, authManager, utilsManager).register();
        new TwoFACommand(this, authManager, botManager, utilsManager).register();
        new EventListener(this, authManager, utilsManager).register();
        getCommand("authadmin").setExecutor(new AuthAdminCommand(authManager, utilsManager.getMessageUtils()));

        createLimboWorld();
        getLogger().info("AuthCraft enabled successfully.");
    }

    @Override
    public void onDisable() {
        databaseManager.close();
        botManager.shutdown();
        getLogger().info("AuthCraft disabled.");
    }

    private void createLimboWorld() {
        WorldCreator creator = new WorldCreator("limbo");
        creator.generator(new VoidChunkGenerator());
        creator.createWorld();
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

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public static AuthCraft getInstance() {
        return instance;
    }
}
