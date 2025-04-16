package com.httydcraft.authcraft.core.command;

import com.httydcraft.lamp.commands.vk.VkCommandHandler;
import com.httydcraft.lamp.commands.vk.core.BaseVkActor;
import com.httydcraft.lamp.commands.vk.core.VkHandler;
import com.httydcraft.lamp.commands.vk.message.ButtonDispatchSource;
import com.httydcraft.lamp.commands.vk.message.DispatchSource;
import com.httydcraft.lamp.commands.vk.message.MessageDispatchSource;
import com.httydcraft.lamp.commands.vk.objects.CallbackButton;
import com.httydcraft.multimessenger.core.identificator.Identificator;
import com.httydcraft.multimessenger.core.message.Message;
import com.httydcraft.multimessenger.core.message.Message.MessageBuilder;
import com.httydcraft.multimessenger.vk.message.keyboard.VkKeyboard;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.google.gson.Gson;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.core.commands.custom.BaseCustomCommandExecutionContext;
import com.httydcraft.authcraft.core.commands.parser.SimpleStringTokenizer;
import com.httydcraft.authcraft.api.config.link.command.LinkCustomCommandSettings;
import com.httydcraft.authcraft.core.hooks.VkPluginHook;
import com.httydcraft.authcraft.api.link.command.context.CustomCommandExecutionContext;
import com.httydcraft.authcraft.core.link.vk.VKCommandActorWrapper;
import com.httydcraft.authcraft.core.link.vk.VKLinkType;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Keyboard;
import com.httydcraft.vk.api.core.api.parsers.objects.CallbackButtonEvent;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// #region Class Documentation
/**
 * Abstract listener for handling VK message and button click events, dispatching commands and custom command executions.
 * Processes incoming messages and button interactions asynchronously using a cached thread pool.
 */
public abstract class DispatchCommandListener {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private static final VKLinkType LINK_TYPE = VKLinkType.getInstance();
    private static final VkPluginHook VK_HOOK = AuthPlugin.instance().getHook(VkPluginHook.class);
    private static final Gson GSON = new Gson();
    private static final double CONVERSATION_PEER_ID_OFFSET = 2e9;
    // #endregion

    // #region Message Handling
    /**
     * Processes a VK message and dispatches associated commands or custom command executions.
     *
     * @param vkMessage The VK message to process. Must not be null.
     * @param peerId    The peer ID associated with the message. Must be positive.
     */
    protected void onMessage(com.vk.api.sdk.objects.messages.Message vkMessage, int peerId) {
        Preconditions.checkNotNull(vkMessage, "vkMessage must not be null");
        Preconditions.checkArgument(peerId > 0, "peerId must be positive");

        LOGGER.atInfo().log("Processing VK message for peerId: %d", peerId);
        EXECUTOR_SERVICE.execute(() -> VkHandler.getInstances().forEach(commandHandler -> {
            handleCommandDispatch(commandHandler, new MessageDispatchSource(vkMessage));

            LINK_TYPE.getSettings().getCustomCommands()
                    .execute(new BaseCustomCommandExecutionContext(vkMessage.getText()))
                    .forEach(customCommand -> {
                        Message message = createMessage(customCommand);
                        LOGGER.atFine().log("Sending custom command response to peerId: %d", peerId);
                        message.send(Identificator.of(peerId));
                    });
        }));
    }
    // #endregion

