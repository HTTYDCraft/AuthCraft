package com.httydcraft.authcraft.core.config.message.vk;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.core.config.message.link.LinkMessages;

// #region Class Documentation
/**
 * Configuration for VK messages.
 * Extends link messages with VK-specific delimiter.
 */
public class VKMessages extends LinkMessages {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VKMessages}.
     *
     * @param configurationSection The configuration section. Must not be null.
     */
    public VKMessages(ConfigurationSectionHolder configurationSection) {
        super(Preconditions.checkNotNull(configurationSection, "configurationSection must not be null"), "<br>");
        LOGGER.atInfo().log("Initialized VKMessages");
    }
    // #endregion
}