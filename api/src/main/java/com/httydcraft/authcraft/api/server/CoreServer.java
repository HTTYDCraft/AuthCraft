package com.httydcraft.authcraft.api.server;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.server.bossbar.ServerBossbar;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.server.proxy.ProxyServer;
import com.httydcraft.authcraft.api.server.scheduler.ServerScheduler;
import com.httydcraft.authcraft.api.server.title.ServerTitle;
import com.httydcraft.authcraft.api.util.Castable;

import net.kyori.adventure.audience.Audience;

public interface CoreServer extends Castable<CoreServer> {
    <E> void callEvent(E event);

    List<ServerPlayer> getPlayers();

    Optional<ServerPlayer> getPlayer(UUID uniqueId);

    Optional<ServerPlayer> getPlayer(String name);

    Optional<ServerPlayer> wrapPlayer(Object player);

    Logger getLogger();

    ServerTitle createTitle(ServerComponent title);

    ServerBossbar createBossbar(ServerComponent barText);

    ServerComponent componentPlain(String plain);

    ServerComponent componentJson(String json);

    ServerComponent componentLegacy(String legacy);

    Optional<ProxyServer> serverFromName(String serverName);

    void registerListener(AuthPlugin plugin, Object listener);

    ServerScheduler schedule(Runnable task, long delay, long period, TimeUnit unit);

    ServerScheduler schedule(Runnable task, long delay, TimeUnit milliseconds);

    void runAsync(Runnable task);

    default Audience getAudience(ServerPlayer player) {
        return AuthPlugin.instance().getAudienceProvider().player(player.getUniqueId());
    }

    String colorize(String text);
}
