package com.httydcraft.authcraft.velocity.api.bossbar;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.velocity.player.VelocityServerPlayer;
import com.httydcraft.authcraft.core.server.adventure.AdventureServerBossbar;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import net.kyori.adventure.bossbar.BossBar;

// #region Class Documentation
/**
 * Velocity-specific implementation of a server boss bar.
 * Extends {@link AdventureServerBossbar} to handle boss bar display for Velocity players.
 */
public class VelocityServerBossbar extends AdventureServerBossbar {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VelocityServerBossbar}.
     *
     * @param component The component to display in the boss bar. Must not be null.
     */
    public VelocityServerBossbar(ServerComponent component) {
        super(Preconditions.checkNotNull(component, "component must not be null"));
        LOGGER.atInfo().log("Initialized VelocityServerBossbar");
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
        player.as(VelocityServerPlayer.class).getPlayer().showBossBar(bossBar);
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
        player.as(VelocityServerPlayer.class).getPlayer().hideBossBar(bossBar);
    }
    // #endregion
}