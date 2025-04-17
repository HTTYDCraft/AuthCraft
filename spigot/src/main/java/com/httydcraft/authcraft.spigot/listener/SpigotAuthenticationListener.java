package com.httydcraft.authcraft.listener;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.AuthPlugin;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;
import com.httydcraft.authcraft.event.PlayerChatPasswordEvent;
import com.httydcraft.authcraft.server.player.ServerPlayer;
import com.httydcraft.authcraft.spigot.SpigotAuthPluginBootstrap;
import com.httydcraft.authcraft.spigot.hooks.SpigotLuckPermsHook;
import com.httydcraft.authcraft.spigot.hooks.SpigotWorldEditHook;
import com.httydcraft.authcraft.spigot.limbo.SpigotLimboManager;
import com.httydcraft.authcraft.spigot.security.AntiBotManager;
import com.httydcraft.authcraft.spigot.security.CloudflareWarpChecker;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.ServicesManager;

import java.util.Optional;

// #region Class Documentation
/**
 * Listener for handling authentication-related events in Spigot.
 * Manages player join, quit, chat, and command restrictions during authentication.
 */
public class AuthenticationListener implements Listener {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final GoogleLogger LIMBO_LOGGER = GoogleLogger.forEnclosingClass();
    private final AuthPlugin plugin;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AuthenticationListener}.
     *
     * @param plugin The AuthCraft plugin instance. Must not be null.
     */
    public AuthenticationListener(AuthPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin must not be null");
        LOGGER.atInfo().log("Initialized AuthenticationListener");
    }
    // #endregion

    // #region Event Handlers
    /**
     * Handles the player join event.
     *
     * @param event The player join event. Must not be null.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event == null) {
            SecurityAuditLogger.logFailure("SpigotAuthenticationListener", null, "Join event is null");
            return;
        }
        Player player = event.getPlayer();
        String address = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";
        SecurityAuditLogger.logSuccess("PlayerJoin", null, String.format("Player %s [%s] joined the server", player.getName(), address));
        LOGGER.atFine().log("Handling player join event for player: %s [%s]", player.getName(), address);
        CloudflareWarpChecker warpChecker = SpigotAuthPluginBootstrap.getInstance().getWarpChecker();
        if (!warpChecker.canJoin(player)) {
            player.kickPlayer("§cВы не можете зайти на сервер с текущим VPN/WARP.\nПроверьте настройки Cloudflare Warp или VPN.");
            SecurityAuditLogger.logFailure("PlayerJoin", null, String.format("Player %s [%s] kicked due to VPN/WARP restriction", player.getName(), address));
            LOGGER.atWarning().log("Player %s [%s] kicked due to Cloudflare Warp/VPN restrictions", player.getName(), address);
            LIMBO_LOGGER.atWarning().log("Player %s [%s] kicked: VPN/WARP restriction", player.getName(), address);
            return;
        }
        AntiBotManager antiBotManager = SpigotAuthPluginBootstrap.getInstance().getAntiBotManager();
        if (antiBotManager.isBot(player)) {
            player.kickPlayer("§cОбнаружена подозрительная активность IP. Попробуйте позже.");
            SecurityAuditLogger.logFailure("PlayerJoin", null, String.format("Player %s [%s] kicked due to AntiBot system", player.getName(), address));
            LOGGER.atWarning().log("Player %s [%s] kicked due to AntiBot system", player.getName(), address);
            LIMBO_LOGGER.atWarning().log("Player %s [%s] kicked: AntiBot system", player.getName(), address);
            return;
        }
        SpigotLimboManager limboManager = getLimboManagerFromService();
        plugin.getCore().wrapPlayer(player).ifPresent(p -> {
            if (!plugin.getAuthenticatingAccountBucket().isAuthenticating(p)) {
                limboManager.sendToLimbo(player);
                SecurityAuditLogger.logFailure("PlayerJoin", null, String.format("Player %s [%s] sent to Limbo (not authenticating)", player.getName(), address));
                LIMBO_LOGGER.atInfo().log("Player %s [%s] sent to Limbo region on join", player.getName(), address);
            } else {
                SecurityAuditLogger.logSuccess("PlayerJoin", null, String.format("Player %s [%s] started authentication", player.getName(), address));
            }
            plugin.getLoginManagement().onLogin(p);
        });
    }

    /**
     * Handles the player quit event.
     *
     * @param event The player quit event. Must not be null.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event == null) {
            SecurityAuditLogger.logFailure("SpigotAuthenticationListener", null, "Quit event is null");
            return;
        }
        Player player = event.getPlayer();
        SecurityAuditLogger.logSuccess("PlayerQuit", null, String.format("Player %s quit the server", player.getName()));
        LOGGER.atFine().log("Handling player quit event for player: %s", player.getName());
        plugin.getCore().wrapPlayer(player).ifPresent(p -> plugin.getLoginManagement().onDisconnect(p));
    }

    /**
     * Handles the player chat event, including password input.
     *
     * @param event The async player chat event. Must not be null.
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event == null) {
            SecurityAuditLogger.logFailure("SpigotAuthenticationListener", null, "Chat event is null");
            return;
        }
        Player player = event.getPlayer();
        String address = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";
        if (event.isCancelled()) {
            SecurityAuditLogger.logFailure("PlayerChat", null, String.format("Player %s [%s] chat event cancelled", player.getName(), address));
            LOGGER.atFine().log("Chat event cancelled, skipping");
            return;
        }
        plugin.getCore().wrapPlayer(player).ifPresent(serverPlayer -> {
            if (!plugin.getAuthenticatingAccountBucket().isAuthenticating(serverPlayer)) {
                SecurityAuditLogger.logSuccess("PlayerChat", null, String.format("Player %s [%s] sent chat: '%s'", player.getName(), address, event.getMessage()));
                LOGGER.atFine().log("Chat allowed for player: %s", serverPlayer.getNickname());
                return;
            }
            SecurityAuditLogger.logFailure("PlayerChat", null, String.format("Player %s [%s] attempted chat during authentication: '%s'", player.getName(), address, event.getMessage()));
            LIMBO_LOGGER.atInfo().log("Player %s [%s] attempted chat during authentication: '%s'", player.getName(), address, event.getMessage());
            plugin.getEventBus().publish(PlayerChatPasswordEvent.class, serverPlayer, event.getMessage());
            event.setCancelled(true);
        });
        if (!plugin.getConfig().shouldBlockChat()) {
            SecurityAuditLogger.logSuccess("PlayerChat", null, String.format("Player %s chat not blocked by config", player.getName()));
            LOGGER.atFine().log("Chat not blocked for player: %s", player.getName());
            return;
        }
        SecurityAuditLogger.logFailure("PlayerChat", null, String.format("Player %s chat blocked by config", player.getName()));
        LOGGER.atFine().log("Blocking chat for player: %s", player.getName());
        player.sendMessage(plugin.getConfig().getServerMessages().getMessage("disabled-chat"));
        event.setCancelled(true);
    }

    /**
     * Handles the player command preprocess event, restricting commands during authentication.
     *
     * @param event The player command preprocess event. Must not be null.
     */
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event == null) {
            SecurityAuditLogger.logFailure("SpigotAuthenticationListener", null, "Command event is null");
            return;
        }
        Player player = event.getPlayer();
        String address = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";
        if (event.isCancelled()) {
            SecurityAuditLogger.logFailure("PlayerCommand", null, String.format("Player %s [%s] command event cancelled", player.getName(), address));
            LOGGER.atFine().log("Command event cancelled, skipping");
            return;
        }
        SpigotLimboManager limboManager = getLimboManagerFromService();
        if (limboManager != null && limboManager.isInLimboRegion(player)) {
            if (!player.hasPermission("authcraft.limbo.build")) {
                event.setCancelled(true);
                player.sendMessage("§cВы не можете использовать команды в Limbo!");
                SecurityAuditLogger.logFailure("PlayerCommand", null, String.format("Player %s [%s] tried to use command '%s' in Limbo without permission", player.getName(), address, event.getMessage()));
                LIMBO_LOGGER.atWarning().log("Player %s tried to use command '%s' in Limbo without permission", player.getName(), event.getMessage());
            }
        }
        Optional<ServerPlayer> playerOptional = plugin.getCore().wrapPlayer(player);
        if (!playerOptional.isPresent()) {
            SecurityAuditLogger.logFailure("PlayerCommand", null, String.format("No ServerPlayer found for %s [%s]", player.getName(), address));
            LOGGER.atFine().log("No player found for command, skipping");
            return;
        }
        ServerPlayer serverPlayer = playerOptional.get();
        if (!plugin.getAuthenticatingAccountBucket().isAuthenticating(serverPlayer)) {
            SecurityAuditLogger.logSuccess("PlayerCommand", null, String.format("Player %s [%s] executed command: %s", player.getName(), address, event.getMessage()));
            LOGGER.atFine().log("Player %s not authenticating, allowing command", serverPlayer.getNickname());
            return;
        }
        String command = event.getMessage();
        if (plugin.getConfig().getAllowedCommands().stream().anyMatch(pattern -> pattern.matcher(command).find())) {
            SecurityAuditLogger.logSuccess("PlayerCommand", null, String.format("Player %s [%s] executed allowed command during authentication: %s", player.getName(), address, command));
            LOGGER.atFine().log("Command %s allowed for player: %s", command, serverPlayer.getNickname());
            return;
        }
        SecurityAuditLogger.logFailure("PlayerCommand", null, String.format("Player %s [%s] tried to execute blocked command during authentication: %s", player.getName(), address, command));
        LOGGER.atFine().log("Blocking command %s for player: %s", command, serverPlayer.getNickname());
        serverPlayer.sendMessage(plugin.getConfig().getServerMessages().getMessage("disabled-command"));
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event == null) {
            SecurityAuditLogger.logFailure("SpigotAuthenticationListener", null, "Block place event is null");
            return;
        }
        Player player = event.getPlayer();
        SpigotLimboManager limboManager = getLimboManagerFromService();
        if (limboManager != null && limboManager.isInLimboRegion(player)) {
            if (!player.hasPermission("authcraft.limbo.build")) {
                event.setCancelled(true);
                player.sendMessage("§cВы не можете строить в Limbo!");
                SecurityAuditLogger.logFailure("BlockPlace", null, String.format("Player %s tried to place block in Limbo without permission", player.getName()));
                LIMBO_LOGGER.atWarning().log("Player %s tried to place block in Limbo without permission", player.getName());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event == null) {
            SecurityAuditLogger.logFailure("SpigotAuthenticationListener", null, "Block break event is null");
            return;
        }
        Player player = event.getPlayer();
        SpigotLimboManager limboManager = getLimboManagerFromService();
        if (limboManager != null && limboManager.isInLimboRegion(player)) {
            if (!player.hasPermission("authcraft.limbo.build")) {
                event.setCancelled(true);
                player.sendMessage("§cВы не можете ломать блоки в Limbo!");
                SecurityAuditLogger.logFailure("BlockBreak", null, String.format("Player %s tried to break block in Limbo without permission", player.getName()));
                LIMBO_LOGGER.atWarning().log("Player %s tried to break block in Limbo without permission", player.getName());
            }
        }
    }

    private SpigotLimboManager getLimboManagerFromService() {
        ServicesManager services = org.bukkit.Bukkit.getServer().getServicesManager();
        return services.load(SpigotLimboManager.class);
    }
    // #endregion
}