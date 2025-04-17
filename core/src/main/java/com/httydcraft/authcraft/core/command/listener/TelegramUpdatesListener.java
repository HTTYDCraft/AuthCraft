package com.httydcraft.authcraft.core.command.listener;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.google.common.collect.ImmutableList;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import java.util.List;

// #region Class Documentation
/**
 * Abstract base class for processing Telegram updates, implementing {@link UpdatesListener}.
 * Tracks the last processed update ID to avoid duplicate processing and delegates valid updates to subclasses.
 */
public abstract class TelegramUpdatesListener implements UpdatesListener {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private long lastUpdateId;
    // #endregion

    // #region Update Processing
    /**
     * Processes a list of Telegram updates, filtering duplicates based on update ID.
     * Delegates valid updates to {@link #processValidUpdates(List)}.
     *
     * @param updates The list of updates to process. Must not be null or empty.
     * @return The confirmation status for processed updates, typically {@link UpdatesListener#CONFIRMED_UPDATES_ALL}.
     * @throws IllegalArgumentException if {@code updates} is null or empty.
     */
    @Override
    public int process(List<Update> updates) {
        try {
            Preconditions.checkNotNull(updates, "updates must not be null");
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("TelegramUpdatesListener", null, "Updates list is null");
            throw ex;
        }
        try {
            Preconditions.checkArgument(!updates.isEmpty(), "updates must not be empty");
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("TelegramUpdatesListener", null, "Updates list is empty");
            throw ex;
        }

        updates.stream()
                .findFirst()
                .ifPresent(update -> {
                    try {
                        Preconditions.checkNotNull(update, "update must not be null");
                    } catch (Exception ex) {
                        SecurityAuditLogger.logFailure("TelegramUpdatesListener", null, "Update is null");
                        throw ex;
                    }
                    if (lastUpdateId == update.updateId()) {
                        SecurityAuditLogger.logFailure("TelegramUpdatesListener", null, "Duplicate update with ID: " + lastUpdateId);
                        LOGGER.atFine().log("Skipping duplicate update with ID: %d", lastUpdateId);
                        return;
                    }
                    lastUpdateId = update.updateId();
                    SecurityAuditLogger.logSuccess("TelegramUpdatesListener", null, "Processing update with ID: " + lastUpdateId);
                    LOGGER.atInfo().log("Processing update with ID: %d", lastUpdateId);
                    processValidUpdates(ImmutableList.copyOf(updates));
                });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
    // #endregion

    // #region Abstract Methods
    /**
     * Processes a list of valid Telegram updates.
     * Subclasses must implement this method to define specific update handling logic.
     *
     * @param updates The list of valid updates to process. Guaranteed to be non-null and non-empty.
     */
    protected abstract void processValidUpdates(List<Update> updates);
    // #endregion
}