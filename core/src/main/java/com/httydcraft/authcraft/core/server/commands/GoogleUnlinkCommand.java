package com.httydcraft.authcraft.core.server.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.event.AccountUnlinkEvent;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.core.link.google.GoogleLinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.core.server.commands.annotations.GoogleUse;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.commands.MessageableCommandActor;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import io.github.revxrsal.eventbus.EventBus;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;

// #region Class Documentation
/**
 * Command for unlinking a Google account from a player.
 * Removes the Google authenticator link and updates the account.
 */
@Command({"googleunlink", "google unlink", "gunlink"})
public class GoogleUnlinkCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final Messages<ServerComponent> GOOGLE_MESSAGES =
            AuthPlugin.instance().getConfig().getServerMessages().getSubMessages("google");
    @Dependency
    private PluginConfig config;
    @Dependency
    private AccountDatabase accountStorage;
    @Dependency
    private EventBus eventBus;
    // #endregion

    // #region Command Logic
    /**
     * Unlinks a Google account for a player.
     *
     * @param actor The command issuer. Must not be null.
     * @param player The player to unlink. Must not be null.
     */
    @GoogleUse
    @DefaultFor({"googleunlink", "google unlink", "gunlink"})
    @CommandCooldown(CommandCooldown.DEFAULT_VALUE)
    public void unlink(MessageableCommandActor actor, ServerPlayer player) {
        Preconditions.checkNotNull(actor, "actor must not be null");
        Preconditions.checkNotNull(player, "player must not be null");
        LOGGER.atFine().log("Executing unlink for player: %s", player.getNickname());

        String id = config.getActiveIdentifierType().getId(player);
        accountStorage.getAccount(id).thenAccept(account -> {
            if (account == null || !account.isRegistered()) {
                player.sendMessage(config.getServerMessages().getMessage("account-not-found"));
                LOGGER.atFine().log("Account not found for player: %s", player.getNickname());
                return;
            }

            LinkUser linkUser = account.findFirstLinkUser(GoogleLinkType.LINK_USER_FILTER).orElse(null);
            if (linkUser == null || linkUser.isIdentifierDefaultOrNull()) {
                player.sendMessage(GOOGLE_MESSAGES.getMessage("unlink-not-exists"));
                LOGGER.atFine().log("No Google link user found for account: %s", account.getPlayerId());
                return;
            }

            eventBus.publish(AccountUnlinkEvent.class, account, false, GoogleLinkType.getInstance(), linkUser,
                    linkUser.getLinkUserInfo().getIdentificator(), actor).thenAccept(result -> {
                if (result.getEvent().isCancelled()) {
                    LOGGER.atFine().log("Unlink cancelled for account: %s", account.getPlayerId());
                    return;
                }

                player.sendMessage(GOOGLE_MESSAGES.getMessage("unlinked"));
                linkUser.getLinkUserInfo().setIdentificator(GoogleLinkType.getInstance().getDefaultIdentificator());
                accountStorage.updateAccountLinks(account);
                LOGGER.atInfo().log("Unlinked Google for account: %s", account.getPlayerId());
            });
        });
    }
    // #endregion
}