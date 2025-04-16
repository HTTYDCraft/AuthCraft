package com.httydcraft.authcraft.bangee;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.bangee.api.bossbar.BungeeServerBossbar;
import com.httydcraft.authcraft.api.server.CoreServer;
import com.httydcraft.authcraft.bangee.api.title.BungeeServerTitle;
import com.httydcraft.authcraft.api.hook.LimboPluginHook;
import com.httydcraft.authcraft.bangee.message.BungeeComponent;
import com.httydcraft.authcraft.bangee.message.BungeeServerComponent;
import com.httydcraft.authcraft.bangee.player.BungeeServerPlayer;
import com.httydcraft.authcraft.bangee.scheduler.BungeeSchedulerWrapper;
import com.httydcraft.authcraft.bangee.server.BungeeServer;
import com.httydcraft.authcraft.api.server.bossbar.ServerBossbar;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.server.scheduler.ServerScheduler;
import com.httydcraft.authcraft.api.server.title.ServerTitle;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// #region Class Documentation
/**
 * BungeeCord-specific implementation of the server core.
 * Implements {@link CoreServer} to provide core functionality for the AuthCraft plugin.
 */
public enum BungeeProxyCoreServer implements CoreServer {
    INSTANCE;

    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final ProxyServer PROXY_SERVER = ProxyServer.getInstance();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    // #endregion

    // #region CoreServer Implementation
    /**
     * Calls a BungeeCord event.
     *
     * @param event The event to call. Must not be null.
     */
    @Override
    public <E> void callEvent(@NotNull E event) {
        Preconditions.checkNotNull(event, "event must not be null");
        LOGGER.atFine().log("Calling event: %s", event.getClass().getSimpleName());
        PROXY_SERVER.getPluginManager().callEvent((Event) event);
    }

    /**
     * Gets all online players.
     *
     * @return A list of {@link ServerPlayer} instances.
     */
    @Override
    public List<ServerPlayer> getPlayers() {
        List<ServerPlayer> players = PROXY_SERVER.getPlayers().stream()
                .map(BungeeServerPlayer::new)
                .collect(Collectors.toList());
        LOGGER.atFine().log("Retrieved %d online players", players.size());
        return players;
    }

    /**
     * Gets a player by their UUID.
     *
     * @param uniqueId The player's UUID. Must not be null.
     * @return An optional {@link ServerPlayer}.
     */
    @Override
    public Optional<ServerPlayer> getPlayer(@NotNull UUID uniqueId) {
        Preconditions.checkNotNull(uniqueId, "uniqueId must not be null");
        ProxiedPlayer proxiedPlayer = PROXY_SERVER.getPlayer(uniqueId);
        if (proxiedPlayer == null) {
            LOGGER.atFine().log("No player found for UUID: %s", uniqueId);
            return Optional.empty();
        }
        LOGGER.atFine().log("Found player for UUID: %s", uniqueId);
        return Optional.of(new BungeeServerPlayer(proxiedPlayer));
    }

    /**
     * Gets a player by their name.
     *
     * @param name The player's name. Must not be null.
     * @return An optional {@link ServerPlayer}.
     */
    @Override
    public Optional<ServerPlayer> getPlayer(@NotNull String name) {
        Preconditions.checkNotNull(name, "name must not be null");
        ProxiedPlayer proxiedPlayer = PROXY_SERVER.getPlayer(name);
        if (proxiedPlayer == null) {
            LOGGER.atFine().log("No player found for name: %s", name);
            return Optional.empty();
        }
        LOGGER.atFine().log("Found player for name: %s", name);
        return Optional.of(new BungeeServerPlayer(proxiedPlayer));
    }

    /**
     * Wraps an object into a {@link ServerPlayer}.
     *
     * @param player The player object to wrap. May be null.
     * @return An optional {@link ServerPlayer}.
     */
    @Override
    public Optional<ServerPlayer> wrapPlayer(Object player) {
        if (player == null) {
            LOGGER.atFine().log("Cannot wrap null player, returning empty");
            return Optional.empty();
        }
        if (player instanceof ServerPlayer) {
            LOGGER.atFine().log("Player already a ServerPlayer");
            return Optional.of((ServerPlayer) player);
        }
        if (player instanceof ProxiedPlayer) {
            LOGGER.atFine().log("Wrapped ProxiedPlayer to ServerPlayer");
            return Optional.of(new BungeeServerPlayer((ProxiedPlayer) player));
        }
        LOGGER.atWarning().log("Unsupported player type: %s", player.getClass().getName());
        return Optional.empty();
    }

    /**
     * Gets the server logger.
     *
     * @return The {@link Logger}.
     */
    @Override
    public Logger getLogger() {
        LOGGER.atFine().log("Retrieved server logger");
        return PROXY_SERVER.getLogger();
    }

    /**
     * Creates a new server title.
     *
     * @param title The title component. Must not be null.
     * @return A {@link ServerTitle} instance.
     */
    @Override
    public ServerTitle createTitle(@NotNull ServerComponent title) {
        Preconditions.checkNotNull(title, "title must not be null");
        LOGGER.atFine().log("Creating title with component: %s", title.plainText());
        return new BungeeServerTitle(title);
    }

