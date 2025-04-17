package com.httydcraft.authcraft.velocity.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.google.common.collect.ImmutableList;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.commands.MessageableCommandActor;
import com.httydcraft.authcraft.velocity.VelocityAuthPluginBootstrap;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.core.server.commands.exception.SendComponentException;
import com.httydcraft.authcraft.velocity.commands.exception.VelocityExceptionHandler;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.message.MessageContext;
import com.httydcraft.authcraft.api.model.PlayerIdSupplier;
import com.httydcraft.authcraft.velocity.player.VelocityServerPlayer;
import com.httydcraft.authcraft.api.server.command.ServerCommandActor;
import com.httydcraft.authcraft.core.server.commands.ServerCommandsRegistry;
import com.httydcraft.authcraft.core.server.commands.annotations.AuthenticationAccount;
import com.httydcraft.authcraft.core.server.commands.annotations.AuthenticationStepCommand;
import com.httydcraft.authcraft.core.server.commands.annotations.Permission;
import com.httydcraft.authcraft.core.server.commands.parameters.ArgumentServerPlayer;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.velocitypowered.api.proxy.Player;
import revxrsal.commands.annotation.dynamic.Annotations;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.velocity.VelocityCommandActor;
import revxrsal.commands.velocity.annotation.CommandPermission;
import revxrsal.commands.velocity.core.VelocityHandler;

import java.util.Optional;

// #region Class Documentation
/**
 * Command registry for Velocity-specific commands.
 * Extends {@link ServerCommandsRegistry} to register and handle commands for the AuthCraft plugin.
 */
