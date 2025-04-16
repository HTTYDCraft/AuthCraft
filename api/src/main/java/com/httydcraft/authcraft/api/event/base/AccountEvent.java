package com.httydcraft.authcraft.api.event.base;

import com.httydcraft.authcraft.api.account.Account;

import io.github.revxrsal.eventbus.gen.Index;

public interface AccountEvent {
    @Index(0)
    Account getAccount();
}
