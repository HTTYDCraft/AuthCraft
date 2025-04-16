package com.httydcraft.authcraft.core.config.link;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.link.command.LinkCustomCommandSettings;
import com.httydcraft.authcraft.api.config.link.command.LinkCustomCommands;
import com.httydcraft.authcraft.api.link.command.context.CustomCommandExecutionContext;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.core.config.factory.ConfigurationHolderMapResolverFactory.ConfigurationHolderMap;

import java.util.Collection;
import java.util.stream.Collectors;

// #region Class Documentation
/**
 * Configuration for custom messenger commands.
 * Manages a collection of custom command settings and their execution.
 */
public class BaseMessengerCustomCommands implements ConfigurationHolder, LinkCustomCommands {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("self")
    private ConfigurationHolderMap<BaseCustomCommandSettings> customCommands = new ConfigurationHolderMap<>();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseMessengerCustomCommands} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public BaseMessengerCustomCommands(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized BaseMessengerCustomCommands with %d commands", customCommands.size());
    }
    // #endregion

    // #region Command Execution
    /**
     * Executes custom commands based on the context.
     *
     * @param context The execution context. Must not be null.
     * @return A collection of applicable {@link LinkCustomCommandSettings}.
     */
    @Override
    public Collection<LinkCustomCommandSettings> execute(CustomCommandExecutionContext context) {
        Preconditions.checkNotNull(context, "context must not be null");
        Collection<LinkCustomCommandSettings> applicableCommands = customCommands.values().stream()
                .filter(customCommand -> customCommand.shouldExecute(context))
                .collect(Collectors.toList());
        LOGGER.atFine().log("Found %d applicable custom commands", applicableCommands.size());
        return applicableCommands;
    }
    // #endregion
}