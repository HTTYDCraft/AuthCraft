package com.httydcraft.authcraft.api.config.link;

public interface DiscordSettings extends LinkSettings {
    String getBotToken();

    boolean isAllowedChannel(String channelId);
}
