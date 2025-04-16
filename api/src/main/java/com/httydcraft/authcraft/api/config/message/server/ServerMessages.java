package com.httydcraft.authcraft.api.config.message.server;

import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.api.server.message.ServerComponent;

public interface ServerMessages extends Messages<ServerComponent> {
    ComponentDeserializer getDeserializer();
}
