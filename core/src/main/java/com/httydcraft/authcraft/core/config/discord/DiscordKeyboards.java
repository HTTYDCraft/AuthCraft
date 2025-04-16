package com.httydcraft.authcraft.core.config.discord;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.config.link.LinkKeyboards;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.multimessenger.core.keyboard.Keyboard;
import com.httydcraft.multimessenger.discord.message.keyboard.DiscordKeyboard;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// #region Class Documentation
/**
 * Configuration for Discord keyboards.
 * Manages raw JSON keyboard data and creates keyboard models.
 */
public class DiscordKeyboards implements ConfigurationHolder, LinkKeyboards {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final Map<String, String> jsonKeyboards;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code DiscordKeyboards} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public DiscordKeyboards(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        this.jsonKeyboards = sectionHolder.keys().stream()
                .collect(Collectors.toMap(Function.identity(), sectionHolder::getString));
        LOGGER.atInfo().log("Initialized DiscordKeyboards with %d keyboards", jsonKeyboards.size());
    }
    // #endregion

    // #region Keyboard Management
    /**
     * Gets the raw JSON keyboards.
     *
     * @return An unmodifiable map of keyboard IDs to JSON strings.
     */
    @Override
    public Map<String, String> getRawJsonKeyboards() {
        return Collections.unmodifiableMap(jsonKeyboards);
    }

    /**
     * Creates a keyboard model from raw JSON.
     *
     * @param rawJson The raw JSON string. Must not be null.
     * @return A {@link Keyboard} instance, or an empty {@link DiscordKeyboard} if parsing fails.
     */
    @Override
    public Keyboard createKeyboardModel(String rawJson) {
        Preconditions.checkNotNull(rawJson, "rawJson must not be null");
        LOGGER.atFine().log("Creating keyboard model from JSON");

        try {
            DataObject dataObject = DataObject.fromJson(rawJson);
            DataArray rowsDataArray = dataObject.getArray("rows");
            List<ActionRow> actionRows = new ArrayList<>();
            for (int i = 0; i < rowsDataArray.length(); i++) {
                actionRows.add(ActionRow.fromData(rowsDataArray.getObject(i)));
            }
            DiscordKeyboard keyboard = new DiscordKeyboard(actionRows);
            LOGGER.atFine().log("Created keyboard with %d action rows", actionRows.size());
            return keyboard;
        } catch (Throwable t) {
            LOGGER.atSevere().withCause(t).log("Failed to create keyboard model");
            return new DiscordKeyboard();
        }
    }
    // #endregion
}