package com.httydcraft.authcraft.core.command;

import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.lamp.telegram.TelegramActor;
import com.httydcraft.lamp.telegram.core.TelegramHandler;
import com.httydcraft.lamp.telegram.dispatch.CallbackQueryDispatchSource;
import com.httydcraft.lamp.telegram.dispatch.DispatchSource;
import com.httydcraft.lamp.telegram.dispatch.MessageDispatchSource;
import com.httydcraft.multimessenger.core.identificator.Identificator;
import com.httydcraft.multimessenger.core.message.Message;
import com.httydcraft.multimessenger.core.message.Message.MessageBuilder;
import com.httydcraft.multimessenger.telegram.message.keyboard.TelegramKeyboard;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.google.gson.Gson;
import com.httydcraft.authcraft.core.commands.custom.BaseCustomCommandExecutionContext;
import com.httydcraft.authcraft.core.commands.parser.SimpleStringTokenizer;
import com.httydcraft.authcraft.api.config.link.command.LinkCustomCommandSettings;
import com.httydcraft.authcraft.api.link.command.context.CustomCommandExecutionContext;
import com.httydcraft.authcraft.core.link.telegram.TelegramCommandActorWrapper;
import com.httydcraft.authcraft.core.link.telegram.TelegramLinkType;
import com.httydcraft.authcraft.core.command.listener.TelegramUpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import revxrsal.commands.command.ArgumentStack;
import java.util.List;

// #region Class Documentation
/**
 * Listener for processing Telegram updates, extending {@link TelegramUpdatesListener}.
 * Handles message and callback query updates, dispatching commands and custom command executions.
 */
public class TelegramCommandUpdatesListener extends TelegramUpdatesListener {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final Gson GSON = new Gson();
    private static final LinkType LINK_TYPE = TelegramLinkType.getInstance();
    // #endregion

    // #region Update Processing
    /**
     * Processes a list of valid Telegram updates by delegating to individual update handling.
     *
     * @param updates The list of valid updates to process. Must not be null or empty.
     */
    @Override
    public void processValidUpdates(List<Update> updates) {
        try {
            Preconditions.checkNotNull(updates, "updates must not be null");
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("TelegramCommandUpdatesListener", null, "updates list is null");
            throw ex;
        }
        try {
            Preconditions.checkArgument(!updates.isEmpty(), "updates must not be empty");
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("TelegramCommandUpdatesListener", null, "updates list is empty");
            throw ex;
        }

        SecurityAuditLogger.logSuccess("TelegramCommandUpdatesListener", null, "Processing " + updates.size() + " valid updates");
        LOGGER.atInfo().log("Processing %d valid updates", updates.size());
        updates.forEach(this::processUpdate);
    }

    /**
     * Processes a single Telegram update, handling message or callback query events.
     *
     * @param update The update to process. Must not be null.
     */
    private void processUpdate(Update update) {
        try {
            Preconditions.checkNotNull(update, "update must not be null");
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("TelegramCommandUpdatesListener", null, "update is null");
            throw ex;
        }

        if (update.message() != null) {
            LOGGER.atFine().log("Processing message update");
            processMessageUpdate(update);
        }
        if (update.callbackQuery() != null) {
            LOGGER.atFine().log("Processing callback query update");
            processCallbackUpdate(update);
        }
    }
    // #endregion

    // #region Message Handling
    /**
     * Processes a message update and dispatches associated commands or custom command executions.
     *
     * @param update The message update to process. Must not be null, and must contain a message.
     */
    private void processMessageUpdate(Update update) {
        try {
            Preconditions.checkNotNull(update, "update must not be null");
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("TelegramCommandUpdatesListener", null, "update is null in processMessageUpdate");
            throw ex;
        }
        try {
            Preconditions.checkNotNull(update.message(), "update.message must not be null");
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("TelegramCommandUpdatesListener", null, "update.message is null in processMessageUpdate");
            throw ex;
        }

        com.pengrad.telegrambot.model.Message message = update.message();
        SecurityAuditLogger.logSuccess("TelegramCommandUpdatesListener", null, "Processing message from chatId: " + message.chat().id());
        LOGGER.atInfo().log("Processing message from chatId: %d", message.chat().id());
        TelegramHandler.getInstances().forEach(handler -> {
            handleCommandDispatch(handler, new MessageDispatchSource(message));
            LINK_TYPE.getSettings().getCustomCommands()
                    .execute(new BaseCustomCommandExecutionContext(message.text()))
                    .forEach(customCommand -> {
                        Message response = createMessageResponse(customCommand);
                        LOGGER.atFine().log("Sending custom command response to chatId: %d", message.chat().id());
                        try {
                            response.send(Identificator.of(message.chat().id()));
                            SecurityAuditLogger.logSuccess("TelegramCommandUpdatesListener", null, "Sent custom command response to chatId: " + message.chat().id());
                        } catch (Exception ex) {
                            SecurityAuditLogger.logFailure("TelegramCommandUpdatesListener", null, "Failed to send custom command response to chatId: " + message.chat().id() + ", error: " + ex.getMessage());
                            throw ex;
                        }
                    });
        });
    }
    // #endregion

