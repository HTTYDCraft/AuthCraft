package com.httydcraft.authcraft.api.bucket;

import java.util.ArrayList;
import java.util.List;

import com.httydcraft.authcraft.api.factory.AuthenticationStepFactory;

public interface AuthenticationStepFactoryBucket extends Bucket<AuthenticationStepFactory> {

    @Deprecated
    default List<AuthenticationStepFactory> getList() {
        return new ArrayList<>(getUnmodifiableRaw());
    }

    @Deprecated
    default void add(AuthenticationStepFactory authenticationStepCreator) {
        modifiable().add(authenticationStepCreator);
    }

    @Deprecated
    default void remove(AuthenticationStepFactory authenticationStepCreator) {
        modifiable().remove(authenticationStepCreator);
    }

}
