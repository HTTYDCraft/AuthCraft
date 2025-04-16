package com.httydcraft.authcraft.api.factory;

import java.util.function.Supplier;

import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;

public interface AuthenticationStepContextFactory {
    static AuthenticationStepContextFactory of(Supplier<AuthenticationStepContext> supplier) {
        return of(supplier.get());
    }

    static AuthenticationStepContextFactory of(AuthenticationStepContext context) {
        return (account) -> context;
    }

    AuthenticationStepContext createContext(Account account);
}
