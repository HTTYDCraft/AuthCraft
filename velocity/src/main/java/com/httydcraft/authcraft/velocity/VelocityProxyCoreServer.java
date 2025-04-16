package com.httydcraft.authcraft.velocity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.velocity.api.bossbar.VelocityServerBossbar;
import com.httydcraft.authcraft.api.server.CoreServer;
import com.httydcraft.authcraft.velocity.api.title.VelocityServerTitle;
import com.httydcraft.authcraft.velocity.component.VelocityComponent;
import com.httydcraft.authcraft.api.hook.LimboPluginHook;
import com.httydcraft.authcraft.velocity.player.VelocityServerPlayer;
import com.httydcraft.authcraft.velocity.scheduler.VelocitySchedulerWrapper;
import com.httydcraft.authcraft.api.server.bossbar.ServerBossbar;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.server.proxy.ProxyServer;
import com.httydcraft.authcraft.api.server.scheduler.ServerScheduler;
import com.httydcraft.authcraft.api.server.title.ServerTitle;
import com.httydcraft.authcraft.velocity.server.VelocityProxyServer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

// #region Class Documentation
/**
 * Velocity-specific implementation of the server core.
 * Implements {@link CoreServer} to manage players, events, scheduling, and components.
 */
public class VelocityProxyCoreServer implements CoreServer {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ProxyServer server;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VelocityProxyCoreServer}.
     *
     * @param server The Velocity proxy server instance. Must not be null.
     */
    public VelocityProxyCoreServer(ProxyServer server) {
        this.server = Preconditions.checkNotNull(server, "server must not be null");
        LOGGER.atInfo().log("Initialized VelocityProxyCoreServer");
    }
    // #endregion

    // #region CoreServer Implementation
    /**
     * Calls an event on the server.
     *
     * @param event The event to call. Must not be null.
     * @param <E> The event type.
     */
    @Override
    public <E> void callEvent(E event) {
        Preconditions.checkNotNull(event, "event must not be null");
        LOGGER.atFine().log("Calling event: %s", event.getClass().getSimpleName());
        server.getEventManager().fireAndForget(event);
    }

    /**
     * Gets all online players.
     *
     * @return An immutable list of {@link ServerPlayer}.
     */
    @Override
    public List<ServerPlayer> getPlayers() {
        List<ServerPlayer> result = server.getAllPlayers()
                .stream()
                .map(VelocityServerPlayer::new)
                .collect(ImmutableList.toImmutableList());
        LOGGER.atFine().log("Retrieved %d online players", result.size());
        return result;
    }

    /**
     * Gets a player by UUID.
     *
     * @param uniqueId The player's UUID. Must not be null.
     * @return An optional {@link ServerPlayer}.
     */
    @Override
    public Optional<ServerPlayer> getPlayer(UUID uniqueId) {
        Preconditions.checkNotNull(uniqueId, "uniqueId must not be null");
        Optional<ServerPlayer> result = server.getPlayer(uniqueId).map(VelocityServerPlayer::new);
        LOGGER.atFine().log("Retrieved player by UUID %s: %s", uniqueId, result.isPresent() ? "found" : "not found");
        return result;
    }

    /**
     * Gets a player by name.
     *
     * @param name The player's name. Must not be null.
     * @return An optional {@link ServerPlayer}.
     */
    @Override
    public Optional<ServerPlayer> getPlayer(String name) {
        Preconditions.checkNotNull(name, "name must not be null");
        Optional<ServerPlayer> result = server.getPlayer(name).map(VelocityServerPlayer::new);
        LOGGER.atFine().log("Retrieved player by name %s: %s", name, result.isPresent() ? "found" : "not found");
        return result;
    }

    /**
     * Wraps a player object into a {@link ServerPlayer}.
     *
     * @param player The player object to wrap. May be null.
     * @return An optional {@link ServerPlayer}.
     */
    @Override
    public Optional<ServerPlayer> wrapPlayer(Object player) {
        if (player == null) {
            LOGGER.atFine().log("Player object is null, returning empty");
            return Optional.empty();
        }
        if (player instanceof ServerPlayer) {
            LOGGER.atFine().log("Player is already ServerPlayer, returning as is");
            return Optional.of((ServerPlayer) player);
        }
        if (player instanceof Player) {
            LOGGER.atFine().log("Wrapping Velocity Player into ServerPlayer");
            return Optional.of(new VelocityServerPlayer((Player) player));
        }
        LOGGER.atWarning().log("Cannot wrap unknown player type: %s", player.getClass().getName());
        return Optional.empty();
    }

    /**
     * Gets the server logger.
     *
     * @return The {@link Logger}, or null if unavailable.
     */
    @Override
    public Logger getLogger() {
        LOGGER.atWarning().log("Logger not implemented, returning null");
        return null;
    }

    /**
     * Creates a new title with the specified component.
     *
     * @param title The title component. Must not be null.
     * @return A {@link ServerTitle}.
     */
    @Override
    public ServerTitle createTitle(ServerComponent title) {
        Preconditions.checkNotNull(title, "title must not be null");
        LOGGER.atFine().log("Creating new title");
        return new VelocityServerTitle(title);
    }

    /**
     * Creates a new boss bar with the specified component.
     *
     * @param component The boss bar component. Must not be null.
     * @return A {@link ServerBossbar}.
     */
    @Override
    public ServerBossbar createBossbar(ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        LOGGER.atFine().log("Creating new boss bar");
        return new VelocityServerBossbar(component);
    }

    /**
     * Creates a component from plain text.
     *
     * @param plain The plain text. Must not be null.
     * @return A {@link ServerComponent}.
     */
    @Override
    public ServerComponent componentPlain(String plain) {
        Preconditions.checkNotNull(plain, "plain must not be null");
        LOGGER.atFine().log("Creating plain text component");
        return new VelocityComponent(PlainTextComponentSerializer.plainText().deserialize(plain));
    }

    /**
     * Creates a component from JSON text.
     *
     * @param json The JSON text. Must not be null.
     * @return A {@link ServerComponent}.
     */
    @Override
    public ServerComponent componentJson(String json) {
        Preconditions.checkNotNull(json, "json must not be null");
        LOGGER.atFine().log("Creating JSON text component");
        return new VelocityComponent(GsonComponentSerializer.gson().deserialize(json));
    }

    /**
     * Creates a component from legacy text.
     *
     * @param legacy The legacy text. Must not be null.
     * @return A {@link ServerComponent}.
     */
    @Override
    public ServerComponent componentLegacy(String legacy) {
        Preconditions.checkNotNull(legacy, "legacy must not be null");
        LOGGER.atFine().log("Creating legacy text component");
        return new VelocityComponent(LegacyComponentSerializer.legacyAmpersand().deserialize(legacy));
    }

    /**
     * Gets a server by name, creating a limbo server if necessary.
     *
     * @param serverName The server name. Must not be null.
     * @return An optional {@link ProxyServer}.
     */
    @Override
    public Optional<ProxyServer> serverFromName(String serverName) {
        Preconditions.checkNotNull(serverName, "serverName must not be null");
        Optional<RegisteredServer> serverOptional = server.getServer(serverName);
        LimboPluginHook limboHook = AuthPlugin.instance().getHook(LimboPluginHook.class);
        if (!serverOptional.isPresent() && limboHook != null) {
            LOGGER.atFine().log("Creating limbo server for name: %s", serverName);
            return Optional.of(limboHook.createServer(serverName));
        }
        Optional<ProxyServer> result = serverOptional.map(VelocityProxyServer::new);
        LOGGER.atFine().log("Retrieved server %s: %s", serverName, result.isPresent() ? "found" : "not found");
        return result;
    }

    /**
     * Registers a listener for the plugin.
     *
     * @param plugin The plugin instance. Must not be null.
     * @param listener The listener to register. Must not be null.
     */
    @Override
    public void registerListener(AuthPlugin plugin, Object listener) {
        Preconditions.checkNotNull(plugin, "plugin must not be null");
        Preconditions.checkNotNull(listener, "listener must not be null");
        LOGGER.atFine().log("Registering listener: %s", listener.getClass().getSimpleName());
        server.getEventManager().register(plugin, listener);
    }

    /**
     * Schedules a repeating task.
     *
     * @param task The task to schedule. Must not be null.
     * @param delay The initial delay. Must be non-negative.
     * @param period The repeat period. Must be positive.
     * @param unit The time unit. Must not be null.
     * @return A {@link ServerScheduler}.
     */
    @Override
    public ServerScheduler schedule(Runnable task, long delay, long period, TimeUnit unit) {
        Preconditions.checkNotNull(task, "task must not be null");
        Preconditions.checkNotNull(unit, "unit must not be null");
        Preconditions.checkArgument(delay >= 0, "delay must be non-negative");
        Preconditions.checkArgument(period > 0, "period must be positive");
        LOGGER.atFine().log("Scheduling repeating task with delay %d and period %d %s", delay, period, unit);
        return new VelocitySchedulerWrapper(
                server.getScheduler()
                        .buildTask(VelocityAuthPluginBootstrap.getInstance(), task)
                        .delay(delay, unit)
                        .repeat(period, unit)
                        .schedule()
        );
    }

    /**
     * Schedules a delayed task.
     *
     * @param task The task to schedule. Must not be null.
     * @param delay The delay. Must be non-negative.
     * @param unit The time unit. Must not be null.
     * @return A {@link ServerScheduler}.
     */
    @Override
    public ServerScheduler schedule(Runnable task, long delay, TimeUnit unit) {
        Preconditions.checkNotNull(task, "task must not be null");
        Preconditions.checkNotNull(unit, "unit must not be null");
        Preconditions.checkArgument(delay >= 0, "delay must be non-negative");
        LOGGER.atFine().log("Scheduling delayed task with delay %d %s", delay, unit);
        return new VelocitySchedulerWrapper(
                server.getScheduler()
                        .buildTask(VelocityAuthPluginBootstrap.getInstance(), task)
                        .delay(delay, unit)
                        .schedule()
        );
    }

    /**
     * Runs a task asynchronously.
     *
     * @param task The task to run. Must not be null.
     */
    @Override
    public void runAsync(Runnable task) {
        Preconditions.checkNotNull(task, "task must not be null");
        LOGGER.atFine().log("Running async task");
        server.getScheduler().buildTask(VelocityAuthPluginBootstrap.getInstance(), task).schedule();
    }

    /**
     * Colorizes text (no-op in Velocity).
     *
     * @param text The text to colorize. Must not be null.
     * @return The input text.
     */
    @Override
    public String colorize(String text) {
        Preconditions.checkNotNull(text, "text must not be null");
        LOGGER.atFine().log("Colorizing text (no-op): %s", text);
        return text;
    }
    // #endregion
}