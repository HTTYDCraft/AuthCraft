package com.httydcraft.authcraft.api.server.player;

import java.util.Optional;
import java.util.UUID;

import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.model.PlayerIdSupplier;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.proxy.ProxyServer;
import com.httydcraft.authcraft.api.util.Castable;

public interface ServerPlayer extends Castable<ServerPlayer>, PlayerIdSupplier {
    @Override
    default String getPlayerId() {
        return AuthPlugin.instance().getConfig().getActiveIdentifierType().getId(this);
    }

    default void disconnect(String reason) {
        disconnect(AuthPlugin.instance().getConfig().getServerMessages().getDeserializer().deserialize(reason));
    }

    default void sendMessage(String message) {
        if (message.isEmpty())
            return;
        sendMessage(AuthPlugin.instance().getConfig().getServerMessages().getDeserializer().deserialize(message));
    }

    boolean hasPermission(String permission);

    void disconnect(ServerComponent component);

    void sendMessage(ServerComponent component);

    String getNickname();

    UUID getUniqueId();

    String getPlayerIp();

    Optional<ProxyServer> getCurrentServer();

    <T> T getRealPlayer();
}
