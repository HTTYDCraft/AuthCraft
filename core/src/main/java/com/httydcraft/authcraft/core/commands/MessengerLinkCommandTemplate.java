package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.commands.MessageableCommandActor;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.message.MessageContext;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.confirmation.LinkConfirmationUser;
import com.httydcraft.authcraft.api.server.command.ServerCommandActor;
import com.httydcraft.authcraft.api.type.LinkConfirmationType;
import revxrsal.commands.orphan.OrphanCommand;

import java.util.function.Predicate;
import java.util.function.Supplier;

// #region Class Documentation
/**
 * Base template for messenger link commands, providing common functionality for account linking.
 * Handles validation, code generation, and confirmation sending.
 */
public class MessengerLinkCommandTemplate implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "link-game";

    private final AuthPlugin plugin = AuthPlugin.instance();
    private final PluginConfig config = plugin.getConfig();
    private final Messages<?> messages;
    private final LinkType linkType;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code MessengerLinkCommandTemplate} with the specified messages and link type.
     *
     * @param messages The messages configuration for this command. Must not be null.
     * @param linkType The link type for this command. Must not be null.
     */
    public MessengerLinkCommandTemplate(Messages<?> messages, LinkType linkType) {
        this.messages = Preconditions.checkNotNull(messages, "messages must not be null");
        this.linkType = Preconditions.checkNotNull(linkType, "linkType must not be null");
        LOGGER.atInfo().log("Initialized MessengerLinkCommandTemplate for linkType: %s", linkType);
    }
    // #endregion

    // #region Validation
    /**
     * Validates if an account is eligible for linking.
     *
     * @param account      The account to validate. May be null.
     * @param commandActor The actor executing the command. Must not be null.
     * @param linkFilter   The filter to check linked users. Must not be null.
     * @return {@code true} if the account is invalid or already linked, {@code false} otherwise.
     */
    public boolean isInvalidAccount(Account account, MessageableCommandActor commandActor, Predicate<LinkUser> linkFilter) {
        Preconditions.checkNotNull(commandActor, "commandActor must not be null");
        Preconditions.checkNotNull(linkFilter, "linkFilter must not be null");

        if (account == null || !account.isRegistered()) {
            commandActor.replyWithMessage(messages.getMessage("account-not-found"));
            LOGGER.atFine().log("Account not found or not registered");
            return true;
        }
        LinkUser linkUser = account.findFirstLinkUserOrNew(linkFilter, linkType);
        if (!linkUser.isIdentifierDefaultOrNull()) {
            commandActor.replyWithMessage(messages.getMessage("already-linked"));
            LOGGER.atFine().log("Account already linked for linkType: %s", linkType);
            return true;
        }
        return false;
    }
    // #endregion

    // #region Confirmation Sending
    /**
     * Sends a link confirmation to the actor.
     *
     * @param commandActor     The actor to send the confirmation to. Must not be null.
     * @param confirmationUser The confirmation user data. Must not be null.
     */
    public void sendLinkConfirmation(MessageableCommandActor commandActor, LinkConfirmationUser confirmationUser) {
        Preconditions.checkNotNull(commandActor, "commandActor must not be null");
        Preconditions.checkNotNull(confirmationUser, "confirmationUser must not be null");

        LOGGER.atInfo().log("Sending link confirmation for linkType: %s", linkType);
        plugin.getLinkConfirmationBucket().modifiable().add(confirmationUser);
        commandActor.replyWithMessage(messages.getMessage("confirmation-sent", MessageContext.of("%code%", confirmationUser.getConfirmationCode())));
    }
    // #endregion

    // #region Code Generation
    /**
     * Generates a unique confirmation code.
     *
     * @param codeGenerator The supplier for generating codes. Must not be null.
     * @return A unique confirmation code.
     */
    public String generateCode(Supplier<String> codeGenerator) {
        Preconditions.checkNotNull(codeGenerator, "codeGenerator must not be null");

        String code;
        do {
            code = codeGenerator.get();
        } while (codeExists(code));
        LOGGER.atFine().log("Generated unique code for linkType: %s", linkType);
        return code;
    }
    // #endregion

    // #region Confirmation Type Resolution
    /**
     * Resolves the link confirmation type based on the actor.
     *
     * @param actor The actor executing the command. Must not be null.
     * @return The resolved {@link LinkConfirmationType}.
     * @throws IllegalArgumentException if the actor type is unsupported.
     */
    public LinkConfirmationType getLinkConfirmationType(MessageableCommandActor actor) {
        Preconditions.checkNotNull(actor, "actor must not be null");

        if (actor instanceof ServerCommandActor) {
            LOGGER.atFine().log("Resolved confirmation type: FROM_LINK");
            return LinkConfirmationType.FROM_LINK;
        }
        if (actor instanceof LinkCommandActorWrapper) {
            LOGGER.atFine().log("Resolved confirmation type: FROM_GAME");
            return LinkConfirmationType.FROM_GAME;
        }
        LOGGER.atSevere().log("Unsupported actor type: %s", actor.getClass().getName());
        throw new IllegalArgumentException("Cannot resolve confirmation type for actor: " + actor);
    }
    // #endregion

    // #region Helper Methods
    /**
     * Checks if a confirmation code already exists.
     *
     * @param code The code to check. Must not be null.
     * @return {@code true} if the code exists, {@code false} otherwise.
     */
    private boolean codeExists(String code) {
        Preconditions.checkNotNull(code, "code must not be null");
        return plugin.getLinkConfirmationBucket().findFirst(user -> user.getConfirmationCode().equals(code)).isPresent();
    }
    // #endregion
}