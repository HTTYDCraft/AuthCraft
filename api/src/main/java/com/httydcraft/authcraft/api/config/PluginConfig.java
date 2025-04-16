package com.httydcraft.authcraft.api.config;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import com.httydcraft.authcraft.api.config.bossbar.BossBarSettings;
import com.httydcraft.authcraft.api.config.database.DatabaseSettings;
import com.httydcraft.authcraft.api.config.database.LegacyStorageDataSettings;
import com.httydcraft.authcraft.api.config.link.DiscordSettings;
import com.httydcraft.authcraft.api.config.link.GoogleAuthenticatorSettings;
import com.httydcraft.authcraft.api.config.link.TelegramSettings;
import com.httydcraft.authcraft.api.config.link.VKSettings;
import com.httydcraft.authcraft.api.config.message.server.ServerMessages;
import com.httydcraft.authcraft.api.config.server.ConfigurationServer;
import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.httydcraft.authcraft.api.database.DatabaseConnectionProvider;
import com.httydcraft.authcraft.api.type.IdentifierType;

public interface PluginConfig {
    @Deprecated
    LegacyStorageDataSettings getStorageDataSettings();

    DatabaseSettings getDatabaseConfiguration();

    IdentifierType getActiveIdentifierType();

    boolean isNameCaseCheckEnabled();

    boolean isAutoMigrateConfigEnabled();

    CryptoProvider getActiveHashType();

    DatabaseConnectionProvider getStorageType();

    Pattern getNamePattern();

    List<ConfigurationServer> getAuthServers();

    List<ConfigurationServer> getGameServers();

    List<ConfigurationServer> getBlockedServers();

    List<Pattern> getAllowedCommands();

    IntStream getLimboPortRange();

    List<String> getAuthenticationSteps();

    String getAuthenticationStepName(int index);

    boolean isPasswordConfirmationEnabled();

    boolean isPasswordInChatEnabled();

    int getPasswordMinLength();

    int getPasswordMaxLength();

    int getPasswordAttempts();

    int getMaxLoginPerIP();

    int getMessagesDelay();

    long getSessionDurability();

    long getJoinDelay();

    long getAuthTime();

    boolean shouldBlockChat();

    ServerMessages getServerMessages();

    BossBarSettings getBossBarSettings();

    ConfigurationServer findServerInfo(List<ConfigurationServer> servers);

    void reload();

    GoogleAuthenticatorSettings getGoogleAuthenticatorSettings();

    TelegramSettings getTelegramSettings();

    VKSettings getVKSettings();

    DiscordSettings getDiscordSettings();
}