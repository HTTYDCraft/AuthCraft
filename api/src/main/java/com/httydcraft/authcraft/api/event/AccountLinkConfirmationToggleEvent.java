package com.httydcraft.authcraft.api.event;

import com.httydcraft.authcraft.api.event.base.AccountEvent;
import com.httydcraft.authcraft.api.event.base.CancellableEvent;
import com.httydcraft.authcraft.api.event.base.LinkEvent;
import com.httydcraft.authcraft.api.commands.MessageableCommandActor;

import io.github.revxrsal.eventbus.gen.Index;

/**
 * Called when player executes /entertoggle. Cancel prevents toggling.
 */
public interface AccountLinkConfirmationToggleEvent extends AccountEvent, CancellableEvent, LinkEvent {
    @Index(4)
    MessageableCommandActor getActor();
}
