package com.httydcraft.authcraft.api;

import java.io.File;

import com.httydcraft.authcraft.api.account.AccountFactory;
import com.httydcraft.authcraft.api.bucket.AuthenticatingAccountBucket;
import com.httydcraft.authcraft.api.bucket.AuthenticationStepContextFactoryBucket;
import com.httydcraft.authcraft.api.bucket.AuthenticationStepFactoryBucket;
import com.httydcraft.authcraft.api.bucket.AuthenticationTaskBucket;
import com.httydcraft.authcraft.api.bucket.CryptoProviderBucket;
import com.httydcraft.authcraft.api.bucket.LinkAuthenticationBucket;
import com.httydcraft.authcraft.api.bucket.LinkConfirmationBucket;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.hook.PluginHook;
import com.httydcraft.authcraft.api.link.user.entry.LinkEntryUser;
import com.httydcraft.authcraft.api.management.LibraryManagement;
import com.httydcraft.authcraft.api.management.LoginManagement;
import com.httydcraft.authcraft.api.provider.LinkTypeProvider;
import com.httydcraft.authcraft.api.server.CoreServer;
import com.httydcraft.authcraft.api.util.Castable;
import com.httydcraft.configuration.ConfigurationProcessor;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import io.github.revxrsal.eventbus.EventBus;
import net.kyori.adventure.platform.AudienceProvider;

public interface AuthPlugin extends Castable<AuthPlugin> {
    static AuthPlugin instance() {
        return AuthPluginProvider.getPluginInstance();
    }

    CoreServer getCore();

    AudienceProvider getAudienceProvider();

    PluginConfig getConfig();

    AccountFactory getAccountFactory();

    AccountDatabase getAccountDatabase();

    GoogleAuthenticator getGoogleAuthenticator();

    AuthenticationStepFactoryBucket getAuthenticationStepFactoryBucket();

    AuthenticationStepContextFactoryBucket getAuthenticationContextFactoryBucket();

    ConfigurationProcessor getConfigurationProcessor();

    LoginManagement getLoginManagement();

    AuthPlugin setLoginManagement(LoginManagement loginManagement);

    LinkTypeProvider getLinkTypeProvider();

    EventBus getEventBus();

    AuthPlugin setEventBus(EventBus eventBus);

    AuthenticationTaskBucket getAuthenticationTaskBucket();

    AuthenticatingAccountBucket getAuthenticatingAccountBucket();

    LinkConfirmationBucket getLinkConfirmationBucket();

    LinkAuthenticationBucket<LinkEntryUser> getLinkEntryBucket();

    CryptoProviderBucket getCryptoProviderBucket();

    LibraryManagement getLibraryManagement();

    <T extends PluginHook> T getHook(Class<T> clazz);

    String getVersion();

    /**
     * Returns folder of plugin in plugins. For example:
     * some/path/plugins/PluginName
     *
     * @return Plugin folder.
     */
    File getFolder();
}
