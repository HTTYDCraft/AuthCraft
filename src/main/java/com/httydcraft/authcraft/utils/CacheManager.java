package com.httydcraft.authcraft.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    public void put(String key, Object value) {
        cache.put(key, value);
    }

    public Object get(String key) {
        return cache.get(key);
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }
}