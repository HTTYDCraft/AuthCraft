package com.httydcraft.authcraft.listener;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.AuthPlugin;
import com.httydcraft.authcraft.event.PlayerChatPasswordEvent;
import com.httydcraft.authcraft.server.player.ServerPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

// #region Class Documentation
/**
 * Listener for handling authentication-related events in Spigot.
 * Manages player join, quit, chat, and command restrictions during authentication.
 */
public class AuthenticationListener implements Listener {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
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
        Preconditions.checkNotNull(event, "event must not be null");
        LOGGER.atFine().log("Handling player join event for player: %s", event.getPlayer().getName());
        plugin.getCore().wrapPlayer(event.getPlayer())
                .ifPresent(player -> plugin.getLoginManagement().onLogin(player));
    }

    /**
     * Handles the player quit event.
     *
     * @param event The player quit event. Must not be null.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        LOGGER.atFine().log("Handling player quit event for player: %s", event.getPlayer().getName());
        plugin.getCore().wrapPlayer(event.getPlayer())
                .ifPresent(player -> plugin.getLoginManagement().onDisconnect(player));
    }

    /**
     * Handles the player chat event, including password input.
     *
     * @param event The async player chat event. Must not be null.
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        if (event.isCancelled()) {
            LOGGER.atFine().log("Chat event cancelled, skipping");
            return;
        }
        plugin.getCore().wrapPlayer(event.getPlayer()).ifPresent(player -> {
            if (!plugin.getAuthenticatingAccountBucket().isAuthenticating(player)) {
                LOGGER.atFine().log("Player %s not authenticating, allowing chat", player.getNickname());
                return;
            }
            if (plugin.getConfig().isPasswordInChatEnabled()) {
                LOGGER.atFine().log("Processing chat as password for player: %s", player.getNickname());
                plugin.getEventBus().publish(PlayerChatPasswordEvent.class, player, event.getMessage());
                event.setCancelled(true);
                return;
            }
            if (!plugin.getConfig().shouldBlockChat()) {
                LOGGER.atFine().log("Chat not blocked for player: %s", player.getNickname());
                return;
            }
            LOGGER.atFine().log("Blocking chat for player: %s", player.getNickname());
            player.sendMessage(plugin.getConfig().getServerMessages().getMessage("disabled-chat"));
            event.setCancelled(true);
        });
    }

    /**
     * Handles the player command preprocess event, restricting commands during authentication.
     *
     * @param event The player command preprocess event. Must not be null.
     */
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        if (event.isCancelled()) {
            LOGGER.atFine().log("Command event cancelled, skipping");
            return;
        }
        Optional<ServerPlayer> playerOptional = plugin.getCore().wrapPlayer(event.getPlayer());
        if (!playerOptional.isPresent()) {
            LOGGER.atFine().log("No player found for command, skipping");
            return;
        }
        ServerPlayer player = playerOptional.get();
        if (!plugin.getAuthenticatingAccountBucket().isAuthenticating(player)) {
            LOGGER.atFine().log("Player %s not authenticating, allowing command", player.getNickname());
            return;
        }
        String command = event.getMessage();
        if (plugin.getConfig().getAllowedCommands().stream().anyMatch(pattern -> pattern.matcher(command).find())) {
            LOGGER.atFine().log("Command %s allowed for player: %s", command, player.getNickname());
            return;
        }
        LOGGER.atFine().log("Blocking command %s for player: %s", command, player.getNickname());
        player.sendMessage(plugin.getConfig().getServerMessages().getMessage("disabled-command"));
        event.setCancelled(true);
    }
    // #endregion
}