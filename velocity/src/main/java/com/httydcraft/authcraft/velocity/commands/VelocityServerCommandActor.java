package com.httydcraft.authcraft.velocity.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.commands.MessageableCommandActor;
import com.httydcraft.authcraft.api.server.command.ServerCommandActor;
import com.httydcraft.authcraft.api.server.message.AdventureServerComponent;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import net.kyori.adventure.text.Component;
import revxrsal.commands.velocity.VelocityCommandActor;

// #region Class Documentation
/**
 * Velocity-specific implementation of a command actor.
 * Implements {@link ServerCommandActor} and {@link MessageableCommandActor} to handle command execution and messaging.
 */
public class VelocityServerCommandActor implements ServerCommandActor, MessageableCommandActor {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final VelocityCommandActor actor;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VelocityServerCommandActor}.
     *
     * @param actor The underlying Velocity command actor. Must not be null.
     */
    public VelocityServerCommandActor(VelocityCommandActor actor) {
        this.actor = Preconditions.checkNotNull(actor, "actor must not be null");
        LOGGER.atInfo().log("Initialized VelocityServerCommandActor");
    }
    // #endregion

    // #region Messaging Methods
    /**
     * Sends a server component message to the actor.
     *
     * @param component The component to send. Must not be null.
     */
    @Override
    public void reply(ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        component.safeAs(AdventureServerComponent.class)
                .map(AdventureServerComponent::component)
                .ifPresent(comp -> {
                    LOGGER.atFine().log("Replying with component to actor");
                    actor.reply(comp);
                });
    }

    /**
     * Sends a message to the actor, supporting multiple formats.
     *
     * @param message The message to send. Must not be null.
     */
    @Override
    public void replyWithMessage(Object message) {
        Preconditions.checkNotNull(message, "message must not be null");
        LOGGER.atFine().log("Replying with message to actor");
        if (message instanceof ServerComponent) {
            reply((ServerComponent) message);
        } else if (message instanceof Component) {
            actor.reply((Component) message);
        } else if (message instanceof String) {
            actor.reply((String) message);
        } else {
            LOGGER.atWarning().log("Unsupported message type: %s", message.getClass().getName());
        }
    }
    // #endregion
}