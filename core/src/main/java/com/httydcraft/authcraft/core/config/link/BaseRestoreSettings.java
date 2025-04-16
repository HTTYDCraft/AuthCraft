package com.httydcraft.authcraft.core.config.link;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.link.stage.LinkRestoreSettings;
import com.httydcraft.authcraft.core.util.RandomCodeFactory;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

// #region Class Documentation
/**
 * Configuration settings for link restoration.
 * Defines parameters for generating restore codes.
 */
public class BaseRestoreSettings implements ConfigurationHolder, LinkRestoreSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("code-length")
    private int codeLength = 6;
    @ConfigField("code-characters")
    private String codeCharacters = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseRestoreSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public BaseRestoreSettings(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized BaseRestoreSettings with codeLength: %d", codeLength);
    }
    // #endregion

    // #region Getters
    /**
     * Gets the length of the restore code.
     *
     * @return The code length.
     */
    @Override
    public int getCodeLength() {
        return codeLength;
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
     * Generates a restore code.
     *
     * @return The generated code.
     */
    @Override
    public String generateCode() {
        String code = RandomCodeFactory.generateCode(codeLength, codeCharacters);
        LOGGER.atFine().log("Generated restore code");
        return code;
    }
    // #endregion
}