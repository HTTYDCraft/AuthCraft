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
import com.httydcraft.authcraft.spigot.hooks.SpigotLuckPermsHook;
import com.httydcraft.authcraft.spigot.hooks.SpigotWorldEditHook;
import com.httydcraft.authcraft.spigot.limbo.SpigotLimboManager;
import com.httydcraft.authcraft.spigot.security.AntiBotManager;
import com.httydcraft.authcraft.spigot.security.CloudflareWarpChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicesManager;
import revxrsal.commands.annotation.dynamic.Annotations;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.bukkit.core.BukkitHandler;
import revxrsal.commands.process.ContextResolver;

import java.util.Optional;
import java.util.Set;

// #region Class Documentation
/**
 * Command registry for Spigot-specific commands.
 * Extends {@link ServerCommandsRegistry} to register and handle commands for the AuthCraft plugin.
 */
public class SpigotCommandRegistry extends ServerCommandsRegistry {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final GoogleLogger COMMAND_LOGGER = GoogleLogger.forEnclosingClass();
    private final PluginConfig config;
    private final SpigotLuckPermsHook luckPermsHook;
    private final SpigotWorldEditHook worldEditHook;

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
        this.luckPermsHook = pluginBootstrap.getLuckPermsHook();
        this.worldEditHook = pluginBootstrap.getWorldEditHook();
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

    private SpigotLimboManager getLimboManagerFromService() {
        ServicesManager services = org.bukkit.Bukkit.getServer().getServicesManager();
        return services.load(SpigotLimboManager.class);
    }

    // Пример команды для выдачи группы через LuckPerms
    @revxrsal.commands.annotation.Command("authcraft:group")
    @revxrsal.commands.bukkit.annotation.CommandPermission("authcraft.admin")
    public void setGroup(BukkitCommandActor actor, Player target, String group) {
        if (luckPermsHook == null) {
            actor.reply("§cLuckPerms не установлен!");
            return;
        }
        luckPermsHook.getLuckPerms().getUserManager().modifyUser(target.getUniqueId(), user -> {
            user.data().add(luckPermsHook.getLuckPerms().getNodeBuilderRegistry().forGroup().group(group).build());
        });
        actor.reply("§aГруппа " + group + " выдана игроку " + target.getName());
    }

    // Перезагрузка конфига и интеграций
    @revxrsal.commands.annotation.Command("authcraft:reload")
    @revxrsal.commands.bukkit.annotation.CommandPermission("authcraft.admin")
    public void reloadConfig(BukkitCommandActor actor) {
        SpigotAuthPluginBootstrap plugin = (SpigotAuthPluginBootstrap) Bukkit.getPluginManager().getPlugin("AuthCraft");
        plugin.reloadConfig();
        actor.reply("§aКонфиг перезагружен!");
    }

    // Диагностика интеграций
    @revxrsal.commands.annotation.Command("authcraft:debug")
    @revxrsal.commands.bukkit.annotation.CommandPermission("authcraft.admin")
    public void debug(BukkitCommandActor actor) {
        SpigotAuthPluginBootstrap plugin = (SpigotAuthPluginBootstrap) Bukkit.getPluginManager().getPlugin("AuthCraft");
        StringBuilder sb = new StringBuilder("§e[AuthCraft Debug]\n");
        sb.append("LuckPerms: ").append(plugin.getLuckPermsHook() != null ? "§aOK" : "§cНет").append("\n");
        sb.append("WorldEdit: ").append(plugin.getWorldEditHook() != null ? "§aOK" : "§cНет").append("\n");
        sb.append("Cloudflare: ").append(plugin.getWarpChecker() != null ? plugin.getWarpChecker().getMode() : "§cНет").append("\n");
        sb.append("AntiBot: ").append(plugin.getAntiBotManager() != null ? "§aOK" : "§cНет").append("\n");
        actor.reply(sb.toString());
    }

    // Телепорт игрока в Limbo
    @revxrsal.commands.annotation.Command("authcraft:limbo")
    @revxrsal.commands.bukkit.annotation.CommandPermission("authcraft.admin")
    public void limboTp(BukkitCommandActor actor, Player target) {
        SpigotLimboManager limbo = getLimboManagerFromService();
        if (limbo == null) {
            COMMAND_LOGGER.atWarning().log("LimboManager service not found for limboTp command by %s", actor.getName());
            actor.reply("§cLimbo не инициализирован!");
            return;
        }
        limbo.sendToLimbo(target);
        COMMAND_LOGGER.atInfo().log("%s used /authcraft limbo on %s", actor.getName(), target.getName());
        actor.reply("§aИгрок отправлен в Limbo!");
    }

    // Смена режима Cloudflare Warp/VPN
    @revxrsal.commands.annotation.Command("authcraft:cloudflare")
    @revxrsal.commands.bukkit.annotation.CommandPermission("authcraft.admin")
    public void setCloudflareMode(BukkitCommandActor actor, String mode) {
        SpigotAuthPluginBootstrap plugin = (SpigotAuthPluginBootstrap) Bukkit.getPluginManager().getPlugin("AuthCraft");
        CloudflareWarpChecker checker = plugin.getWarpChecker();
        try {
            CloudflareWarpChecker.Mode m = CloudflareWarpChecker.Mode.valueOf(mode);
            checker.setMode(m);
            actor.reply("§aРежим Cloudflare Warp/VPN установлен: " + m);
        } catch (Exception e) {
            actor.reply("§cНекорректный режим! Доступно: ONLY_WARP, ALLOW_ALL, BLOCK_VPN");
        }
    }

