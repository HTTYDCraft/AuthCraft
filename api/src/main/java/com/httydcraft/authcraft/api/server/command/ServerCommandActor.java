package com.httydcraft.authcraft.api.server.command;

import com.httydcraft.authcraft.api.server.message.ServerComponent;

public interface ServerCommandActor {
    void reply(ServerComponent component);
}