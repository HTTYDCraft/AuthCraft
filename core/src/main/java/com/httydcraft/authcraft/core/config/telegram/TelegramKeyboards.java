package com.httydcraft.authcraft.core.config.telegram;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.google.common.collect.ImmutableMap;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.multimessenger.core.keyboard.Keyboard;
import com.httydcraft.multimessenger.telegram.message.keyboard.TelegramKeyboard;
import com.httydcraft.authcraft.api.config.link.LinkKeyboards;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// #region Class Documentation
/**
 * Configuration class for Telegram keyboards.
 * Loads and provides Telegram-specific keyboard layouts.
 */
public class TelegramKeyboards implements ConfigurationHolder, LinkKeyboards {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final Map<String, String> jsonKeyboards;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code TelegramKeyboards} from a configuration section.
     *
     * @param sectionHolder The configuration section. Must not be null.
     */
    public TelegramKeyboards(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        this.jsonKeyboards = sectionHolder.keys().stream()
                .collect(Collectors.toMap(Function.identity(), sectionHolder::getString));
        LOGGER.atInfo().log("Initialized TelegramKeyboards with %d keyboards", jsonKeyboards.size());
    }
    // #endregion

    // #region LinkKeyboards Implementation
    /**
     * Gets the raw JSON representations of the keyboards.
     *
     * @return An unmodifiable map of keyboard identifiers to JSON strings.
     */
    @Override
    public Map<String, String> getRawJsonKeyboards() {
        LOGGER.atFine().log("Retrieved raw JSON keyboards");
        return ImmutableMap.copyOf(jsonKeyboards);
    }

    /**
     * Creates a Telegram keyboard model from a raw JSON string.
     *
     * @param rawJson The raw JSON string. Must not be null.
     * @return The {@link Keyboard} model.
     * @throws IllegalArgumentException If rawJson is empty.
     */
    @Override
    public Keyboard createKeyboardModel(String rawJson) {
        Preconditions.checkNotNull(rawJson, "rawJson must not be null");
        Preconditions.checkArgument(!rawJson.isEmpty(), "rawJson must not be empty");
        LOGGER.atFine().log("Creating Telegram keyboard model from JSON");
        InlineKeyboardMarkup markup = GSON.fromJson(rawJson, InlineKeyboardMarkup.class);
        Keyboard keyboard = new TelegramKeyboard(markup);
        LOGGER.atFine().log("Created Telegram keyboard model");
        return keyboard;
    }
    // #endregion
}