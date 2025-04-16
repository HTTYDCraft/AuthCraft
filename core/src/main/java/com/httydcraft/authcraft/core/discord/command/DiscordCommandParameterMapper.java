package com.httydcraft.authcraft.core.discord.command;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.config.discord.DiscordCommandArgumentSettings;
import com.httydcraft.authcraft.core.config.discord.DiscordCommandSettings;
import com.httydcraft.authcraft.api.config.link.command.LinkCommandPathSettings;
import com.httydcraft.authcraft.core.link.discord.DiscordLinkType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.SlashCommandMapper;
import revxrsal.commands.jda.core.adapter.SlashCommandAdapter;
import revxrsal.commands.process.ParameterNamingStrategy;

import java.util.Optional;

// #region Class Documentation
/**
 * Maps Discord slash command parameters and customizes their names and descriptions.
 * Implements {@link SlashCommandMapper} and {@link ParameterNamingStrategy} for Discord integration.
 */
public class DiscordCommandParameterMapper implements SlashCommandMapper, ParameterNamingStrategy {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Slash Command Mapping
    /**
     * Maps a slash command's options to their configured descriptions.
     *
     * @param slashCommand The slash command adapter. Must not be null.
     * @param command      The executable command. Must not be null.
     */
    @Override
    public void mapSlashCommand(@NotNull SlashCommandAdapter slashCommand, @NotNull ExecutableCommand command) {
        Preconditions.checkNotNull(slashCommand, "slashCommand must not be null");
        Preconditions.checkNotNull(command, "command must not be null");
        Optional<DiscordCommandSettings> settingsOptional = getCommandSettings(command);
        if (!settingsOptional.isPresent()) {
            LOGGER.atFine().log("No DiscordCommandSettings found for command: %s", command.getName());
            return;
        }
        DiscordCommandSettings settings = settingsOptional.get();
        for (OptionData option : slashCommand.getOptions()) {
            DiscordCommandArgumentSettings argumentSettings = settings.getArguments().get(option.getName());
            if (argumentSettings == null) {
                LOGGER.atFine().log("No argument settings for option: %s", option.getName());
                continue;
            }
            option.setDescription(argumentSettings.getDescription());
            LOGGER.atFine().log("Mapped option %s with description: %s", option.getName(), argumentSettings.getDescription());
        }
    }
    // #endregion

    // #region Parameter Naming
    /**
     * Gets the name for a command parameter, using configured settings if available.
     *
     * @param parameter The command parameter. Must not be null.
     * @return The parameter name.
     */
    @Override
    public @NotNull String getName(@NotNull CommandParameter parameter) {
        Preconditions.checkNotNull(parameter, "parameter must not be null");
        Optional<DiscordCommandSettings> settingsOptional = getCommandSettings(parameter.getDeclaringCommand());
        if (!settingsOptional.isPresent()) {
            LOGGER.atFine().log("No DiscordCommandSettings found for parameter: %s", parameter.getName());
            return parameter.getName();
        }
        DiscordCommandSettings settings = settingsOptional.get();
        DiscordCommandArgumentSettings argumentSettings = settings.getArguments().get(parameter.getName());
        if (argumentSettings == null) {
            LOGGER.atFine().log("No argument settings for parameter: %s", parameter.getName());
            return parameter.getName();
        }
        String name = argumentSettings.getName();
        LOGGER.atFine().log("Retrieved parameter name: %s for parameter: %s", name, parameter.getName());
        return name;
    }
    // #endregion

    // #region Helper Methods
    /**
     * Retrieves the Discord command settings for a command.
     *
     * @param command The executable command. Must not be null.
     * @return An {@link Optional} containing the {@link DiscordCommandSettings}, or empty if not found.
     */
    private Optional<DiscordCommandSettings> getCommandSettings(ExecutableCommand command) {
        Preconditions.checkNotNull(command, "command must not be null");
        if (!command.hasAnnotation(CommandKey.class)) {
            LOGGER.atFine().log("Command %s has no CommandKey annotation", command.getName());
            return Optional.empty();
        }
        CommandKey commandKey = command.getAnnotation(CommandKey.class);
        LinkCommandPathSettings pathSettings = DiscordLinkType.getInstance().getSettings().getCommandPaths().getCommandPath(commandKey.value());
        if (!(pathSettings instanceof DiscordCommandSettings)) {
            LOGGER.atFine().log("Command %s has no DiscordCommandSettings", command.getName());
            return Optional.empty();
        }
        DiscordCommandSettings settings = (DiscordCommandSettings) pathSettings;
        LOGGER.atFine().log("Retrieved DiscordCommandSettings for command: %s", command.getName());
        return Optional.of(settings);
    }
    // #endregion
}