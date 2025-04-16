package com.httydcraft.authcraft.core.config.link;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.link.stage.LinkEnterSettings;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

// #region Class Documentation
/**
 * Configuration settings for link entry.
 * Defines the delay for entry confirmation.
 */
public class BaseEnterSettings implements ConfigurationHolder, LinkEnterSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("enter-delay")
    private int enterDelay = 60;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseEnterSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public BaseEnterSettings(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized BaseEnterSettings with enterDelay: %d", enterDelay);
    }
    // #endregion

    // #region Getter
    /**
     * Gets the entry delay in seconds.
     *
     * @return The entry delay.
     */
    @Override
    public int getEnterDelay() {
        return enterDelay;
    }
    // #endregion
}