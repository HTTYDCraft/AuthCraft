package com.httydcraft.authcraft.core.config.telegram;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.core.config.link.BaseCommandPaths;
import com.httydcraft.authcraft.core.config.link.BaseConfirmationSettings;
import com.httydcraft.authcraft.core.config.link.BaseEnterSettings;
import com.httydcraft.authcraft.core.config.link.BaseMessengerCustomCommands;
import com.httydcraft.authcraft.core.config.link.BaseRestoreSettings;
import com.httydcraft.authcraft.api.config.link.TelegramSettings;
import com.httydcraft.authcraft.api.config.link.command.LinkCustomCommands;
import com.httydcraft.authcraft.api.config.link.stage.LinkConfirmationSettings;
import com.httydcraft.authcraft.core.config.message.telegram.TelegramMessages;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.type.LinkConfirmationType;
import com.httydcraft.configuration.ConfigurationHolder;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// #region Class Documentation
/**
 * Configuration for Telegram integration.
 * Manages bot token, commands, messages, keyboards, and administrative settings.
 */
public class BaseTelegramSettings implements ConfigurationHolder, TelegramSettings {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    @ConfigField("enabled")
    private boolean enabled = false;
    @ConfigField("token")
    private String token;
    @ConfigField("confirmation")
    private BaseConfirmationSettings confirmationSettings;
    @ConfigField("restore")
    private BaseRestoreSettings restoreSettings;
    @ConfigField("enter")
    private BaseEnterSettings enterSettings;
    @ConfigField("telegram-commands")
    private BaseCommandPaths commandPaths;
    @ConfigField("proxy-commands")
    private BaseCommandPaths proxyCommandPaths;
    @ConfigField("custom-commands")
    private BaseMessengerCustomCommands commands;
    @ConfigField("max-telegram-link")
    private int maxTelegramLinkCount = 0;
    @ConfigField("telegram-messages")
    private TelegramMessages messages;
    @ConfigField("keyboards")
    private TelegramKeyboards keyboards;
    @ConfigField("admin-accounts")
    private List<Number> adminAccounts = new ArrayList<>();
    @ConfigField("link-confirm-ways")
    private List<LinkConfirmationType> linkConfirmationTypes = Collections.singletonList(LinkConfirmationType.FROM_LINK);
    // #endregion

    // #region Constructors
    /**
     * Default constructor for {@code BaseTelegramSettings}.
     */
    public BaseTelegramSettings() {
        LOGGER.atFine().log("Initialized BaseTelegramSettings with default values");
    }

    /**
     * Constructs a new {@code BaseTelegramSettings} from a configuration section.
     *
     * @param sectionHolder The configuration section. Must not be null.
     */
    public BaseTelegramSettings(ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");
        AuthPlugin.instance().getConfigurationProcessor().resolve(sectionHolder, this);
        if (token == null && enabled) {
            LOGGER.atWarning().log("Telegram bot token not found, but Telegram is enabled");
        }
        LOGGER.atInfo().log("Initialized BaseTelegramSettings with %d admin accounts", adminAccounts.size());
    }
    // #endregion

    // #region Getters
    /**
     * Gets the Telegram bot token.
     *
     * @return The bot token, or null if not set.
     */
    @Override
    public String getBotToken() {
        return token;
    }

    /**
     * Checks if Telegram integration is enabled.
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
     * @return The {@link LinkConfirmationSettings}, or null if not set.
     */
    @Override
    public LinkConfirmationSettings getConfirmationSettings() {
        return confirmationSettings;
    }

    /**
     * Gets the custom commands configuration.
     *
     * @return The {@link LinkCustomCommands}, or null if not set.
     */
    @Override
    public LinkCustomCommands getCustomCommands() {
        return commands;
    }

    /**
     * Gets the enter settings.
     *
     * @return The {@link BaseEnterSettings}, or null if not set.
     */
    @Override
    public BaseEnterSettings getEnterSettings() {
        return enterSettings;
    }

    /**
     * Gets the restore settings.
     *
     * @return The {@link BaseRestoreSettings}, or null if not set.
     */
    @Override
    public BaseRestoreSettings getRestoreSettings() {
        return restoreSettings;
    }

    /**
     * Gets the Telegram command paths.
     *
     * @return The {@link BaseCommandPaths}, or null if not set.
     */
    @Override
    public BaseCommandPaths getCommandPaths() {
        return commandPaths;
    }

    /**
     * Gets the proxy command paths.
     *
     * @return The {@link BaseCommandPaths}, or null if not set.
     */
    @Override
    public BaseCommandPaths getProxyCommandPaths() {
        return proxyCommandPaths;
    }

    /**
     * Gets the maximum number of Telegram links allowed.
     *
     * @return The maximum link count.
     */
    @Override
    public int getMaxLinkCount() {
        return maxTelegramLinkCount;
    }

    /**
     * Gets the Telegram messages configuration.
     *
     * @return The {@link TelegramMessages}, or null if not set.
     */
    @Override
    public TelegramMessages getMessages() {
        return messages;
    }

    /**
     * Gets the Telegram keyboards configuration.
     *
     * @return The {@link TelegramKeyboards}, or null if not set.
     */
    @Override
    public TelegramKeyboards getKeyboards() {
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
            LOGGER.atFine().log("Invalid or null identifier for admin check");
            return false;
        }
        boolean isAdmin = adminAccounts.stream()
                .anyMatch(number -> number.longValue() == identificator.asNumber());
        LOGGER.atFine().log("Admin check for identifier %d: %b", identificator.asNumber(), isAdmin);
        return isAdmin;
    }
    // #endregion
}