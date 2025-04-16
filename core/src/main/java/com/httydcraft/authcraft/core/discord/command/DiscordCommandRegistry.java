package com.httydcraft.authcraft.core.discord.command;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.core.commands.DiscordLinkCommand;
import com.httydcraft.authcraft.core.commands.MessengerCommandRegistry;
import com.httydcraft.authcraft.core.commands.MessengerLinkCommandTemplate;
import com.httydcraft.authcraft.core.discord.listener.JDACommandListener;
import com.httydcraft.authcraft.core.hooks.DiscordHook;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.core.link.discord.DiscordCommandActorWrapper;
import com.httydcraft.authcraft.core.link.discord.DiscordLinkType;
import net.dv8tion.jda.api.JDA;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.jda.JDAActor;
import revxrsal.commands.jda.JDACommandHandler;

// #region Class Documentation
/**
 * Registry for Discord commands.
 * Extends {@link MessengerCommandRegistry} to handle Discord-specific command registration and listeners.
 */
public class DiscordCommandRegistry extends MessengerCommandRegistry {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    private static final DiscordHook DISCORD_HOOK = PLUGIN.getHook(DiscordHook.class);
    private static final JDACommandHandler COMMAND_HANDLER = JDACommandHandler.create(DISCORD_HOOK.getJDA(), "");
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code DiscordCommandRegistry}.
     */
    public DiscordCommandRegistry() {
        super(COMMAND_HANDLER, DiscordLinkType.getInstance());
        configureCommandHandler();
        replaceNativeListener();
        registerCommands();
        COMMAND_HANDLER.registerSlashCommands();
        LOGGER.atInfo().log("Initialized DiscordCommandRegistry");
    }
    // #endregion

    // #region Command Handler Configuration
    /**
     * Configures the command handler with necessary settings and listeners.
     */
    private void configureCommandHandler() {
        COMMAND_HANDLER.disableStackTraceSanitizing();
        COMMAND_HANDLER.registerContextResolver(LinkCommandActorWrapper.class, context -> {
            LinkCommandActorWrapper wrapper = new DiscordCommandActorWrapper(context.actor());
            LOGGER.atFine().log("Resolved LinkCommandActorWrapper for actor: %s", context.actor().getId());
            return wrapper;
        });

        COMMAND_HANDLER.registerCondition((actor, command, arguments) -> {
            DiscordCommandActorWrapper actorWrapper = actor.as(DiscordCommandActorWrapper.class);
            if (actorWrapper.isGuildEvent() && !DiscordLinkType.getInstance().getSettings().isAllowedChannel(actorWrapper.getChannel().getId())) {
                LOGGER.atWarning().log("Forbidden channel access for command: %s, channel: %s", command.getName(), actorWrapper.getChannel().getId());
                throw new CommandErrorException(DiscordLinkType.getInstance().getLinkMessages().getMessage("forbidden-channel"));
            }
            LOGGER.atFine().log("Passed channel condition for command: %s", command.getName());
        });

        DiscordCommandParameterMapper parameterMapper = new DiscordCommandParameterMapper();
        COMMAND_HANDLER.registerSlashCommandMapper(parameterMapper);
        COMMAND_HANDLER.setParameterNamingStrategy(parameterMapper);
        LOGGER.atFine().log("Configured command handler with parameter mapper");
    }
    // #endregion

    // #region Listener Replacement
    /**
     * Replaces the native JDA command listener with a custom implementation.
     */
    private void replaceNativeListener() {
        JDA jda = DISCORD_HOOK.getJDA();
        jda.getRegisteredListeners().stream()
                .filter(listener -> listener.getClass().getName().equals("revxrsal.commands.jda.core.JDACommandListener"))
                .forEach(listener -> {
                    jda.removeEventListener(listener);
                    LOGGER.atFine().log("Removed native JDACommandListener");
                });
        JDACommandListener customListener = new JDACommandListener(COMMAND_HANDLER, this::wrapActor);
        jda.addEventListener(customListener);
        LOGGER.atFine().log("Added custom JDACommandListener");
    }
    // #endregion

    // #region Actor Wrapping
    /**
     * Wraps a command actor into a Discord-specific wrapper.
     *
     * @param actor The command actor to wrap. Must not be null.
     * @return The wrapped {@link LinkCommandActorWrapper}.
     */
    @Override
    protected LinkCommandActorWrapper wrapActor(CommandActor actor) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        DiscordCommandActorWrapper wrapper = new DiscordCommandActorWrapper(actor.as(JDAActor.class));
        LOGGER.atFine().log("Wrapped actor into DiscordCommandActorWrapper: %s", actor.getId());
        return wrapper;
    }
    // #endregion

    // #region Command Creation
    /**
     * Creates a Discord-specific link command template.
     *
     * @return The {@link MessengerLinkCommandTemplate}.
     */
    @Override
    protected MessengerLinkCommandTemplate createLinkCommand() {
        MessengerLinkCommandTemplate command = new DiscordLinkCommand(DiscordLinkType.getInstance().getLinkMessages());
        LOGGER.atFine().log("Created DiscordLinkCommand template");
        return command;
    }
    // #endregion
}