    // #region Callback Handling
    /**
     * Processes a callback query update and dispatches associated commands or custom command executions.
     *
     * @param update The callback query update to process. Must not be null, and must contain a callback query.
     */
    private void processCallbackUpdate(Update update) {
        try {
            Preconditions.checkNotNull(update, "update must not be null");
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("TelegramCommandUpdatesListener", null, "update is null in processCallbackUpdate");
            throw ex;
        }
        try {
            Preconditions.checkNotNull(update.callbackQuery(), "update.callbackQuery must not be null");
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("TelegramCommandUpdatesListener", null, "update.callbackQuery is null in processCallbackUpdate");
            throw ex;
        }

        CallbackQuery callbackQuery = update.callbackQuery();
        SecurityAuditLogger.logSuccess("TelegramCommandUpdatesListener", null, "Processing callback query from chatId: " + (callbackQuery.message() != null ? callbackQuery.message().chat().id() : -1));
        LOGGER.atInfo().log("Processing callback query from chatId: %d", callbackQuery.message() != null ? callbackQuery.message().chat().id() : -1);
        TelegramHandler.getInstances().forEach(handler -> {
            handleCommandDispatch(handler, new CallbackQueryDispatchSource(callbackQuery));
            if (callbackQuery.message() == null) {
                LOGGER.atFine().log("Skipping callback query with no message");
                return;
            }
            CustomCommandExecutionContext executionContext = new BaseCustomCommandExecutionContext(callbackQuery.data());
            executionContext.setButtonExecution(true);
            LINK_TYPE.getSettings()
                    .getCustomCommands()
                    .execute(executionContext)
                    .forEach(customCommand -> {
                        Message response = createMessageResponse(customCommand);
                        LOGGER.atFine().log("Sending custom command response to chatId: %d", callbackQuery.message().chat().id());
                        try {
                            response.send(Identificator.of(callbackQuery.message().chat().id()));
                            SecurityAuditLogger.logSuccess("TelegramCommandUpdatesListener", null, "Sent custom command response to chatId: " + callbackQuery.message().chat().id());
                        } catch (Exception ex) {
                            SecurityAuditLogger.logFailure("TelegramCommandUpdatesListener", null, "Failed to send custom command response to chatId: " + callbackQuery.message().chat().id() + ", error: " + ex.getMessage());
                            throw ex;
                        }
                    });
        });
    }
    // #endregion

    // #region Helper Methods
    /**
     * Dispatches a command using the provided handler and dispatch source.
     *
     * @param handler        The command handler to dispatch the command. Must not be null.
     * @param dispatchSource The dispatch source containing command details. Must not be null.
     */
    private void handleCommandDispatch(TelegramHandler handler, DispatchSource dispatchSource) {
        Preconditions.checkNotNull(handler, "handler must not be null");
        Preconditions.checkNotNull(dispatchSource, "dispatchSource must not be null");

        TelegramActor actor = new TelegramCommandActorWrapper(TelegramActor.wrap(handler, dispatchSource));
        ArgumentStack argumentStack = ArgumentStack.copyExact(SimpleStringTokenizer.parse(dispatchSource.getExecutionText()));
        if (argumentStack.isEmpty()) {
            LOGGER.atFine().log("Empty argument stack for dispatchSource");
            return;
        }

        LOGGER.atInfo().log("Dispatching command for handler: %s", handler);
        handler.dispatch(actor, argumentStack);
    }

    /**
     * Creates a message response for a custom command.
     *
     * @param customCommand The custom command settings. Must not be null.
     * @return The constructed message.
     */
    private Message createMessageResponse(LinkCustomCommandSettings customCommand) {
        Preconditions.checkNotNull(customCommand, "customCommand must not be null");

        MessageBuilder builder = LINK_TYPE.newMessageBuilder(customCommand.getAnswer());
        if (customCommand.getSectionHolder().contains("keyboard")) {
            InlineKeyboardMarkup keyboard = GSON.fromJson(customCommand.getSectionHolder().getString("keyboard"), InlineKeyboardMarkup.class);
            builder.keyboard(new TelegramKeyboard(keyboard));
            LOGGER.atFine().log("Added keyboard to message for custom command");
        }
        return builder.build();
    }
    // #endregion
}