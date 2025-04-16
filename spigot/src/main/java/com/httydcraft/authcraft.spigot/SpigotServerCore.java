package com.httydcraft.authcraft;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.bossbar.SpigotServerBossbar;
import com.httydcraft.authcraft.api.title.SpigotServerTitle;
import com.httydcraft.authcraft.component.SpigotComponent;
import com.httydcraft.authcraft.player.SpigotServerPlayer;
import com.httydcraft.authcraft.scheduler.SpigotSchedulerWrapper;
import com.httydcraft.authcraft.server.ServerCore;
import com.httydcraft.authcraft.server.bossbar.ServerBossbar;
import com.httydcraft.authcraft.server.message.ServerComponent;
import com.httydcraft.authcraft.server.player.ServerPlayer;
import com.httydcraft.authcraft.server.proxy.ProxyServer;
import com.httydcraft.authcraft.server.scheduler.ServerScheduler;
import com.httydcraft.authcraft.server.title.ServerTitle;
import com.httydcraft.authcraft.server.SpigotServer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

// #region Class Documentation
/**
 * Spigot-specific implementation of the server core.
 * Implements {@link ServerCore} to manage players, events, scheduling, and components.
 */
public class SpigotServerCore implements ServerCore {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final JavaPlugin plugin;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code SpigotServerCore}.
     *
     * @param plugin The Spigot plugin instance. Must not be null.
     */
    public SpigotServerCore(JavaPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin must not be null");
        LOGGER.atInfo().log("Initialized SpigotServerCore");
    }
    // #endregion

    // #region ServerCore Implementation
    /**
     * Calls an event on the server.
     *
     * @param event The event to call. Must not be null.
     * @param <E> The event type.
     */
    @Override
    public <E> void callEvent(E event) {
        Preconditions.checkNotNull(event, "event must not be null");
        if (!(event instanceof Event)) {
            LOGGER.atWarning().log("Event %s is not a Bukkit event, skipping", event.getClass().getSimpleName());
            return;
        }
        LOGGER.atFine().log("Calling event: %s", event.getClass().getSimpleName());
        Bukkit.getPluginManager().callEvent((Event) event);
    }

    /**
     * Gets all online players.
     *
     * @return An immutable list of {@link ServerPlayer}.
     */
    @Override
    public List<ServerPlayer> getPlayers() {
        List<ServerPlayer> result = Bukkit.getOnlinePlayers().stream()
                .map(SpigotServerPlayer::new)
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
        Optional<ServerPlayer> result = Optional.ofNullable(Bukkit.getPlayer(uniqueId))
                .map(SpigotServerPlayer::new);
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
        Optional<ServerPlayer> result = Optional.ofNullable(Bukkit.getPlayer(name))
                .map(SpigotServerPlayer::new);
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
        if (player instanceof org.bukkit.entity.Player) {
            LOGGER.atFine().log("Wrapping Spigot Player into ServerPlayer");
            return Optional.of(new SpigotServerPlayer((org.bukkit.entity.Player) player));
        }
        LOGGER.atWarning().log("Cannot wrap unknown player type: %s", player.getClass().getName());
        return Optional.empty();
    }

    /**
     * Gets the server logger.
     *
     * @return The {@link Logger}.
     */
    @Override
    public Logger getLogger() {
        Logger result = plugin.getLogger();
        LOGGER.atFine().log("Retrieved server logger");
        return result;
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
        return new SpigotServerTitle(title);
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
        return new SpigotServerBossbar(component);
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
        return new SpigotComponent(PlainTextComponentSerializer.plainText().deserialize(plain));
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
        return new SpigotComponent(GsonComponentSerializer.gson().deserialize(json));
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
        return new SpigotComponent(LegacyComponentSerializer.legacyAmpersand().deserialize(legacy));
    }

    /**
     * Gets a server by name (returns Spigot server if name matches, else empty).
     *
     * @param serverName The server name. Must not be null.
     * @return An optional {@link ProxyServer}.
     */
    @Override
    public Optional<com.httydcraft.authcraft.server.proxy.ProxyServer> serverFromName(String serverName) {
        Preconditions.checkNotNull(serverName, "serverName must not be null");
        if (serverName.equals(Bukkit.getServer().getName())) {
            LOGGER.atFine().log("Retrieved Spigot server for name: %s", serverName);
            return Optional.of(new SpigotServer());
        }
        LOGGER.atFine().log("No server found for name: %s", serverName);
        return Optional.empty();
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
        if (!(listener instanceof Listener)) {
            LOGGER.atWarning().log("Listener %s is not a Bukkit Listener, skipping", listener.getClass().getSimpleName());
            return;
        }
        LOGGER.atFine().log("Registering listener: %s", listener.getClass().getSimpleName());
        Bukkit.getPluginManager().registerEvents((Listener) listener, this.plugin);
    }

    /**
     * Schedules a repeating task.
     *
     * @param task The task to schedule. Must not be null.
     * @param delay The initial delay in ticks. Must be non-negative.
     * @param period The repeat period in ticks. Must be positive.
     * @param unit The time unit (ignored in Spigot, uses ticks).
     * @return A {@link ServerScheduler}.
     */
    @Override
    public ServerScheduler schedule(Runnable task, long delay, long period, TimeUnit unit) {
        Preconditions.checkNotNull(task, "task must not be null");
        Preconditions.checkArgument(delay >= 0, "delay must be non-negative");
        Preconditions.checkArgument(period > 0, "period must be positive");
        LOGGER.atFine().log("Scheduling repeating task with delay %d and period %d ticks", delay, period);
        return new SpigotSchedulerWrapper(
                Bukkit.getScheduler().runTaskTimer(this.plugin, task, delay, period)
        );
    }

    /**
     * Schedules a delayed task.
     *
     * @param task The task to schedule. Must not be null.
     * @param delay The delay in ticks. Must be non-negative.
     * @param unit The time unit (ignored in Spigot, uses ticks).
     * @return A {@link ServerScheduler}.
     */
    @Override
    public ServerScheduler schedule(Runnable task, long delay, TimeUnit unit) {
        Preconditions.checkNotNull(task, "task must not be null");
        Preconditions.checkArgument(delay >= 0, "delay must be non-negative");
        LOGGER.atFine().log("Scheduling delayed task with delay %d ticks", delay);
        return new SpigotSchedulerWrapper(
                Bukkit.getScheduler().runTaskLater(this.plugin, task, delay)
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
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task);
    }

    /**
     * Colorizes text using Bukkit's ChatColor.
     *
     * @param text The text to colorize. Must not be null.
     * @return The colorized text.
     */
    @Override
    public String colorize(String text) {
        Preconditions.checkNotNull(text, "text must not be null");
        String result = org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
        LOGGER.atFine().log("Colorized text: %s", result);
        return result;
    }
    // #endregion
}