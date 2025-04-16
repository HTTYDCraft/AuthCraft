package com.httydcraft.authcraft.api.type;

import java.util.Optional;

import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;

public enum IdentifierType {
    UUID {
        public String getId(ServerPlayer player) {
            return player.getUniqueId().toString();
        }

        public Optional<ServerPlayer> getPlayer(String id) {
            return AuthPlugin.instance().getCore().getPlayer(java.util.UUID.fromString(id));
        }

        public String fromRawString(String uuid) {
            return uuid;
        }
    }, NAME {
        public String getId(ServerPlayer player) {
            return player.getNickname().toLowerCase();
        }

        public Optional<ServerPlayer> getPlayer(String id) {
            return AuthPlugin.instance().getCore().getPlayer(id);
        }

        public String fromRawString(String name) {
            return name.toLowerCase();
        }
    };

    public abstract String getId(ServerPlayer serverPlayer);

    public abstract Optional<ServerPlayer> getPlayer(String id);

    public abstract String fromRawString(String id);
}