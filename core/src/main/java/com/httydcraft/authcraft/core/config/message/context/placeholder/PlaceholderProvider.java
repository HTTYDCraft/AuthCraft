package com.httydcraft.authcraft.core.config.message.context.placeholder;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;

import java.util.function.Supplier;

// #region Interface Documentation
/**
 * Interface for providing placeholder replacement functionality.
 * Defines methods to check for placeholders and replace them in text.
 */
public interface PlaceholderProvider {
    GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #region Factory Methods
    /**
     * Creates a provider for a single placeholder with a static value.
     *
     * @param value      The value to replace with. Must not be null.
     * @param placeholder The placeholder to replace. Must not be null.
     * @return A {@link PlaceholderProvider} instance.
     */
    static PlaceholderProvider of(String value, String placeholder) {
        Preconditions.checkNotNull(value, "value must not be null");
        Preconditions.checkNotNull(placeholder, "placeholder must not be null");
        return of(() -> value, placeholder);
    }

    /**
     * Creates a provider for a single placeholder with a dynamic value.
     *
     * @param valueSupplier The supplier of the value. Must not be null.
     * @param placeholder   The placeholder to replace. Must not be null.
     * @return A {@link PlaceholderProvider} instance.
     */
    static PlaceholderProvider of(Supplier<String> valueSupplier, String placeholder) {
        Preconditions.checkNotNull(valueSupplier, "valueSupplier must not be null");
        Preconditions.checkNotNull(placeholder, "placeholder must not be null");
        LOGGER.atFine().log("Created PlaceholderProvider for single placeholder: %s", placeholder);
        return new PlaceholderProvider() {
            @Override
            public String replaceAll(String text) {
                return text.replaceAll(placeholder, valueSupplier.get());
            }

            @Override
            public boolean containsPlaceholder(String text) {
                return text.contains(placeholder);
            }
        };
    }

    /**
     * Creates a provider for multiple placeholders with a static value.
     *
     * @param value        The value to replace with. Must not be null.
     * @param placeholders The placeholders to replace. Must not be empty or null.
     * @return A {@link PlaceholderProvider} instance.
     */
    static PlaceholderProvider of(String value, String... placeholders) {
        Preconditions.checkNotNull(value, "value must not be null");
        Preconditions.checkNotNull(placeholders, "placeholders must not be null");
        Preconditions.checkArgument(placeholders.length > 0, "placeholders must not be empty");
        return of(() -> value, placeholders);
    }

    /**
     * Creates a provider for multiple placeholders with a dynamic value.
     *
     * @param valueSupplier The supplier of the value. Must not be null.
     * @param placeholders  The placeholders to replace. Must not be empty or null.
     * @return A {@link PlaceholderProvider} instance.
     */
    static PlaceholderProvider of(Supplier<String> valueSupplier, String... placeholders) {
        Preconditions.checkNotNull(valueSupplier, "valueSupplier must not be null");
        Preconditions.checkNotNull(placeholders, "placeholders must not be null");
        Preconditions.checkArgument(placeholders.length > 0, "placeholders must not be empty");
        LOGGER.atFine().log("Created PlaceholderProvider for %d placeholders", placeholders.length);
        return new PlaceholderProvider() {
            @Override
            public String replaceAll(String text) {
                String result = text;
                for (String placeholder : placeholders) {
                    result = result.replaceAll(placeholder, valueSupplier.get());
                }
                return result;
            }

            @Override
            public boolean containsPlaceholder(String text) {
                for (String placeholder : placeholders) {
                    if (text.contains(placeholder)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
    // #endregion

    // #region Methods
    /**
     * Checks if the text contains any placeholders managed by this provider.
     *
     * @param text The text to check. Must not be null.
     * @return {@code true} if a placeholder is found, {@code false} otherwise.
     */
    boolean containsPlaceholder(String text);

    /**
     * Replaces all placeholders in the text with the provided value.
     *
     * @param text The text to process. Must not be null.
     * @return The text with placeholders replaced.
     */
    String replaceAll(String text);
    // #endregion
}