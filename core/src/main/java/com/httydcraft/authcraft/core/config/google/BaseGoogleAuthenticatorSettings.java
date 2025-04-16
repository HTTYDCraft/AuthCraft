package com.httydcraft.authcraft.core.config.google;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.link.GoogleAuthenticatorSettings;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

// #region Class Documentation
/**
 * Configuration settings for Google Authenticator integration.
 * Defines whether the authenticator is enabled.
 */
public class BaseGoogleAuthenticatorSettings implements ConfigurationHolder, GoogleAuthenticatorSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("enabled")
    private boolean enabled = false;
    // #endregion

    // #region Constructors
    /**
     * Constructs a new {@code BaseGoogleAuthenticatorSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public BaseGoogleAuthenticatorSettings(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized BaseGoogleAuthenticatorSettings, enabled: %b", enabled);
    }

    /**
     * Default constructor for {@code BaseGoogleAuthenticatorSettings}.
     */
    public BaseGoogleAuthenticatorSettings() {
        LOGGER.atFine().log("Initialized BaseGoogleAuthenticatorSettings with default values");
    }
    // #endregion

    // #region Getter
    /**
     * Checks if Google Authenticator is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }
    // #endregion
}