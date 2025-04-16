package com.httydcraft.authcraft.api.crypto;

public interface HashInput {
    static HashInput of(String rawInput) {
        return () -> rawInput;
    }

    String getRawInput();
}
