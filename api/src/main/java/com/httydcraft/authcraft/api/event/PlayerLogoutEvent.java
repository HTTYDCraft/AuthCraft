package com.httydcraft.authcraft.api.event;

import com.httydcraft.authcraft.api.event.base.CancellableEvent;
import com.httydcraft.authcraft.api.event.base.PlayerEvent;

/**
 * Called when player or admin executes /logout. Cancel results preventing logging out
 */
public interface PlayerLogoutEvent extends PlayerEvent, CancellableEvent {
}
