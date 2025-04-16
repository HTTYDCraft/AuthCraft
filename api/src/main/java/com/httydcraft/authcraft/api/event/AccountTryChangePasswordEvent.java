package com.httydcraft.authcraft.api.event;

import com.httydcraft.authcraft.api.event.base.AccountEvent;
import com.httydcraft.authcraft.api.event.base.CancellableEvent;
import com.httydcraft.authcraft.api.event.base.PasswordCheckEvent;

/**
 * Called when player changes tries to change his password. Cancel prevents validating, and sending messages.
 */
public interface AccountTryChangePasswordEvent extends AccountEvent, CancellableEvent, PasswordCheckEvent {
}
