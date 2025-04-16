package com.httydcraft.authcraft.api.step;

import com.httydcraft.authcraft.api.account.Account;

public interface AuthenticationStepContext {
    Account getAccount();

    boolean canPassToNextStep();

    void setCanPassToNextStep(boolean canPass);
}
