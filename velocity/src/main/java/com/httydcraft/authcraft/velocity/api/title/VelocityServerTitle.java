package com.httydcraft.authcraft.velocity.api.title;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.velocity.player.VelocityServerPlayer;
import com.httydcraft.authcraft.api.server.message.AdventureServerComponent;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.server.title.ServerTitle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;

// #region Class Documentation
/**
 * Velocity-specific implementation of a server title.
 * Extends {@link ServerTitle} to handle title and subtitle display for Velocity players.
 */
public class VelocityServerTitle extends ServerTitle {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final int MILLIS_PER_TICK = 1000 / 20;
    private Component titleComponent = Component.empty();
    private Component subTitleComponent = Component.empty();

    // #endregion

    // #region Constructors
    /**
     * Constructs a new {@code VelocityServerTitle} with a title component.
     *
     * @param title The title component. Must not be null.
     */
    public VelocityServerTitle(ServerComponent title) {
        super();
        title(Preconditions.checkNotNull(title, "title must not be null"));
        LOGGER.atInfo().log("Initialized VelocityServerTitle with title");
    }

    /**
     * Constructs a new {@code VelocityServerTitle} with default values.
     */
    public VelocityServerTitle() {
        super();
        LOGGER.atInfo().log("Initialized VelocityServerTitle with default values");
    }
    // #endregion

    // #region Component Setters
    /**
     * Sets the title component.
     *
     * @param titleComponent The title component to set. Must not be null.
     */
    public void setTitleComponent(Component titleComponent) {
        this.titleComponent = Preconditions.checkNotNull(titleComponent, "titleComponent must not be null");
        LOGGER.atFine().log("Set title component");
    }

    /**
     * Sets the subtitle component.
     *
     * @param subTitleComponent The subtitle component to set. Must not be null.
     */
    public void setSubTitleComponent(Component subTitleComponent) {
        this.subTitleComponent = Preconditions.checkNotNull(subTitleComponent, "subTitleComponent must not be null");
        LOGGER.atFine().log("Set subtitle component");
    }
    // #endregion

    // #region Title Methods
    /**
     * Sets the title component for this title.
     *
     * @param title The title component. Must not be null.
     * @return This {@code VelocityServerTitle} instance.
     */
    @Override
    public ServerTitle title(ServerComponent title) {
        Preconditions.checkNotNull(title, "title must not be null");
        super.title(title);
        title.safeAs(AdventureServerComponent.class)
                .map(AdventureServerComponent::component)
                .ifPresent(this::setTitleComponent);
        LOGGER.atFine().log("Updated title");
        return this;
    }

    /**
     * Sets the subtitle component for this title.
     *
     * @param subtitle The subtitle component. Must not be null.
     * @return This {@code VelocityServerTitle} instance.
     */
    @Override
    public ServerTitle subtitle(ServerComponent subtitle) {
        Preconditions.checkNotNull(subtitle, "subtitle must not be null");
        super.subtitle(subtitle);
        subtitle.safeAs(AdventureServerComponent.class)
                .map(AdventureServerComponent::component)
                .ifPresent(this::setSubTitleComponent);
        LOGGER.atFine().log("Updated subtitle");
        return this;
    }

    /**
     * Sets the fade-in duration in ticks.
     *
     * @param ticks The number of ticks for fade-in. Must be non-negative.
     * @return This {@code VelocityServerTitle} instance.
     */
    @Override
    public ServerTitle fadeIn(int ticks) {
        Preconditions.checkArgument(ticks >= 0, "ticks must be non-negative");
        LOGGER.atFine().log("Set fade-in to %d ticks", ticks);
        return super.fadeIn(ticks * MILLIS_PER_TICK);
    }

    /**
     * Sets the stay duration in ticks.
     *
     * @param ticks The number of ticks for stay. Must be non-negative.
     * @return This {@code VelocityServerTitle} instance.
     */
    @Override
    public ServerTitle stay(int ticks) {
        Preconditions.checkArgument(ticks >= 0, "ticks must be non-negative");
        LOGGER.atFine().log("Set stay to %d ticks", ticks);
        return super.stay(ticks * MILLIS_PER_TICK);
    }

    /**
     * Sets the fade-out duration in ticks.
     *
     * @param ticks The number of ticks for fade-out. Must be non-negative.
     * @return This {@code VelocityServerTitle} instance.
     */
    @Override
    public ServerTitle fadeOut(int ticks) {
        Preconditions.checkArgument(ticks >= 0, "ticks must be non-negative");
        LOGGER.atFine().log("Set fade-out to %d ticks", ticks);
        return super.fadeOut(ticks * MILLIS_PER_TICK);
    }

    /**
     * Sends the title to the specified players.
     *
     * @param players The players to send the title to. Must not be null.
     * @return This {@code VelocityServerTitle} instance.
     */
    @Override
    public ServerTitle send(ServerPlayer... players) {
        Preconditions.checkNotNull(players, "players must not be null");
        Title createdTitle = Title.title(
                titleComponent,
                subTitleComponent,
                Title.Times.of(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut))
        );
        for (ServerPlayer player : players) {
            Preconditions.checkNotNull(player, "player in players array must not be null");
            LOGGER.atFine().log("Sending title to player: %s", player.getNickname());
            player.as(VelocityServerPlayer.class).getPlayer().showTitle(createdTitle);
        }
        return this;
    }
    // #endregion
}