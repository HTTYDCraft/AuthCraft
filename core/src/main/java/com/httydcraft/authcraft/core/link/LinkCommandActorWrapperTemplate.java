package com.httydcraft.authcraft.core.link;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandActor;

import java.util.UUID;

// #region Class Documentation
/**
 * Template for wrapping command actors with link-specific functionality.
 * Implements {@link LinkCommandActorWrapper} to provide common actor operations.
 *
 * @param <T> The type of the wrapped command actor.
 */
public abstract class LinkCommandActorWrapperTemplate<T extends CommandActor> implements LinkCommandActorWrapper {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    protected final T actor;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code LinkCommandActorWrapperTemplate}.
     *
     * @param actor The command actor to wrap. Must not be null.
     */
    public LinkCommandActorWrapperTemplate(T actor) {
        this.actor = Preconditions.checkNotNull(actor, "actor must not be null");
        LOGGER.atFine().log("Initialized LinkCommandActorWrapperTemplate for actor: %s", actor.getId());
    }
    // #endregion

    // #region Actor Information
    /**
     * Gets the name of the actor.
     *
     * @return The actor's name.
     */
    @Override
    public String getName() {
        String name = actor.getName();
        LOGGER.atFine().log("Retrieved actor name: %s", name);
        return name;
    }

    /**
     * Gets the unique ID of the actor.
     *
     * @return The actor's {@link UUID}.
     */
    @Override
    public UUID getUniqueId() {
        UUID uuid = actor.getUniqueId();
        LOGGER.atFine().log("Retrieved actor UUID: %s", uuid);
        return uuid;
    }
    // #endregion

    // #region Messaging
    /**
     * Sends a reply to the actor.
     *
     * @param message The message to send. Must not be null.
     */
    @Override
    public void reply(String message) {
        Preconditions.checkNotNull(message, "message must not be null");
        actor.reply(message);
        LOGGER.atFine().log("Sent reply to actor: %s", message);
    }

    /**
     * Sends an error message to the actor.
     *
     * @param message The error message to send. Must not be null.
     */
    @Override
    public void error(String message) {
        Preconditions.checkNotNull(message, "message must not be null");
        actor.error(message);
        LOGGER.atWarning().log("Sent error to actor: %s", message);
    }
    // #endregion

    // #region Command Handler
    /**
     * Gets the command handler for the actor.
     *
     * @return The {@link CommandHandler}.
     */
    @Override
    public CommandHandler getCommandHandler() {
        CommandHandler handler = actor.getCommandHandler();
        LOGGER.atFine().log("Retrieved command handler");
        return handler;
    }
    // #endregion
}