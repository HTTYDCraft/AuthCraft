package com.httydcraft.authcraft.core.server.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.core.link.google.GoogleLinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.core.server.commands.annotations.AuthenticationStepCommand;
import com.httydcraft.authcraft.core.server.commands.annotations.GoogleUse;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.core.step.impl.link.GoogleCodeAuthenticationStep;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;

// #region Class Documentation
/**
 * Command for handling Google authentication codes.
 * Verifies codes for Google two-factor authentication.
 */
@Command({"googlecode", "gcode"})
public class GoogleCodeCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final Messages<ServerComponent> GOOGLE_MESSAGES =
            AuthPlugin.instance().getConfig().getServerMessages().getSubMessages("google");
    @Dependency
    private AuthPlugin plugin;
    @Dependency
    private PluginConfig config;
    @Dependency
    private AccountDatabase accountStorage;
    // #endregion

    // #region Default Command
    /**
     * Handles the default case when no arguments are provided.
     *
     * @param player The player issuing the command. Must not be null.
     */
    @DefaultFor({"googlecode", "gcode"})
    public void defaultCommand(ServerPlayer player) {
        Preconditions.checkNotNull(player, "player must not be null");
        player.sendMessage(GOOGLE_MESSAGES.getMessage("code-not-enough-arguments"));
        LOGGER.atFine().log("Sent not-enough-arguments message to player: %s", player.getNickname());
    }
    // #endregion

    // #region Google Code Command
    /**
     * Verifies a Google authentication code.
     *
     * @param player The player issuing the command. Must not be null.
     * @param account The account to verify. Must not be null.
     * @param code The authentication code. Must not be null.
     */
    @GoogleUse
    @AuthenticationStepCommand(stepName = GoogleCodeAuthenticationStep.STEP_NAME)
    @Command({"googlecode", "gcode", "google code"})
    public void googleCode(ServerPlayer player, Account account, Integer code) {
        Preconditions.checkNotNull(player, "player must not be null");
        Preconditions.checkNotNull(account, "account must not be null");
        Preconditions.checkNotNull(code, "code must not be null");
        LOGGER.atFine().log("Processing Google code for player: %s, code: %d", player.getNickname(), code);

        String playerId = config.getActiveIdentifierType().getId(player);
        LinkUser linkUser = account.findFirstLinkUser(GoogleLinkType.LINK_USER_FILTER).orElse(null);
        if (linkUser == null || linkUser.isIdentifierDefaultOrNull()) {
            player.sendMessage(GOOGLE_MESSAGES.getMessage("code-not-exists"));
            LOGGER.atFine().log("No Google link user found for account: %s", account.getPlayerId());
            return;
        }

        if (!plugin.getLinkEntryBucket().find(playerId, GoogleLinkType.getInstance()).isPresent()) {
            player.sendMessage(GOOGLE_MESSAGES.getMessage("code-not-need-enter"));
            LOGGER.atFine().log("No code entry needed for account: %s", account.getPlayerId());
            return;
        }

        if (plugin.getGoogleAuthenticator().authorize(linkUser.getLinkUserInfo().getIdentificator().asString(), code)) {
            player.sendMessage(GOOGLE_MESSAGES.getMessage("code-entered"));
            account.getCurrentAuthenticationStep().getAuthenticationStepContext().setCanPassToNextStep(true);
            account.nextAuthenticationStep(plugin.getAuthenticationContextFactoryBucket().createContext(account));
            LOGGER.atInfo().log("Google code verified for account: %s", account.getPlayerId());
            return;
        }

        player.sendMessage(GOOGLE_MESSAGES.getMessage("code-wrong-code"));
        LOGGER.atFine().log("Invalid Google code for account: %s", account.getPlayerId());
    }
    // #endregion
}