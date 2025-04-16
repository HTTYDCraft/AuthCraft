package com.httydcraft.authcraft.api.config.link.stage;

import com.httydcraft.authcraft.api.config.duration.ConfigurationDuration;

public interface LinkConfirmationSettings {
    boolean canToggleConfirmation();

    ConfigurationDuration getRemoveDelay();

    int getCodeLength();

    String getCodeCharacters();

    String generateCode();
}
