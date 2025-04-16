package com.httydcraft.authcraft.core.command;

import com.httydcraft.lamp.commands.vk.core.VkHandler;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.core.commands.MessengerCommandRegistry;
import com.httydcraft.authcraft.core.commands.MessengerLinkCommandTemplate;
import com.httydcraft.authcraft.core.commands.VKLinkCommand;
import com.httydcraft.authcraft.core.command.exception.VKExceptionHandler;
import com.httydcraft.authcraft.core.hooks.VkPluginHook;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.core.link.vk.VKCommandActorWrapper;
import com.httydcraft.authcraft.core.link.vk.VKLinkType;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import revxrsal.commands.CommandHandler;

// #region Class Documentation
/**
 * Registry for VK commands, extending {@link MessengerCommandRegistry}.
 * Initializes the VK bot, registers commands, and configures API settings.
 */
public class VKCommandRegistry extends MessengerCommandRegistry {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final VkPluginHook VK_HOOK = AuthPlugin.instance().getHook(VkPluginHook.class);
    private static final CommandHandler COMMAND_HANDLER = new VkHandler(VK_HOOK.getClient(), VK_HOOK.getActor()).disableStackTraceSanitizing();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VKCommandRegistry} and initializes the VK bot.
     */
    public VKCommandRegistry() {
        super(COMMAND_HANDLER, VKLinkType.getInstance());
        LOGGER.atInfo().log("Initializing VKCommandRegistry");

        COMMAND_HANDLER.registerContextResolver(LinkCommandActorWrapper.class, context -> {
            Preconditions.checkNotNull(context, "context must not be null");
            return new VKCommandActorWrapper(context.actor());
        });
        COMMAND_HANDLER.setExceptionHandler(new VKExceptionHandler(VKLinkType.getInstance()));
        registerCommands();
        configureVkApi();
    }
    // #endregion

    // #region API Configuration
    /**
     * Configures VK API settings for the bot, enabling bot capabilities and long poll settings.
     */
    private void configureVkApi() {
        LOGGER.atInfo().log("Configuring VK API settings");
        try {
            VK_HOOK.getClient().groups()
                    .setSettings(VK_HOOK.getActor(), VK_HOOK.getActor().getGroupId())
                    .botsCapabilities(true)
                    .messages(true)
                    .execute();
            VK_HOOK.getClient()
                    .groups()
                    .setLongPollSettings(VK_HOOK.getActor(), VK_HOOK.getActor().getGroupId())
                    .enabled(true)
                    .messageEvent(true)
                    .messageNew(true)
                    .apiVersion("5.131")
                    .execute();
            LOGGER.atInfo().log("VK API settings configured successfully");
        } catch (ApiException | ClientException e) {
            LOGGER.atSevere().withCause(e).log("Failed to configure VK API settings");
            System.err.println("Give all permissions to the VK API token for automatic settings apply.");
        }
    }
    // #endregion

    // #region Command Creation
    /**
     * Creates a VK-specific link command.
     *
     * @return A new instance of {@link VKLinkCommand}.
     */
    @Override
    protected MessengerLinkCommandTemplate createLinkCommand() {
        LOGGER.atFine().log("Creating VKLinkCommand");
        return new VKLinkCommand(VKLinkType.getInstance().getLinkMessages());
    }
    // #endregion
}