package com.httydcraft.authcraft.velocity.hooks;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.core.hooks.VkPluginHook;
import com.httydcraft.vk.api.velocity.VelocityVkApiPlugin;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;

// #region Class Documentation
/**
 * Hook for integrating VK API with Velocity.
 * Implements {@link VkPluginHook} to provide VK client and actor access.
 */
public class VelocityVkPluginHook implements VkPluginHook {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #endregion

    // #region VkPluginHook Implementation
    /**
     * Checks if the VK integration can be hooked.
     *
     * @return {@code true} if VK settings are enabled, {@code false} otherwise.
     */
    @Override
    public boolean canHook() {
        boolean result = AuthPlugin.instance().getConfig().getVKSettings().isEnabled();
        LOGGER.atFine().log("Checked VK hook availability: %b", result);
        return result;
    }

    /**
     * Gets the VK API client.
     *
     * @return The {@link VkApiClient}.
     * @throws IllegalStateException If the VK plugin instance is not available.
     */
    @Override
    public VkApiClient getClient() {
        VkApiClient client = VelocityVkApiPlugin.getInstance().getVkApiProvider().getVkApiClient();
        LOGGER.atFine().log("Retrieved VK API client");
        return Preconditions.checkNotNull(client, "VK API client not available");
    }

    /**
     * Gets the VK group actor.
     *
     * @return The {@link GroupActor}.
     * @throws IllegalStateException If the VK plugin instance is not available.
     */
    @Override
    public GroupActor getActor() {
        GroupActor actor = VelocityVkApiPlugin.getInstance().getVkApiProvider().getActor();
        LOGGER.atFine().log("Retrieved VK group actor");
        return Preconditions.checkNotNull(actor, "VK group actor not available");
    }
    // #endregion
}