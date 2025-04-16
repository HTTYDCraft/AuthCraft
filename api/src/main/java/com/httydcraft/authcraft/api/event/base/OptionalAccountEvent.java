package com.httydcraft.authcraft.api.event.base;

import java.util.Optional;

import com.httydcraft.authcraft.api.account.Account;

import io.github.revxrsal.eventbus.gen.Index;

public interface OptionalAccountEvent {
    @Index(0)
    Optional<Account> getAccount();
}
