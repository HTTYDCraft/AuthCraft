package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.multimessenger.core.keyboard.Keyboard;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.orphan.OrphanCommand;

// #region Class Documentation
/**
 * Command for displaying the account control menu.
 * Shows a keyboard-based interface for managing an account.
 */
@CommandKey(AccountCommand.CONFIGURATION_KEY)
public class AccountCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "account-control";
    // #endregion

    // #region Command Execution
    /**
     * Executes the account control command, displaying the account management menu.
     *
     * @param actorWrapper The actor executing the command. Must not be null.
     * @param linkType     The link type associated with the command. Must not be null.
     * @param account      The account to manage. Must not be null.
     */
    @DefaultFor("~")
    @CommandCooldown(CommandCooldown.DEFAULT_VALUE)
    public void accountMenu(LinkCommandActorWrapper actorWrapper, LinkType linkType, Account account) {
        Preconditions.checkNotNull(actorWrapper, "actorWrapper must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(account, "account must not be null");

        LOGGER.atInfo().log("Processing account menu command for account: %s", account.getName());
        Keyboard accountKeyboard = linkType.getSettings().getKeyboards().createKeyboard("account", "%account_name%", account.getName());
        actorWrapper.send(linkType.newMessageBuilder(linkType.getLinkMessages().getMessage("account-control", linkType.newMessageContext(account)))
                .keyboard(accountKeyboard)
                .build());
        LOGGER.atInfo().log("Displayed account menu for account: %s", account.getName());
    }
    // #endregion
}