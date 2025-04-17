package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.bucket.LinkConfirmationBucket;
import com.httydcraft.authcraft.api.commands.MessageableCommandActor;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.core.commands.exception.MessengerExceptionHandler;
import com.httydcraft.authcraft.core.commands.parameter.MessengerLinkContext;
import com.httydcraft.authcraft.core.commands.validator.CommandCooldownCondition;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.confirmation.LinkConfirmationUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.model.PlayerIdSupplier;
import com.httydcraft.authcraft.core.server.commands.annotations.GoogleUse;
import com.httydcraft.authcraft.core.server.commands.parameters.NewPassword;
import com.httydcraft.authcraft.api.type.LinkConfirmationType;
import io.github.revxrsal.eventbus.EventBus;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.SendMessageException;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.Orphans;

import java.util.Optional;

// #region Class Documentation
/**
 * Abstract registry for messenger commands, handling context resolution, dependency injection, and command registration.
 * Provides a framework for registering commands specific to a link type.
 */
public abstract class MessengerCommandRegistry {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    private final CommandHandler commandHandler;
    private final LinkType linkType;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code MessengerCommandRegistry} with the specified command handler and link type.
     *
     * @param commandHandler The command handler to use. Must not be null.
     * @param linkType      The link type for this registry. Must not be null.
     */
    public MessengerCommandRegistry(CommandHandler commandHandler, LinkType linkType) {
        this.commandHandler = Preconditions.checkNotNull(commandHandler, "commandHandler must not be null");
        this.linkType = Preconditions.checkNotNull(linkType, "linkType must not be null");
        LOGGER.atInfo().log("Initializing MessengerCommandRegistry for linkType: %s", linkType);
        register();
    }
    // #endregion

    // #region Registration
    /**
     * Registers contexts and dependencies for the command handler.
     */
    private void register() {
        LOGGER.atFine().log("Registering contexts and dependencies for linkType: %s", linkType);
        registerContexts();
        registerDependencies();
    }

    /**
     * Registers context resolvers and conditions for the command handler.
     */
    private void registerContexts() {
        LOGGER.atFine().log("Registering contexts for linkType: %s", linkType);
        commandHandler.setExceptionHandler(new MessengerExceptionHandler(linkType));
        commandHandler.registerContextValue(LinkType.class, linkType);
        commandHandler.registerCondition(new CommandCooldownCondition<>(linkType.getLinkMessages(), SendMessageException::new));
        commandHandler.registerCondition((actor, command, arguments) -> {
            if (!command.hasAnnotation(GoogleUse.class)) {
                return;
            }
            if (!PLUGIN.getConfig().getGoogleAuthenticatorSettings().isEnabled()) {
                throw new SendMessageException(linkType.getSettings().getMessages().getMessage("google-disabled"));
            }
        });

        commandHandler.registerContextResolver(MessageableCommandActor.class, context -> wrapActor(context.actor()));
        commandHandler.registerValueResolver(PlayerIdSupplier.class, context -> PlayerIdSupplier.of(context.pop()));
        commandHandler.registerContextResolver(LinkUserIdentificator.class, context -> wrapActor(context.actor()).userId());
        commandHandler.registerValueResolver(NewPassword.class, context -> {
            String newRawPassword = context.pop();
            if (newRawPassword.length() < PLUGIN.getConfig().getPasswordMinLength()) {
                throw new SendMessageException(linkType.getSettings().getMessages().getMessage("changepass-password-too-short"));
            }
            if (newRawPassword.length() > PLUGIN.getConfig().getPasswordMaxLength()) {
                throw new SendMessageException(linkType.getSettings().getMessages().getMessage("changepass-password-too-long"));
            }
            return new NewPassword(newRawPassword);
        });
        commandHandler.registerValueResolver(MessengerLinkContext.class, context -> {
            String code = context.popForParameter();
            Optional<LinkConfirmationUser> confirmationUserOptional = PLUGIN.getLinkConfirmationBucket()
                    .findFirst(user -> user.getConfirmationCode().equals(code) && user.getLinkType().equals(linkType));

            if (!confirmationUserOptional.isPresent()) {
                throw new SendMessageException(linkType.getSettings().getMessages().getMessage("confirmation-no-code"));
            }

            LinkConfirmationUser confirmationUser = confirmationUserOptional.get();
            if (System.currentTimeMillis() > confirmationUser.getLinkTimeoutTimestamp()) {
                throw new SendMessageException(linkType.getSettings().getMessages().getMessage("confirmation-timed-out"));
            }

            LinkUser linkUser = confirmationUser.getLinkTarget().findFirstLinkUserOrNew(user -> user.getLinkType().equals(linkType), linkType);
            if (!linkUser.isIdentifierDefaultOrNull()) {
                throw new SendMessageException(linkType.getSettings()
                        .getMessages()
                        .getMessage("confirmation-already-linked", linkType.newMessageContext(confirmationUser.getLinkTarget())));
            }

            return new MessengerLinkContext(code, confirmationUser);
        });
        commandHandler.registerValueResolver(Account.class, context -> {
            String playerName = context.popForParameter();
            LinkUserIdentificator userId = wrapActor(context.actor()).userId();
            Account account = PLUGIN.getAccountDatabase().getAccountFromName(playerName).get();
            if (account == null || !account.isRegistered()) {
                throw new SendMessageException(linkType.getSettings().getMessages().getMessage("account-not-found"));
            }

            Optional<LinkUser> linkUser = account.findFirstLinkUser(user -> user.getLinkType().equals(linkType));
            if (!linkType.getSettings().isAdministrator(userId)) {
                if (!linkUser.isPresent() || !linkUser.get().getLinkUserInfo().getIdentificator().equals(userId)) {
                    throw new SendMessageException(linkType.getSettings().getMessages().getMessage("not-your-account", linkType.newMessageContext(account)));
                }
            }
            return account;
        });
    }

    /**
     * Registers dependencies for the command handler.
     */
    private void registerDependencies() {
        LOGGER.atFine().log("Registering dependencies for linkType: %s", linkType);
        commandHandler.registerDependency(LinkConfirmationBucket.class, PLUGIN.getLinkConfirmationBucket());
        commandHandler.registerDependency(EventBus.class, PLUGIN.getEventBus());
        commandHandler.registerDependency(AccountDatabase.class, PLUGIN.getAccountDatabase());
        commandHandler.registerDependency(PluginConfig.class, PLUGIN.getConfig());
        commandHandler.registerDependency(AuthPlugin.class, PLUGIN);
        commandHandler.registerDependency(LinkType.class, linkType);
    }
    // #endregion

    // #region Command Registration
    /**
     * Registers all commands for this registry.
     */
    protected void registerCommands() {
        LOGGER.atInfo().log("Registering commands for linkType: %s", linkType);
        if (confirmationTypeEnabled(LinkConfirmationType.FROM_LINK)) {
            registerCommand(linkPath(LinkCodeCommand.CONFIGURATION_KEY), new LinkCodeCommand());
        }
        if (confirmationTypeEnabled(LinkConfirmationType.FROM_GAME)) {
            registerCommand(linkPath(MessengerLinkCommandTemplate.CONFIGURATION_KEY), createLinkCommand());
        }

        registerCommand(linkPath(ConfirmationToggleCommand.CONFIGURATION_KEY), new ConfirmationToggleCommand());
        registerCommand(linkPath(AccountsListCommand.CONFIGURATION_KEY), new AccountsListCommand());
        registerCommand(linkPath(AccountCommand.CONFIGURATION_KEY), new AccountCommand());
        registerCommand(linkPath(AccountEnterAcceptCommand.CONFIGURATION_KEY), new AccountEnterAcceptCommand());
        registerCommand(linkPath(AccountEnterDeclineCommand.CONFIGURATION_KEY), new AccountEnterDeclineCommand());
        registerCommand(linkPath(KickCommand.CONFIGURATION_KEY), new KickCommand());
        registerCommand(linkPath(RestoreCommand.CONFIGURATION_KEY), new RestoreCommand());
        registerCommand(linkPath(UnlinkCommand.CONFIGURATION_KEY), new UnlinkCommand());
        registerCommand(linkPath(ChangePasswordCommand.CONFIGURATION_KEY), new ChangePasswordCommand());
        registerCommand(linkPath(GoogleUnlinkCommand.CONFIGURATION_KEY), new GoogleUnlinkCommand());
        registerCommand(linkPath(GoogleCommand.CONFIGURATION_KEY), new GoogleCommand());
        registerCommand(linkPath(GoogleCodeCommand.CONFIGURATION_KEY), new GoogleCodeCommand());
        registerCommand(linkPath(AdminPanelCommand.CONFIGURATION_KEY), new AdminPanelCommand());
    }

    /**
     * Registers a single command with the specified path and instance.
     *
     * @param path           The command path. Must not be null.
     * @param commandInstance The command instance to register. Must not be null.
     */
    private void registerCommand(Orphans path, OrphanCommand commandInstance) {
        Preconditions.checkNotNull(path, "path must not be null");
        Preconditions.checkNotNull(commandInstance, "commandInstance must not be null");

        LOGGER.atFine().log("Registering command: %s", commandInstance.getClass().getSimpleName());
        commandHandler.register(path.handler(commandInstance));
    }
    // #endregion

    // #region Helper Methods
    /**
     * Checks if a confirmation type is enabled for the link type.
     *
     * @param confirmationType The confirmation type to check. Must not be null.
     * @return {@code true} if the confirmation type is enabled, {@code false} otherwise.
     */
    private boolean confirmationTypeEnabled(LinkConfirmationType confirmationType) {
        Preconditions.checkNotNull(confirmationType, "confirmationType must not be null");
        return linkType.getSettings().getLinkConfirmationTypes().contains(confirmationType);
    }

    /**
     * Creates a command path for the specified key.
     *
     * @param key The configuration key. Must not be null.
     * @return The {@code Orphans} path for the command.
     */
    private Orphans linkPath(String key) {
        Preconditions.checkNotNull(key, "key must not be null");
        return Orphans.path(commandPath(key));
    }

    /**
     * Retrieves the command path for the specified key.
     *
     * @param key The configuration key. Must not be null.
     * @return The command path as an array of strings.
     */
    private String[] commandPath(String key) {
        Preconditions.checkNotNull(key, "key must not be null");
        return linkType.getSettings().getCommandPaths().getCommandPath(key).getCommandPaths();
    }

    /**
     * Gets the command handler for this registry.
     *
     * @return The {@code CommandHandler} instance.
     */
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    /**
     * Wraps a command actor as a {@code LinkCommandActorWrapper}.
     *
     * @param actor The actor to wrap. Must not be null.
     * @return The wrapped actor.
     */
    protected LinkCommandActorWrapper wrapActor(CommandActor actor) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        return actor.as(LinkCommandActorWrapper.class);
    }

    /**
     * Creates a link command specific to the link type.
     *
     * @return The {@code MessengerLinkCommandTemplate} instance.
     */
    protected abstract MessengerLinkCommandTemplate createLinkCommand();
    // #endregion
}