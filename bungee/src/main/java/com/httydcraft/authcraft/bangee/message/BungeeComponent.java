package com.httydcraft.authcraft.bangee.message;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// #region Class Documentation
/**
 * Interface for BungeeCord-specific components.
 * Extends {@link ServerComponent} to provide BungeeCord-specific component handling.
 */
public interface BungeeComponent extends ServerComponent {
    Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
    GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #region Component Methods
    /**
     * Gets the BungeeCord component array.
     *
     * @return The {@link BaseComponent} array.
     */
    BaseComponent[] components();
    // #endregion

    // #region Static Methods
    /**
     * Colorizes text with hex colors and alternate color codes.
     *
     * @param message The text to colorize. Must not be null.
     * @return The colorized text.
     */
    static String colorText(@NotNull String message) {
        Preconditions.checkNotNull(message, "message must not be null");
        LOGGER.atFine().log("Colorizing text: %s", message);
        Matcher matcher = HEX_PATTERN.matcher(message);
        String result = message;
        while (matcher.find()) {
            ChatColor hexColor = ChatColor.of(Color.decode("#" + matcher.group(1)));
            String before = result.substring(0, matcher.start());
            String after = result.substring(matcher.end());
            result = before + hexColor + after;
            matcher = HEX_PATTERN.matcher(result);
        }
        result = ChatColor.translateAlternateColorCodes('&', result);
        LOGGER.atFine().log("Colorized result: %s", result);
        return result;
    }
    // #endregion
}