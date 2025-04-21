package com.httydcraft.authcraft.listener;

import com.httydcraft.authcraft.AuthCraft;
import com.httydcraft.authcraft.auth.AuthManager;
import com.httydcraft.authcraft.utils.ConnectionLimiter;
import com.httydcraft.authcraft.utils.MojangAuth;
import com.httydcraft.authcraft.utils.UtilsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
    private final AuthCraft plugin;
    private final AuthManager authManager;
    private final ConnectionLimiter connectionLimiter;
    private final MojangAuth mojangAuth;
    private final UtilsManager utilsManager;

    public EventListener(AuthCraft plugin, AuthManager authManager, ConnectionLimiter connectionLimiter, MojangAuth mojangAuth, UtilsManager utilsManager) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.connectionLimiter = connectionLimiter;
        this.mojangAuth = mojangAuth;
        this.utilsManager = utilsManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        if (!connectionLimiter.canConnect(ip)) {
            event.getPlayer().kickPlayer(utilsManager.getMessageUtils().getMessage("connection_limit.exceeded"));
            utilsManager.getAuditLogger().log("Connection limit exceeded for IP " + ip);
            return;
        }

        if (!mojangAuth.verifyPlayer(event.getPlayer().getName(), event.getPlayer().getUniqueId().toString())) {
            event.getPlayer().kickPlayer(utilsManager.getMessageUtils().getMessage("mojang_auth.failed"));
            utilsManager.getAuditLogger().log("Mojang authentication failed for " + event.getPlayer().getName());
            return;
        }

        if (!authManager.isAuthenticated(event.getPlayer().getUniqueId()) && authManager.isRegistered(event.getPlayer().getUniqueId())) {
            plugin.storePlayerState(event.getPlayer());
            plugin.applyLimboEffects(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        connectionLimiter.disconnect(ip);
    }
}