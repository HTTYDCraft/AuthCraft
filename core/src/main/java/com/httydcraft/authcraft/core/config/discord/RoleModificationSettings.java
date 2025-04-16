package com.httydcraft.authcraft.core.config.discord;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.annotation.ImportantField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

import java.util.ArrayList;
import java.util.List;

// #region Class Documentation
/**
 * Configuration settings for Discord role modifications.
 * Defines role assignment or removal based on permissions.
 */
public class RoleModificationSettings implements ConfigurationHolder {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("type")
    private Type type;
    @ConfigField("role-id")
    @ImportantField
    private long roleId;
    @ConfigField("have-permission")
    private List<String> havePermission = new ArrayList<>();
    @ConfigField("absent-permission")
    private List<String> absentPermission = new ArrayList<>();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code RoleModificationSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public RoleModificationSettings(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized RoleModificationSettings for roleId: %d, type: %s", roleId, type);
    }
    // #endregion

    // #region Getters
    /**
     * Gets the type of role modification.
     *
     * @return The {@link Type} (GIVE_ROLE or REMOVE_ROLE).
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the role ID.
     *
     * @return The role ID.
     */
    public long getRoleId() {
        return roleId;
    }

    /**
     * Gets the list of required permissions.
     *
     * @return The list of permissions that must be present.
     */
    public List<String> getHavePermission() {
        return havePermission;
    }

    /**
     * Gets the list of permissions that must be absent.
     *
     * @return The list of permissions that must not be present.
     */
    public List<String> getAbsentPermission() {
        return absentPermission;
    }
    // #endregion

    // #region Enum
    /**
     * Enum defining the type of role modification.
     */
    public enum Type {
        GIVE_ROLE, REMOVE_ROLE
    }
    // #endregion
}