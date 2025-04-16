package com.httydcraft.authcraft.core.config.message.context.placeholder;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.config.message.MessageContext;

import java.util.ArrayList;
import java.util.List;

// #region Class Documentation
/**
 * Abstract context for handling message placeholders.
 * Manages a list of placeholder providers and applies them to raw strings.
 */
public abstract class MessagePlaceholderContext implements MessageContext {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final List<PlaceholderProvider> placeholderProviders = new ArrayList<>();
    // #endregion

    // #region Placeholder Management
    /**
     * Registers a new placeholder provider.
     *
     * @param supplier The placeholder provider to register. Must not be null.
     * @return This context for chaining.
     */
    public MessagePlaceholderContext registerPlaceholderProvider(PlaceholderProvider supplier) {
        Preconditions.checkNotNull(supplier, "supplier must not be null");
        placeholderProviders.add(supplier);
        LOGGER.atFine().log("Registered new placeholder provider");
        return this;
    }

    /**
     * Applies all registered placeholder providers to a raw string.
     *
     * @param rawString The input string. Must not be null.
     * @return The string with placeholders replaced.
     */
    @Override
    public String apply(String rawString) {
        Preconditions.checkNotNull(rawString, "rawString must not be null");
        String result = rawString;
        for (PlaceholderProvider placeholderProvider : placeholderProviders) {
            if (placeholderProvider.containsPlaceholder(result)) {
                result = placeholderProvider.replaceAll(result);
                LOGGER.atFine().log("Applied placeholder to string");
            }
        }
        return result;
    }
    // #endregion
}