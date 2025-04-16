package com.httydcraft.authcraft.bangee.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.commands.MessageableCommandActor;
import com.httydcraft.authcraft.bangee.BungeeAuthPluginBootstrap;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.message.MessageContext;
import com.httydcraft.authcraft.api.model.PlayerIdSupplier;
import com.httydcraft.authcraft.bangee.player.BungeeServerPlayer;
import com.httydcraft.authcraft.api.server.command.ServerCommandActor;
import com.httydcraft.authcraft.core.server.commands.ServerCommandsRegistry;
import com.httydcraft.authcraft.core.server.commands.annotations.AuthenticationAccount;
import com.httydcraft.authcraft.authcraft.server.commands.annotations.AuthenticationStepCommand;
import com.httydcraft.authcraft.core.server.commands.annotations.Permission;
import com.httydcraft.authcraft.core.server.commands.exception.SendComponentException;
import com.httydcraft.authcraft.core.server.commands.parameters.ArgumentServerPlayer;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.dynamic.Annotations;
import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.bungee.core.BungeeHandler;
import revxrsal.commands.process.ContextResolver;

// #region Class Documentation
/**
 * Command registry for BungeeCord-specific commands.
 * Extends {@link ServerCommandsRegistry} to register and handle commands for the AuthCraft plugin.
 */
public class BungeeCommandsRegistry extends ServerCommandsRegistry {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final PluginConfig config;
    private final AuthPlugin plugin;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BungeeCommandsRegistry}.
     *
     * @param pluginBootstrap The BungeeCord plugin bootstrap instance. Must not be null.
     * @param authPlugin The AuthCraft plugin instance. Must not be null.
     */
    public BungeeCommandsRegistry(@NotNull BungeeAuthPluginBootstrap pluginBootstrap, @NotNull AuthPlugin authPlugin) {
        super(new BungeeHandler(Preconditions.checkNotNull(pluginBootstrap, "pluginBootstrap must not be null"))
                .setExceptionHandler(new BungeeExceptionHandler(
                        Preconditions.checkNotNull(authPlugin, "authPlugin must not be null").getConfig().getServerMessages()))
                .disableStackTraceSanitizing());
        this.config = authPlugin.getConfig();
        this.plugin = authPlugin;
        registerResolvers();
        registerConditions();
        commandHandler.registerExceptionHandler(SendComponentException.class,
                (actor, componentException) -> new BungeeServerCommandActor(actor.as(BungeeCommandActor.class))
                        .reply(Preconditions.checkNotNull(componentException.getComponent(), "component must not be null")));
        registerCommands();
        LOGGER.atInfo().log("Initialized BungeeCommandsRegistry");
    }
    // #endregion

    // #region Resolver Registration
    /**
     * Registers context resolvers and value resolvers for command parameters.
     */
    private void registerResolvers() {
        commandHandler.registerContextResolver(ServerPlayer.class, this::resolveServerPlayer);
        commandHandler.registerContextResolver(PlayerIdSupplier.class, context ->
                PlayerIdSupplier.of(resolveServerPlayer(context).getNickname()));
        commandHandler.registerContextResolver(MessageableCommandActor.class, this::resolveServerCommandActor);
        commandHandler.registerContextResolver(ServerCommandActor.class, this::resolveServerCommandActor);
        commandHandler.registerValueResolver(ArgumentServerPlayer.class, context -> {
            String value = context.pop();
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(value);
            if (player == null) {
                throw new SendComponentException(config.getServerMessages().getMessage(
                        "player-offline", MessageContext.of("%player_name%", value)));
            }
            LOGGER.atFine().log("Resolved ArgumentServerPlayer: %s", value);
            return new ArgumentServerPlayer(new BungeeServerPlayer(player));
        });
        commandHandler.registerContextResolver(Account.class, context -> {
            ServerPlayer player = resolveServerPlayer(context);
            if (player.getRealPlayer() == null) {
                throw new SendComponentException(config.getServerMessages().getMessage("players-only"));
            }
            String id = config.getActiveIdentifierType().getId(player);
            if (!plugin.getAuthenticatingAccountBucket().isAuthenticating(player)) {
                throw new SendComponentException(config.getServerMessages().getMessage("already-logged-in"));
            }
            Account account = plugin.getAuthenticatingAccountBucket().getAuthenticatingAccountNullable(player);
            if (!account.isRegistered() && !context.parameter().hasAnnotation(AuthenticationAccount.class)) {
                throw new SendComponentException(config.getServerMessages().getMessage("account-not-found"));
            }
            if (account.isRegistered() && context.parameter().hasAnnotation(AuthenticationAccount.class)) {
                throw new SendComponentException(config.getServerMessages().getMessage("account-exists"));
            }
            LOGGER.atFine().log("Resolved Account for player: %s", player.getNickname());
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
     */
    private void registerConditions() {
        commandHandler.registerCondition((actor, command, arguments) -> {
            if (!actor.as(BungeeCommandActor.class).isPlayer()) {
                return;
            }
            ServerPlayer player = new BungeeServerPlayer(actor.as(BungeeCommandActor.class).asPlayer());
            if (!plugin.getAuthenticatingAccountBucket().isAuthenticating(player)) {
                return;
            }
            if (!command.hasAnnotation(AuthenticationStepCommand.class)) {
                return;
            }
            Account account = plugin.getAuthenticatingAccountBucket().getAuthenticatingAccountNullable(player);
            if (account.getCurrentAuthenticationStep() == null) {
                return;
            }
            String stepName = command.getAnnotation(AuthenticationStepCommand.class).stepName();
            if (account.getCurrentAuthenticationStep().getStepName().equals(stepName)) {
                return;
            }
            LOGGER.atFine().log("Blocked command for player %s due to authentication step mismatch", player.getNickname());
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
     * @return The resolved {@link BungeeServerCommandActor}.
     */
    private BungeeServerCommandActor resolveServerCommandActor(@NotNull ContextResolver.ContextResolverContext context) {
        Preconditions.checkNotNull(context, "context must not be null");
        LOGGER.atFine().log("Resolving ServerCommandActor");
        return new BungeeServerCommandActor(context.actor().as(BungeeCommandActor.class));
    }

    /**
     * Resolves a {@link ServerPlayer} from the context.
     *
     * @param context The resolver context. Must not be null.
     * @return The resolved {@link BungeeServerPlayer}.
     * @throws SendComponentException If the actor is not a player.
     */
    private BungeeServerPlayer resolveServerPlayer(@NotNull ContextResolver.ContextResolverContext context) {
        Preconditions.checkNotNull(context, "context must not be null");
        ProxiedPlayer player = context.actor().as(BungeeCommandActor.class).asPlayer();
        if (player == null) {
            throw new SendComponentException(config.getServerMessages().getMessage("players-only"));
        }
        LOGGER.atFine().log("Resolved ServerPlayer: %s", player.getName());
        return new BungeeServerPlayer(player);
    }
    // #endregion
}