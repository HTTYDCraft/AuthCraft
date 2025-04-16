package com.httydcraft.authcraft.api.event;

import com.httydcraft.authcraft.api.event.base.AccountEvent;
import com.httydcraft.authcraft.api.event.base.CancellableEvent;
import com.httydcraft.authcraft.api.event.base.PasswordCheckEvent;

/**
 * Called when player tried to enter password with <b>command</b>. Cancel results for do not validating, but do not prevent "wrong attempt" increasing.
 */
public interface AccountTryLoginEvent extends AccountEvent, CancellableEvent, PasswordCheckEvent {
}
