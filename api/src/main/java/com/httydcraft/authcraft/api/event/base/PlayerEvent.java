package com.httydcraft.authcraft.api.event.base;

import com.httydcraft.authcraft.api.server.player.ServerPlayer;

import io.github.revxrsal.eventbus.gen.Index;

public interface PlayerEvent {
    @Index(0)
    ServerPlayer getPlayer();
}
