package com.httydcraft.authcraft.core;

import com.alessiodp.libby.classloader.IsolatedClassLoader;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.AuthPluginProvider;
import com.httydcraft.authcraft.api.account.AccountFactory;
import com.httydcraft.authcraft.api.bucket.*;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.config.duration.ConfigurationDuration;
import com.httydcraft.authcraft.api.config.server.ConfigurationServer;
import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.api.hook.PluginHook;
import com.httydcraft.authcraft.api.link.user.entry.LinkEntryUser;
import com.httydcraft.authcraft.api.management.LibraryManagement;
import com.httydcraft.authcraft.api.management.LoginManagement;
import com.httydcraft.authcraft.api.provider.LinkTypeProvider;
import com.httydcraft.authcraft.api.resource.Resource;
import com.httydcraft.authcraft.api.resource.impl.FolderResource;
import com.httydcraft.authcraft.api.resource.impl.FolderResourceReader;
import com.httydcraft.authcraft.api.server.CoreServer;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.core.account.factory.AuthAccountFactory;
import com.httydcraft.authcraft.core.bucket.*;
import com.httydcraft.authcraft.core.command.TelegramCommandRegistry;
import com.httydcraft.authcraft.core.config.BasePluginConfig;
import com.httydcraft.authcraft.core.config.factory.ConfigurationHolderMapResolverFactory;
import com.httydcraft.authcraft.core.config.resolver.RawURLProviderFieldResolverFactory;
import com.httydcraft.authcraft.core.config.resolver.ServerComponentFieldResolver;
import com.httydcraft.authcraft.core.config.server.BaseConfigurationServer;
import com.httydcraft.authcraft.core.crypto.Argon2CryptoProvider;
import com.httydcraft.authcraft.core.crypto.BcryptCryptoProvider;
import com.httydcraft.authcraft.core.crypto.MessageDigestCryptoProvider;
import com.httydcraft.authcraft.core.crypto.ScryptCryptoProvider;
import com.httydcraft.authcraft.core.crypto.authme.AuthMeSha256CryptoProvider;
import com.httydcraft.authcraft.core.crypto.belkaauth.UAuthCryptoProvider;
import com.httydcraft.authcraft.core.database.AuthAccountDatabaseProxy;
import com.httydcraft.authcraft.core.database.DatabaseHelper;
import com.httydcraft.authcraft.core.discord.command.DiscordCommandRegistry;
import com.httydcraft.authcraft.core.discord.listener.DiscordLinkRoleModifierListener;
import com.httydcraft.authcraft.core.hooks.BaseDiscordHook;
import com.httydcraft.authcraft.core.hooks.BaseTelegramPluginHook;
import com.httydcraft.authcraft.core.hooks.DiscordHook;
import com.httydcraft.authcraft.core.hooks.TelegramPluginHook;
import com.httydcraft.authcraft.core.link.BaseLinkTypeProvider;
import com.httydcraft.authcraft.core.listener.AuthenticationAttemptListener;
import com.httydcraft.authcraft.core.listener.AuthenticationChatPasswordListener;
import com.httydcraft.authcraft.core.management.BaseLibraryManagement;
import com.httydcraft.authcraft.core.management.BaseLoginManagement;
import com.httydcraft.authcraft.core.server.commands.ServerCommandsRegistry;
import com.httydcraft.authcraft.core.step.impl.*;
import com.httydcraft.authcraft.core.step.impl.link.DiscordLinkAuthenticationStep;
import com.httydcraft.authcraft.core.step.impl.link.GoogleCodeAuthenticationStep;
import com.httydcraft.authcraft.core.step.impl.link.TelegramLinkAuthenticationStep;
import com.httydcraft.authcraft.core.step.impl.link.VKLinkAuthenticationStep;
import com.httydcraft.authcraft.core.task.AuthenticationMessageSendTask;
import com.httydcraft.authcraft.core.task.AuthenticationProgressBarTask;
import com.httydcraft.authcraft.core.task.AuthenticationTimeoutTask;
import com.httydcraft.authcraft.core.util.HashUtils;
import com.httydcraft.authcraft.core.util.TimeUtils;
import com.httydcraft.configuration.ConfigurationProcessor;
import com.httydcraft.configuration.configurate.SpongeConfigurateProcessor;
import com.httydcraft.multimessenger.discord.message.DiscordMessage;
import com.httydcraft.multimessenger.discord.provider.DiscordApiProvider;
import com.httydcraft.multimessenger.telegram.message.TelegramMessage;
import com.httydcraft.multimessenger.telegram.providers.TelegramApiProvider;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.github.revxrsal.eventbus.EventBus;
import io.github.revxrsal.eventbus.EventBusBuilder;
import net.kyori.adventure.platform.AudienceProvider;
import ru.vyarus.yaml.updater.YamlUpdater;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

