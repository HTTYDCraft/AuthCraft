package com.httydcraft.authcraft.api.config.duration;

public class ConfigurationDuration {
    private final long millis;

    public ConfigurationDuration(long millis) {
        this.millis = millis;
    }

    public long getMillis() {
        return millis;
    }
}
