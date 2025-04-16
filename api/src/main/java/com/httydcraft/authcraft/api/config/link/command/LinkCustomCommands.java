package com.httydcraft.authcraft.api.config.link.command;

import java.util.Collection;

import com.httydcraft.authcraft.api.link.command.context.CustomCommandExecutionContext;

public interface LinkCustomCommands {
    Collection<LinkCustomCommandSettings> execute(CustomCommandExecutionContext context);
}
