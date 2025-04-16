package com.httydcraft.authcraft.bangee.commands.exception;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.SendableException;

// #region Class Documentation
/**
 * BungeeCord-specific sendable exception for command errors.
 * Extends {@link SendableException} to send error messages to the command actor.
 */
public class BungeeSendableException extends SendableException {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #region Constructor
    /**
     * Constructs a new {@code BungeeSendableException}.
     *
     * @param message The error message to send. Must not be null.
     */
    public BungeeSendableException(@NotNull String message) {
        super(Preconditions.checkNotNull(message, "message must not be null"));
        LOGGER.atInfo().log("Initialized BungeeSendableException with message: %s", message);
    }
    // #endregion

    // #region SendableException Implementation
    /**
     * Sends the exception message to the command actor.
     *
     * @param actor The command actor. Must not be null.
     */
    @Override
    public void sendTo(@NotNull CommandActor actor) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        LOGGER.atFine().log("Sending exception message to actor: %s", getMessage());
        actor.as(BungeeCommandActor.class).reply(getMessage());
    }
    // #endregion
}