public class VelocityCommandRegistry extends ServerCommandsRegistry {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final PluginConfig config;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VelocityCommandRegistry}.
     *
     * @param pluginBootstrap The Velocity plugin bootstrap instance. Must not be null.
     * @param authPlugin The AuthCraft plugin instance. Must not be null.
     */
    public VelocityCommandRegistry(VelocityAuthPluginBootstrap pluginBootstrap, AuthPlugin authPlugin) {
        super(new VelocityHandler(
                Preconditions.checkNotNull(pluginBootstrap, "pluginBootstrap must not be null"),
                pluginBootstrap.getProxyServer())
                .setExceptionHandler(new VelocityExceptionHandler(
                        Preconditions.checkNotNull(authPlugin, "authPlugin must not be null").getConfig().getServerMessages()))
                .disableStackTraceSanitizing());
        this.config = authPlugin.getConfig();
        registerResolvers(authPlugin);
        registerConditions(authPlugin);
        commandHandler.registerExceptionHandler(SendComponentException.class,
                (actor, componentException) -> new VelocityServerCommandActor(actor.as(VelocityCommandActor.class))
                        .reply(Preconditions.checkNotNull(componentException.getComponent(), "component must not be null")));
        registerCommands();
        LOGGER.atInfo().log("Initialized VelocityCommandRegistry");
    }
    // #endregion

    // #region Resolver Registration
    /**
     * Registers context resolvers and value resolvers for command parameters.
     *
     * @param authPlugin The AuthCraft plugin instance. Must not be null.
     */
    private void registerResolvers(AuthPlugin authPlugin) {
        commandHandler.registerContextResolver(ServerPlayer.class, this::resolveServerPlayer);
        commandHandler.registerContextResolver(PlayerIdSupplier.class, context ->
                PlayerIdSupplier.of(resolveServerPlayer(context).getNickname()));
        commandHandler.registerContextResolver(MessageableCommandActor.class, this::resolveServerCommandActor);
        commandHandler.registerContextResolver(ServerCommandActor.class, this::resolveServerCommandActor);
        commandHandler.registerValueResolver(ArgumentServerPlayer.class, context -> {
            String value = context.pop();
            Optional<ServerPlayer> player = authPlugin.getCore().getPlayer(value);
            if (!player.isPresent()) {
                throw new SendComponentException(config.getServerMessages().getMessage(
                        "player-offline", MessageContext.of("%player_name%", value)));
            }
            return new ArgumentServerPlayer(player.get());
        });
        commandHandler.registerContextResolver(Account.class, context -> {
            ServerPlayer player = resolveServerPlayer(context);
            if (player.getRealPlayer() == null) {
                throw new SendComponentException(config.getServerMessages().getMessage("players-only"));
            }
            String id = config.getActiveIdentifierType().getId(player);
            if (!authPlugin.getAuthenticatingAccountBucket().isAuthenticating(player)) {
                throw new SendComponentException(config.getServerMessages().getMessage("already-logged-in"));
            }
            Account account = authPlugin.getAuthenticatingAccountBucket().getAuthenticatingAccountNullable(player);
            if (!account.isRegistered() && !context.parameter().hasAnnotation(AuthenticationAccount.class)) {
                throw new SendComponentException(config.getServerMessages().getMessage("account-not-found"));
            }
            if (account.isRegistered() && context.parameter().hasAnnotation(AuthenticationAccount.class)) {
                throw new SendComponentException(config.getServerMessages().getMessage("account-exists"));
            }
            return account;
        });
        commandHandler.registerAnnotationReplacer(Permission.class, (element, annotation) ->
                ImmutableList.of(Annotations.create(CommandPermission.class, "value", annotation.value())));
        LOGGER.atFine().log("Registered context and value resolvers");
    }
    // #endregion

    // #region Condition Registration
    /**
     * Registers conditions for command execution, including authentication step checks.
     *
     * @param authPlugin The AuthCraft plugin instance. Must not be null.
     */
    private void registerConditions(AuthPlugin authPlugin) {
        commandHandler.registerCondition((actor, command, arguments) -> {
            if (!actor.as(VelocityCommandActor.class).isPlayer()) {
                return;
            }
            ServerPlayer player = new VelocityServerPlayer(actor.as(VelocityCommandActor.class).getAsPlayer());
            if (!authPlugin.getAuthenticatingAccountBucket().isAuthenticating(player)) {
                return;
            }
            if (!command.hasAnnotation(AuthenticationStepCommand.class)) {
                return;
            }
            Account account = authPlugin.getAuthenticatingAccountBucket().getAuthenticatingAccountNullable(player);
            if (account.getCurrentAuthenticationStep() == null) {
                return;
            }
            String stepName = command.getAnnotation(AuthenticationStepCommand.class).stepName();
            if (account.getCurrentAuthenticationStep().getStepName().equals(stepName)) {
                return;
            }
            throw new SendComponentException(config.getServerMessages().getSubMessages("authentication-step-usage")
                    .getMessage(account.getCurrentAuthenticationStep().getStepName()));
        });
        LOGGER.atFine().log("Registered command conditions");
    }
    // #endregion

    // #region Resolver Methods
    /**
     * Resolves a {@link ServerCommandActor} from the context.
     *
     * @param context The resolver context. Must not be null.
     * @return The resolved {@link VelocityServerCommandActor}.
     */
    private VelocityServerCommandActor resolveServerCommandActor(ContextResolver.ContextResolverContext context) {
        Preconditions.checkNotNull(context, "context must not be null");
        LOGGER.atFine().log("Resolving ServerCommandActor");
        return new VelocityServerCommandActor(context.actor().as(VelocityCommandActor.class));
    }

    /**
     * Resolves a {@link ServerPlayer} from the context.
     *
     * @param context The resolver context. Must not be null.
     * @return The resolved {@link VelocityServerPlayer}.
     * @throws SendComponentException If the actor is not a player.
     */
    private VelocityServerPlayer resolveServerPlayer(ContextResolver.ContextResolverContext context) {
        Preconditions.checkNotNull(context, "context must not be null");
        Player player = context.actor().as(VelocityCommandActor.class).getAsPlayer();
        if (player == null) {
            throw new SendComponentException(config.getServerMessages().getMessage("players-only"));
        }
        LOGGER.atFine().log("Resolved ServerPlayer: %s", player.getUsername());
        return new VelocityServerPlayer(player);
    }
    // #endregion
}