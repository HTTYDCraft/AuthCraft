package com.httydcraft.authcraft.core.config;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.duration.ConfigurationDuration;
import com.httydcraft.configuration.annotation.ConfigField;
import com.httydcraft.configuration.annotation.ImportantField;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.core.config.bossbar.BaseBossBarSettings;
import com.httydcraft.authcraft.api.config.bossbar.BossBarSettings;
import com.httydcraft.authcraft.api.config.database.LegacyStorageDataSettings;
import com.httydcraft.authcraft.core.config.discord.BaseDiscordSettings;
import com.httydcraft.authcraft.core.config.google.BaseGoogleAuthenticatorSettings;
import com.httydcraft.authcraft.api.config.link.DiscordSettings;
import com.httydcraft.authcraft.api.config.link.GoogleAuthenticatorSettings;
import com.httydcraft.authcraft.api.config.link.TelegramSettings;
import com.httydcraft.authcraft.api.config.link.VKSettings;
import com.httydcraft.authcraft.core.config.message.server.BaseServerMessages;
import com.httydcraft.authcraft.api.config.message.server.ServerMessages;
import com.httydcraft.authcraft.core.config.resolver.RawURLProviderFieldResolverFactory.RawURLProvider;
import com.httydcraft.authcraft.api.config.server.ConfigurationServer;
import com.httydcraft.authcraft.core.config.storage.BaseDatabaseConfiguration;
import com.httydcraft.authcraft.core.config.storage.BaseLegacyStorageDataSettings;
import com.httydcraft.authcraft.core.config.telegram.BaseTelegramSettings;
import com.httydcraft.authcraft.core.config.vk.BaseVKSettings;
import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.httydcraft.authcraft.api.database.DatabaseConnectionProvider;
import com.httydcraft.authcraft.api.server.proxy.ProxyServer;
import com.httydcraft.authcraft.api.type.FillType;
import com.httydcraft.authcraft.api.type.IdentifierType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// #region Class Documentation
/**
 * Abstract template for the plugin configuration.
 * Implements {@link PluginConfig} to provide all configuration options for AuthCraft.
 */
