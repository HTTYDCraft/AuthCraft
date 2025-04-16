package com.httydcraft.authcraft.api.step;

import com.httydcraft.authcraft.api.server.player.ServerPlayer;

public interface MessageableAuthenticationStep {
    void process(ServerPlayer player);
}
