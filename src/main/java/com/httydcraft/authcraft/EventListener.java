package com.httydcraft.authcraft;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

public class EventListener implements Listener {
    private final AuthCraft plugin;
    private final AuthManager authManager;
    private final MessageUtils messageUtils;

    public EventListener(AuthCraft plugin, AuthManager authManager, UtilsManager utilsManager) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.messageUtils = utilsManager.getMessageUtils();
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!authManager.checkWarp(player)) {
            player.kickPlayer(messageUtils.getMessage("cloudflare_warp.kick"));
            return;
        }
        authManager.setPlayerState(player.getUniqueId(), PlayerState.UNAUTHENTICATED);
        player.teleport(plugin.getServer().getWorld("limbo").getSpawnLocation());
        // Включить разрешение на полет в limbo, чтобы не кикало
        player.setAllowFlight(true);
        player.setFlying(true);

        // Проверить регистрацию и авторизацию
        boolean isRegistered = authManager.isRegistered(String.valueOf(player.getUniqueId()));
        boolean isAuthenticated = authManager.getPlayerState(player.getUniqueId()) == PlayerState.AUTHENTICATED;
        if (!isRegistered) {
            player.sendMessage(messageUtils.getMessage("register.usage"));
        } else if (!isAuthenticated) {
            player.sendMessage(messageUtils.getMessage("login.usage"));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        authManager.setPlayerState(event.getPlayer().getUniqueId(), PlayerState.UNAUTHENTICATED);
        // Отключить полет при выходе
        event.getPlayer().setAllowFlight(false);
        event.getPlayer().setFlying(false);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (authManager.getPlayerState(player.getUniqueId()) != PlayerState.AUTHENTICATED) {
            event.setCancelled(true);
            // Всегда держим разрешение на полет в limbo
            if (player.getWorld().getName().equalsIgnoreCase("limbo")) {
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        } else {
            // Отключить полет после авторизации
            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (authManager.getPlayerState(event.getPlayer().getUniqueId()) != PlayerState.AUTHENTICATED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (authManager.getPlayerState(event.getPlayer().getUniqueId()) != PlayerState.AUTHENTICATED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player &&
                authManager.getPlayerState(((Player) event.getEntity()).getUniqueId()) != PlayerState.AUTHENTICATED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (authManager.getPlayerState(event.getPlayer().getUniqueId()) != PlayerState.AUTHENTICATED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (authManager.getPlayerState(event.getPlayer().getUniqueId()) != PlayerState.AUTHENTICATED &&
                !event.getMessage().startsWith("/register") &&
                !event.getMessage().startsWith("/login") &&
                !event.getMessage().startsWith("/2fa")) {
            event.setCancelled(true);
            messageUtils.sendMessage(event.getPlayer(), "command.not_authenticated");
        }
    }
}
