package com.httydcraft.authcraft.api.event;

import com.httydcraft.authcraft.api.event.base.AccountEvent;
import com.httydcraft.authcraft.api.event.base.CancellableEvent;

/**
 * Called when Account was <b>validated</b>. Cancel results skipping authentication like session, but without message (except BungeeCord).
 */
public interface AccountJoinEvent extends AccountEvent, CancellableEvent {
}
