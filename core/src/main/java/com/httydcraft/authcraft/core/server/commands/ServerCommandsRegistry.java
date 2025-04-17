package com.httydcraft.authcraft.core.server.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.bucket.AuthenticatingAccountBucket;
import com.httydcraft.authcraft.api.bucket.LinkConfirmationBucket;
import com.httydcraft.authcraft.api.config.link.LinkSettings;
import com.httydcraft.authcraft.api.config.message.server.ServerMessages;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.confirmation.LinkConfirmationUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.impl.UserNumberIdentificator;
import com.httydcraft.authcraft.api.type.LinkConfirmationType;
import com.httydcraft.authcraft.core.commands.*;
import com.httydcraft.authcraft.core.commands.parameter.MessengerLinkContext;
import com.httydcraft.authcraft.core.commands.validator.CommandCooldownCondition;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.core.link.discord.DiscordLinkType;
import com.httydcraft.authcraft.core.link.telegram.TelegramLinkType;
import com.httydcraft.authcraft.core.link.vk.VKLinkType;
import com.httydcraft.authcraft.core.server.commands.annotations.Admin;
import com.httydcraft.authcraft.core.server.commands.annotations.DiscordUse;
import com.httydcraft.authcraft.core.server.commands.annotations.GoogleUse;
import com.httydcraft.authcraft.core.server.commands.annotations.TelegramUse;
import com.httydcraft.authcraft.core.server.commands.annotations.VkUse;
import com.httydcraft.authcraft.core.server.commands.exception.SendComponentException;
import com.httydcraft.authcraft.core.server.commands.parameters.ArgumentAccount;
import com.httydcraft.authcraft.core.server.commands.parameters.DoublePassword;
import com.httydcraft.authcraft.core.server.commands.parameters.NewPassword;
import com.httydcraft.authcraft.core.server.commands.parameters.RegisterPassword;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;
import io.github.revxrsal.eventbus.EventBus;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.SendMessageException;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.Orphans;

import java.util.Collection;
import java.util.Optional;

// #region Class Documentation
/**
 * Abstract registry for server commands.
 * Configures and registers command handlers, contexts, and dependencies.
 */
