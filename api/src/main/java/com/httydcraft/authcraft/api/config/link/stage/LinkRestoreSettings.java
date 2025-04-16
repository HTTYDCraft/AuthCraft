package com.httydcraft.authcraft.api.config.link.stage;

public interface LinkRestoreSettings {
    int getCodeLength();

    String getCodeCharacters();

    String generateCode();
}
