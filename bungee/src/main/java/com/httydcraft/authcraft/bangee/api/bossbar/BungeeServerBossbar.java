package com.httydcraft.authcraft.bangee.api.bossbar;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.bangee.BungeeAuthPluginBootstrap;
import com.httydcraft.authcraft.bangee.player.BungeeServerPlayer;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.core.server.adventure.AdventureServerBossbar;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import org.jetbrains.annotations.NotNull;

// #region Class Documentation
/**
 * BungeeCord-specific implementation of a server boss bar.
 * Extends {@link AdventureServerBossbar} to display boss bars using BungeeCord's Adventure API.
 */
public class BungeeServerBossbar extends AdventureServerBossbar {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final BungeeAudiences audiences;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BungeeServerBossbar}.
     *
     * @param component The server component for the boss bar's text. Must not be null.
     */
    public BungeeServerBossbar(@NotNull ServerComponent component) {
        super(Preconditions.checkNotNull(component, "component must not be null"));
        this.audiences = Preconditions.checkNotNull(BungeeAuthPluginBootstrap.getInstance().getBungeeAudiences(),
                "BungeeAudiences must not be null");
        LOGGER.atInfo().log("Initialized BungeeServerBossbar with component: %s", component.plainText());
    }
    // #endregion

    // #region Boss Bar Methods
    /**
     * Shows the boss bar to the specified player.
     *
     * @param player The player to show the boss bar to. Must not be null.
     * @param bossBar The Adventure boss bar to display. Must not be null.
     */
    @Override
    public void showBossBar(@NotNull ServerPlayer player, @NotNull BossBar bossBar) {
        Preconditions.checkNotNull(player, "player must not be null");
        Preconditions.checkNotNull(bossBar, "bossBar must not be null");
        LOGGER.atFine().log("Showing boss bar to player: %s", player.getNickname());
        audiences.player(player.as(BungeeServerPlayer.class).getBungeePlayer()).showBossBar(bossBar);
    }

    /**
     * Hides the boss bar from the specified player.
     *
     * @param player The player to hide the boss bar from. Must not be null.
     * @param bossBar The Adventure boss bar to hide. Must not be null.
     */
    @Override
    public void hideBossBar(@NotNull ServerPlayer player, @NotNull BossBar bossBar) {
        Preconditions.checkNotNull(player, "player must not be null");
        Preconditions.checkNotNull(bossBar, "bossBar must not be null");
        LOGGER.atFine().log("Hiding boss bar from player: %s", player.getNickname());
        audiences.player(player.as(BungeeServerPlayer.class).getBungeePlayer()).hideBossBar(bossBar);
    }
    // #endregion
}