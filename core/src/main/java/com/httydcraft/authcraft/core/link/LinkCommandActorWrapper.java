package com.httydcraft.authcraft.core.link;

import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.commands.MessageableCommandActor;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.multimessenger.core.message.Message;
import revxrsal.commands.command.CommandActor;

// #region Interface Documentation
/**
 * Interface for wrapping command actors with link-specific functionality.
 * Extends {@link CommandActor} and {@link MessageableCommandActor} to support messaging and identification.
 */
public interface LinkCommandActorWrapper extends CommandActor, MessageableCommandActor {
    GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Messaging
    /**
     * Sends a message to the actor.
     *
     * @param message The message to send.
     */
    void send(Message message);
    // #endregion

    // #region Identification
    /**
     * Gets the user identifier for the actor.
     *
     * @return The {@link LinkUserIdentificator}.
     */
    LinkUserIdentificator userId();
    // #endregion

    // #region Reply Handling
    /**
     * Sends a reply with a message object.
     *
     * @param message The message object. If a {@link String}, sends as a reply.
     */
    @Override
    default void replyWithMessage(Object message) {
        if (message instanceof String) {
            reply((String) message);
            LOGGER.atFine().log("Sent string reply: %s", message);
        } else {
            LOGGER.atFine().log("Ignored non-string reply message");
        }
    }
    // #endregion
}