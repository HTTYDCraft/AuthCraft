package com.httydcraft.authcraft.core.server.commands.exception;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import revxrsal.commands.exception.ThrowableFromCommand;

// #region Class Documentation
/**
 * Exception thrown to send a {@link ServerComponent} message to the command issuer.
 * Used to communicate errors with formatted components in command execution.
 */
@ThrowableFromCommand
public class SendComponentException extends RuntimeException {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ServerComponent component;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code SendComponentException}.
     *
     * @param component The component to send. Must not be null.
     */
    public SendComponentException(ServerComponent component) {
        this.component = Preconditions.checkNotNull(component, "component must not be null");
        LOGGER.atFine().log("Created SendComponentException with component: %s", component.plainText());
    }
    // #endregion

    // #region Getter
    /**
     * Gets the component associated with this exception.
     *
     * @return The {@link ServerComponent}.
     */
    public ServerComponent getComponent() {
        LOGGER.atFine().log("Retrieved component from SendComponentException");
        return component;
    }
    // #endregion
}