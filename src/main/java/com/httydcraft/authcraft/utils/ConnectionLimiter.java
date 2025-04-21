package com.httydcraft.authcraft.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionLimiter {
    private final int maxConnections;
    private final Map<String, Integer> connectionCounts = new ConcurrentHashMap<>();

    public ConnectionLimiter(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public boolean canConnect(String ip) {
        int count = connectionCounts.getOrDefault(ip, 0);
        if (count >= maxConnections) {
            return false;
        }
        connectionCounts.put(ip, count + 1);
        return true;
    }

    public void disconnect(String ip) {
        connectionCounts.computeIfPresent(ip, (key, count) -> count <= 1 ? null : count - 1);
    }
}