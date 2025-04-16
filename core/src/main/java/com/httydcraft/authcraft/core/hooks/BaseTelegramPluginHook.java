package com.httydcraft.authcraft.core.hooks;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.pengrad.telegrambot.TelegramBot;

// #region Class Documentation
/**
 * Base implementation of {@link TelegramPluginHook}.
 * Provides access to a Telegram bot instance.
 */
public class BaseTelegramPluginHook implements TelegramPluginHook {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final TelegramBot telegramBot;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseTelegramPluginHook}.
     */
    public BaseTelegramPluginHook() {
        String botToken = PLUGIN.getConfig().getTelegramSettings().getBotToken();
        Preconditions.checkNotNull(botToken, "botToken must not be null");
        this.telegramBot = new TelegramBot(botToken);
        LOGGER.atFine().log("Initialized BaseTelegramPluginHook");
    }
    // #endregion

    // #region Bot Access
    /**
     * Gets the Telegram bot instance.
     *
     * @return The {@link TelegramBot}.
     */
    @Override
    public TelegramBot getTelegramBot() {
        LOGGER.atFine().log("Retrieved TelegramBot instance");
        return telegramBot;
    }
    // #endregion
}