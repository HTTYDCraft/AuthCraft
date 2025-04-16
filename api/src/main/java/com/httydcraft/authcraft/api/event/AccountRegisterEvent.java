package com.httydcraft.authcraft.api.event;

import com.httydcraft.authcraft.api.event.base.AccountEvent;
import com.httydcraft.authcraft.api.event.base.CancellableEvent;

/**
 * Called when player registers. Cancel prevents saving data to the database and logging in player.
 */
public interface AccountRegisterEvent extends AccountEvent, CancellableEvent {
}
