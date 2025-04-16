package com.httydcraft.authcraft.api.bossbar;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.player.SpigotServerPlayer;
import com.httydcraft.authcraft.server.adventure.AdventureServerBossbar;
import com.httydcraft.authcraft.server.message.ServerComponent;
import com.httydcraft.authcraft.server.player.ServerPlayer;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;

// #region Class Documentation
/**
 * Spigot-specific implementation of a server boss bar.
 * Extends {@link AdventureServerBossbar} to handle boss bar display for Spigot players.
 */
public class SpigotServerBossbar extends AdventureServerBossbar {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code SpigotServerBossbar}.
     *
     * @param component The component to display in the boss bar. Must not be null.
     */
    public SpigotServerBossbar(ServerComponent component) {
        super(Preconditions.checkNotNull(component, "component must not be null"));
        LOGGER.atInfo().log("Initialized SpigotServerBossbar");
    }
    // #endregion

    // #region Boss Bar Methods
    /**
     * Shows the boss bar to the specified player.
     *
     * @param player The player to show the boss bar to. Must not be null.
     * @param bossBar The boss bar to display. Must not be null.
     */
    @Override
    public void showBossBar(ServerPlayer player, BossBar bossBar) {
        Preconditions.checkNotNull(player, "player must not be null");
        Preconditions.checkNotNull(bossBar, "bossBar must not be null");
        LOGGER.atFine().log("Showing boss bar to player: %s", player.getNickname());
        Bukkit.getServer().adventure().player(player.as(SpigotServerPlayer.class).getPlayer()).showBossBar(bossBar);
    }

    /**
     * Hides the boss bar from the specified player.
     *
     * @param player The player to hide the boss bar from. Must not be null.
     * @param bossBar The boss bar to hide. Must not be null.
     */
    @Override
    public void hideBossBar(ServerPlayer player, BossBar bossBar) {
        Preconditions.checkNotNull(player, "player must not be null");
        Preconditions.checkNotNull(bossBar, "bossBar must not be null");
        LOGGER.atFine().log("Hiding boss bar from player: %s", player.getNickname());
        Bukkit.getServer().adventure().player(player.as(SpigotServerPlayer.class).getPlayer()).hideBossBar(bossBar);
    }
    // #endregion
}