public abstract class ServerCommandsRegistry {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    protected final AuthPlugin plugin = AuthPlugin.instance();
    protected final CommandHandler commandHandler;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code ServerCommandsRegistry}.
     *
     * @param commandHandler The command handler to register commands with. Must not be null.
     */
    public ServerCommandsRegistry(CommandHandler commandHandler) {
        SecurityAuditLogger.logSuccess("ServerCommandsRegistry: command registry event", null, "Command registry event triggered");
        if (commandHandler == null) {
            SecurityAuditLogger.logFailure("ServerCommandsRegistry", null, "CommandHandler is null on command registry event");
            throw new NullPointerException("commandHandler must not be null");
        }
        this.commandHandler = commandHandler;
        LOGGER.atFine().log("Initialized ServerCommandsRegistry");

        registerCommandContexts();
        registerDependencies();
    }
    // #endregion

    // #region Command Context Registration
    /**
     * Registers command context resolvers for parameter types.
     */
    private void registerCommandContexts() {
        LOGGER.atFine().log("Registering command contexts");
        PluginConfig config = plugin.getConfig();

        commandHandler.registerValueResolver(LinkUserIdentificator.class, context -> {
            LOGGER.atFine().log("Resolved null LinkUserIdentificator");
            return null;
        });

        commandHandler.registerValueResolver(UserNumberIdentificator.class, context -> {
            int value = context.popInt();
            LOGGER.atFine().log("Resolved UserNumberIdentificator: %d", value);
            return new UserNumberIdentificator(value);
        });

        commandHandler.registerValueResolver(MessengerLinkContext.class, context -> {
            String code = context.popForParameter();
            LOGGER.atFine().log("Resolving MessengerLinkContext for code: %s", code);

            Optional<LinkConfirmationUser> confirmationUserOptional = plugin.getLinkConfirmationBucket()
                    .findFirst(user -> user.getConfirmationCode().equals(code));

            if (!confirmationUserOptional.isPresent()) {
                LOGGER.atFine().log("No confirmation user found for code: %s", code);
                throw new SendComponentException(
                        config.getServerMessages().getSubMessages("link-code").getMessage("no-code"));
            }

            LinkConfirmationUser confirmationUser = confirmationUserOptional.get();

            if (System.currentTimeMillis() > confirmationUser.getLinkTimeoutTimestamp()) {
                LOGGER.atFine().log("Confirmation timed out for code: %s", code);
                throw new SendComponentException(
                        config.getServerMessages().getSubMessages("link-code").getMessage("timed-out"));
            }

            CommandActor actor = context.actor();
            if (!actor.getUniqueId().equals(confirmationUser.getLinkTarget().getUniqueId())) {
                LOGGER.atFine().log("Actor %s does not match target for code: %s", actor.getUniqueId(), code);
                throw new SendComponentException(
                        config.getServerMessages().getSubMessages("link-code").getMessage("no-code"));
            }

            LinkUser linkUser = confirmationUser.getLinkTarget()
                    .findFirstLinkUserOrNew(user -> user.getLinkType().equals(confirmationUser.getLinkType()),
                            confirmationUser.getLinkType());

            if (!linkUser.isIdentifierDefaultOrNull()) {
                LOGGER.atFine().log("Account already linked for code: %s", code);
                throw new SendComponentException(
                        config.getServerMessages().getSubMessages("link-code").getMessage("already-linked"));
            }

            LOGGER.atFine().log("Resolved MessengerLinkContext for code: %s", code);
            return new MessengerLinkContext(code, confirmationUser);
        });

        commandHandler.registerValueResolver(DoublePassword.class, context -> {
            ArgumentStack arguments = context.arguments();
            String oldPassword = arguments.pop();
            if (arguments.isEmpty()) {
                LOGGER.atFine().log("Missing new password in DoublePassword");
                throw new SendComponentException(config.getServerMessages().getMessage("enter-new-password"));
            }
            String newPassword = arguments.pop();
            if (oldPassword.equals(newPassword)) {
                LOGGER.atFine().log("Old and new passwords are identical");
                throw new SendComponentException(config.getServerMessages().getMessage("nothing-to-change"));
            }

            if (newPassword.length() < config.getPasswordMinLength()) {
                LOGGER.atFine().log("New password too short: %d", newPassword.length());
                throw new SendComponentException(config.getServerMessages().getMessage("password-too-short"));
            }

            if (newPassword.length() > config.getPasswordMaxLength()) {
                LOGGER.atFine().log("New password too long: %d", newPassword.length());
                throw new SendComponentException(config.getServerMessages().getMessage("password-too-long"));
            }

            DoublePassword password = new DoublePassword(oldPassword, newPassword);
            LOGGER.atFine().log("Resolved DoublePassword");
            return password;
        });

        commandHandler.registerValueResolver(NewPassword.class, context -> {
            String newRawPassword = context.pop();
            if (newRawPassword.length() < config.getPasswordMinLength()) {
                LOGGER.atFine().log("New password too short: %d", newRawPassword.length());
                throw new SendComponentException(config.getServerMessages().getMessage("password-too-short"));
            }

            if (newRawPassword.length() > config.getPasswordMaxLength()) {
                LOGGER.atFine().log("New password too long: %d", newRawPassword.length());
                throw new SendComponentException(config.getServerMessages().getMessage("password-too-long"));
            }

            NewPassword password = new NewPassword(newRawPassword);
            LOGGER.atFine().log("Resolved NewPassword");
            return password;
        });

        commandHandler.registerValueResolver(RegisterPassword.class, context -> {
            ArgumentStack arguments = context.arguments();
            String registerPassword = arguments.pop();

            if (config.isPasswordConfirmationEnabled()) {
                if (arguments.isEmpty()) {
                    LOGGER.atFine().log("Missing confirmation password");
                    throw new SendComponentException(config.getServerMessages().getMessage("confirm-password"));
                }
                String confirmationPassword = arguments.pop();
                if (!confirmationPassword.equals(registerPassword)) {
                    LOGGER.atFine().log("Password confirmation failed");
                    throw new SendComponentException(config.getServerMessages().getMessage("confirm-failed"));
                }
            }

            if (registerPassword.length() < config.getPasswordMinLength()) {
                LOGGER.atFine().log("Register password too short: %d", registerPassword.length());
                throw new SendComponentException(config.getServerMessages().getMessage("password-too-short"));
            }

            if (registerPassword.length() > config.getPasswordMaxLength()) {
                LOGGER.atFine().log("Register password too long: %d", registerPassword.length());
                throw new SendComponentException(config.getServerMessages().getMessage("password-too-long"));
            }

            RegisterPassword password = new RegisterPassword(registerPassword);
            LOGGER.atFine().log("Resolved RegisterPassword");
            return password;
        });

        commandHandler.registerValueResolver(ArgumentAccount.class, context -> {
            String parameter = context.popForParameter();
            ExecutableCommand command = context.parameter().getDeclaringCommand();
            if (!command.hasAnnotation(Admin.class)) {
                LOGGER.atFine().log("Non-admin command attempted to resolve Account: %s", command.getPath().toRealString());
                throw new SendMessageException(
                        "Cannot resolve Account in non-admin class in '" + command.getPath().toRealString() + "'");
            }

            ArgumentAccount account = new ArgumentAccount(plugin.getAccountDatabase().getAccountFromName(parameter)
                    .thenApply(accountData -> {
                        if (accountData == null || !accountData.isRegistered()) {
                            LOGGER.atFine().log("Account not found for name: %s", parameter);
                            throw new SendComponentException(config.getServerMessages().getMessage("account-not-found"));
                        }
                        return accountData;
                    }));
            LOGGER.atFine().log("Resolved ArgumentAccount for name: %s", parameter);
            return account;
        });

        commandHandler.registerCondition(new CommandCooldownCondition<>(config.getServerMessages(), SendComponentException::new));
        commandHandler.registerCondition((actor, command, arguments) -> {
            if (!command.hasAnnotation(GoogleUse.class)) {
                return;
            }
            if (!config.getGoogleAuthenticatorSettings().isEnabled()) {
                LOGGER.atFine().log("Google command blocked: Google disabled");
                throw new SendComponentException(
                        config.getServerMessages().getSubMessages("google").getMessage("disabled"));
            }
        });

        commandHandler.registerCondition((actor, command, arguments) -> {
            if (!command.hasAnnotation(VkUse.class)) {
                return;
            }
            if (!config.getVKSettings().isEnabled()) {
                LOGGER.atFine().log("VK command blocked: VK disabled");
                throw new SendComponentException(
                        config.getServerMessages().getSubMessages("vk").getMessage("disabled"));
            }
        });

        commandHandler.registerCondition((actor, command, arguments) -> {
            if (!command.hasAnnotation(DiscordUse.class)) {
                return;
            }
            if (!config.getDiscordSettings().isEnabled()) {
                LOGGER.atFine().log("Discord command blocked: Discord disabled");
                throw new SendComponentException(
                        config.getServerMessages().getSubMessages("discord").getMessage("disabled"));
            }
        });

        commandHandler.registerCondition((actor, command, arguments) -> {
            if (!command.hasAnnotation(TelegramUse.class)) {
                return;
            }
            if (!config.getTelegramSettings().isEnabled()) {
                LOGGER.atFine().log("Telegram command blocked: Telegram disabled");
                throw new SendComponentException(
                        config.getServerMessages().getSubMessages("telegram").getMessage("disabled"));
            }
        });
    }
    // #endregion

    // #region Dependency Registration
    /**
     * Registers dependencies for the command handler.
     */
    private void registerDependencies() {
        commandHandler.registerDependency(AuthenticatingAccountBucket.class, plugin.getAuthenticatingAccountBucket());
        commandHandler.registerDependency(LinkConfirmationBucket.class, plugin.getLinkConfirmationBucket());
        commandHandler.registerDependency(EventBus.class, plugin.getEventBus());
        commandHandler.registerDependency(PluginConfig.class, plugin.getConfig());
        commandHandler.registerDependency(ServerMessages.class, plugin.getConfig().getServerMessages());
        commandHandler.registerDependency(AccountDatabase.class, plugin.getAccountDatabase());
        commandHandler.registerDependency(AuthPlugin.class, plugin);
    }

