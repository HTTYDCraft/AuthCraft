package com.httydcraft.authcraft.core.config.discord;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.config.link.DiscordSettings;
import com.httydcraft.authcraft.core.config.link.BaseCommandPaths;
import com.httydcraft.authcraft.core.config.link.BaseEnterSettings;
import com.httydcraft.authcraft.core.config.link.BaseMessengerCustomCommands;
import com.httydcraft.authcraft.core.config.link.BaseRestoreSettings;
import com.httydcraft.authcraft.api.config.link.command.LinkCustomCommands;
import com.httydcraft.authcraft.api.config.link.stage.LinkConfirmationSettings;
import com.httydcraft.authcraft.core.config.message.discord.DiscordMessages;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.impl.UserNumberIdentificator;
import com.httydcraft.authcraft.api.type.LinkConfirmationType;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// #region Class Documentation
/**
 * Configuration settings for Discord integration.
 * Manages bot token, commands, messages, keyboards, and administrative settings.
 */
public class BaseDiscordSettings implements ConfigurationHolder, DiscordSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("enabled")
    private boolean enabled = false;
    @ConfigField("token")
    private String token;
    @ConfigField("confirmation")
    private DiscordConfirmationSettings confirmationSettings;
    @ConfigField("restore")
    private BaseRestoreSettings restoreSettings;
    @ConfigField("enter")
    private BaseEnterSettings enterSettings;
    @ConfigField("discord-commands")
    private DiscordCommandPaths commandPaths;
    @ConfigField("proxy-commands")
    private BaseCommandPaths proxyCommandPaths;
    @ConfigField("custom-commands")
    private BaseMessengerCustomCommands commands;
    @ConfigField("max-discord-link")
    private int maxDiscordLinkCount = 0;
    @ConfigField("discord-messages")
    private DiscordMessages messages;
    @ConfigField("keyboards")
    private DiscordKeyboards keyboards;
    @ConfigField("admin-accounts")
    private List<Number> adminAccounts = new ArrayList<>();
    @ConfigField("whitelist-channels")
    private List<String> whiteListChannels = new ArrayList<>();
    @ConfigField("link-confirm-ways")
    private List<LinkConfirmationType> linkConfirmationTypes = Collections.singletonList(LinkConfirmationType.FROM_LINK);
    // #endregion

    // #region Constructors
    /**
     * Default constructor for {@code BaseDiscordSettings}.
     */
    public BaseDiscordSettings() {
        LOGGER.atFine().log("Initialized BaseDiscordSettings with default values");
    }

    /**
     * Constructs a new {@code BaseDiscordSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section holder. Must not be null.
     */
    public BaseDiscordSettings(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        if (token == null && enabled) {
            LOGGER.atSevere().log("Discord bot token not found!");
        }
        LOGGER.atInfo().log("Initialized BaseDiscordSettings from configuration");
    }
    // #endregion

    // #region Getters
    /**
     * Gets the Discord bot token.
     *
     * @return The bot token.
     */
    @Override
    public String getBotToken() {
        return token;
    }

    /**
     * Checks if Discord integration is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the confirmation settings.
     *
     * @return The {@link LinkConfirmationSettings}.
     */
    @Override
    public LinkConfirmationSettings getConfirmationSettings() {
        return confirmationSettings;
    }

    /**
     * Gets the custom commands configuration.
     *
     * @return The {@link LinkCustomCommands}.
     */
    @Override
    public LinkCustomCommands getCustomCommands() {
        return commands;
    }

    /**
     * Gets the enter settings.
     *
     * @return The {@link BaseEnterSettings}.
     */
    @Override
    public BaseEnterSettings getEnterSettings() {
        return enterSettings;
    }

    /**
     * Gets the restore settings.
     *
     * @return The {@link BaseRestoreSettings}.
     */
    @Override
    public BaseRestoreSettings getRestoreSettings() {
        return restoreSettings;
    }

    /**
     * Gets the Discord command paths.
     *
     * @return The {@link DiscordCommandPaths}.
     */
    @Override
    public DiscordCommandPaths getCommandPaths() {
        return commandPaths;
    }

    /**
     * Gets the proxy command paths.
     *
     * @return The {@link BaseCommandPaths}.
     */
    @Override
    public BaseCommandPaths getProxyCommandPaths() {
        return proxyCommandPaths;
    }

    /**
     * Gets the maximum number of Discord links allowed.
     *
     * @return The maximum link count.
     */
    @Override
    public int getMaxLinkCount() {
        return maxDiscordLinkCount;
    }

    /**
     * Gets the Discord messages configuration.
     *
     * @return The {@link DiscordMessages}.
     */
    @Override
    public DiscordMessages getMessages() {
        return messages;
    }

    /**
     * Gets the Discord keyboards configuration.
     *
     * @return The {@link DiscordKeyboards}.
     */
    @Override
    public DiscordKeyboards getKeyboards() {
        return keyboards;
    }

    /**
     * Gets the supported link confirmation types.
     *
     * @return An unmodifiable list of {@link LinkConfirmationType}.
     */
    @Override
    public List<LinkConfirmationType> getLinkConfirmationTypes() {
        return Collections.unmodifiableList(linkConfirmationTypes);
    }

    /**
     * Checks if conversation commands should be disabled.
     *
     * @return {@code false} as conversation commands are enabled by default.
     */
    @Override
    public boolean shouldDisableConversationCommands() {
        return false;
    }
    // #endregion

    // #region Administrator Check
    /**
     * Checks if the given identifier belongs to an administrator.
     *
     * @param identificator The user identifier. May be null.
     * @return {@code true} if the identifier is an admin, {@code false} otherwise.
     */
    @Override
    public boolean isAdministrator(LinkUserIdentificator identificator) {
        if (identificator == null || !identificator.isNumber()) {
            LOGGER.atFine().log("Invalid or null identificator for admin check");
            return false;
        }
        boolean isAdmin = adminAccounts.stream().anyMatch(number -> number.longValue() == identificator.asNumber());
        LOGGER.atFine().log("Admin check for identificator %d: %b", identificator.asNumber(), isAdmin);
        return isAdmin;
    }

    /**
     * Checks if the given user ID belongs to an administrator.
     *
     * @param userId The user ID.
     * @return {@code true} if the user is an admin, {@code false} otherwise.
     */
    public boolean isAdministrator(long userId) {
        LOGGER.atFine().log("Checking if userId %d is administrator", userId);
        return isAdministrator(new UserNumberIdentificator(userId));
    }
    // #endregion

    // #region Channel Whitelist
    /**
     * Checks if the given channel ID is allowed.
     *
     * @param channelId The channel ID.
     * @return {@code true} if the channel is allowed, {@code false} otherwise.
     */
    @Override
    public boolean isAllowedChannel(String channelId) {
        if (whiteListChannels.isEmpty()) {
            LOGGER.atFine().log("No whitelist channels defined, allowing all");
            return true;
        }
        boolean allowed = whiteListChannels.contains(channelId);
        LOGGER.atFine().log("Channel %s allowed: %b", channelId, allowed);
        return allowed;
    }
    // #endregion
}