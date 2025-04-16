package com.httydcraft.authcraft.core.hooks;

import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.hook.PluginHook;
import net.dv8tion.jda.api.JDA;

// #region Interface Documentation
/**
 * Hook for Discord integration.
 * Extends {@link PluginHook} to provide Discord-specific functionality.
 */
public interface DiscordHook extends PluginHook {
    GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    AuthPlugin PLUGIN = AuthPlugin.instance();
    // #endregion

    // #region Hook Check
    /**
     * Checks if the Discord hook can be activated.
     *
     * @return {@code true} if Discord is enabled in the configuration, {@code false} otherwise.
     */
    @Override
    default boolean canHook() {
        boolean enabled = PLUGIN.getConfig().getDiscordSettings().isEnabled();
        LOGGER.atFine().log("Checked Discord hook availability: %b", enabled);
        return enabled;
    }
    // #endregion

    // #region JDA Access
    /**
     * Gets the JDA instance for Discord interactions.
     *
     * @return The {@link JDA} instance.
     */
    JDA getJDA();
    // #endregion
}