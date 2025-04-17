package com.httydcraft.authcraft.core.server.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.message.MessageContext;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.core.link.google.GoogleLinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.core.link.user.LinkUserTemplate;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.LinkUserInfo;
import com.httydcraft.authcraft.core.server.commands.annotations.GoogleUse;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;

// #region Class Documentation
/**
 * Command for linking a Google account for authentication.
 * Generates or regenerates a Google authenticator key.
 */
@Command("google")
public class GoogleCommand {
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

    // #region Command Logic
    /**
     * Links or regenerates a Google authenticator key for a player.
     *
     * @param player The player issuing the command. Must not be null.
     */
    @GoogleUse
    @DefaultFor("google")
    @CommandCooldown(CommandCooldown.DEFAULT_VALUE)
    public void linkGoogle(ServerPlayer player) {
        Preconditions.checkNotNull(player, "player must not be null");
        LOGGER.atFine().log("Executing linkGoogle for player: %s", player.getNickname());

        String id = config.getActiveIdentifierType().getId(player);
        accountStorage.getAccount(id).thenAccept(account -> {
            if (account == null || !account.isRegistered()) {
                player.sendMessage(config.getServerMessages().getMessage("account-not-found"));
                LOGGER.atWarning().log("Auth fail: player %s [%s], reason: account not found", player.getNickname(), player.getPlayerIp());
                return;
            }

            String key = plugin.getGoogleAuthenticator().createCredentials().getKey();
            LinkUser linkUser = account.findFirstLinkUserOrCreate(GoogleLinkType.LINK_USER_FILTER,
                    createGoogleLinkUser(key, account));
            if (linkUser.isIdentifierDefaultOrNull()) {
                player.sendMessage(GOOGLE_MESSAGES.getMessage("generated",
                        MessageContext.of("%google_key%", key)));
                LOGGER.atInfo().log("Generated Google key for account: %s", account.getPlayerId());
            } else {
                player.sendMessage(GOOGLE_MESSAGES.getMessage("regenerated",
                        MessageContext.of("%google_key%", key)));
                LOGGER.atInfo().log("Regenerated Google key for account: %s", account.getPlayerId());
            }

            linkUser.getLinkUserInfo().getIdentificator().setString(key);
            accountStorage.updateAccountLinks(account);
        });
    }
    // #endregion

    // #region Helper Methods
    /**
     * Creates a new Google link user.
     *
     * @param key The Google authenticator key. Must not be null.
     * @param account The account to link. Must not be null.
     * @return The created {@link LinkUser}.
     */
    private LinkUser createGoogleLinkUser(String key, Account account) {
        Preconditions.checkNotNull(key, "key must not be null");
        Preconditions.checkNotNull(account, "account must not be null");
        LinkUser linkUser = LinkUserTemplate.of(GoogleLinkType.getInstance(), account,
                LinkUserInfo.of(LinkUserIdentificator.of(key)));
        LOGGER.atFine().log("Created Google link user for account: %s", account.getPlayerId());
        return linkUser;
    }
    // #endregion
}