package com.httydcraft.authcraft.core.hooks;

import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.hook.PluginHook;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;

// #region Interface Documentation
/**
 * Hook for VK integration.
 * Extends {@link PluginHook} to provide VK-specific functionality.
 */
public interface VkPluginHook extends PluginHook {
    GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Client Access
    /**
     * Gets the VK API client.
     *
     * @return The {@link VkApiClient}.
     */
    VkApiClient getClient();
    // #endregion

    // #region Actor Access
    /**
     * Gets the VK group actor.
     *
     * @return The {@link GroupActor}.
     */
    GroupActor getActor();
    // #endregion
}