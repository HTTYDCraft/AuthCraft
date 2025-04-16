package com.httydcraft.authcraft.api.event;

import com.httydcraft.authcraft.api.event.base.AccountEvent;
import com.httydcraft.authcraft.api.event.base.CancellableEvent;

/**
 * Called when player have session and enters to the server. Cancel prevents skipping login process
 */
public interface AccountSessionEnterEvent extends AccountEvent, CancellableEvent {
}
