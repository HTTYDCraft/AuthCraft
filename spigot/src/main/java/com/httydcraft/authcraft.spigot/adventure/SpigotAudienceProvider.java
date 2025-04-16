package com.httydcraft.authcraft.adventure;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

// #region Class Documentation
/**
 * Provides audience-based messaging for Spigot using Kyori Adventure.
 * Implements {@link AudienceProvider} to manage audiences for players, console, and permissions.
 */
public class SpigotAudienceProvider implements AudienceProvider {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final Plugin plugin;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code SpigotAudienceProvider}.
     *
     * @param plugin The Spigot plugin instance. Must not be null.
     */
    public SpigotAudienceProvider(Plugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin must not be null");
        LOGGER.atInfo().log("Initialized SpigotAudienceProvider");
    }
    // #endregion

    // #region Audience Methods
    /**
     * Returns an audience containing all online players and the console.
     *
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience all() {
        LOGGER.atFine().log("Retrieved audience for all");
        return Audience.audience(Bukkit.getOnlinePlayers().stream()
                .map(p -> plugin.getServer().adventure().player(p))
                .collect(ImmutableList.toImmutableList()));
    }

    /**
     * Returns an audience for the console.
     *
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience console() {
        LOGGER.atFine().log("Retrieved console audience");
        return plugin.getServer().adventure().console();
    }

    /**
     * Returns an audience containing all online players.
     *
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience players() {
        LOGGER.atFine().log("Retrieved players audience");
        return Audience.audience(Bukkit.getOnlinePlayers().stream()
                .map(p -> plugin.getServer().adventure().player(p))
                .collect(ImmutableList.toImmutableList()));
    }

    /**
     * Returns an audience for a specific player by their UUID.
     *
     * @param playerId The player's UUID. Must not be null.
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience player(@NotNull UUID playerId) {
        Preconditions.checkNotNull(playerId, "playerId must not be null");
        LOGGER.atFine().log("Retrieved audience for player UUID: %s", playerId);
        Player player = Bukkit.getPlayer(playerId);
        return player != null ? plugin.getServer().adventure().player(player) : Audience.empty();
    }

    /**
     * Returns an audience for players with the specified permission.
     *
     * @param permission The permission to check. Must not be null.
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience permission(@NotNull String permission) {
        Preconditions.checkNotNull(permission, "permission must not be null");
        LOGGER.atFine().log("Retrieved audience for permission: %s", permission);
        return Audience.audience(Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(permission))
                .map(p -> plugin.getServer().adventure().player(p))
                .collect(ImmutableList.toImmutableList()));
    }

    /**
     * Returns an audience for players in a specific world.
     *
     * @param world The world key. Must not be null.
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience world(@NotNull Key world) {
        Preconditions.checkNotNull(world, "world must not be null");
        LOGGER.atFine().log("Retrieved audience for world: %s", world);
        return Audience.audience(Bukkit.getWorlds().stream()
                .filter(w -> w.getKey().equals(world))
                .flatMap(w -> w.getPlayers().stream())
                .map(p -> plugin.getServer().adventure().player(p))
                .collect(ImmutableList.toImmutableList()));
    }

    /**
     * Returns an audience for a server (not applicable in Spigot, returns empty).
     *
     * @param serverName The server name. Must not be null.
     * @return A non-null {@link Audience}.
     */
    @Override
    public @NotNull Audience server(@NotNull String serverName) {
        Preconditions.checkNotNull(serverName, "serverName must not be null");
        LOGGER.atFine().log("Server audience not supported in Spigot, returning empty");
        return Audience.empty();
    }
    // #endregion

    // #region Utility Methods
    /**
     * Returns the component flattener for text rendering.
     *
     * @return A non-null {@link ComponentFlattener}.
     */
    @Override
    public @NotNull ComponentFlattener flattener() {
        LOGGER.atFine().log("Retrieved component flattener");
        return ComponentFlattener.basic();
    }

    /**
     * Closes the audience provider.
     * No-op in this implementation.
     */
    @Override
    public void close() {
        LOGGER.atFine().log("Audience provider closed (no-op)");
    }
    // #endregion
}