package com.httydcraft.authcraft.core.config.link;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.link.command.LinkCommandPathSettings;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.annotation.ImportantField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

// #region Class Documentation
/**
 * Configuration for a command path.
 * Defines the main command path and its aliases.
 */
public class BaseCommandPath implements ConfigurationHolder, LinkCommandPathSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ImportantField
    @ConfigField("main-command")
    private String commandPath = null;
    @ConfigField("aliases")
    private List<String> aliases = new ArrayList<>();
    // #endregion

    // #region Constructors
    /**
     * Default constructor for {@code BaseCommandPath}.
     */
    public BaseCommandPath() {
        LOGGER.atFine().log("Initialized BaseCommandPath with default values");
    }

    /**
     * Constructs a new {@code BaseCommandPath} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public BaseCommandPath(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized BaseCommandPath with commandPath: %s", commandPath);
    }
    // #endregion

    // #region Getters
    /**
     * Gets the main command path.
     *
     * @return The command path.
     */
    public String getCommandPath() {
        return commandPath;
    }

    /**
     * Gets the command aliases.
     *
     * @return An array of aliases.
     */
    public String[] getAliases() {
        return aliases.toArray(new String[0]);
    }

    /**
     * Gets all command paths, including the main path and aliases.
     *
     * @return An array of all command paths.
     */
    public String[] getCommandPaths() {
        String[] originalCommandPath = {commandPath};
        String[] result = Stream.concat(Arrays.stream(getAliases()), Arrays.stream(originalCommandPath))
                .toArray(String[]::new);
        LOGGER.atFine().log("Retrieved %d command paths", result.length);
        return result;
    }
    // #endregion
}