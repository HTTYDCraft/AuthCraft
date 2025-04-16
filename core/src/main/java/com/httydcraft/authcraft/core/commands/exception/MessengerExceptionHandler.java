package com.httydcraft.authcraft.core.commands.exception;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.core.commands.annotation.ConfigurationArgumentError;
import com.httydcraft.authcraft.api.link.LinkType;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.*;

// #region Class Documentation
/**
 * Custom exception handler for messenger commands.
 * Maps command exceptions to user-friendly messages using the link type's message configuration.
 */
public class MessengerExceptionHandler extends DefaultExceptionHandler {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final LinkType linkType;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code MessengerExceptionHandler}.
     *
     * @param linkType The link type for message configuration. Must not be null.
     */
    public MessengerExceptionHandler(LinkType linkType) {
        this.linkType = Preconditions.checkNotNull(linkType, "linkType must not be null");
        LOGGER.atInfo().log("Initialized MessengerExceptionHandler for linkType: %s", linkType);
    }
    // #endregion

    // #region Exception Handlers
    /**
     * Handles missing argument exceptions.
     *
     * @param actor     The actor executing the command. Must not be null.
     * @param exception The exception. Must not be null.
     */
    @Override
    public void missingArgument(CommandActor actor, MissingArgumentException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");

        LOGGER.atFine().log("Handling missing argument for command: %s", exception.getCommand().getName());
        if (exception.getCommand().hasAnnotation(ConfigurationArgumentError.class)) {
            actor.reply(linkType.getLinkMessages().getMessage(exception.getCommand().getAnnotation(ConfigurationArgumentError.class).value()));
            return;
        }
        actor.reply(linkType.getLinkMessages().getMessage("unresolved-argument")
                .replaceAll("%argument_name%", exception.getParameter().getName()));
    }

    /**
     * Handles invalid number exceptions.
     *
     * @param actor     The actor executing the command. Must not be null.
     * @param exception The exception. Must not be null.
     */
    @Override
    public void invalidNumber(CommandActor actor, InvalidNumberException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");

        LOGGER.atFine().log("Handling invalid number: %s", exception.getInput());
        actor.reply(linkType.getLinkMessages().getMessage("unresolved-number")
                .replaceAll("%input%", exception.getInput()));
    }

    /**
     * Handles argument parsing exceptions.
     *
     * @param actor     The actor executing the command. Must not be null.
     * @param exception The exception. Must not be null.
     */
    @Override
    public void argumentParse(CommandActor actor, ArgumentParseException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");

        LOGGER.atFine().log("Handling argument parse error");
        actor.reply(linkType.getLinkMessages().getMessage("command-invocation"));
    }

    /**
     * Handles command invocation exceptions.
     *
     * @param actor     The actor executing the command. Must not be null.
     * @param exception The exception. Must not be null.
     */
    @Override
    public void commandInvocation(CommandActor actor, CommandInvocationException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");

        LOGGER.atSevere().withCause(exception.getCause()).log("Command invocation error");
        actor.reply(linkType.getLinkMessages().getMessage("command-invocation"));
    }

    /**
     * Handles sendable exceptions.
     *
     * @param actor     The actor executing the command. Must not be null.
     * @param exception The exception. Must not be null.
     */
    @Override
    public void sendableException(CommandActor actor, SendableException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");

        LOGGER.atFine().log("Handling sendable exception");
        exception.sendTo(actor);
    }

    /**
     * Handles unhandled exceptions.
     *
     * @param actor     The actor executing the command. Must not be null.
     * @param throwable The throwable. Must not be null.
     */
    @Override
    public void onUnhandledException(CommandActor actor, Throwable throwable) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(throwable, "throwable must not be null");

        LOGGER.atSevere().withCause(throwable).log("Unhandled exception");
        actor.reply(linkType.getLinkMessages().getMessage("command-invocation"));
    }
    // #endregion
}