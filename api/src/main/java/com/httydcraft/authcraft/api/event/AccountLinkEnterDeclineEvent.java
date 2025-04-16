package com.httydcraft.authcraft.api.event;

import com.httydcraft.authcraft.api.event.base.AccountEvent;
import com.httydcraft.authcraft.api.event.base.CancellableEvent;
import com.httydcraft.authcraft.api.event.base.LinkEvent;
import com.httydcraft.authcraft.api.link.user.entry.LinkEntryUser;
import com.httydcraft.authcraft.api.commands.MessageableCommandActor;

import io.github.revxrsal.eventbus.gen.Index;

/**
 * Called when /decline executed. Cancel prevents enter decline.
 */
public interface AccountLinkEnterDeclineEvent extends AccountEvent, CancellableEvent, LinkEvent {
    @Index(4)
    LinkEntryUser getEntryUser();

    @Index(5)
    MessageableCommandActor getActor();
}