    /**
     * Creates a new server boss bar.
     *
     * @param component The boss bar component. Must not be null.
     * @return A {@link ServerBossbar} instance.
     */
    @Override
    public ServerBossbar createBossbar(@NotNull ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        LOGGER.atFine().log("Creating boss bar with component: %s", component.plainText());
        return new BungeeServerBossbar(component);
    }

    /**
     * Creates a plain text component.
     *
     * @param plain The plain text. Must not be null.
     * @return A {@link ServerComponent}.
     */
    @Override
    public ServerComponent componentPlain(@NotNull String plain) {
        Preconditions.checkNotNull(plain, "plain must not be null");
        LOGGER.atFine().log("Creating plain component: %s", plain);
        return new BungeeServerComponent(ChatColor.stripColor(plain));
    }

    /**
     * Creates a JSON text component.
     *
     * @param json The JSON text. Must not be null.
     * @return A {@link ServerComponent}.
     */
    @Override
    public ServerComponent componentJson(@NotNull String json) {
        Preconditions.checkNotNull(json, "json must not be null");
        LOGGER.atFine().log("Creating JSON component");
        return new BungeeServerComponent(ComponentSerializer.parse(json));
    }

    /**
     * Creates a legacy text component.
     *
     * @param legacy The legacy text. Must not be null.
     * @return A {@link ServerComponent}.
     */
    @Override
    public ServerComponent componentLegacy(@NotNull String legacy) {
        Preconditions.checkNotNull(legacy, "legacy must not be null");
        LOGGER.atFine().log("Creating legacy component: %s", legacy);
        return new BungeeServerComponent(legacy);
    }

    /**
     * Gets a server by its name, creating a limbo server if necessary.
     *
     * @param serverName The server name. Must not be null.
     * @return An optional {@link com.httydcraft.authcraft.api.server.proxy.ProxyServer}.
     */
    @Override
    public Optional<com.httydcraft.authcraft.api.server.proxy.ProxyServer> serverFromName(@NotNull String serverName) {
        Preconditions.checkNotNull(serverName, "serverName must not be null");
        ServerInfo serverInfo = PROXY_SERVER.getServerInfo(serverName);
        if (serverInfo == null) {
            LimboPluginHook limboHook = AuthPlugin.instance().getHook(LimboPluginHook.class);
            if (limboHook != null) {
                LOGGER.atFine().log("Creating limbo server for name: %s", serverName);
                return Optional.of(limboHook.createServer(serverName));
            }
            LOGGER.atFine().log("No server found for name: %s", serverName);
            return Optional.empty();
        }
        LOGGER.atFine().log("Found server for name: %s", serverName);
        return Optional.of(new BungeeServer(serverInfo));
    }

    /**
     * Registers a listener with the plugin.
     *
     * @param plugin The AuthCraft plugin. Must not be null.
     * @param listener The listener to register. Must not be null.
     */
    @Override
    public void registerListener(@NotNull AuthPlugin plugin, @NotNull Object listener) {
        Preconditions.checkNotNull(plugin, "plugin must not be null");
        Preconditions.checkNotNull(listener, "listener must not be null");
        LOGGER.atFine().log("Registering listener: %s", listener.getClass().getSimpleName());
        PROXY_SERVER.getPluginManager().registerListener(BungeeAuthPluginBootstrap.getInstance(), (Listener) listener);
    }

    /**
     * Schedules a repeating task.
     *
     * @param task The task to schedule. Must not be null.
     * @param delay The initial delay.
     * @param period The period between executions.
     * @param unit The time unit. Must not be null.
     * @return A {@link ServerScheduler} instance.
     */
    @Override
    public ServerScheduler schedule(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
        Preconditions.checkNotNull(task, "task must not be null");
        Preconditions.checkNotNull(unit, "unit must not be null");
        LOGGER.atFine().log("Scheduling task with delay %d, period %d %s", delay, period, unit);
        return new BungeeSchedulerWrapper(
                PROXY_SERVER.getScheduler().schedule(BungeeAuthPluginBootstrap.getInstance(), task, delay, period, unit));
    }

    /**
     * Schedules a delayed task.
     *
     * @param task The task to schedule. Must not be null.
     * @param delay The delay before execution.
     * @param unit The time unit. Must not be null.
     * @return A {@link ServerScheduler} instance.
     */
    @Override
    public ServerScheduler schedule(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        Preconditions.checkNotNull(task, "task must not be null");
        Preconditions.checkNotNull(unit, "unit must not be null");
        LOGGER.atFine().log("Scheduling task with delay %d %s", delay, unit);
        return new BungeeSchedulerWrapper(
                PROXY_SERVER.getScheduler().schedule(BungeeAuthPluginBootstrap.getInstance(), task, delay, unit));
    }

    /**
     * Runs a task asynchronously.
     *
     * @param task The task to run. Must not be null.
     */
    @Override
    public void runAsync(@NotNull Runnable task) {
        Preconditions.checkNotNull(task, "task must not be null");
        LOGGER.atFine().log("Running task asynchronously");
        EXECUTOR_SERVICE.execute(task);
    }

    /**
     * Colorizes text using BungeeCord's color system.
     *
     * @param text The text to colorize. Must not be null.
     * @return The colorized text.
     */
    @Override
    public String colorize(@NotNull String text) {
        Preconditions.checkNotNull(text, "text must not be null");
        String result = BungeeComponent.colorText(text);
        LOGGER.atFine().log("Colorized text: %s", result);
        return result;
    }
    // #endregion
}