public abstract class PluginConfigTemplate implements PluginConfig {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    protected final AuthPlugin plugin;
    private final List<Pattern> allowedPatternCommands;
    protected ConfigurationSectionHolder configurationRoot;
    @ConfigField("auto-migrate-config")
    private boolean autoMigrateConfig;
    @ConfigField("id-type")
    private IdentifierType activeIdentifierType = IdentifierType.NAME;
    @ConfigField("check-name-case")
    private boolean nameCaseCheckEnabled = true;
    @ConfigField("enable-password-confirm")
    private boolean passwordConfirmationEnabled = false;
    @ConfigField("enable-password-in-chat")
    private boolean passwordInChatEnabled = false;
    @ConfigField("hash-type")
    private CryptoProvider activeCryptoProvider;
    @ConfigField("storage-type")
    private DatabaseConnectionProvider databaseConnectionProvider = DatabaseConnectionProvider.SQLITE;
    @ConfigField("name-regex-pattern")
    private Pattern namePattern = Pattern.compile("[a-zA-Z0-9_]*");
    @ConfigField("password-min-length")
    private int passwordMinLength = 5;
    @ConfigField("password-max-length")
    private int passwordMaxLength = 20;
    @ConfigField("password-attempts")
    private int passwordAttempts = 3;
    @ConfigField("auth-time")
    private ConfigurationDuration authTime = new ConfigurationDuration(60);
    @ImportantField
    @ConfigField("auth-servers")
    private List<ConfigurationServer> authServers = Collections.emptyList();
    @ImportantField
    @ConfigField("game-servers")
    private List<ConfigurationServer> gameServers = Collections.emptyList();
    @ConfigField("blocked-servers")
    private List<ConfigurationServer> blockedServers = Collections.emptyList();
    @ConfigField("allowed-commands")
    private List<String> allowedCommands = Collections.emptyList();
    @ConfigField("data")
    private BaseLegacyStorageDataSettings legacyStorageDataSettings = null;
    @ConfigField("database")
    private BaseDatabaseConfiguration databaseConfiguration;
    @ConfigField("max-login-per-ip")
    private int maxLoginPerIP = 0;
    @ConfigField("messages-delay")
    private int messagesDelay = 5;
    @ConfigField("telegram")
    private BaseTelegramSettings telegramSettings = new BaseTelegramSettings();
    @ConfigField("vk")
    private BaseVKSettings vkSettings = new BaseVKSettings();
    @ConfigField("discord")
    private BaseDiscordSettings discordSettings = new BaseDiscordSettings();
    @ConfigField("google-authenticator")
    private BaseGoogleAuthenticatorSettings googleAuthenticatorSettings = new BaseGoogleAuthenticatorSettings();
    @ImportantField
    @ConfigField("messages")
    private BaseServerMessages serverMessages = null;
    @ConfigField("boss-bar")
    private BaseBossBarSettings barSettings = new BaseBossBarSettings();
    @ConfigField("fill-type")
    private FillType fillType = FillType.GRADUALLY;
    @ConfigField("session-durability")
    private ConfigurationDuration sessionDurability = new ConfigurationDuration(14400L);
    @ConfigField("join-delay")
    private ConfigurationDuration joinDelay = new ConfigurationDuration(0);
    @ConfigField("block-chat")
    private boolean blockChat = true;
    @ConfigField("limbo-port")
    private IntStream limboPortRange = IntStream.range(49152, 65535);
    @ConfigField("authentication-steps")
    private List<String> authenticationSteps = Arrays.asList("REGISTER", "LOGIN", "VK_LINK", "TELEGRAM_LINK", "GOOGLE_LINK", "ENTER_SERVER");

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code PluginConfigTemplate}.
     *
     * @param plugin The AuthPlugin instance. Must not be null.
     */
    public PluginConfigTemplate(AuthPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin must not be null");
        this.configurationRoot = createConfiguration(plugin);
        plugin.getConfigurationProcessor().resolve(configurationRoot, this);
        this.allowedPatternCommands = allowedCommands.stream().map(Pattern::compile).collect(Collectors.toList());
        if (databaseConfiguration == null && legacyStorageDataSettings != null) {
            databaseConfiguration = new BaseDatabaseConfiguration(
                    RawURLProvider.of(databaseConnectionProvider.getConnectionUrl(legacyStorageDataSettings)),
                    databaseConnectionProvider.getDriverDownloadUrl(),
                    legacyStorageDataSettings.getUser(),
                    legacyStorageDataSettings.getPassword());
        }
        LOGGER.atInfo().log("Initialized PluginConfigTemplate");
    }
    // #endregion

    // #region PluginConfig Implementation
    /**
     * Finds a suitable server from the provided list based on the fill type.
     *
     * @param servers The list of servers. Must not be null.
     * @return The selected {@link ConfigurationServer}.
     */
    @Override
    public ConfigurationServer findServerInfo(List<ConfigurationServer> servers) {
        Preconditions.checkNotNull(servers, "servers must not be null");
        LOGGER.atFine().log("Finding server info from %d servers", servers.size());

        List<ConfigurationServer> filteredServers = fillType.shuffle(servers.stream().filter(server -> {
            ProxyServer proxyServer = server.asProxyServer();
            if (!proxyServer.isExists()) {
                LOGGER.atWarning().log("Server %s does not exist in proxy", server.getId());
                return false;
            }
            boolean valid = server.getMaxPlayers() == -1 || (proxyServer.getPlayersCount() < server.getMaxPlayers());
            LOGGER.atFine().log("Server %s valid: %b", server.getId(), valid);
            return valid;
        }).collect(Collectors.toList()));

        ConfigurationServer result = filteredServers.isEmpty() ? servers.get(0) : filteredServers.get(0);
        LOGGER.atFine().log("Selected server: %s", result.getId());
        return result;
    }

    /**
     * Reloads the configuration from the plugin.
     */
    @Override
    public void reload() {
        LOGGER.atInfo().log("Reloading configuration");
        this.configurationRoot = createConfiguration(plugin);
        AuthPlugin.instance().getConfigurationProcessor().resolve(configurationRoot, this);
        LOGGER.atInfo().log("Configuration reloaded");
    }

    /**
     * Gets the active identifier type.
     *
     * @return The {@link IdentifierType}.
     */
    @Override
    public IdentifierType getActiveIdentifierType() {
        LOGGER.atFine().log("Retrieved active identifier type: %s", activeIdentifierType);
        return activeIdentifierType;
    }

    /**
     * Checks if name case checking is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    @Override
    public boolean isNameCaseCheckEnabled() {
        LOGGER.atFine().log("Retrieved name case check enabled: %b", nameCaseCheckEnabled);
        return nameCaseCheckEnabled;
    }

    /**
     * Checks if auto-migration of configuration is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    @Override
    public boolean isAutoMigrateConfigEnabled() {
        LOGGER.atFine().log("Retrieved auto-migrate config enabled: %b", autoMigrateConfig);
        return autoMigrateConfig;
    }

    /**
     * Checks if password confirmation is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    @Override
    public boolean isPasswordConfirmationEnabled() {
        LOGGER.atFine().log("Retrieved password confirmation enabled: %b", passwordConfirmationEnabled);
        return passwordConfirmationEnabled;
    }

    /**
     * Checks if password input in chat is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    @Override
    public boolean isPasswordInChatEnabled() {
        LOGGER.atFine().log("Retrieved password in chat enabled: %b", passwordInChatEnabled);
        return passwordInChatEnabled;
    }

    /**
     * Gets the active cryptographic provider.
     *
     * @return The {@link CryptoProvider}.
     */
    @Override
    public CryptoProvider getActiveHashType() {
        LOGGER.atFine().log("Retrieved active hash type: %s", activeCryptoProvider != null ? activeCryptoProvider.getIdentifier() : null);
        return activeCryptoProvider;
    }

    /**
     * Gets the storage type for the database.
     *
     * @return The {@link DatabaseConnectionProvider}.
     */
    @Override
    public DatabaseConnectionProvider getStorageType() {
        LOGGER.atFine().log("Retrieved storage type: %s", databaseConnectionProvider);
        return databaseConnectionProvider;
    }

    /**
     * Gets the regex pattern for valid names.
     *
     * @return The name {@link Pattern}.
     */
    @Override
    public Pattern getNamePattern() {
        LOGGER.atFine().log("Retrieved name pattern");
        return namePattern;
    }

    /**
     * Gets the minimum password length.
     *
     * @return The minimum password length.
     */
    @Override
    public int getPasswordMinLength() {
        LOGGER.atFine().log("Retrieved password min length: %d", passwordMinLength);
        return passwordMinLength;
    }

    /**
     * Gets the maximum password length.
     *
     * @return The maximum password length.
     */
    @Override
    public int getPasswordMaxLength() {
        LOGGER.atFine().log("Retrieved password max length: %d", passwordMaxLength);
        return passwordMaxLength;
    }

    /**
     * Gets the maximum number of password attempts.
     *
     * @return The number of attempts.
     */
    @Override
    public int getPasswordAttempts() {
        LOGGER.atFine().log("Retrieved password attempts: %d", passwordAttempts);
        return passwordAttempts;
    }

    /**
     * Gets the session durability in milliseconds.
     *
     * @return The session durability.
     */
    @Override
    public long getSessionDurability() {
        long result = sessionDurability.getMillis();
        LOGGER.atFine().log("Retrieved session durability: %d ms", result);
        return result;
    }

    /**
     * Gets the join delay in milliseconds.
     *
     * @return The join delay.
     */
    @Override
    public long getJoinDelay() {
        long result = joinDelay.getMillis();
        LOGGER.atFine().log("Retrieved join delay: %d ms", result);
        return result;
    }

    /**
     * Gets the authentication time in milliseconds.
     *
     * @return The authentication time.
     */
    @Override
    public long getAuthTime() {
        long result = authTime.getMillis();
        LOGGER.atFine().log("Retrieved auth time: %d ms", result);
        return result;
    }

    /**
     * Checks if chat should be blocked during authentication.
     *
     * @return {@code true} if chat is blocked, {@code false} otherwise.
     */
    @Override
    public boolean shouldBlockChat() {
        LOGGER.atFine().log("Retrieved block chat status: %b", blockChat);
        return blockChat;
    }

    /**
     * Gets the list of authentication servers.
     *
     * @return An unmodifiable list of {@link ConfigurationServer}.
     */
    @Override
    public List<ConfigurationServer> getAuthServers() {
        LOGGER.atFine().log("Retrieved auth servers: %d", authServers.size());
        return Collections.unmodifiableList(authServers);
    }

    /**
     * Gets the list of game servers.
     *
     * @return An unmodifiable list of {@link ConfigurationServer}.
     */
    @Override
    public List<ConfigurationServer> getGameServers() {
        LOGGER.atFine().log("Retrieved game servers: %d", gameServers.size());
        return Collections.unmodifiableList(gameServers);
    }

    /**
     * Gets the list of blocked servers.
     *
     * @return An unmodifiable list of {@link ConfigurationServer}.
     */
    @Override
    public List<ConfigurationServer> getBlockedServers() {
        LOGGER.atFine().log("Retrieved blocked servers: %d", blockedServers.size());
        return Collections.unmodifiableList(blockedServers);
    }

    /**
     * Gets the legacy storage data settings.
     *
     * @return The {@link LegacyStorageDataSettings}.
     */
    @Override
    public LegacyStorageDataSettings getStorageDataSettings() {
        LOGGER.atFine().log("Retrieved legacy storage data settings");
        return legacyStorageDataSettings;
    }

    /**
     * Gets the database configuration.
     *
     * @return The {@link BaseDatabaseConfiguration}.
     */
    @Override
    public BaseDatabaseConfiguration getDatabaseConfiguration() {
        LOGGER.atFine().log("Retrieved database configuration");
        return databaseConfiguration;
    }

    /**
     * Gets the server messages.
     *
     * @return The {@link ServerMessages}.
     */
    @Override
    public ServerMessages getServerMessages() {
        LOGGER.atFine().log("Retrieved server messages");
        return serverMessages;
    }

    /**
     * Gets the Telegram settings.
     *
     * @return The {@link TelegramSettings}.
     */
    @Override
    public TelegramSettings getTelegramSettings() {
        LOGGER.atFine().log("Retrieved Telegram settings");
        return telegramSettings;
    }

    /**
     * Gets the VK settings.
     *
     * @return The {@link VKSettings}.
     */
    @Override
    public VKSettings getVKSettings() {
        LOGGER.atFine().log("Retrieved VK settings");
        return vkSettings;
    }

    /**
     * Gets the Discord settings.
     *
     * @return The {@link DiscordSettings}.
     */
    @Override
    public DiscordSettings getDiscordSettings() {
        LOGGER.atFine().log("Retrieved Discord settings");
        return (DiscordSettings) discordSettings;
    }

    /**
     * Gets the maximum number of logins per IP.
     *
     * @return The maximum login count.
     */
    @Override
    public int getMaxLoginPerIP() {
        LOGGER.atFine().log("Retrieved max login per IP: %d", maxLoginPerIP);
        return maxLoginPerIP;
    }

    /**
     * Gets the delay between messages.
     *
     * @return The message delay in seconds.
     */
    @Override
    public int getMessagesDelay() {
        LOGGER.atFine().log("Retrieved messages delay: %d", messagesDelay);
        return messagesDelay;
    }

    /**
     * Gets the boss bar settings.
     *
     * @return The {@link BossBarSettings}.
     */
    @Override
    public BossBarSettings getBossBarSettings() {
        LOGGER.atFine().log("Retrieved boss bar settings");
        return barSettings;
    }

    /**
     * Gets the Google Authenticator settings.
     *
     * @return The {@link GoogleAuthenticatorSettings}.
     */
    @Override
    public GoogleAuthenticatorSettings getGoogleAuthenticatorSettings() {
        LOGGER.atFine().log("Retrieved Google Authenticator settings");
        return googleAuthenticatorSettings;
    }

    /**
     * Gets the list of allowed commands.
     *
     * @return An unmodifiable list of {@link Pattern}.
     */
    @Override
    public List<Pattern> getAllowedCommands() {
        LOGGER.atFine().log("Retrieved allowed commands: %d", allowedPatternCommands.size());
        return Collections.unmodifiableList(allowedPatternCommands);
    }

    /**
     * Gets the list of authentication steps.
     *
     * @return An unmodifiable list of step names.
     */
    @Override
    public List<String> getAuthenticationSteps() {
        LOGGER.atFine().log("Retrieved authentication steps: %d", authenticationSteps.size());
        return Collections.unmodifiableList(authenticationSteps);
    }

    /**
     * Gets the authentication step name at the specified index.
     *
     * @param index The index of the step.
     * @return The step name, or "NULL" if index is invalid.
     */
    @Override
    public String getAuthenticationStepName(int index) {
        String result = index >= 0 && index < authenticationSteps.size() ? authenticationSteps.get(index) : "NULL";
        LOGGER.atFine().log("Retrieved authentication step at index %d: %s", index, result);
        return result;
    }

    /**
     * Gets the range of limbo ports.
     *
     * @return The {@link IntStream} of port numbers.
     */
    @Override
    public IntStream getLimboPortRange() {
        LOGGER.atFine().log("Retrieved limbo port range");
        return limboPortRange;
    }
    // #endregion

    // #region Abstract Methods
    /**
     * Creates a configuration section for the plugin.
     *
     * @param plugin The AuthPlugin instance. Must not be null.
     * @return The configuration section.
     */
    protected abstract ConfigurationSectionHolder createConfiguration(AuthPlugin plugin);
    // #endregion
}