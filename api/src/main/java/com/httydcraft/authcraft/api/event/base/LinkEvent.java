package com.httydcraft.authcraft.api.event.base;

import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;

import io.github.revxrsal.eventbus.gen.Index;

public interface LinkEvent {
    @Index(2)
    LinkType getLinkType();

    @Index(3)
    LinkUser getLinkUser();

}
