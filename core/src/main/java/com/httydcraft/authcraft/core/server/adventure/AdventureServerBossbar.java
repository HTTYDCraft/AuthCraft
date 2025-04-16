package com.httydcraft.authcraft.core.server.adventure;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.server.bossbar.ServerBossbar;
import com.httydcraft.authcraft.api.server.message.AdventureServerComponent;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// #region Class Documentation
/**
 * Abstract base class for Adventure-based boss bars.
 * Extends {@link ServerBossbar} to manage boss bar display and updates using Adventure API.
 */
public abstract class AdventureServerBossbar extends ServerBossbar {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final GsonComponentSerializer GSON_COMPONENT_SERIALIZER = GsonComponentSerializer.gson();
    private final List<ServerPlayer> bossBarPlayers = new ArrayList<>();
    private final BossBar bossBar;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AdventureServerBossbar}.
     *
     * @param component The title component for the boss bar. Must not be null.
     */
    public AdventureServerBossbar(ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        this.title = component;
        BossBar.Color bossBarColor = BossBar.Color.values()[color.ordinal()];
        BossBar.Overlay bossBarOverlay = BossBar.Overlay.values()[segmentStyle.ordinal()];
        bossBar = BossBar.bossBar(GSON_COMPONENT_SERIALIZER.deserialize(component.jsonText()), progress,
                bossBarColor, bossBarOverlay);
        LOGGER.atFine().log("Initialized AdventureServerBossbar with title: %s", component.plainText());
    }
    // #endregion

    // #region Abstract Methods
    /**
     * Shows the boss bar to a player.
     *
     * @param player The player to show the boss bar to. Must not be null.
     * @param bossBar The boss bar to show. Must not be null.
     */
    public abstract void showBossBar(ServerPlayer player, BossBar bossBar);

    /**
     * Hides the boss bar from a player.
     *
     * @param player The player to hide the boss bar from. Must not be null.
     * @param bossBar The boss bar to hide. Must not be null.
     */
    public abstract void hideBossBar(ServerPlayer player, BossBar bossBar);
    // #endregion

    // #region Boss Bar Management
    /**
     * Sends the boss bar to the specified players.
     *
     * @param viewers The players to show the boss bar to. Must not be null.
     * @return This instance for method chaining.
     */
    @Override
    public ServerBossbar send(ServerPlayer... viewers) {
        Preconditions.checkNotNull(viewers, "viewers must not be null");
        for (ServerPlayer player : viewers) {
            Preconditions.checkNotNull(player, "player must not be null");
            showBossBar(player, bossBar);
            bossBarPlayers.add(player);
            LOGGER.atFine().log("Sent boss bar to player: %s", player.getNickname());
        }
        return this;
    }

    /**
     * Removes the boss bar from the specified players.
     *
     * @param viewers The players to hide the boss bar from. Must not be null.
     * @return This instance for method chaining.
     */
    @Override
    public ServerBossbar remove(ServerPlayer... viewers) {
        Preconditions.checkNotNull(viewers, "viewers must not be null");
        for (ServerPlayer player : viewers) {
            Preconditions.checkNotNull(player, "player must not be null");
            hideBossBar(player, bossBar);
            bossBarPlayers.remove(player);
            LOGGER.atFine().log("Removed boss bar from player: %s", player.getNickname());
        }
        return this;
    }

    /**
     * Updates the boss bar's properties.
     *
     * @return This instance for method chaining.
     */
    @Override
    public ServerBossbar update() {
        BossBar.Color bossBarColor = BossBar.Color.values()[color.ordinal()];
        BossBar.Overlay bossBarOverlay = BossBar.Overlay.values()[segmentStyle.ordinal()];
        if (title instanceof AdventureServerComponent adventureComponent) {
            bossBar.name(adventureComponent.component());
        } else {
            bossBar.name(GSON_COMPONENT_SERIALIZER.deserialize(title.jsonText()));
        }
        bossBar.color(bossBarColor).overlay(bossBarOverlay).progress(progress);
        LOGGER.atFine().log("Updated boss bar with color: %s, overlay: %s, progress: %f",
                bossBarColor, bossBarOverlay, progress);
        return this;
    }

    /**
     * Sets the title of the boss bar.
     *
     * @param component The new title component. Must not be null.
     * @return This instance for method chaining.
     */
    @Override
    public ServerBossbar title(ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        super.title(component);
        bossBar.name(GSON_COMPONENT_SERIALIZER.deserialize(component.jsonText()));
        LOGGER.atFine().log("Set boss bar title: %s", component.plainText());
        return this;
    }

    /**
     * Sets the color of the boss bar.
     *
     * @param color The new color. Must not be null.
     * @return This instance for method chaining.
     */
    @Override
    public ServerBossbar color(Color color) {
        Preconditions.checkNotNull(color, "color must not be null");
        super.color(color);
        bossBar.color(BossBar.Color.values()[color.ordinal()]);
        LOGGER.atFine().log("Set boss bar color: %s", color);
        return this;
    }

    /**
     * Sets the style of the boss bar.
     *
     * @param segmentStyle The new style. Must not be null.
     * @return This instance for method chaining.
     */
    @Override
    public ServerBossbar style(Style segmentStyle) {
        Preconditions.checkNotNull(segmentStyle, "segmentStyle must not be null");
        super.style(segmentStyle);
        bossBar.overlay(BossBar.Overlay.values()[segmentStyle.ordinal()]);
        LOGGER.atFine().log("Set boss bar style: %s", segmentStyle);
        return this;
    }

    /**
     * Sets the progress of the boss bar.
     *
     * @param progress The new progress value (0.0 to 1.0).
     * @return This instance for method chaining.
     */
    @Override
    public ServerBossbar progress(float progress) {
        super.progress(progress);
        bossBar.progress(progress);
        LOGGER.atFine().log("Set boss bar progress: %f", progress);
        return this;
    }

    /**
     * Removes the boss bar from all players.
     *
     * @return This instance for method chaining.
     */
    @Override
    public ServerBossbar removeAll() {
        remove(bossBarPlayers.toArray(new ServerPlayer[0]));
        LOGGER.atFine().log("Removed boss bar from all players");
        return this;
    }

    /**
     * Gets the players currently viewing the boss bar.
     *
     * @return An unmodifiable collection of {@link ServerPlayer}s.
     */
    @Override
    public Collection<ServerPlayer> players() {
        Collection<ServerPlayer> players = Collections.unmodifiableList(bossBarPlayers);
        LOGGER.atFine().log("Retrieved %d boss bar players", players.size());
        return players;
    }
    // #endregion
}