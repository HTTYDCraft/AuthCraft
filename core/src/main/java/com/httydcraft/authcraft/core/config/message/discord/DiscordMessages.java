package com.httydcraft.authcraft.core.config.message.discord;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.core.config.message.link.LinkMessages;

// #region Class Documentation
/**
 * Configuration for Discord messages.
 * Extends link messages with Discord-specific delimiter.
 */
public class DiscordMessages extends LinkMessages {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code DiscordMessages}.
     *
     * @param configurationSection The configuration section. Must not be null.
     */
    public DiscordMessages(ConfigurationSectionHolder configurationSection) {
        super(Preconditions.checkNotNull(configurationSection, "configurationSection must not be null"), "\n");
        LOGGER.atInfo().log("Initialized DiscordMessages");
    }
    // #endregion
}