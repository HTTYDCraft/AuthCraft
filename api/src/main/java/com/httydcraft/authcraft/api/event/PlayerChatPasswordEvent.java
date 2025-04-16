package com.httydcraft.authcraft.api.event;

import com.httydcraft.authcraft.api.event.base.PlayerEvent;
import io.github.revxrsal.eventbus.gen.Index;

public interface PlayerChatPasswordEvent extends PlayerEvent {
    @Index(1)
    String getPassword();
}
