package com.httydcraft.authcraft.core.discord.listener;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.core.discord.command.actor.BaseJDAButtonActor;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.core.link.discord.DiscordLinkType;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.CommandPath;
import revxrsal.commands.jda.JDAActor;
import revxrsal.commands.jda.annotation.OptionData;
import revxrsal.commands.jda.core.actor.BaseJDASlashCommandActor;
import revxrsal.commands.jda.JDACommandHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// #region Class Documentation
/**
 * Listener for handling Discord command-related events.
 * Processes slash commands, button interactions, and autocomplete requests.
 */
public class JDACommandListener implements EventListener {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final DiscordLinkType DISCORD_LINK_TYPE = DiscordLinkType.getInstance();
    private final JDACommandHandler handler;
    private final Function<JDAActor, LinkCommandActorWrapper> wrapper;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code JDACommandListener}.
     *
     * @param handler The JDA command handler. Must not be null.
     * @param wrapper The function to wrap actors. Must not be null.
     */
    public JDACommandListener(JDACommandHandler handler, Function<JDAActor, LinkCommandActorWrapper> wrapper) {
        this.handler = Preconditions.checkNotNull(handler, "handler must not be null");
        this.wrapper = Preconditions.checkNotNull(wrapper, "wrapper must not be null");
        LOGGER.atFine().log("Initialized JDACommandListener");
    }
    // #endregion

    // #region Event Dispatch
    /**
     * Dispatches a command to the handler with a wrapped actor.
     *
     * @param actor     The JDA actor. Must not be null.
     * @param arguments The command arguments. Must not be null.
     */
    private void dispatch(JDAActor actor, ArgumentStack arguments) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(arguments, "arguments must not be null");
        LinkCommandActorWrapper actorWrapper = wrapper.apply(actor);
        try {
            handler.dispatch(actorWrapper, arguments);
            LOGGER.atFine().log("Dispatched command for actor: %s, arguments: %s", actor.getId(), arguments);
        } catch (Throwable t) {
            handler.getExceptionHandler().handleException(t, actorWrapper);
            LOGGER.atWarning().withCause(t).log("Failed to dispatch command for actor: %s", actor.getId());
        }
    }
    // #endregion

    // #region Event Handling
    /**
     * Handles incoming Discord events.
     *
     * @param genericEvent The generic event. Must not be null.
     */
    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        Preconditions.checkNotNull(genericEvent, "genericEvent must not be null");
        if (genericEvent instanceof SlashCommandInteractionEvent) {
            onSlashCommandEvent((SlashCommandInteractionEvent) genericEvent);
            LOGGER.atFine().log("Processed SlashCommandInteractionEvent");
        } else if (genericEvent instanceof ButtonInteractionEvent) {
            onButtonEvent((ButtonInteractionEvent) genericEvent);
            LOGGER.atFine().log("Processed ButtonInteractionEvent");
        } else if (genericEvent instanceof CommandAutoCompleteInteractionEvent) {
            onAutocompleteEvent((CommandAutoCompleteInteractionEvent) genericEvent);
            LOGGER.atFine().log("Processed CommandAutoCompleteInteractionEvent");
        }
    }

    /**
     * Handles autocomplete interaction events.
     *
     * @param event The autocomplete interaction event. Must not be null.
     */
    private void onAutocompleteEvent(CommandAutoCompleteInteractionEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        Optional<ExecutableCommand> commandOptional = findExecutableCommand(event);
        if (!commandOptional.isPresent()) {
            LOGGER.atFine().log("No executable command found for autocomplete event");
            return;
        }
        ExecutableCommand command = commandOptional.get();
        AutoCompleteQuery focusedOption = event.getFocusedOption();
        Optional<CommandParameter> foundParameter = command.getValueParameters()
                .values()
                .stream()
                .filter(parameter -> getParameterName(parameter).equals(focusedOption.getName()))
                .findFirst();
        if (!foundParameter.isPresent()) {
            LOGGER.atFine().log("No parameter found for focused option: %s", focusedOption.getName());
            return;
        }
        CommandParameter parameter = foundParameter.get();
        try {
            Collection<String> suggestions = parameter.getSuggestionProvider()
                    .getSuggestions(event.getOptions().stream().map(OptionMapping::getAsString).collect(Collectors.toList()),
                            JDAActor.wrap(event, handler), command);
            List<Choice> choices = suggestions.stream().map(suggestion -> {
                if (focusedOption.getType() == OptionType.NUMBER) {
                    return new Choice(suggestion, Double.parseDouble(suggestion));
                }
                if (focusedOption.getType() == OptionType.INTEGER) {
                    return new Choice(suggestion, Long.parseLong(suggestion));
                }
                return new Choice(suggestion, suggestion);
            }).collect(Collectors.toList());
            event.replyChoices(choices).queue();
            LOGGER.atFine().log("Replied with %d autocomplete suggestions for parameter: %s", choices.size(), parameter.getName());
        } catch (Throwable e) {
            LOGGER.atWarning().withCause(e).log("Failed to process autocomplete for parameter: %s", parameter.getName());
        }
    }

    /**
     * Handles button interaction events.
     *
     * @param event The button interaction event. Must not be null.
     */
    private void onButtonEvent(ButtonInteractionEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        String content = event.getComponentId();
        if (content.isEmpty()) {
            LOGGER.atFine().log("Empty component ID for button interaction");
            return;
        }
        event.deferReply(true).queue();
        JDAActor actor = new BaseJDAButtonActor(event, handler);
        try {
            ArgumentStack arguments = ArgumentStack.parse(content);
            dispatch(actor, arguments);
            LOGGER.atFine().log("Processed button interaction with componentId: %s", content);
        } catch (Throwable t) {
            handler.getExceptionHandler().handleException(t, actor);
            LOGGER.atWarning().withCause(t).log("Failed to process button interaction with componentId: %s", content);
        }
    }

    /**
     * Handles slash command interaction events.
     *
     * @param event The slash command interaction event. Must not be null.
     */
    private void onSlashCommandEvent(SlashCommandInteractionEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        parseSlashCommandEvent(event).ifPresent(arguments -> {
            JDAActor actor = new BaseJDASlashCommandActor(event, handler);
            if (!event.getInteraction().isAcknowledged() && !event.getHook().isExpired()) {
                event.deferReply(true).queue();
                LOGGER.atFine().log("Deferred reply for slash command: %s", event.getName());
            }
            dispatch(actor, arguments);
            LOGGER.atFine().log("Processed slash command: %s", event.getName());
        });
    }
    // #endregion

    // #region Command Parsing
    /**
     * Finds the executable command for a command interaction.
     *
     * @param event The command interaction payload. Must not be null.
     * @return An {@link Optional} containing the {@link ExecutableCommand}, or empty if not found.
     */
    private Optional<ExecutableCommand> findExecutableCommand(CommandInteractionPayload event) {
        Preconditions.checkNotNull(event, "event must not be null");
        CommandPath commandPath = CommandPath.get(
                Stream.of(event.getName(), event.getSubcommandGroup(), event.getSubcommandName())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        ExecutableCommand command = handler.getCommand(commandPath);
        if (command != null) {
            LOGGER.atFine().log("Found executable command: %s", commandPath);
            return Optional.of(command);
        }
        CommandCategory category = handler.getCategory(commandPath);
        if (category == null) {
            LOGGER.atFine().log("No command or category found for path: %s", commandPath);
            return Optional.empty();
        }
        ExecutableCommand defaultAction = category.getDefaultAction();
        LOGGER.atFine().log("Found default action for category: %s", commandPath);
        return Optional.ofNullable(defaultAction);
    }

    /**
     * Gets the name of a command parameter.
     *
     * @param parameter The command parameter. Must not be null.
     * @return The parameter name.
     */
    private String getParameterName(CommandParameter parameter) {
        Preconditions.checkNotNull(parameter, "parameter must not be null");
        if (parameter.hasAnnotation(OptionData.class)) {
            OptionData optionData = parameter.getAnnotation(OptionData.class);
            String name = optionData.name().isEmpty() ? parameter.getName() : optionData.name();
            LOGGER.atFine().log("Retrieved parameter name from OptionData: %s", name);
            return name;
        }
        LOGGER.atFine().log("Retrieved default parameter name: %s", parameter.getName());
        return parameter.getName();
    }

    /**
     * Parses a slash command interaction event into an argument stack.
     *
     * @param event The slash command interaction event. Must not be null.
     * @return An {@link Optional} containing the {@link ArgumentStack}, or empty if parsing fails.
     */
    private Optional<ArgumentStack> parseSlashCommandEvent(SlashCommandInteractionEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        if (event.getCommandType() != Command.Type.SLASH) {
            ArgumentStack arguments = ArgumentStack.copyExact(event.getName());
            LOGGER.atFine().log("Parsed non-slash command: %s", event.getName());
            return Optional.of(arguments);
        }
        return findExecutableCommand(event).map(foundCommand -> {
            List<String> arguments = Lists.newArrayList(foundCommand.getPath().toList());
            Map<Integer, CommandParameter> valueParameters = foundCommand.getValueParameters();
            for (int i = 0; i < valueParameters.size(); i++) {
                CommandParameter parameter = valueParameters.get(i);
                OptionMapping optionMapping = event.getOption(getParameterName(parameter));
                if (optionMapping == null) {
                    arguments.addAll(parameter.getDefaultValue());
                    LOGGER.atFine().log("Added default values for parameter: %s", parameter.getName());
                    continue;
                }
                if (parameter.isFlag()) {
                    arguments.add("-" + parameter.getFlagName());
                    LOGGER.atFine().log("Added flag: %s", parameter.getFlagName());
                }
                if (parameter.isSwitch() && optionMapping.getType() == OptionType.BOOLEAN && optionMapping.getAsBoolean()) {
                    arguments.add("-" + parameter.getSwitchName());
                    LOGGER.atFine().log("Added switch: %s", parameter.getSwitchName());
                    continue;
                }
                appendOptionMapping(arguments, optionMapping);
                LOGGER.atFine().log("Appended option mapping for parameter: %s", parameter.getName());
            }
            ArgumentStack argumentStack = ArgumentStack.copyExact(arguments);
            LOGGER.atFine().log("Parsed slash command arguments: %s", argumentStack);
            return argumentStack;
        });
    }

    /**
     * Appends an option mapping to the argument list.
     *
     * @param arguments    The argument list to append to. Must not be null.
     * @param optionMapping The option mapping. Must not be null.
     */
    private void appendOptionMapping(Collection<String> arguments, OptionMapping optionMapping) {
        Preconditions.checkNotNull(arguments, "arguments must not be null");
        Preconditions.checkNotNull(optionMapping, "optionMapping must not be null");
        switch (optionMapping.getType()) {
            case CHANNEL:
                arguments.add(optionMapping.getAsChannel().getName());
                LOGGER.atFine().log("Appended channel argument: %s", optionMapping.getAsChannel().getName());
                break;
            case USER:
                arguments.add(optionMapping.getAsUser().getName());
                LOGGER.atFine().log("Appended user argument: %s", optionMapping.getAsUser().getName());
                break;
            case ROLE:
                arguments.add(optionMapping.getAsRole().getName());
                LOGGER.atFine().log("Appended role argument: %s", optionMapping.getAsRole().getName());
                break;
            case MENTIONABLE:
                String mentionable = "<@" + optionMapping.getAsMentionable().getIdLong() + ">";
                arguments.add(mentionable);
                LOGGER.atFine().log("Appended mentionable argument: %s", mentionable);
                break;
            default:
                arguments.add(optionMapping.getAsString());
                LOGGER.atFine().log("Appended string argument: %s", optionMapping.getAsString());
        }
    }
    // #endregion
}