// #region Class Documentation
/**
 * Base class for the AuthCraft plugin.
 * Initializes core components and manages plugin lifecycle.
 */
public class BaseAuthPlugin implements AuthPlugin {

    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    private final ConfigurationProcessor configurationProcessor = new SpongeConfigurateProcessor();
    private final Map<Class<? extends PluginHook>, PluginHook> hooks = new HashMap<>();
    private final AuthenticationTaskBucket taskBucket = new BaseAuthenticationTaskBucket();
    private final LinkConfirmationBucket linkConfirmationBucket = new BaseLinkConfirmationBucket();
    private final LinkAuthenticationBucket<LinkEntryUser> linkEntryBucket = new BaseLinkAuthenticationBucket<>();
    private final AuthenticationStepFactoryBucket authenticationStepFactoryBucket = new BaseAuthenticationStepFactoryBucket();
    private final CryptoProviderBucket cryptoProviderBucket = new BaseCryptoProviderBucket();
    private final String version;
    private final File pluginFolder;
    private AuthenticationStepContextFactoryBucket authenticationStepContextFactoryBucket;
    private AudienceProvider audienceProvider;
    private LibraryManagement libraryManagement;
    private CoreServer core;
    private File dataFolder;
    private AuthenticatingAccountBucket accountBucket;
    private EventBus eventBus = EventBusBuilder.asm().executor(Executors.newFixedThreadPool(4)).build();
    private GoogleAuthenticator googleAuthenticator;
    private PluginConfig config;
    private AccountFactory accountFactory;
    private LinkTypeProvider linkTypeProvider;
    private AccountDatabase accountDatabase;
    private LoginManagement loginManagement;

    /**
     * Constructs the base plugin instance.
     *
     * @param audienceProvider Non-null audience provider
     * @param version Non-null plugin version
     * @param pluginFolder Non-null plugin directory
     * @param core Non-null server core implementation
     * @param libraryManagement Non-null library manager
     */
    public BaseAuthPlugin(AudienceProvider audienceProvider, String version, File pluginFolder, CoreServer core, LibraryManagement libraryManagement) {
        AuthPluginProvider.setPluginInstance(this);
        this.core = Preconditions.checkNotNull(core, "Core server implementation cannot be null");
        this.audienceProvider = Preconditions.checkNotNull(audienceProvider, "Audience provider cannot be null");
        this.version = Preconditions.checkNotNull(version, "Version cannot be null");
        this.pluginFolder = Preconditions.checkNotNull(pluginFolder, "Plugin folder cannot be null");
        this.libraryManagement = Preconditions.checkNotNull(libraryManagement, "Library management cannot be null");

        logger.atInfo().log("Initializing AuthCraft v%s", version);
        libraryManagement.loadLibraries();
        initializeBasic();
        if (config.getTelegramSettings().isEnabled())
            initializeTelegram();
        if (config.getDiscordSettings().isEnabled())
            initializeDiscord();
        if (config.getGoogleAuthenticatorSettings().isEnabled())
            googleAuthenticator = new GoogleAuthenticator();
        logger.atInfo().log("Completed core initialization");
    }

