package com.httydcraft.authcraft.api.model;

public interface PlayerIdSupplier {
    static PlayerIdSupplier of(String id) {
        return () -> id;
    }

    String getPlayerId();
}
