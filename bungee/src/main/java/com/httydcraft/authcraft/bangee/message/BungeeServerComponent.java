package com.httydcraft.authcraft.bangee.message;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

// #region Class Documentation
/**
 * BungeeCord-specific implementation of a server component.
 * Implements {@link BungeeComponent} to handle text serialization and component creation.
 */
public class BungeeServerComponent implements BungeeComponent {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final BaseComponent[] components;

    // #endregion

    // #region Constructors
    /**
     * Constructs a new {@code BungeeServerComponent} with the given components.
     *
     * @param components The BungeeCord components. Must not be null.
     */
    public BungeeServerComponent(@NotNull BaseComponent[] components) {
        this.components = Preconditions.checkNotNull(components, "components must not be null");
        LOGGER.atInfo().log("Initialized BungeeServerComponent with components");
    }

    /**
     * Constructs a new {@code BungeeServerComponent} from legacy text.
     *
     * @param legacyText The legacy text to parse. Must not be null.
     */
    public BungeeServerComponent(@NotNull String legacyText) {
        this(TextComponent.fromLegacyText(Preconditions.checkNotNull(
                BungeeComponent.colorText(legacyText), "colorized legacyText must not be null")));
        LOGGER.atInfo().log("Initialized BungeeServerComponent with legacy text: %s", legacyText);
    }
    // #endregion

    // #region Component Methods
    /**
     * Serializes the component to JSON format.
     *
     * @return The JSON string representation.
     */
    @Override
    public String jsonText() {
        String result = ComponentSerializer.toString(components);
        LOGGER.atFine().log("Serialized component to JSON: %s", result);
        return result;
    }

    /**
     * Serializes the component to legacy text format.
     *
     * @return The legacy text representation.
     */
    @Override
    public String legacyText() {
        String result = TextComponent.toLegacyText(components);
        LOGGER.atFine().log("Serialized component to legacy text: %s", result);
        return result;
    }

    /**
     * Serializes the component to plain text format.
     *
     * @return The plain text representation.
     */
    @Override
    public String plainText() {
        String result = ChatColor.stripColor(legacyText());
        LOGGER.atFine().log("Serialized component to plain text: %s", result);
        return result;
    }

    /**
     * Gets the BungeeCord component array.
     *
     * @return The {@link BaseComponent} array.
     */
    @Override
    public BaseComponent[] components() {
        LOGGER.atFine().log("Retrieved component array");
        return components;
    }
    // #endregion
}