    /**
     * Initializes basic plugin subsystems.
     * <p>
     * Configures core components like configuration loading, database connections,
     * and event system setup.
     */
    private void initializeBasic() {
        logger.atFine().log("Initializing basic subsystems");
        this.accountBucket = new BaseAuthenticatingAccountBucket(this);

        this.registerCryptoProviders();
        this.registerConfigurationProcessor();
        this.config = new BasePluginConfig(this);
        if (config.isAutoMigrateConfigEnabled()) {
            try {
                this.migrateConfig();
            } catch (IOException | URISyntaxException e) {
                logger.atSevere().withCause(e).log("Failed to migrate configuration");
            }
        }

        this.authenticationStepContextFactoryBucket = new BaseAuthenticationStepContextFactoryBucket(config.getAuthenticationSteps());
        this.accountFactory = new AuthAccountFactory();
        this.linkTypeProvider = BaseLinkTypeProvider.allLinks();
        this.accountDatabase = new AuthAccountDatabaseProxy(new DatabaseHelper(this, new IsolatedClassLoader()));
        this.loginManagement = new BaseLoginManagement(this);

        this.registerAuthenticationSteps();

        this.registerTasks();

        this.eventBus.register(new AuthenticationAttemptListener(this));
        this.eventBus.register(new AuthenticationChatPasswordListener(this));
    }

    /**
     * Registers the crypto providers.
     */
    private void registerCryptoProviders() {
        logger.atFine().log("Registering crypto providers");
        this.cryptoProviderBucket.modifiable().add(new BcryptCryptoProvider());
        this.cryptoProviderBucket.modifiable().add(new MessageDigestCryptoProvider("SHA256", HashUtils.getSHA256()));
        this.cryptoProviderBucket.modifiable().add(new MessageDigestCryptoProvider("MD5", HashUtils.getMD5()));
        this.cryptoProviderBucket.modifiable().add(new Argon2CryptoProvider());
        this.cryptoProviderBucket.modifiable().add(new ScryptCryptoProvider());

        this.cryptoProviderBucket.modifiable().add(new AuthMeSha256CryptoProvider());
        this.cryptoProviderBucket.modifiable().add(new UAuthCryptoProvider());
    }

    /**
     * Registers the authentication steps.
     */
    private void registerAuthenticationSteps() {
        logger.atFine().log("Registering authentication steps");
        this.authenticationStepFactoryBucket.modifiable().add(new NullAuthenticationStep.NullAuthenticationStepFactory());
        this.authenticationStepFactoryBucket.modifiable().add(new LoginAuthenticationStep.LoginAuthenticationStepFactory());
        this.authenticationStepFactoryBucket.modifiable().add(new RegisterAuthenticationStep.RegisterAuthenticationStepFactory());
        this.authenticationStepFactoryBucket.modifiable().add(new VKLinkAuthenticationStep.VKLinkAuthenticationStepFactory());
        this.authenticationStepFactoryBucket.modifiable().add(new GoogleCodeAuthenticationStep.GoogleLinkAuthenticationStepFactory());
        this.authenticationStepFactoryBucket.modifiable().add(new TelegramLinkAuthenticationStep.TelegramLinkAuthenticationStepFactory());
        this.authenticationStepFactoryBucket.modifiable().add(new DiscordLinkAuthenticationStep.DiscordLinkAuthenticationStepFactory());
        this.authenticationStepFactoryBucket.modifiable().add(new EnterServerAuthenticationStep.EnterServerAuthenticationStepFactory());
        this.authenticationStepFactoryBucket.modifiable().add(new EnterAuthServerAuthenticationStep.EnterAuthServerAuthenticationStepFactory());
    }

    /**
     * Registers the tasks.
     */
    private void registerTasks() {
        logger.atFine().log("Registering tasks");
        this.taskBucket.modifiable().add(new AuthenticationTimeoutTask(this));
        this.taskBucket.modifiable().add(new AuthenticationProgressBarTask(this));
        this.taskBucket.modifiable().add(new AuthenticationMessageSendTask(this));
    }

    /**
     * Initializes the Telegram plugin.
     */
    private void initializeTelegram() {
        logger.atFine().log("Initializing Telegram plugin");
        hooks.put(TelegramPluginHook.class, new BaseTelegramPluginHook());

        TelegramMessage.setDefaultApiProvider(TelegramApiProvider.of(getHook(TelegramPluginHook.class).getTelegramBot()));

        new TelegramCommandRegistry();
    }

