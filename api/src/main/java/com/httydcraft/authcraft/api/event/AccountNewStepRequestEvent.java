package com.httydcraft.authcraft.api.event;

import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.event.base.AccountEvent;
import com.httydcraft.authcraft.api.event.base.CancellableEvent;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;

import io.github.revxrsal.eventbus.gen.Index;

/**
 * Called when {@link Account#nextAuthenticationStep(AuthenticationStepContext)} <b>called</b>. Cancel results for just ignoring
 * AuthenticationStep
 */
public interface AccountNewStepRequestEvent extends AccountEvent, CancellableEvent {
    @Index(2)
    AuthenticationStepContext getContext();
}
