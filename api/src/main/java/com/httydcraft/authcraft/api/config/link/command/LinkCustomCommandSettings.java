package com.httydcraft.authcraft.api.config.link.command;

import com.httydcraft.authcraft.api.link.command.context.CustomCommandExecutionContext;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

public interface LinkCustomCommandSettings {
    boolean shouldExecute(CustomCommandExecutionContext context);

    String getAnswer();

    ConfigurationSectionHolder getSectionHolder();
}
