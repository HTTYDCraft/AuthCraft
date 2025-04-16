package com.httydcraft.authcraft.core.config.bossbar;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.bossbar.BossBarSettings;
import com.httydcraft.authcraft.api.server.bossbar.ServerBossbar;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

import java.text.SimpleDateFormat;

// #region Class Documentation
/**
 * Configuration settings for the boss bar used in authentication.
 * Defines properties such as visibility, color, style, title, and duration format.
 */
public class BaseBossBarSettings implements ConfigurationHolder, BossBarSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("use")
    private boolean enabled = false;
    @ConfigField("bar-color")
    private ServerBossbar.Color color = ServerBossbar.Color.BLUE;
    @ConfigField("bar-style")
    private ServerBossbar.Style style = ServerBossbar.Style.SOLID;
    @ConfigField("bar-text")
    private ServerComponent title = ServerComponent.fromPlain("[Authentication]");
    @ConfigField("bar-duration-placeholder-format")
    private SimpleDateFormat durationPlaceholderFormat = new SimpleDateFormat("mm:ss");
    // #endregion

    // #region Constructors
    /**
     * Constructs a new {@code BaseBossBarSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public BaseBossBarSettings(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized BaseBossBarSettings from configuration");
    }

    /**
     * Default constructor for {@code BaseBossBarSettings}.
     */
    public BaseBossBarSettings() {
        LOGGER.atFine().log("Initialized BaseBossBarSettings with default values");
    }
    // #endregion

    // #region Getters
    /**
     * Checks if the boss bar is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the title of the boss bar.
     *
     * @return The {@link ServerComponent} title.
     */
    @Override
    public ServerComponent getTitle() {
        return title;
    }

    /**
     * Gets the format for displaying duration placeholders.
     *
     * @return The {@link SimpleDateFormat} for duration.
     */
    @Override
    public SimpleDateFormat getDurationPlaceholderFormat() {
        return durationPlaceholderFormat;
    }
    // #endregion

    // #region Boss Bar Creation
    /**
     * Creates a boss bar instance if enabled.
     *
     * @return A {@link ServerBossbar} instance, or {@code null} if disabled.
     */
    @Override
    public ServerBossbar createBossbar() {
        if (!enabled) {
            LOGGER.atFine().log("Boss bar creation skipped: disabled");
            return null;
        }
        ServerBossbar bossbar = AuthPlugin.instance().getCore().createBossbar(title)
                .color(color)
                .style(style)
                .update();
        LOGGER.atInfo().log("Created boss bar with color: %s, style: %s", color, style);
        return bossbar;
    }
    // #endregion
}