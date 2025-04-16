package com.httydcraft.authcraft.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.AuthPlugin;
import com.httydcraft.authcraft.SpigotAuthPluginBootstrap;
import com.httydcraft.authcraft.account.Account;
import com.httydcraft.authcraft.commands.exception.SendComponentException;
import com.httydcraft.authcraft.commands.exception.SpigotExceptionHandler;
import com.httydcraft.authcraft.config.PluginConfig;
import com.httydcraft.authcraft.config.message.MessageContext;
import com.httydcraft.authcraft.model.PlayerIdSupplier;
import com.httydcraft.authcraft.player.SpigotServerPlayer;
import com.httydcraft.authcraft.server.command.ServerCommandActor;
import com.httydcraft.authcraft.server.commands.ServerCommandsRegistry;
import com.httydcraft.authcraft.server.commands.annotations.AuthenticationAccount;
import com.httydcraft.authcraft.server.commands.annotations.AuthenticationStepCommand;
import com.httydcraft.authcraft.server.commands.annotations.Permission;
import com.httydcraft.authcraft.server.commands.parameters.ArgumentServerPlayer;
import com.httydcraft.authcraft.server.player.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.annotation.dynamic.Annotations;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.bukkit.core.BukkitHandler;
import revxrsal.commands.process.ContextResolver;

import java.util.Optional;

// #region Class Documentation
/**
 * Command registry for Spigot-specific commands.
 * Extends {@link ServerCommandsRegistry} to register and handle commands for the AuthCraft plugin.
 */
public class SpigotCommandRegistry extends ServerCommandsRegistry {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final PluginConfig config;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code SpigotCommandRegistry}.
     *
     * @param pluginBootstrap The Spigot plugin bootstrap instance. Must not be null.
     * @param authPlugin The AuthCraft plugin instance. Must not be null.
     */
    public SpigotCommandRegistry(SpigotAuthPluginBootstrap pluginBootstrap, AuthPlugin authPlugin) {
        super(new BukkitHandler(
                Preconditions.checkNotNull(pluginBootstrap, "pluginBootstrap must not be null"))
                .setExceptionHandler(new SpigotExceptionHandler(
                        Preconditions.checkNotNull(authPlugin, "authPlugin must not be null").getConfig().getServerMessages()))
                .disableStackTraceSanitizing());
        this.config = authPlugin.getConfig();
        registerResolvers(authPlugin);
        registerConditions(authPlugin);
        commandHandler.registerExceptionHandler(SendComponentException.class,
                (actor, componentException) -> new SpigotServerCommandActor(actor.as(BukkitCommandActor.class))
                        .reply(Preconditions.checkNotNull(componentException.getComponent(), "component must not be null")));
        registerCommands();
        LOGGER.atInfo().log("Initialized SpigotCommandRegistry");
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
            if (!actor.as(BukkitCommandActor.class).isPlayer()) {
                return;
            }
            ServerPlayer player = new SpigotServerPlayer(actor.as(BukkitCommandActor.class).getAsPlayer());
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
     * @return The resolved {@link SpigotServerCommandActor}.
     */
    private SpigotServerCommandActor resolveServerCommandActor(ContextResolver.ContextResolverContext context) {
        Preconditions.checkNotNull(context, "context must not be null");
        LOGGER.atFine().log("Resolving ServerCommandActor");
        return new SpigotServerCommandActor(context.actor().as(BukkitCommandActor.class));
    }

    /**
     * Resolves a {@link ServerPlayer} from the context.
     *
     * @param context The resolver context. Must not be null.
     * @return The resolved {@link SpigotServerPlayer}.
     * @throws SendComponentException If the actor is not a player.
     */
    private SpigotServerPlayer resolveServerPlayer(ContextResolver.ContextResolverContext context) {
        Preconditions.checkNotNull(context, "context must not be null");
        Player player = context.actor().as(BukkitCommandActor.class).getAsPlayer();
        if (player == null) {
            throw new SendComponentException(config.getServerMessages().getMessage("players-only"));
        }
        LOGGER.atFine().log("Resolved ServerPlayer: %s", player.getName());
        return new SpigotServerPlayer(player);
    }
    // #endregion
}