package com.httydcraft.authcraft.velocity.listener;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.core.command.DispatchCommandListener;
import com.httydcraft.vk.api.velocity.events.VKCallbackButtonPressEvent;
import com.httydcraft.vk.api.velocity.events.VKMessageEvent;
import com.velocitypowered.api.event.Subscribe;

// #region Class Documentation
/**
 * Listener for handling VK-related events in Velocity.
 * Extends {@link DispatchCommandListener} to process VK messages and button presses.
 */
public class VkDispatchListener extends DispatchCommandListener {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #endregion

    // #region Event Handlers
    /**
     * Handles incoming VK messages.
     *
     * @param event The VK message event. Must not be null.
     */
    @Subscribe
    public void onMessage(VKMessageEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        LOGGER.atFine().log("Handling VK message event from peer: %d", event.getPeer());
        onMessage(event.getMessage(), event.getPeer());
    }

    /**
     * Handles VK button press events.
     *
     * @param event The VK button press event. Must not be null.
     */
    @Subscribe
    public void onButtonPress(VKCallbackButtonPressEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        LOGGER.atFine().log("Handling VK button press event");
        onButtonClick(event.getButtonEvent());
    }
    // #endregion
}