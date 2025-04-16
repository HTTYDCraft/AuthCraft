package com.httydcraft.authcraft.core.config.vk;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.google.common.collect.ImmutableMap;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.multimessenger.core.keyboard.Keyboard;
import com.httydcraft.multimessenger.vk.message.keyboard.VkKeyboard;
import com.httydcraft.authcraft.api.config.link.LinkKeyboards;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// #region Class Documentation
/**
 * Configuration class for VK keyboards.
 * Loads and provides VK-specific keyboard layouts.
 */
public class VKKeyboards implements ConfigurationHolder, LinkKeyboards {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final Map<String, String> jsonKeyboards;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VKKeyboards} from a configuration section.
     *
     * @param sectionHolder The configuration section. Must not be null.
     */
    public VKKeyboards(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        this.jsonKeyboards = sectionHolder.keys().stream()
                .collect(Collectors.toMap(Function.identity(), sectionHolder::getString));
        LOGGER.atInfo().log("Initialized VKKeyboards with %d keyboards", jsonKeyboards.size());
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
     * Creates a VK keyboard model from a raw JSON string.
     *
     * @param rawJson The raw JSON string. Must not be null.
     * @return The {@link Keyboard} model.
     * @throws IllegalArgumentException If rawJson is empty.
     */
    @Override
    public Keyboard createKeyboardModel(String rawJson) {
        Preconditions.checkNotNull(rawJson, "rawJson must not be null");
        Preconditions.checkArgument(!rawJson.isEmpty(), "rawJson must not be empty");
        LOGGER.atFine().log("Creating VK keyboard model from JSON");
        com.vk.api.sdk.objects.messages.Keyboard keyboard = GSON.fromJson(rawJson, com.vk.api.sdk.objects.messages.Keyboard.class);
        Keyboard result = new VkKeyboard(keyboard);
        LOGGER.atFine().log("Created VK keyboard model");
        return result;
    }
    // #endregion
}