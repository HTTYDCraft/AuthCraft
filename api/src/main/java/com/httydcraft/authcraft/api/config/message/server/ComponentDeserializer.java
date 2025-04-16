package com.httydcraft.authcraft.api.config.message.server;

import com.httydcraft.authcraft.api.server.message.ServerComponent;

public interface ComponentDeserializer {
    ServerComponent deserialize(String rawText);
}
