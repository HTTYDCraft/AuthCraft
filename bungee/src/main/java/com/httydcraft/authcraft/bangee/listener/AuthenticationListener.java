package com.httydcraft.authcraft.bangee.listener;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.bangee.BungeeAuthPluginBootstrap;
import com.httydcraft.authcraft.core.config.message.server.ServerMessageContext;
import com.httydcraft.authcraft.api.event.PlayerChatPasswordEvent;
import com.httydcraft.authcraft.bangee.player.BungeeConnectionProxyPlayer;
import com.httydcraft.authcraft.bangee.player.BungeeServerPlayer;
import com.httydcraft.authcraft.bangee.server.BungeeServer;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// #region Class Documentation
/**
 * Listener for handling authentication-related events in BungeeCord.
 * Manages player login, disconnect, chat, and server connection restrictions during authentication.
 */
public class AuthenticationListener implements Listener {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final Set<UUID> INVALID_ACCOUNTS = new HashSet<>();
    private final BungeeAuthPluginBootstrap bungeePlugin;
    private final AuthPlugin plugin;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AuthenticationListener}.
     *
     * @param plugin The AuthCraft plugin instance. Must not be null.
     */
    public AuthenticationListener(@NotNull AuthPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin must not be null");
        this.bungeePlugin = BungeeAuthPluginBootstrap.getInstance();
        LOGGER.atInfo().log("Initialized AuthenticationListener");
    }
    // #endregion

    // #region Event Handlers
    /**
     * Handles the player login event.
     *
     * @param event The login event. Must not be null.
     */
    @EventHandler
    public void onLogin(@NotNull LoginEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        LOGGER.atFine().log("Handling login event for player: %s", event.getConnection().getName());
        event.registerIntent(bungeePlugin);
        ServerPlayer connectionPlayer = new BungeeConnectionProxyPlayer(event.getConnection());
        plugin.getLoginManagement().onLogin(connectionPlayer).whenComplete((account, exception) -> {
            if (exception != null) {
                LOGGER.atWarning().withCause(exception).log("Login failed for player: %s", connectionPlayer.getNickname());
                INVALID_ACCOUNTS.add(event.getConnection().getUniqueId());
            }
            event.completeIntent(bungeePlugin);
            if (account == null) {
                LOGGER.atFine().log("No account for player: %s", connectionPlayer.getNickname());
                return;
            }
            plugin.getCore().schedule(() -> {
                if (plugin.getAuthenticatingAccountBucket().isAuthenticating(connectionPlayer)) {
                    LOGGER.atFine().log("Player %s still authenticating, skipping autoconnect message",
                            connectionPlayer.getNickname());
                    return;
                }
                account.getPlayer().ifPresent(player ->
                        player.sendMessage(plugin.getConfig().getServerMessages().getMessage(
                                "autoconnect", new ServerMessageContext(account))));
                LOGGER.atFine().log("Sent autoconnect message to player: %s", connectionPlayer.getNickname());
            }, 1, TimeUnit.SECONDS);
        });
    }

    /**
     * Handles the player disconnect event.
     *
     * @param event The player disconnect event. Must not be null.
     */
    @EventHandler
    public void onPlayerLeave(@NotNull PlayerDisconnectEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        UUID playerId = event.getPlayer().getUniqueId();
        if (INVALID_ACCOUNTS.remove(playerId)) {
            LOGGER.atFine().log("Player %s had invalid account, skipping disconnect handling", playerId);
            return;
        }
        plugin.getCore().wrapPlayer(event.getPlayer()).ifPresent(player -> {
            plugin.getLoginManagement().onDisconnect(player);
            LOGGER.atFine().log("Handled disconnect for player: %s", player.getNickname());
        });
    }

    /**
     * Handles the player chat event, including password input.
     *
     * @param event The chat event. Must not be null.
     */
    @EventHandler
    public void onPlayerChat(@NotNull ChatEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        if (event.isCancelled()) {
            LOGGER.atFine().log("Chat event cancelled, skipping");
            return;
        }
        Optional<ServerPlayer> playerOptional = plugin.getCore().wrapPlayer(event.getSender());
        if (!playerOptional.isPresent()) {
            LOGGER.atFine().log("No player found for chat event, skipping");
            return;
        }
        ServerPlayer player = playerOptional.get();
        if (!plugin.getAuthenticatingAccountBucket().isAuthenticating(player)) {
            LOGGER.atFine().log("Player %s not authenticating, allowing chat", player.getNickname());
            return;
        }
        if (plugin.getConfig().isPasswordInChatEnabled() && !event.isCommand()) {
            LOGGER.atFine().log("Processing chat as password for player: %s", player.getNickname());
            plugin.getEventBus().publish(PlayerChatPasswordEvent.class, player, event.getMessage());
            event.setCancelled(true);
            return;
        }
        if (plugin.getConfig().shouldBlockChat() && !event.isCommand()) {
            LOGGER.atFine().log("Blocking chat for player: %s", player.getNickname());
            player.sendMessage(plugin.getConfig().getServerMessages().getMessage("disabled-chat"));
            event.setCancelled(true);
            return;
        }
        if (event.isCommand() &&
                plugin.getConfig().getAllowedCommands().stream().noneMatch(pattern -> pattern.matcher(event.getMessage()).find())) {
            LOGGER.atFine().log("Blocking command %s for player: %s", event.getMessage(), player.getNickname());
            player.sendMessage(plugin.getConfig().getServerMessages().getMessage("disabled-command"));
            event.setCancelled(true);
        }
    }

    /**
     * Handles the server connect event to restrict access to blocked servers.
     *
     * @param event The server connect event. Must not be null.
     */
    @EventHandler
    public void onBlockedServerConnect(@NotNull ServerConnectEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        ServerPlayer player = new BungeeServerPlayer(event.getPlayer());
        if (!plugin.getAuthenticatingAccountBucket().isAuthenticating(player)) {
            LOGGER.atFine().log("Player %s not authenticating, allowing server connect", player.getNickname());
            return;
        }
        String targetServer = event.getTarget().getName();
        if (plugin.getConfig().getBlockedServers().stream().noneMatch(server -> targetServer.equals(server.getId()))) {
            ServerInfo authServer = plugin.getConfig().findServerInfo(plugin.getConfig().getAuthServers())
                    .asProxyServer().as(BungeeServer.class).getServerInfo();
            LOGGER.atFine().log("Redirecting player %s to auth server: %s", player.getNickname(), authServer.getName());
            event.setTarget(authServer);
            return;
        }
        LOGGER.atFine().log("Blocking server %s for player: %s", targetServer, player.getNickname());
        player.sendMessage(plugin.getConfig().getServerMessages().getMessage("disabled-server"));
        event.setCancelled(true);
    }
    // #endregion
}