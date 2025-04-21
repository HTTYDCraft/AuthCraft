package com.httydcraft.authcraft;

import com.httydcraft.authcraft.database.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CacheManager {
    private final Map<UUID, PlayerData> playerCache;

    public CacheManager() {
        this.playerCache = new HashMap<>();
    }

    public void cachePlayer(UUID uuid, PlayerData data) {
        playerCache.put(uuid, data);
    }

    public PlayerData getPlayer(UUID uuid) {
        return playerCache.get(uuid);
    }

    public void removePlayer(UUID uuid) {
        playerCache.remove(uuid);
    }
}
