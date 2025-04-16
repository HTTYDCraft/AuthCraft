package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.commands.annotation.ConfigurationArgumentError;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.type.KickResultType;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.orphan.OrphanCommand;

// #region Class Documentation
/**
 * Command for kicking a player from the server.
 * Notifies the actor of the kick result.
 */
@CommandKey(KickCommand.CONFIGURATION_KEY)
public class KickCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "kick";
    // #endregion

    // #region Command Execution
    /**
     * Executes the kick command, kicking the specified account from the server.
     *
     * @param actorWrapper The actor executing the command. Must not be null.
     * @param linkType     The link type associated with the command. Must not be null.
     * @param account      The account to kick. Must not be null.
     */
    @ConfigurationArgumentError("kick-not-enough-arguments")
    @DefaultFor("~")
    public void onKick(LinkCommandActorWrapper actorWrapper, LinkType linkType, Account account) {
        Preconditions.checkNotNull(actorWrapper, "actorWrapper must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(account, "account must not be null");

        LOGGER.atInfo().log("Processing kick command for account: %s", account.getName());
        actorWrapper.reply(linkType.getLinkMessages().getMessage("kick-starting", linkType.newMessageContext(account)));
        KickResultType kickResult = account.kick(linkType.getServerMessages().getStringMessage("kicked"));
        actorWrapper.reply(linkType.getLinkMessages().getMessage(kickResult.getConfigurationPath(), linkType.newMessageContext(account)));
        LOGGER.atInfo().log("Kicked account: %s with result: %s", account.getName(), kickResult);
    }
    // #endregion
}