package com.httydcraft.authcraft;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.PermissionAttachment;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RoleManager {
    private final AuthCraft plugin;
    private final AuditLogger auditLogger;
    private final Map<UUID, String> internalRoles;
    private final Map<String, RoleDefinition> roles = new HashMap<>();
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    // LuckPerms reflection fields
    private Object luckPermsProvider = null;
    private Class<?> luckPermsClass = null;
    private boolean luckPermsAvailable = false;

    // Класс для описания роли
    private static class RoleDefinition {
        public final String name;
        public final Set<String> permissions = new HashSet<>();
        public final Set<String> inherit = new HashSet<>();
        public RoleDefinition(String name) { this.name = name; }
    }

    public RoleManager(AuthCraft plugin, AuditLogger auditLogger) {
        this.plugin = plugin;
        this.auditLogger = auditLogger;
        this.internalRoles = new HashMap<>();
        loadRoles();
        try {
            luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
            Class<?> servicesManagerClass = plugin.getServer().getServicesManager().getClass();
            Object provider = plugin.getServer().getServicesManager().getRegistration(luckPermsClass);
            if (provider != null) {
                luckPermsProvider = provider.getClass().getMethod("getProvider").invoke(provider);
                luckPermsAvailable = luckPermsProvider != null;
            }
        } catch (Exception | NoClassDefFoundError e) {
            luckPermsAvailable = false;
        }
        if (luckPermsAvailable) {
            auditLogger.log("LuckPerms detected (reflection), using LuckPerms for role management");
        } else {
            auditLogger.log("Using internal role system");
        }
    }

    public void loadRoles() {
        roles.clear();
        File file = new File(plugin.getDataFolder(), "roles.yml");
        if (!file.exists()) {
            plugin.saveResource("roles.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.isConfigurationSection("roles")) return;
        for (String key : config.getConfigurationSection("roles").getKeys(false)) {
            RoleDefinition def = new RoleDefinition(key);
            def.permissions.addAll(config.getStringList("roles." + key + ".permissions"));
            def.inherit.addAll(config.getStringList("roles." + key + ".inherit"));
            roles.put(key, def);
        }
        // Применить наследование
        for (RoleDefinition def : roles.values()) {
            resolveInheritance(def, new HashSet<>());
        }
        auditLogger.log("[RoleManager] Загружено ролей: " + roles.size());
    }

    private void resolveInheritance(RoleDefinition def, Set<String> visited) {
        if (!visited.add(def.name)) return;
        for (String parent : def.inherit) {
            RoleDefinition parentDef = roles.get(parent);
            if (parentDef != null) {
                resolveInheritance(parentDef, visited);
                def.permissions.addAll(parentDef.permissions);
            }
        }
    }

    public void assignRole(Player player, String role) {
        internalRoles.put(player.getUniqueId(), role);
        auditLogger.log("[RoleManager] Сохранили роль '" + role + "' во внутреннюю систему для " + player.getName());
        if (luckPermsAvailable) {
            try {
                Object userManager = luckPermsProvider.getClass().getMethod("getUserManager").invoke(luckPermsProvider);
                Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class).invoke(userManager, player.getUniqueId());
                if (user != null) {
                    // Node.builder("group." + role).build()
                    Class<?> nodeBuilderClass = Class.forName("net.luckperms.api.node.Node").getMethod("builder", String.class).getDeclaringClass();
                    Object builder = nodeBuilderClass.getMethod("builder", String.class).invoke(null, "group." + role);
                    Object node = builder.getClass().getMethod("build").invoke(builder);
                    Object data = user.getClass().getMethod("data").invoke(user);
                    data.getClass().getMethod("add", Class.forName("net.luckperms.api.node.Node"))
                        .invoke(data, node);
                    userManager.getClass().getMethod("saveUser", user.getClass()).invoke(userManager, user);
                    auditLogger.log("[RoleManager] Назначили LuckPerms роль '" + role + "' игроку " + player.getName());
                } else {
                    auditLogger.log("[RoleManager] Не удалось получить LuckPerms User для " + player.getName());
                }
            } catch (Exception e) {
                auditLogger.log("[RoleManager] Ошибка при работе с LuckPerms через reflection: " + e.getMessage());
            }
        } else {
            applyPermissions(player, role);
        }
    }

    private void applyPermissions(Player player, String role) {
        // Удаляем старый attachment
        PermissionAttachment old = attachments.remove(player.getUniqueId());
        if (old != null) player.removeAttachment(old);
        RoleDefinition def = roles.get(role);
        if (def == null) def = roles.get("default");
        if (def == null) return;
        PermissionAttachment attachment = player.addAttachment(plugin);
        for (String perm : def.permissions) {
            if (perm.equals("*")) {
                // '*' — все права, Spigot не поддерживает напрямую, можно обработать отдельно
                continue;
            }
            attachment.setPermission(perm, true);
        }
        attachments.put(player.getUniqueId(), attachment);
        auditLogger.log("[RoleManager] Применены права для роли '" + role + "' игроку " + player.getName());
    }

    public String getRole(Player player) {
        if (luckPermsAvailable) {
            try {
                Object userManager = luckPermsProvider.getClass().getMethod("getUserManager").invoke(luckPermsProvider);
                Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class).invoke(userManager, player.getUniqueId());
                if (user != null) {
                    String group = (String) user.getClass().getMethod("getPrimaryGroup").invoke(user);
                    if (group != null && !group.isEmpty()) {
                        return group;
                    }
                }
            } catch (Exception e) {
                auditLogger.log("[RoleManager] Ошибка при получении роли из LuckPerms через reflection: " + e.getMessage());
            }
        }
        return internalRoles.getOrDefault(player.getUniqueId(), "player");
    }

    public void removeRole(Player player, String role) {
        internalRoles.remove(player.getUniqueId());
        auditLogger.log("[RoleManager] Удалили роль '" + role + "' из внутренней системы для " + player.getName());
        if (luckPermsAvailable) {
            try {
                Object userManager = luckPermsProvider.getClass().getMethod("getUserManager").invoke(luckPermsProvider);
                Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class).invoke(userManager, player.getUniqueId());
                if (user != null) {
                    Class<?> nodeBuilderClass = Class.forName("net.luckperms.api.node.Node").getMethod("builder", String.class).getDeclaringClass();
                    Object builder = nodeBuilderClass.getMethod("builder", String.class).invoke(null, "group." + role);
                    Object node = builder.getClass().getMethod("build").invoke(builder);
                    Object data = user.getClass().getMethod("data").invoke(user);
                    data.getClass().getMethod("remove", Class.forName("net.luckperms.api.node.Node"))
                        .invoke(data, node);
                    userManager.getClass().getMethod("saveUser", user.getClass()).invoke(userManager, user);
                    auditLogger.log("[RoleManager] Удалили LuckPerms роль '" + role + "' у " + player.getName());
                }
            } catch (Exception e) {
                auditLogger.log("[RoleManager] Ошибка при удалении роли из LuckPerms через reflection: " + e.getMessage());
            }
        } else {
            cleanupPlayer(player);
        }
    }

    // При выходе игрока — чистим permissions
    public void cleanupPlayer(Player player) {
        PermissionAttachment old = attachments.remove(player.getUniqueId());
        if (old != null) player.removeAttachment(old);
    }

    // Новый метод: проверка наличия роли
    public boolean hasRole(Player player, String role) {
        String current = getRole(player);
        return current.equalsIgnoreCase(role);
    }
}