    // Проверка прав на WorldEdit для игрока
    @revxrsal.commands.annotation.Command("authcraft:worldeditcheck")
    @revxrsal.commands.bukkit.annotation.CommandPermission("authcraft.admin")
    public void checkWorldEdit(BukkitCommandActor actor, Player target) {
        if (worldEditHook == null) {
            actor.reply("§cWorldEdit не установлен!");
            return;
        }
        boolean canEdit = worldEditHook.canEdit(target, target.getLocation());
        actor.reply("§eWorldEdit для " + target.getName() + ": " + (canEdit ? "§aДоступен" : "§cНет доступа"));
    }

    // Выдача/снятие прав на Limbo build
    @revxrsal.commands.annotation.Command("authcraft:limbo:build")
    @revxrsal.commands.bukkit.annotation.CommandPermission("authcraft.admin")
    public void setLimboBuild(BukkitCommandActor actor, Player target, boolean allow) {
        if (luckPermsHook == null) {
            actor.reply("§cLuckPerms не установлен!");
            return;
        }
        luckPermsHook.getLuckPerms().getUserManager().modifyUser(target.getUniqueId(), user -> {
            if (allow) {
                user.data().add(luckPermsHook.getLuckPerms().getNodeBuilderRegistry().forPermission().permission("authcraft.limbo.build").value(true).build());
            } else {
                user.data().remove(luckPermsHook.getLuckPerms().getNodeBuilderRegistry().forPermission().permission("authcraft.limbo.build").build());
            }
        });
        actor.reply("§a" + (allow ? "Выдано" : "Снято") + " право Limbo build для " + target.getName());
    }

    // Вывести список игроков в Limbo
    @revxrsal.commands.annotation.Command("authcraft:limbo:list")
    @revxrsal.commands.bukkit.annotation.CommandPermission("authcraft.admin")
    public void limboList(BukkitCommandActor actor) {
        SpigotLimboManager limbo = getLimboManagerFromService();
        if (limbo == null) {
            COMMAND_LOGGER.atWarning().log("LimboManager service not found for limboList command by %s", actor.getName());
            actor.reply("§cLimbo не инициализирован!");
            return;
        }
        Set<String> players = limbo.getLimboPlayers();
        if (players.isEmpty()) {
            actor.reply("§7В Limbo никого нет.");
        } else {
            StringBuilder sb = new StringBuilder("§eВ Limbo: ");
            for (String uuid : players) {
                Player p = Bukkit.getPlayer(java.util.UUID.fromString(uuid));
                if (p != null) sb.append(p.getName()).append(", ");
            }
            actor.reply(sb.substring(0, sb.length() - 2));
        }
        COMMAND_LOGGER.atInfo().log("%s used /authcraft limbo:list (count=%d)", actor.getName(), players.size());
    }

    // Выгнать всех из Limbo (телепорт на спавн)
    @revxrsal.commands.annotation.Command("authcraft:limbo:clear")
    @revxrsal.commands.bukkit.annotation.CommandPermission("authcraft.admin")
    public void limboClear(BukkitCommandActor actor) {
        SpigotLimboManager limbo = getLimboManagerFromService();
        if (limbo == null) {
            COMMAND_LOGGER.atWarning().log("LimboManager service not found for limboClear command by %s", actor.getName());
            actor.reply("§cLimbo не инициализирован!");
            return;
        }
        Set<String> players = new java.util.HashSet<>(limbo.getLimboPlayers());
        for (String uuid : players) {
            Player p = Bukkit.getPlayer(java.util.UUID.fromString(uuid));
            if (p != null) {
                p.teleport(p.getWorld().getSpawnLocation());
                limbo.removeFromLimbo(p);
            }
        }
        COMMAND_LOGGER.atInfo().log("%s used /authcraft limbo:clear (cleared=%d)", actor.getName(), players.size());
        actor.reply("§aВсе игроки выведены из Limbo!");
    }

    // Справка по командам
    @revxrsal.commands.annotation.Command("authcraft:help")
    public void help(BukkitCommandActor actor) {
        actor.reply("§eДоступные команды:\n" +
                "/authcraft reload — перезагрузка конфига\n" +
                "/authcraft debug — диагностика интеграций\n" +
                "/authcraft group <игрок> <группа> — выдать группу через LuckPerms\n" +
                "/authcraft limbo <игрок> — отправить в Limbo\n" +
                "/authcraft limbo:list — список игроков в Limbo\n" +
                "/authcraft limbo:clear — выгнать всех из Limbo\n" +
                "/authcraft limbo:build <игрок> <true/false> — выдать/снять право Limbo build\n" +
                "/authcraft worldeditcheck <игрок> — проверить право на WorldEdit\n" +
                "/authcraft cloudflare <режим> — сменить режим Cloudflare (ONLY_WARP, ALLOW_ALL, BLOCK_VPN)");
    }
}