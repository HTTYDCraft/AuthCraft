package com.httydcraft.authcraft.api.config.link;

import java.util.List;

import com.httydcraft.authcraft.api.config.link.command.LinkCommandPaths;
import com.httydcraft.authcraft.api.config.link.command.LinkCustomCommands;
import com.httydcraft.authcraft.api.config.link.stage.LinkConfirmationSettings;
import com.httydcraft.authcraft.api.config.link.stage.LinkEnterSettings;
import com.httydcraft.authcraft.api.config.link.stage.LinkRestoreSettings;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.type.LinkConfirmationType;

public interface LinkSettings {
    boolean isEnabled();

    int getMaxLinkCount();

    boolean isAdministrator(LinkUserIdentificator identificator);

    LinkConfirmationSettings getConfirmationSettings();

    LinkRestoreSettings getRestoreSettings();

    LinkCustomCommands getCustomCommands();

    LinkEnterSettings getEnterSettings();

    LinkCommandPaths getCommandPaths();

    LinkCommandPaths getProxyCommandPaths();

    LinkKeyboards getKeyboards();

    Messages<String> getMessages();

    List<LinkConfirmationType> getLinkConfirmationTypes();

    boolean shouldDisableConversationCommands();
}
