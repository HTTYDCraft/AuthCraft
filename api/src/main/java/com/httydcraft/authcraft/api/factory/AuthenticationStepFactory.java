package com.httydcraft.authcraft.api.factory;

import com.httydcraft.authcraft.api.step.AuthenticationStep;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;

public interface AuthenticationStepFactory {
    String getAuthenticationStepName();

    AuthenticationStep createNewAuthenticationStep(AuthenticationStepContext context);
}
