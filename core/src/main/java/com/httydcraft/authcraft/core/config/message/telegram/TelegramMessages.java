package com.httydcraft.authcraft.core.config.message.telegram;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.core.config.message.link.LinkMessages;

// #region Class Documentation
/**
 * Configuration for Telegram messages.
 * Extends link messages with Telegram-specific delimiter.
 */
public class TelegramMessages extends LinkMessages {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code TelegramMessages}.
     *
     * @param configurationSection The configuration section. Must not be null.
     */
    public TelegramMessages(ConfigurationSectionHolder configurationSection) {
        super(Preconditions.checkNotNull(configurationSection, "configurationSection must not be null"), "\n");
        LOGGER.atInfo().log("Initialized TelegramMessages");
    }
    // #endregion
}