    // #region Button Handling
    /**
     * Processes a VK button click event and dispatches associated commands or custom command executions.
     *
     * @param buttonEvent The button click event to process. Must not be null.
     */
    protected void onButtonClick(CallbackButtonEvent buttonEvent) {
        Preconditions.checkNotNull(buttonEvent, "buttonEvent must not be null");

        LOGGER.atInfo().log("Processing button click for peerId: %d", buttonEvent.getPeerID());
        EXECUTOR_SERVICE.execute(() -> VkHandler.getInstances().forEach(commandHandler -> {
            responseToButtonClick(buttonEvent);

            CallbackButton callbackButton = GSON.fromJson(GSON.toJson(buttonEvent), CallbackButton.class);
            handleCommandDispatch(commandHandler, new ButtonDispatchSource(callbackButton));

            CustomCommandExecutionContext executionContext = new BaseCustomCommandExecutionContext(buttonEvent.getPayload());
            executionContext.setButtonExecution(true);
            LINK_TYPE.getSettings()
                    .getCustomCommands()
                    .execute(executionContext)
                    .forEach(customCommand -> {
                        Message message = createMessage(customCommand);
                        LOGGER.atFine().log("Sending custom command response to peerId: %d", buttonEvent.getPeerID());
                        message.send(Identificator.of(buttonEvent.getPeerID()));
                    });
        }));
    }
    // #endregion

    // #region Helper Methods
    /**
     * Sends a response to a VK button click event.
     *
     * @param event The button click event to respond to. Must not be null.
     * @throws RuntimeException if the response fails due to API or client issues.
     */
    private void responseToButtonClick(CallbackButtonEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");

        try {
            LOGGER.atFine().log("Sending button click response for eventId: %s", event.getEventID());
            VK_HOOK.getClient()
                    .messages()
                    .sendMessageEventAnswer(VK_HOOK.getActor(), event.getEventID(), event.getUserID(), event.getPeerID())
                    .execute();
        } catch (ApiException | ClientException e) {
            LOGGER.atSevere().withCause(e).log("Failed to send button click response for eventId: %s", event.getEventID());
            throw new RuntimeException(e);
        }
    }

    /**
     * Dispatches a command using the provided handler and dispatch source.
     *
     * @param handler The command handler to dispatch the command. Must not be null.
     * @param source  The dispatch source containing command details. Must not be null.
     */
    private void handleCommandDispatch(VkCommandHandler handler, DispatchSource source) {
        Preconditions.checkNotNull(handler, "handler must not be null");
        Preconditions.checkNotNull(source, "source must not be null");

        if (LINK_TYPE.getSettings().shouldDisableConversationCommands() && isConversationPeerId(source.getPeerId())) {
            LOGGER.atFine().log("Skipping command dispatch for conversation peerId: %d", source.getPeerId());
            return;
        }

        CommandActor commandActor = new VKCommandActorWrapper(new BaseVkActor(source, handler));
        ArgumentStack argumentStack = ArgumentStack.copyExact(SimpleStringTokenizer.parse(source.getText()));
        if (argumentStack.isEmpty()) {
            LOGGER.atFine().log("Empty argument stack for peerId: %d", source.getPeerId());
            return;
        }

        LOGGER.atInfo().log("Dispatching command for peerId: %d", source.getPeerId());
        handler.dispatch(commandActor, argumentStack);
    }

    /**
     * Creates a message response for a custom command.
     *
     * @param customCommand The custom command settings. Must not be null.
     * @return The constructed message.
     */
    private Message createMessage(LinkCustomCommandSettings customCommand) {
        Preconditions.checkNotNull(customCommand, "customCommand must not be null");

        MessageBuilder builder = LINK_TYPE.newMessageBuilder(customCommand.getAnswer());
        if (customCommand.getSectionHolder().contains("keyboard")) {
            Keyboard keyboard = GSON.fromJson(customCommand.getSectionHolder().getString("keyboard"), Keyboard.class);
            builder.keyboard(new VkKeyboard(keyboard));
            LOGGER.atFine().log("Added keyboard to message for custom command");
        }
        return builder.build();
    }

    /**
     * Checks if the peer ID represents a conversation.
     *
     * @param peerId The peer ID to check.
     * @return {@code true} if the peer ID is for a conversation, {@code false} otherwise.
     */
    private boolean isConversationPeerId(int peerId) {
        return peerId > CONVERSATION_PEER_ID_OFFSET;
    }
    // #endregion
}