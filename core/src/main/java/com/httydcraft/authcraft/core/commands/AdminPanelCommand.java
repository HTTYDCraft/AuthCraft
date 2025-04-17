package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.multimessenger.core.keyboard.Keyboard;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.orphan.OrphanCommand;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;

// #region Class Documentation
/**
 * Command for displaying the admin panel menu.
 * Accessible only to administrators.
 */
@CommandKey(AdminPanelCommand.CONFIGURATION_KEY)
public class AdminPanelCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "admin-panel";
    // #endregion

    // #region Command Execution
    /**
     * Executes the admin panel command, displaying the admin menu to authorized users.
     *
     * @param actorWrapper The actor executing the command. Must not be null.
     * @param linkType     The link type associated with the command. Must not be null.
     */
    @DefaultFor("~")
    public void adminPanelMenu(LinkCommandActorWrapper actorWrapper, LinkType linkType) {
        Preconditions.checkNotNull(actorWrapper, "actorWrapper must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");

        LOGGER.atInfo().log("Processing admin panel command for userId: %s", actorWrapper.userId());
        if (!linkType.getSettings().isAdministrator(actorWrapper.userId())) {
            actorWrapper.reply(linkType.getLinkMessages().getMessage("not-enough-permission"));
            LOGGER.atFine().log("UserId: %s is not an administrator", actorWrapper.userId());
            return;
        }

        Keyboard adminPanelKeyboard = linkType.getSettings().getKeyboards().createKeyboard("admin-panel");
        try {
            actorWrapper.send(linkType.newMessageBuilder(linkType.getLinkMessages().getMessage("admin-panel"))
                    .keyboard(adminPanelKeyboard)
                    .build());
            LOGGER.atInfo().log("Admin panel displayed for userId: %s", actorWrapper.userId());
            SecurityAuditLogger.logSuccess("AdminPanelCommand", null, "Admin panel displayed for userId: " + actorWrapper.userId());
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("AdminPanelCommand", null, "Failed to display admin panel for userId: " + actorWrapper.userId() + ", error: " + ex.getMessage());
            throw ex;
        }
    }
    // #endregion
}