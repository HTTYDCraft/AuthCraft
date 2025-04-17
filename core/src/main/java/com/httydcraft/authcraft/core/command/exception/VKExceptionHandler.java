package com.httydcraft.authcraft.core.command.exception;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.core.commands.exception.MessengerExceptionHandler;
import com.vk.api.sdk.exceptions.ApiMessagesDenySendException;
import revxrsal.commands.command.CommandActor;

// #region Class Documentation
/**
 * Handles exceptions specific to VK command execution, extending the base {@link MessengerExceptionHandler}.
 * Ignores {@link ApiMessagesDenySendException} to prevent unnecessary error propagation.
 */
public class VKExceptionHandler extends MessengerExceptionHandler {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VKExceptionHandler} with the specified link type.
     *
     * @param linkType The type of link associated with this handler. Must not be null.
     * @throws IllegalArgumentException if {@code linkType} is null.
     */
    public VKExceptionHandler(LinkType linkType) {
        super(Preconditions.checkNotNull(linkType, "linkType must not be null"));
        LOGGER.atInfo().log("Initialized VKExceptionHandler for linkType: %s", linkType);
    }
    // #endregion

    // #region Exception Handling
    /**
     * Processes unhandled exceptions during VK command execution.
     * Ignores {@link ApiMessagesDenySendException} and delegates other exceptions to the parent handler.
     *
     * @param actor     The command actor responsible for the command execution. Must not be null.
     * @param throwable The exception that occurred. Must not be null.
     * @throws IllegalArgumentException if {@code actor} or {@code throwable} is null.
     */
    @Override
    public void onUnhandledException(CommandActor actor, Throwable throwable) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(throwable, "throwable must not be null");

        if (throwable instanceof ApiMessagesDenySendException) {
            LOGGER.atFine().log("Ignored ApiMessagesDenySendException for actor: %s", actor);
            return;
        }

        SecurityAuditLogger.logFailure("VKExceptionHandler", null, "Unhandled exception for actor: " + actor.getName() + ", error: " + throwable.getMessage());
        LOGGER.atWarning().withCause(throwable).log("Unhandled exception occurred for actor: %s", actor);
        super.onUnhandledException(actor, throwable);
    }
    // #endregion
}