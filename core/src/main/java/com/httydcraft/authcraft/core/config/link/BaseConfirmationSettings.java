package com.httydcraft.authcraft.core.config.link;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.duration.ConfigurationDuration;
import com.httydcraft.authcraft.api.config.link.stage.LinkConfirmationSettings;
import com.httydcraft.authcraft.core.util.RandomCodeFactory;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

// #region Class Documentation
/**
 * Configuration settings for link confirmation.
 * Defines code generation parameters and confirmation behavior.
 */
public class BaseConfirmationSettings implements ConfigurationHolder, LinkConfirmationSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("remove-delay")
    private ConfigurationDuration removeDelay = new ConfigurationDuration(120 * 1000);
    @ConfigField("code-length")
    private int codeLength = 6;
    @ConfigField("can-toggle")
    private boolean canToggleConfirmation = false;
    @ConfigField("code-characters")
    private String codeCharacters = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    // #endregion

    // #region Constructors
    /**
     * Default constructor for {@code BaseConfirmationSettings}.
     */
    public BaseConfirmationSettings() {
        LOGGER.atFine().log("Initialized BaseConfirmationSettings with default values");
    }

    /**
     * Constructs a new {@code BaseConfirmationSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public BaseConfirmationSettings(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized BaseConfirmationSettings with codeLength: %d", codeLength);
    }
    // #endregion

    // #region Getters
    /**
     * Gets the delay before removing a confirmation.
     *
     * @return The {@link ConfigurationDuration}.
     */
    @Override
    public ConfigurationDuration getRemoveDelay() {
        return removeDelay;
    }

    /**
     * Gets the length of the confirmation code.
     *
     * @return The code length.
     */
    @Override
    public int getCodeLength() {
        return codeLength;
    }

    /**
     * Checks if confirmation can be toggled.
     *
     * @return {@code true} if toggling is allowed, {@code false} otherwise.
     */
    @Override
    public boolean canToggleConfirmation() {
        return canToggleConfirmation;
    }

    /**
     * Gets the characters used for code generation.
     *
     * @return The code characters.
     */
    @Override
    public String getCodeCharacters() {
        return codeCharacters;
    }
    // #endregion

    // #region Code Generation
    /**
     * Generates a confirmation code.
     *
     * @return The generated code.
     */
    @Override
    public String generateCode() {
        String code = RandomCodeFactory.generateCode(codeLength, codeCharacters);
        LOGGER.atFine().log("Generated confirmation code");
        return code;
    }
    // #endregion
}