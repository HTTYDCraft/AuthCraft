package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.commands.MessageableCommandActor;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.message.Messages;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.hooks.VkPluginHook;
import com.httydcraft.authcraft.core.link.user.confirmation.BaseLinkConfirmationUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.core.link.vk.VKLinkType;
import com.httydcraft.authcraft.api.model.PlayerIdSupplier;
import com.httydcraft.authcraft.core.server.commands.annotations.VkUse;
import com.httydcraft.authcraft.api.type.LinkConfirmationType;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.orphan.OrphanCommand;

// #region Class Documentation
/**
 * Command for linking a VK account to a player's game account.
 * Generates a confirmation code and initiates the linking process.
 */
@CommandKey(MessengerLinkCommandTemplate.CONFIGURATION_KEY)
public class VKLinkCommand extends MessengerLinkCommandTemplate implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String LINK_NAME = "VK";

    @Dependency
    private AuthPlugin plugin;
    @Dependency
    private PluginConfig config;
    @Dependency
    private AccountDatabase accountStorage;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VKLinkCommand} with the specified messages.
     *
     * @param messages The messages configuration for this command. Must not be null.
     */
    public VKLinkCommand(Messages<?> messages) {
        super(messages, VKLinkType.getInstance());
        LOGGER.atInfo().log("Initialized VKLinkCommand");
    }
    // #endregion

    // #region Command Execution
    /**
     * Executes the VK link command, initiating the account linking process.
     *
     * @param commandActor        The actor executing the command. Must not be null.
     * @param idSupplier         The supplier of the player's ID. Must not be null.
     * @param linkUserIdentificator The optional user identificator for linking.
     */
    @VkUse
    @DefaultFor("vk.link")
    @CommandCooldown(CommandCooldown.DEFAULT_VALUE)
    public void vkLink(
        MessageableCommandActor commandActor,
        PlayerIdSupplier idSupplier,
        @Optional @Default("") LinkUserIdentificator linkUserIdentificator
    ) {
        Preconditions.checkNotNull(commandActor, "commandActor must not be null");
        Preconditions.checkNotNull(idSupplier, "idSupplier must not be null");

        String accountId = idSupplier.getPlayerId();
        LOGGER.atInfo().log("Processing VK link command for accountId: %s", accountId);

        accountStorage.getAccountFromName(accountId).thenAccept(account -> {
            if (isInvalidAccount(account, commandActor, VKLinkType.LINK_USER_FILTER)) {
                LOGGER.atFine().log("Invalid account for accountId: %s", accountId);
                return;
            }
            String code = generateCode(() -> config.getVKSettings().getConfirmationSettings().generateCode());
            LOGGER.atFine().log("Generated confirmation code for accountId: %s", accountId);

            LinkConfirmationType linkConfirmationType = getLinkConfirmationType(commandActor);
            long timeoutTimestamp = System.currentTimeMillis() + VKLinkType.getInstance().getSettings().getConfirmationSettings().getRemoveDelay().getMillis();
            sendLinkConfirmation(commandActor, linkConfirmationType.bindLinkConfirmationUser(
                    new BaseLinkConfirmationUser(linkConfirmationType, timeoutTimestamp, VKLinkType.getInstance(), account, code), linkUserIdentificator));
            LOGGER.atInfo().log("Sent link confirmation for accountId: %s", accountId);
        });
    }
    // #endregion

    // #region Helper Methods
    /**
     * Fetches a VK user based on the provided identificator.
     *
     * @param vkIdentificator The VK identificator to query. Must not be null.
     * @return An {@code Optional} containing the user response, or empty if the fetch fails.
     */
    private java.util.Optional<GetResponse> fetchUserFromIdentificator(String vkIdentificator) {
        Preconditions.checkNotNull(vkIdentificator, "vkIdentificator must not be null");
        try {
            VkPluginHook vkHook = AuthPlugin.instance().getHook(VkPluginHook.class);
            return vkHook.getClient().users().get(vkHook.getActor()).userIds(vkIdentificator).execute().stream().findFirst();
        } catch(ApiException | ClientException e) {
            e.printStackTrace();
            LOGGER.atWarning().withCause(e).log("Failed to fetch VK user for identificator: %s", vkIdentificator);
            return java.util.Optional.empty();
        }
    }

    // #endregion
}