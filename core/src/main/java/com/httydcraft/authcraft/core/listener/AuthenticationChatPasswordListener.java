package com.httydcraft.authcraft.core.listener;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.event.PlayerChatPasswordEvent;
import com.httydcraft.authcraft.core.server.commands.impl.LoginCommandImplementation;
import com.httydcraft.authcraft.core.server.commands.impl.RegisterCommandImplementation;
import com.httydcraft.authcraft.core.server.commands.parameters.RegisterPassword;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import io.github.revxrsal.eventbus.SubscribeEvent;

// #region Class Documentation
/**
 * Listener for handling chat-based password authentication events.
 * Processes registration and login attempts based on chat input.
 */
public class AuthenticationChatPasswordListener {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final AuthPlugin plugin;
    private final PluginConfig config;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AuthenticationChatPasswordListener}.
     *
     * @param plugin The AuthPlugin instance. Must not be null.
     */
    public AuthenticationChatPasswordListener(AuthPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin must not be null");
        this.config = plugin.getConfig();
        LOGGER.atFine().log("Initialized AuthenticationChatPasswordListener");
    }
    // #endregion

    // #region Event Handlers
    /**
     * Handles the {@link PlayerChatPasswordEvent} to process password input.
     * Performs registration or login based on account state and password confirmation.
     *
     * @param event The chat password event. Must not be null.
     */
    @SubscribeEvent
    public void onPlayerChatPassword(PlayerChatPasswordEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        String[] messageParts = event.getPassword().split("\\s+");
        Preconditions.checkNotNull(messageParts, "messageParts must not be null");
        if (messageParts.length == 0) {
            LOGGER.atWarning().log("Empty password input from player: %s", event.getPlayer().getNickname());
            return;
        }

        String password = messageParts[0];
        ServerPlayer player = event.getPlayer();
        Account account = plugin.getAuthenticatingAccountBucket().getAuthenticatingAccountNullable(player);
        Preconditions.checkNotNull(account, "account must not be null for player: %s", player.getNickname());

        if (!account.isRegistered()) {
            if (passwordConfirmationFailed(messageParts, password, player)) {
                LOGGER.atFine().log("Password confirmation failed for player: %s", player.getNickname());
                return;
            }
            RegisterCommandImplementation impl = new RegisterCommandImplementation(plugin);
            impl.performRegister(player, account, new RegisterPassword(password));
            LOGGER.atInfo().log("Performed registration for player: %s", player.getNickname());
        } else {
            LoginCommandImplementation impl = new LoginCommandImplementation(plugin);
            impl.performLogin(player, account, password);
            LOGGER.atInfo().log("Performed login attempt for player: %s", player.getNickname());
        }
    }
    // #endregion

    // #region Helper Methods
    /**
     * Checks if password confirmation failed for registration.
     *
     * @param messageParts The split message parts. Must not be null.
     * @param password The primary password. Must not be null.
     * @param player The player attempting registration. Must not be null.
     * @return {@code true} if confirmation failed, {@code false} otherwise.
     */
    private boolean passwordConfirmationFailed(String[] messageParts, String password, ServerPlayer player) {
        Preconditions.checkNotNull(messageParts, "messageParts must not be null");
        Preconditions.checkNotNull(password, "password must not be null");
        Preconditions.checkNotNull(player, "player must not be null");

        if (!config.isPasswordConfirmationEnabled()) {
            LOGGER.atFine().log("Password confirmation disabled for player: %s", player.getNickname());
            return false;
        }

        if (messageParts.length < 2) {
            player.sendMessage(config.getServerMessages().getMessage("confirm-password"));
            LOGGER.atFine().log("Insufficient parts for confirmation for player: %s", player.getNickname());
            return true;
        }

        String confirmationPassword = messageParts[1];
        if (!confirmationPassword.equals(password)) {
            player.sendMessage(config.getServerMessages().getMessage("confirm-failed"));
            LOGGER.atFine().log("Confirmation password mismatch for player: %s", player.getNickname());
            return true;
        }

        LOGGER.atFine().log("Password confirmation successful for player: %s", player.getNickname());
        return false;
    }
    // #endregion
}