// #endregion

    // #region Command Registration
    protected void registerCommands() {
        try {
            commandHandler.register(new AuthCommand(), new LoginCommand(), new RegisterCommand(), new ChangePasswordCommand(), new GoogleCodeCommand(),
                    new GoogleCommand(), new GoogleUnlinkCommand(), new LogoutCommand());

            if (confirmationTypeEnabled(LinkConfirmationType.FROM_GAME))
                commandHandler.register(Orphans.path("code").handler(new LinkCodeCommand()));
            if (plugin.getConfig().getVKSettings().isEnabled())
                registerLinkCommand(VKLinkType.getInstance(), new VKLinkCommand(VKLinkType.getInstance().getServerMessages()));
            if (plugin.getConfig().getTelegramSettings().isEnabled())
                registerLinkCommand(TelegramLinkType.getInstance(), new TelegramLinkCommand(TelegramLinkType.getInstance().getServerMessages()));
            if (plugin.getConfig().getDiscordSettings().isEnabled())
                registerLinkCommand(DiscordLinkType.getInstance(), new DiscordLinkCommand(DiscordLinkType.getInstance().getServerMessages()));
            SecurityAuditLogger.logSuccess("ServerCommandsRegistry: command registration successful", null, "Commands registered successfully");
        } catch (Exception e) {
            SecurityAuditLogger.logFailure("ServerCommandsRegistry", null, "Error registering commands: " + e.getMessage());
            throw e;
        }
    }

    private void registerLinkCommand(LinkType linkType, OrphanCommand linkCommand) {
        if (!confirmationTypeEnabled(linkType, LinkConfirmationType.FROM_LINK))
            return;
        commandHandler.register(Orphans.path(makeServerCommandPaths(linkType, MessengerLinkCommandTemplate.CONFIGURATION_KEY)).handler(linkCommand));
    }

    private boolean confirmationTypeEnabled(LinkType linkType, LinkConfirmationType confirmationType) {
        return linkType.findSettings()
                .map(LinkSettings::getLinkConfirmationTypes)
                .map(confirmationTypes -> confirmationTypes.contains(confirmationType))
                .orElse(false);
    }

    private boolean confirmationTypeEnabled(LinkConfirmationType confirmationType) {
        Collection<LinkType> linkTypes = plugin.getLinkTypeProvider().getLinkTypes();
        return linkTypes.stream().anyMatch(linkType -> confirmationTypeEnabled(linkType, confirmationType));
    }

    private String[] makeServerCommandPaths(LinkType linkType, String commandPathKey) {
        return linkType.getSettings().getProxyCommandPaths().getCommandPath(commandPathKey).getCommandPaths();
    }


    // #endregion
}