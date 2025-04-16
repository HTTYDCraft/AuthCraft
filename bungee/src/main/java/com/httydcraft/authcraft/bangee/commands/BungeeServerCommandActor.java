package com.httydcraft.authcraft.bangee.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.commands.MessageableCommandActor;
import com.httydcraft.authcraft.bangee.BungeeAuthPluginBootstrap;
import com.httydcraft.authcraft.bangee.message.BungeeComponent;
import com.httydcraft.authcraft.api.server.command.ServerCommandActor;
import com.httydcraft.authcraft.api.server.message.AdventureServerComponent;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bungee.BungeeCommandActor;

// #region Class Documentation
/**
 * BungeeCord-specific implementation of a command actor.
 * Implements {@link ServerCommandActor} and {@link MessageableCommandActor} to handle command execution and messaging.
 */
public class BungeeServerCommandActor implements ServerCommandActor, MessageableCommandActor {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final BungeeCommandActor actor;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BungeeServerCommandActor}.
     *
     * @param actor The underlying BungeeCord command actor. Must not be null.
     */
    public BungeeServerCommandActor(@NotNull BungeeCommandActor actor) {
        this.actor = Preconditions.checkNotNull(actor, "actor must not be null");
        LOGGER.atInfo().log("Initialized BungeeServerCommandActor");
    }
    // #endregion

    // #region Messaging Methods
    /**
     * Sends a server component message to the actor.
     *
     * @param component The component to send. Must not be null.
     */
    @Override
    public void reply(@NotNull ServerComponent component) {
        Preconditions.checkNotNull(component, "component must not be null");
        LOGGER.atFine().log("Replying with component to actor");
        component.safeAs(BungeeComponent.class).ifPresent(bungeeComponent ->
                actor.getSender().sendMessage(bungeeComponent.components()));
        component.safeAs(AdventureServerComponent.class).ifPresent(adventureComponent ->
                BungeeAuthPluginBootstrap.getInstance()
                        .getBungeeAudiences()
                        .sender(actor.getSender())
                        .sendMessage(adventureComponent.component()));
    }

    /**
     * Sends a message to the actor, supporting multiple formats.
     *
     * @param message The message to send. Must not be null.
     */
    @Override
    public void replyWithMessage(@NotNull Object message) {
        Preconditions.checkNotNull(message, "message must not be null");
        LOGGER.atFine().log("Replying with message to actor");
        if (message instanceof ServerComponent) {
            reply((ServerComponent) message);
        } else if (message instanceof Component) {
            BungeeAuthPluginBootstrap.getInstance()
                    .getBungeeAudiences()
                    .sender(actor.getSender())
                    .sendMessage((Component) message);
        } else if (message instanceof String) {
            actor.reply((String) message);
        } else {
            LOGGER.atWarning().log("Unsupported message type: %s", message.getClass().getName());
        }
    }
    // #endregion
}