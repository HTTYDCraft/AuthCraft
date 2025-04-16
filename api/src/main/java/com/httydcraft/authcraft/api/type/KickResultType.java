package com.httydcraft.authcraft.api.type;

public enum KickResultType {
    KICKED("kicked"), PLAYER_OFFLINE("player-offline");
    private final String configurationPath;

    KickResultType(String configurationPath) {
        this.configurationPath = configurationPath;
    }

    public String getConfigurationPath() {
        return configurationPath;
    }
}
