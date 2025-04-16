package com.httydcraft.authcraft.api.hook;

import com.httydcraft.authcraft.api.server.proxy.ProxyServer;

public interface LimboPluginHook extends PluginHook {

    ProxyServer createServer(String serverName);

}
