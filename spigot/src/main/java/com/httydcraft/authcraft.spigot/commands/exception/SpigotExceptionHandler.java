package com.httydcraft.authcraft.commands.exception;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.commands.SpigotServerCommandActor;
import com.httydcraft.authcraft.config.message.MessageContext;
import com.httydcraft.authcraft.config.message.Messages;
import com.httydcraft.authcraft.server.message.ServerComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.*;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.*;

// #region Class Documentation
/**
 * Exception handler for Spigot-specific command errors.
 * Extends {@link BukkitExceptionAdapter} to handle command-related exceptions and send localized messages.
 */
public class SpigotExceptionHandler extends BukkitExceptionAdapter {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final Messages<ServerComponent> messages;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code SpigotExceptionHandler}.
     *
     * @param messages The messages provider for localized error messages. Must not be null.
     */
    public SpigotExceptionHandler(Messages<ServerComponent> messages) {
        this.messages = Preconditions.checkNotNull(messages, "messages must not be null");
        LOGGER.atInfo().log("Initialized SpigotExceptionHandler");
    }
    // #endregion

    // #region Exception Handlers
    /**
     * Handles the case when the command sender is not a player.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void senderNotPlayer(@NotNull CommandActor actor, @NotNull SenderNotPlayerException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled SenderNotPlayerException");
        sendComponent(actor, messages.getMessage("players-only"));
    }

    /**
     * Handles the case when the command sender is not the console.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void senderNotConsole(@NotNull CommandActor actor, @NotNull SenderNotConsoleException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled SenderNotConsoleException");
        sendComponent(actor, messages.getMessage("console-only"));
    }

    /**
     * Handles invalid player input.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void invalidPlayer(@NotNull CommandActor actor, @NotNull InvalidPlayerException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled InvalidPlayerException for input: %s", exception.getInput());
        sendComponent(actor, messages.getMessage("player-offline", MessageContext.of("%player_name%", exception.getInput())));
    }

    /**
     * Handles missing command arguments.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void missingArgument(@NotNull CommandActor actor, @NotNull MissingArgumentException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled MissingArgumentException for parameter: %s", exception.getParameter().getName());
        sendComponent(actor, messages.getMessage("unresolved-argument", MessageContext.of("%argument_name%", exception.getParameter().getName())));
    }

    /**
     * Handles invalid enum values.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void invalidEnumValue(@NotNull CommandActor actor, @NotNull EnumNotFoundException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled EnumNotFoundException for input: %s", exception.getInput());
        sendComponent(actor, messages.getMessage("invalid-enum", MessageContext.of("%input%", exception.getInput())));
    }

    /**
     * Handles invalid number input.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void invalidNumber(@NotNull CommandActor actor, @NotNull InvalidNumberException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled InvalidNumberException for input: %s", exception.getInput());
        sendComponent(actor, messages.getMessage("unresolved-number", MessageContext.of("%input%", exception.getInput())));
    }

    /**
     * Handles invalid UUID input.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void invalidUUID(@NotNull CommandActor actor, @NotNull InvalidUUIDException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled InvalidUUIDException for input: %s", exception.getInput());
        sendComponent(actor, messages.getMessage("invalid-uuid", MessageContext.of("%input%", exception.getInput())));
    }

    /**
     * Handles invalid URL input.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void invalidURL(@NotNull CommandActor actor, @NotNull InvalidURLException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled InvalidURLException for input: %s", exception.getInput());
        sendComponent(actor, messages.getMessage("invalid-url", MessageContext.of("%input%", exception.getInput())));
    }

    /**
     * Handles invalid boolean input.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void invalidBoolean(@NotNull CommandActor actor, @NotNull InvalidBooleanException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled InvalidBooleanException for input: %s", exception.getInput());
        sendComponent(actor, messages.getMessage("invalid-boolean", MessageContext.of("%input%", exception.getInput())));
    }

    /**
     * Handles missing permission errors.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void noPermission(@NotNull CommandActor actor, @NotNull NoPermissionException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled NoPermissionException");
        sendComponent(actor, messages.getMessage("no-permission"));
    }

    /**
     * Handles argument parsing errors.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void argumentParse(@NotNull CommandActor actor, @NotNull ArgumentParseException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled ArgumentParseException");
        sendComponent(actor, messages.getMessage("command-invocation"));
    }

    /**
     * Handles command invocation errors.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void commandInvocation(@NotNull CommandActor actor, @NotNull CommandInvocationException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atWarning().withCause(exception.getCause()).log("Handled CommandInvocationException");
        sendComponent(actor, messages.getMessage("command-invocation"));
    }

    /**
     * Handles too many arguments errors.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void tooManyArguments(@NotNull CommandActor actor, @NotNull TooManyArgumentsException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled TooManyArgumentsException");
        sendComponent(actor, messages.getMessage("too-many-arguments"));
    }

    /**
     * Handles invalid command errors.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void invalidCommand(@NotNull CommandActor actor, @NotNull InvalidCommandException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled InvalidCommandException for input: %s", exception.getInput());
        sendComponent(actor, messages.getMessage("invalid-command", MessageContext.of("%input%", exception.getInput())));
    }

    /**
     * Handles invalid subcommand errors.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void invalidSubcommand(@NotNull CommandActor actor, @NotNull InvalidSubcommandException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled InvalidSubcommandException for input: %s", exception.getInput());
        sendComponent(actor, messages.getMessage("invalid-subcommand", MessageContext.of("%input%", exception.getInput())));
    }

    /**
     * Handles cases where no subcommand is specified.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override
    public void noSubcommandSpecified(@NotNull CommandActor actor, @NotNull NoSubcommandSpecifiedException exception) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(exception, "exception must not be null");
        LOGGER.atFine().log("Handled NoSubcommandSpecifiedException");
        sendComponent(actor, messages.getMessage("no-subcommand-specified"));
    }

    /**
     * Handles command cooldown errors.
     *
     * @param actor The command actor. Must not be null.
     * @param exception The exception instance. Must not be null.
     */
    @Override