    /**
     * Initializes the Discord plugin.
     */
    private void initializeDiscord() {
        logger.atFine().log("Initializing Discord plugin");
        libraryManagement.loadLibrary(BaseLibraryManagement.JDA_LIBRARY);

        BaseDiscordHook discordHook = new BaseDiscordHook();
        hooks.put(DiscordHook.class, discordHook);

        discordHook.initialize().thenAccept(jda -> {
            DiscordMessage.setDefaultApiProvider(DiscordApiProvider.of(jda));

            eventBus.register(new DiscordLinkRoleModifierListener());
            new DiscordCommandRegistry();
        }).exceptionally(throwable -> {
            logger.atSevere().withCause(throwable).log("Failed to initialize Discord plugin");
            return null;
        });
    }

    /**
     * Migrates the configuration.
     *
     * @throws IOException if an I/O error occurs
     * @throws URISyntaxException if the URI syntax is incorrect
     */
    private void migrateConfig() throws IOException, URISyntaxException {
        logger.atFine().log("Migrating configuration");
        FolderResource folderResource = new FolderResourceReader(getClass().getClassLoader(), "configurations").read();
        for (Resource resource : folderResource.getResources()) {
            String realConfigurationName = resource.getName().substring(folderResource.getName().length() + 1);
            File resourceConfiguration = new File(getFolder(), realConfigurationName);
            if (!resourceConfiguration.exists())
                continue;
            YamlUpdater.create(resourceConfiguration, resource.getStream()).backup(true).update();
        }
    }

    /**
     * Registers the configuration processor.
     */
    private void registerConfigurationProcessor() {
        logger.atFine().log("Registering configuration processor");
        configurationProcessor.registerFieldResolver(ConfigurationServer.class, (context) -> new BaseConfigurationServer(context.getString()))
                .registerFieldResolver(ConfigurationDuration.class, (context) -> new ConfigurationDuration(TimeUtils.parseDuration(context.getString("1s"))))
                .registerFieldResolverFactory(ConfigurationHolderMapResolverFactory.ConfigurationHolderMap.class, new ConfigurationHolderMapResolverFactory())
                .registerFieldResolver(CryptoProvider.class,
                        (context) -> cryptoProviderBucket.findFirstByValue(CryptoProvider::getIdentifier, context.getString())
                                .orElseThrow(() -> new IllegalArgumentException("Cannot find CryptoProvider with name " + context.getString())))
                .registerFieldResolver(ServerComponent.class, new ServerComponentFieldResolver())
                .registerFieldResolverFactory(RawURLProviderFieldResolverFactory.RawURLProvider.class, new RawURLProviderFieldResolverFactory())
                .registerFieldResolver(SimpleDateFormat.class, context -> new SimpleDateFormat(context.getString("mm:ss")))
                .registerFieldResolver(File.class, (context) -> {
                    String path = context.getString("");
                    if (path.isEmpty())
                        return null;
                    return new File(path.replace("%plugin_folder%", getFolder().getAbsolutePath()));
                })
                .registerFieldResolver(IntStream.class, (context) -> {
                    String number = context.getString("");
                    if (number.isEmpty())
                        return IntStream.of(0);
                    if (number.contains("-")) {
                        String[] range = number.split("-");
                        return IntStream.range(Integer.parseInt(range[0]), Integer.parseInt(range[1]));
                    }
                    return IntStream.of(Integer.parseInt(number));
                });
    }

    /**
     * Retrieves the server core implementation.
     *
     * @return Non-null server core instance
     */
    public CoreServer getCore() {
        return core;
    }

    /**
     * Retrieves the audience provider.
     *
     * @return Non-null audience provider
     */
    public AudienceProvider getAudienceProvider() {
        return audienceProvider;
    }

    /**
     * Retrieves the plugin configuration.
     *
     * @return Non-null plugin configuration
     */
    public PluginConfig getConfig() {
        return config;
    }

    /**
     * Retrieves the account factory.
     *
     * @return Non-null account factory
     */
    public AccountFactory getAccountFactory() {
        return accountFactory;
    }

    /**
     * Retrieves the account database.
     *
     * @return Non-null account database
     */
    public AccountDatabase getAccountDatabase() {
        return accountDatabase;
    }

    /**
     * Retrieves the Google Authenticator instance.
     *
     * @return Non-null Google Authenticator instance
     */
    public GoogleAuthenticator getGoogleAuthenticator() {
        return googleAuthenticator;
    }

