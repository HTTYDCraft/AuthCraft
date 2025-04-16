package com.httydcraft.authcraft.core.config.vk;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.config.link.LinkKeyboards;
import com.httydcraft.authcraft.api.config.link.VKSettings;
import com.httydcraft.authcraft.api.config.link.command.LinkCommandPaths;
import com.httydcraft.authcraft.api.config.link.command.LinkCustomCommands;
import com.httydcraft.authcraft.api.config.link.stage.LinkConfirmationSettings;
import com.httydcraft.authcraft.api.config.link.stage.LinkEnterSettings;
import com.httydcraft.authcraft.api.config.link.stage.LinkRestoreSettings;
import com.httydcraft.authcraft.core.config.link.*;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.core.config.message.link.LinkMessages;
import com.httydcraft.authcraft.core.config.message.vk.VKMessages;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.impl.UserNumberIdentificator;
import com.httydcraft.authcraft.api.type.LinkConfirmationType;

import java.util.Collections;
import java.util.List;

// #region Class Documentation
/**
 * Configuration class for VK settings.
 * Implements {@link VKSettings} to provide VK-specific configuration options.
 */
public class BaseVKSettings implements ConfigurationHolder, VKSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("enabled")
    private boolean enabled = false;
    @ConfigField("confirmation")
    private BaseConfirmationSettings confirmationSettings;
    @ConfigField("restore")
    private BaseRestoreSettings restoreSettings;
    @ConfigField("enter")
    private BaseEnterSettings enterSettings;
    @ConfigField("vk-commands")
    private BaseCommandPaths commandPaths;
    @ConfigField("proxy-commands")
    private BaseCommandPaths proxyCommandPaths;
    @ConfigField("custom-commands")
    private BaseMessengerCustomCommands commands;
    @ConfigField("max-vk-link")
    private int maxVkLinkCount = 0;
    @ConfigField("vk-messages")
    private VKMessages messages;
    @ConfigField("keyboards")
    private VKKeyboards keyboards;
    @ConfigField("admin-accounts")
    private List<Integer> adminAccounts;
    @ConfigField("disable-conversation-commands")
    private boolean disableConversationCommands;
    @ConfigField("link-confirm-ways")
    private List<LinkConfirmationType> linkConfirmationTypes = Collections.singletonList(LinkConfirmationType.FROM_LINK);
    @ConfigField("link-game-commands")
    private List<String> gameLinkCommands;

    // #endregion

    // #region Constructors
    /**
     * Constructs a new {@code BaseVKSettings} with default values.
     */
    public BaseVKSettings() {
        LOGGER.atInfo().log("Initialized BaseVKSettings with default values");
    }

    /**
     * Constructs a new {@code BaseVKSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section. Must not be null.
     */
    public BaseVKSettings(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        LOGGER.atInfo().log("Initialized BaseVKSettings from configuration");
    }
    // #endregion

    // #region VKSettings Implementation
    /**
     * Checks if VK integration is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    @Override
    public boolean isEnabled() {
        LOGGER.atFine().log("Retrieved VK enabled status: %b", enabled);
        return enabled;
    }

    /**
     * Gets the confirmation settings for VK linking.
     *
     * @return The {@link LinkConfirmationSettings}.
     */
    @Override
    public LinkConfirmationSettings getConfirmationSettings() {
        LOGGER.atFine().log("Retrieved VK confirmation settings");
        return confirmationSettings;
    }

    /**
     * Gets the custom commands for VK.
     *
     * @return The {@link LinkCustomCommands}.
     */
    @Override
    public LinkCustomCommands getCustomCommands() {
        LOGGER.atFine().log("Retrieved VK custom commands");
        return commands;
    }

    /**
     * Gets the enter settings for VK.
     *
     * @return The {@link LinkEnterSettings}.
     */
    @Override
    public LinkEnterSettings getEnterSettings() {
        LOGGER.atFine().log("Retrieved VK enter settings");
        return enterSettings;
    }

    /**
     * Checks if the user is an administrator.
     *
     * @param identificator The user identifier. May be null.
     * @return {@code true} if the user is an admin, {@code false} otherwise.
     */
    @Override
    public boolean isAdministrator(LinkUserIdentificator identificator) {
        if (identificator == null || !identificator.isNumber()) {
            LOGGER.atFine().log("Invalid or null identificator, not an admin");
            return false;
        }
        boolean result = adminAccounts.contains((int) identificator.asNumber());
        LOGGER.atFine().log("Checked admin status for identificator %s: %b", identificator.asNumber(), result);
        return result;
    }

    /**
     * Checks if conversation commands are disabled.
     *
     * @return {@code true} if disabled, {@code false} otherwise.
     */
    public boolean shouldDisableConversationCommands() {
        LOGGER.atFine().log("Retrieved conversation commands disabled status: %b", disableConversationCommands);
        return disableConversationCommands;
    }

    /**
     * Checks if the user ID is an administrator.
     *
     * @param userId The user ID.
     * @return {@code true} if the user is an admin, {@code false} otherwise.
     */
    public boolean isAdministrator(int userId) {
        boolean result = isAdministrator(new UserNumberIdentificator(userId));
        LOGGER.atFine().log("Checked admin status for user ID %d: %b", userId, result);
        return result;
    }

    /**
     * Gets the restore settings for VK.
     *
     * @return The {@link LinkRestoreSettings}.
     */
    @Override
    public LinkRestoreSettings getRestoreSettings() {
        LOGGER.atFine().log("Retrieved VK restore settings");
        return restoreSettings;
    }

    /**
     * Gets the command paths for VK.
     *
     * @return The {@link LinkCommandPaths}.
     */
    @Override
    public LinkCommandPaths getCommandPaths() {
        LOGGER.atFine().log("Retrieved VK command paths");
        return commandPaths;
    }

    /**
     * Gets the proxy command paths for VK.
     *
     * @return The {@link LinkCommandPaths}.
     */
    @Override
    public LinkCommandPaths getProxyCommandPaths() {
        LOGGER.atFine().log("Retrieved VK proxy command paths");
        return proxyCommandPaths;
    }

    /**
     * Gets the maximum number of VK links allowed.
     *
     * @return The maximum link count.
     */
    @Override
    public int getMaxLinkCount() {
        LOGGER.atFine().log("Retrieved max VK link count: %d", maxVkLinkCount);
        return maxVkLinkCount;
    }

    /**
     * Gets the VK-specific messages.
     *
     * @return The {@link LinkMessages}.
     */
    @Override
    public LinkMessages getMessages() {
        LOGGER.atFine().log("Retrieved VK messages");
        return messages;
    }

    /**
     * Gets the supported link confirmation types.
     *
     * @return An unmodifiable list of {@link LinkConfirmationType}.
     */
    @Override
    public List<LinkConfirmationType> getLinkConfirmationTypes() {
        LOGGER.atFine().log("Retrieved VK link confirmation types");
        return Collections.unmodifiableList(linkConfirmationTypes);
    }

    /**
     * Gets the VK keyboard configurations.
     *
     * @return The {@link LinkKeyboards}.
     */
    @Override
    public LinkKeyboards getKeyboards() {
        LOGGER.atFine().log("Retrieved VK keyboards");
        return keyboards;
    }
    // #endregion
}