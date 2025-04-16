package com.httydcraft.authcraft.bangee.api.title;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.server.title.ServerTitle;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

// #region Class Documentation
/**
 * BungeeCord-specific implementation of a server title.
 * Extends {@link ServerTitle} to display titles and subtitles using BungeeCord's title API.
 */
public class BungeeServerTitle extends ServerTitle {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final Title bungeeTitle;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BungeeServerTitle}.
     *
     * @param title The server component for the title text. Must not be null.
     */
    public BungeeServerTitle(@NotNull ServerComponent title) {
        super();
        this.bungeeTitle = Preconditions.checkNotNull(ProxyServer.getInstance().createTitle(),
                "Bungee Title must not be null");
        title(Preconditions.checkNotNull(title, "title must not be null"));
        LOGGER.atInfo().log("Initialized BungeeServerTitle with title: %s", title.plainText());
    }
    // #endregion

    // #region Title Methods
    /**
     * Sets the title text.
     *
     * @param title The server component for the title. Must not be null.
     * @return This {@code BungeeServerTitle} instance.
     */
    @Override
    public ServerTitle title(@NotNull ServerComponent title) {
        Preconditions.checkNotNull(title, "title must not be null");
        LOGGER.atFine().log("Setting title: %s", title.plainText());
        super.title(title);
        bungeeTitle.title(ComponentSerializer.parse(title.jsonText()));
        return this;
    }

    /**
     * Sets the subtitle text.
     *
     * @param subtitle The server component for the subtitle. Must not be null.
     * @return This {@code BungeeServerTitle} instance.
     */
    @Override
    public ServerTitle subtitle(@NotNull ServerComponent subtitle) {
        Preconditions.checkNotNull(subtitle, "subtitle must not be null");
        LOGGER.atFine().log("Setting subtitle: %s", subtitle.plainText());
        super.subtitle(subtitle);
        bungeeTitle.subTitle(ComponentSerializer.parse(subtitle.jsonText()));
        return this;
    }

    /**
     * Sends the title to the specified players.
     *
     * @param players The players to send the title to. Must not be null or contain null elements.
     * @return This {@code BungeeServerTitle} instance.
     */
    @Override
    public ServerTitle send(@NotNull ServerPlayer... players) {
        Preconditions.checkNotNull(players, "players must not be null");
        for (ServerPlayer player : players) {
            Preconditions.checkNotNull(player, "player in players array must not be null");
        }
        LOGGER.atFine().log("Sending title to %d players", players.length);
        bungeeTitle.fadeIn(fadeIn);
        bungeeTitle.stay(stay);
        bungeeTitle.fadeOut(fadeOut);
        for (ServerPlayer player : players) {
            bungeeTitle.send(player.getRealPlayer());
        }
        return this;
    }
    // #endregion
}