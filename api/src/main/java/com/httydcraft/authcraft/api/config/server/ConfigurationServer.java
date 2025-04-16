package com.httydcraft.authcraft.api.config.server;

import com.httydcraft.authcraft.api.server.proxy.ProxyServer;

public interface ConfigurationServer {
    ProxyServer asProxyServer();

    String getId();

    int getMaxPlayers();
}