package com.httydcraft.authcraft.bangee.listener;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.core.command.DispatchCommandListener;
import com.ubivashka.vk.bungee.events.VKCallbackButtonPressEvent;
import com.ubivashka.vk.bungee.events.VKMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

// #region Class Documentation
/**
 * Listener for handling VK dispatch events in BungeeCord.
 * Extends {@link DispatchCommandListener} to process VK messages and button presses.
 */
public class VkDispatchListener extends DispatchCommandListener implements Listener {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #region Constructor
    /**
     * Constructs a new {@code VkDispatchListener}.
     */
    public VkDispatchListener() {
        LOGGER.atInfo().log("Initialized VkDispatchListener");
    }
    // #endregion

    // #region Event Handlers
    /**
     * Handles incoming VK messages.
     *
     * @param event The VK message event. Must not be null.
     */
    @EventHandler
    public void onMessage(@NotNull VKMessageEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        LOGGER.atFine().log("Handling VK message from peer: %d", event.getPeer());
        onMessage(event.getMessage(), event.getPeer());
    }

    /**
     * Handles VK button press events.
     *
     * @param event The VK callback button press event. Must not be null.
     */
    @EventHandler
    public void onButtonPress(@NotNull VKCallbackButtonPressEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        LOGGER.atFine().log("Handling VK button press");
        onButtonClick(event.getButtonEvent());
    }
    // #endregion
}