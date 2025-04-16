package com.httydcraft.authcraft.core.config.message.vk;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.core.config.message.context.placeholder.PlaceholderProvider;
import com.httydcraft.authcraft.core.config.message.link.context.LinkPlaceholderContext;
import com.httydcraft.authcraft.core.hooks.VkPluginHook;
import com.httydcraft.authcraft.core.link.vk.VKLinkType;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.users.responses.GetResponse;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

// #region Class Documentation
/**
 * Context for VK-specific message placeholders.
 * Extends link placeholder context with VK-specific user information.
 */
public class VKMessagePlaceholderContext extends LinkPlaceholderContext {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final VkPluginHook VK_HOOK = AuthPlugin.instance().getHook(VkPluginHook.class);
    private static final VkApiClient CLIENT = VK_HOOK.getClient();
    private static final GroupActor ACTOR = VK_HOOK.getActor();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VKMessagePlaceholderContext}.
     *
     * @param account The account to provide placeholders for. Must not be null.
     */
    public VKMessagePlaceholderContext(Account account) {
        super(Preconditions.checkNotNull(account, "account must not be null"),
                VKLinkType.getInstance(), "vk");
        getLinkUser().ifPresent(linkUser -> {
            if (linkUser.isIdentifierDefaultOrNull() || !linkUser.getLinkUserInfo().getIdentificator().isNumber()) {
                LOGGER.atFine().log("Skipping VK placeholders: invalid identifier");
                return;
            }
            findUser(linkUser.getLinkUserInfo().getIdentificator().asNumber()).thenAccept(userOptional -> {
                if (!userOptional.isPresent()) {
                    LOGGER.atFine().log("No VK user found for ID: %d", linkUser.getLinkUserInfo().getIdentificator().asNumber());
                    return;
                }
                GetResponse user = userOptional.get();
                registerPlaceholderProvider(PlaceholderProvider.of(user.getScreenName(), "%vk_screen_name%"));
                registerPlaceholderProvider(PlaceholderProvider.of(user.getFirstName(), "%vk_first_name%"));
                registerPlaceholderProvider(PlaceholderProvider.of(user.getLastName(), "%vk_last_name%"));
                LOGGER.atFine().log("Registered VK placeholders for user: %s", user.getScreenName());
            });
        });
        LOGGER.atInfo().log("Initialized VKMessagePlaceholderContext for account: %s", account.getName());
    }
    // #endregion

    // #region User Lookup
    /**
     * Asynchronously finds a VK user by ID.
     *
     * @param userId The VK user ID.
     * @return A {@link CompletableFuture} with an {@link Optional} containing the user response.
     */
    private CompletableFuture<Optional<GetResponse>> findUser(long userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<GetResponse> userInformationResponses = CLIENT.users()
                        .get(ACTOR)
                        .userIds(String.valueOf(userId))
                        .execute();
                if (userInformationResponses.isEmpty()) {
                    LOGGER.atFine().log("No user found for VK ID: %d", userId);
                    return Optional.empty();
                }
                LOGGER.atFine().log("Found VK user for ID: %d", userId);
                return Optional.of(userInformationResponses.get(0));
            } catch (ClientException | ApiException e) {
                LOGGER.atSevere().withCause(e).log("Failed to fetch VK user for ID: %d", userId);
                return Optional.empty();
            }
        });
    }
    // #endregion
}