package com.httydcraft.authcraft.api.config.link.command;

import com.httydcraft.authcraft.api.util.CollectionUtil;

public interface LinkCommandPathSettings {
    String getCommandPath();

    String[] getAliases();

    default String[] getCommandPaths() {
        String[] commandPath = {getCommandPath()};
        String[] aliases = getAliases();
        return CollectionUtil.addAll(commandPath, aliases);
    }
}
