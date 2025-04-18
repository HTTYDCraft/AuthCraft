package com.httydcraft.authcraft.api.bucket;

import java.util.ArrayList;
import java.util.List;

import com.httydcraft.authcraft.api.model.AuthenticationTask;

public interface AuthenticationTaskBucket extends Bucket<AuthenticationTask> {

    @Deprecated
    default void addTask(AuthenticationTask task) {
        modifiable().add(task);
    }

    @Deprecated
    default void removeTask(AuthenticationTask task) {
        modifiable().remove(task);
    }

    @Deprecated
    default List<AuthenticationTask> getTasks() {
        return new ArrayList<>(getUnmodifiableRaw());
    }

}
