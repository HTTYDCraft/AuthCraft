package com.httydcraft.authcraft.api.config.bossbar;

import java.text.SimpleDateFormat;

import com.httydcraft.authcraft.api.server.bossbar.ServerBossbar;
import com.httydcraft.authcraft.api.server.message.ServerComponent;

public interface BossBarSettings {
    ServerComponent getTitle();

    SimpleDateFormat getDurationPlaceholderFormat();

    boolean isEnabled();

    ServerBossbar createBossbar();
}
