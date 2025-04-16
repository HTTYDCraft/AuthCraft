package com.httydcraft.authcraft.core.config.discord;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.core.config.link.BaseConfirmationSettings;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.core.config.factory.ConfigurationHolderMapResolverFactory.ConfigurationHolderMap;

import java.util.Collections;
import java.util.Map;

// #region Class Documentation
/**
 * Configuration settings for Discord confirmation.
 * Extends base confirmation settings with guild ID and role modification settings.
 */
public class DiscordConfirmationSettings extends BaseConfirmationSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("guild-id")
    private long guildId;
    @ConfigField("update-roles-on-each-enter")
    private boolean updateRoles = false;
    @ConfigField("role-modification")
    private ConfigurationHolderMap<RoleModificationSettings> roleModificationSettings = new ConfigurationHolderMap<>();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code DiscordConfirmationSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public DiscordConfirmationSettings(ConfigurationSectionHolder sectionHolder) {
        super(sectionHolder);
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized DiscordConfirmationSettings with guildId: %d", guildId);
    }
    // #endregion

    // #region Getters
    /**
     * Gets the Discord guild ID.
     *
     * @return The guild ID.
     */
    public long getGuildId() {
        return guildId;
    }

    /**
     * Checks if roles should be updated on each enter.
     *
     * @return {@code true} if roles should be updated, {@code false} otherwise.
     */
    public boolean shouldUpdateRoles() {
        return updateRoles;
    }

    /**
     * Gets the role modification settings.
     *
     * @return An unmodifiable map of role modification settings.
     */
    public Map<String, RoleModificationSettings> getRoleModificationSettings() {
        return Collections.unmodifiableMap(roleModificationSettings);
    }
    // #endregion
}