    /**
     * Retrieves the authentication step factory bucket.
     *
     * @return Non-null authentication step factory bucket
     */
    public AuthenticationStepFactoryBucket getAuthenticationStepFactoryBucket() {
        return authenticationStepFactoryBucket;
    }

    /**
     * Retrieves the authentication context factory bucket.
     *
     * @return Non-null authentication context factory bucket
     */
    public AuthenticationStepContextFactoryBucket getAuthenticationContextFactoryBucket() {
        return authenticationStepContextFactoryBucket;
    }

    /**
     * Retrieves the configuration processor.
     *
     * @return Non-null configuration processor
     */
    public ConfigurationProcessor getConfigurationProcessor() {
        return configurationProcessor;
    }

    /**
     * Retrieves the login management.
     *
     * @return Non-null login management
     */
    public LoginManagement getLoginManagement() {
        return loginManagement;
    }

    /**
     * Sets the login management.
     *
     * @param loginManagement Non-null login management
     * @return This instance
     */
    public AuthPlugin setLoginManagement(LoginManagement loginManagement) {
        this.loginManagement = Preconditions.checkNotNull(loginManagement, "Login management cannot be null");
        return this;
    }

    /**
     * Retrieves the link type provider.
     *
     * @return Non-null link type provider
     */
    public LinkTypeProvider getLinkTypeProvider() {
        return linkTypeProvider;
    }

    /**
     * Retrieves the event bus.
     *
     * @return Non-null event bus
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Sets the event bus.
     *
     * @param eventBus Non-null event bus
     * @return This instance
     */
    public AuthPlugin setEventBus(EventBus eventBus) {
        this.eventBus = Preconditions.checkNotNull(eventBus, "Event bus cannot be null");
        return this;
    }

    /**
     * Retrieves the authentication task bucket.
     *
     * @return Non-null authentication task bucket
     */
    public AuthenticationTaskBucket getAuthenticationTaskBucket() {
        return taskBucket;
    }

    /**
     * Retrieves the authenticating account bucket.
     *
     * @return Non-null authenticating account bucket
     */
    public AuthenticatingAccountBucket getAuthenticatingAccountBucket() {
        return accountBucket;
    }

    /**
     * Retrieves the link confirmation bucket.
     *
     * @return Non-null link confirmation bucket
     */
    public LinkConfirmationBucket getLinkConfirmationBucket() {
        return linkConfirmationBucket;
    }

    /**
     * Retrieves the link entry bucket.
     *
     * @return Non-null link entry bucket
     */
    public LinkAuthenticationBucket<LinkEntryUser> getLinkEntryBucket() {
        return linkEntryBucket;
    }

    /**
     * Retrieves the crypto provider bucket.
     *
     * @return Non-null crypto provider bucket
     */
    public CryptoProviderBucket getCryptoProviderBucket() {
        return cryptoProviderBucket;
    }

    /**
     * Retrieves the library management.
     *
     * @return Non-null library management
     */
    public LibraryManagement getLibraryManagement() {
        return libraryManagement;
    }

    /**
     * Retrieves a plugin hook.
     *
     * @param clazz Non-null hook class
     * @param <T> Hook type
     * @return Non-null hook instance or null if not found
     */
    public <T extends PluginHook> T getHook(Class<T> clazz) {
        PluginHook hook = hooks.get(clazz);
        if (hook == null)
            return null;
        return hook.as(clazz);
    }

    /**
     * Retrieves the plugin version.
     *
     * @return Non-null plugin version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Retrieves the plugin folder.
     *
     * @return Non-null plugin folder
     */
    public File getFolder() {
        return pluginFolder;
    }

    /**
     * Puts a plugin hook.
     *
     * @param clazz Non-null hook class
     * @param instance Non-null hook instance
     * @param <T> Hook type
     */
    public <T extends PluginHook> void putHook(Class<? extends T> clazz, T instance) {
        hooks.put(clazz, instance);
    }

    /**
     * Sets the event bus.
     *
     * @param eventBus Non-null event bus
     * @return This instance
     */
    public BaseAuthPlugin eventBus(EventBus eventBus) {
        this.eventBus = Preconditions.checkNotNull(eventBus, "Event bus cannot be null");
        return this;
    }


}
