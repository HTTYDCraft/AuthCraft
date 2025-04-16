package com.httydcraft.authcraft.velocity.listener;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.event.PlayerChatPasswordEvent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitywired.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.httydcraft.authcraft.velocity.server.VelocityProxyServer;

import java.util.Optional;

// #region Class Documentation
/**
 * Listener for handling authentication-related events in Velocity.
 * Manages player login, disconnect, chat, command execution, and server connection restrictions.
 */
public class AuthenticationListener {
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
     * Handles the post-login event for a player.
     *
     * @param event The post-login event. Must not be null.
     */
    @Subscribe
    public void onPostLoginEvent(PostLoginEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        LOGGER.atFine().log("Handling post-login event for player: %s", event.getPlayer().getUsername());
        plugin.getCore().wrapPlayer(event.getPlayer())
                .ifPresent(player -> plugin.getLoginManagement().onLogin(player));
    }

    /**
     * Handles the disconnect event for a player.
     *
     * @param event The disconnect event. Must not be null.
     */
    @Subscribe
    public void onPlayerLeave(DisconnectEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        LOGGER.atFine().log("Handling disconnect event for player: %s", event.getPlayer().getUsername());
        plugin.getCore().wrapPlayer(event.getPlayer())
                .ifPresent(player -> plugin.getLoginManagement().onDisconnect(player));
    }

    /**
     * Handles the player chat event, including password input in chat.
     *
     * @param event The player chat event. Must not be null.
     */
    @Subscribe
    public void onChatEvent(PlayerChatEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        if (!event.getResult().isAllowed()) {
            LOGGER.atFine().log("Chat event not allowed, skipping");
            return;
        }
        plugin.getCore().wrapPlayer(event.getPlayer()).ifPresent(player -> {
            if (!plugin.getAuthenticatingAccountBucket().isAuthenticating(player)) {
                LOGGER.atFine().log("Player %s not authenticating, skipping chat event", player.getNickname());
                return;
            }
            if (plugin.getConfig().isPasswordInChatEnabled()) {
                LOGGER.atFine().log("Processing chat as password for player: %s", player.getNickname());
                plugin.getEventBus().publish(PlayerChatPasswordEvent.class, player, event.getMessage());
                event.setResult(PlayerChatEvent.ChatResult.denied());
                return;
            }
            if (!plugin.getConfig().shouldBlockChat() || event.getResult().getMessage().orElse("").startsWith("/")) {
                LOGGER.atFine().log("Chat not blocked or is command, allowing for player: %s", player.getNickname());
                return;
            }
            LOGGER.atFine().log("Blocking chat for player: %s", player.getNickname());
            player.sendMessage(plugin.getConfig().getServerMessages().getMessage("disabled-chat"));
            event.setResult(PlayerChatEvent.ChatResult.denied());
        });
    }

    /**
     * Handles the command execution event, restricting commands during authentication.
     *
     * @param event The command execute event. Must not be null.
     */
    @Subscribe
    public void onCommandEvent(CommandExecuteEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        if (!event.getResult().isAllowed()) {
            LOGGER.atFine().log("Command event not allowed, skipping");
            return;
        }
        Optional<ServerPlayer> proxyPlayerOptional = plugin.getCore().wrapPlayer(event.getCommandSource());
        if (!proxyPlayerOptional.isPresent()) {
            LOGGER.atFine().log("No player found for command source, skipping");
            return;
        }
        ServerPlayer player = proxyPlayerOptional.get();
        if (!plugin.getAuthenticatingAccountBucket().isAuthenticating(player)) {
            LOGGER.atFine().log("Player %s not authenticating, allowing command", player.getNickname());
            return;
        }
        String command = "/" + event.getCommand();
        if (plugin.getConfig().getAllowedCommands().stream().anyMatch(pattern -> pattern.matcher(command).find())) {
            LOGGER.atFine().log("Command %s allowed for player: %s", command, player.getNickname());
            return;
        }
        LOGGER.atFine().log("Blocking command %s for player: %s", command, player.getNickname());
        player.sendMessage(plugin.getConfig().getServerMessages().getMessage("disabled-command"));
        event.setResult(CommandExecuteEvent.CommandResult.denied());
    }

    /**
     * Handles the server pre-connect event, restricting access to blocked servers.
     *
     * @param event The server pre-connect event. Must not be null.
     */
    @Subscribe
    public void onBlockedServerConnect(ServerPreConnectEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        plugin.getCore().wrapPlayer(event.getPlayer()).ifPresent(player -> {
            String id = plugin.getConfig().getActiveIdentifierType().getId(player);
            if (!plugin.getAuthenticatingAccountBucket().isAuthenticating(player)) {
                LOGGER.atFine().log("Player %s not authenticating, allowing server connect", player.getNickname());
                return;
            }
            Optional<RegisteredServer> resultServerOptional = event.getResult().getServer();
            if (!resultServerOptional.isPresent()) {
                LOGGER.atFine().log("No server selected, redirecting to auth server for player: %s", player.getNickname());
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(
                        plugin.getConfig().findServerInfo(plugin.getConfig().getAuthServers())
                                .asProxyServer()
                                .as(VelocityProxyServer.class)
                                .getServer()));
                return;
            }
            String serverId = resultServerOptional.get().getServerInfo().getName();
            if (plugin.getConfig().getBlockedServers().stream().noneMatch(server -> serverId.equals(server.getId()))) {
                LOGGER.atFine().log("Server %s not blocked, allowing connect for player: %s", serverId, player.getNickname());
                return;
            }
            LOGGER.atFine().log("Blocking server %s for player: %s", serverId, player.getNickname());
            player.sendMessage(plugin.getConfig().getServerMessages().getMessage("disabled-server"));
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        });
    }
    // #endregion
}