package com.httydcraft.authcraft;

import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class ConnectionLimiter {
    private final AuthCraft plugin;
    private final MessageUtils messageUtils;
    private final Map<InetAddress, Integer> connections;

    public ConnectionLimiter(AuthCraft plugin, MessageUtils messageUtils) {
        this.plugin = plugin;
        this.messageUtils = messageUtils;
        this.connections = new HashMap<>();
    }

    public boolean allowConnection(Player player) {
        InetAddress address = player.getAddress().getAddress();
        int maxConnections = plugin.getConfig().getInt("connection_limit", 3);
        int current = connections.getOrDefault(address, 0);
        if (current >= maxConnections) {
            messageUtils.sendMessage(player, "connection_limit.exceeded");
            return false;
        }
        connections.put(address, current + 1);
        return true;
    }

    public void removeConnection(Player player) {
        InetAddress address = player.getAddress().getAddress();
        int current = connections.getOrDefault(address, 1);
        if (current <= 1) {
            connections.remove(address);
        } else {
            connections.put(address, current - 1);
        }
    }
}
