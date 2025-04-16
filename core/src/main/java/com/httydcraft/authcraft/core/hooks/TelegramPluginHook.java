package com.httydcraft.authcraft.core.hooks;

import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.hook.PluginHook;
import com.pengrad.telegrambot.TelegramBot;

// #region Interface Documentation
/**
 * Hook for Telegram integration.
 * Extends {@link PluginHook} to provide Telegram-specific functionality.
 */
public interface TelegramPluginHook extends PluginHook {
    GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    AuthPlugin PLUGIN = AuthPlugin.instance();
    // #endregion

    // #region Hook Check
    /**
     * Checks if the Telegram hook can be activated.
     *
     * @return {@code true} if Telegram is enabled in the configuration, {@code false} otherwise.
     */
    @Override
    default boolean canHook() {
        boolean enabled = PLUGIN.getConfig().getTelegramSettings().isEnabled();
        LOGGER.atFine().log("Checked Telegram hook availability: %b", enabled);
        return enabled;
    }
    // #endregion

    // #region Bot Access
    /**
     * Gets the Telegram bot instance.
     *
     * @return The {@link TelegramBot}.
     */
    TelegramBot getTelegramBot();
    // #endregion
}