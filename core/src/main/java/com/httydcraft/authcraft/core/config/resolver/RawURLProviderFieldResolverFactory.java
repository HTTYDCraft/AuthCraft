package com.httydcraft.authcraft.core.config.resolver;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.configuration.context.ConfigurationFieldFactoryContext;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.configuration.resolver.field.ConfigurationFieldResolver;
import com.httydcraft.configuration.resolver.field.ConfigurationFieldResolverFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// #region Class Documentation
/**
 * Factory for resolving raw URL provider fields.
 * Supports SQL connection URLs and custom string replacements.
 */
public class RawURLProviderFieldResolverFactory implements ConfigurationFieldResolverFactory {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    private static final String REMOTE_URI_TEMPLATE = "jdbc:%s://%s:%s/%s";
    private static final String PLUGIN_FOLDER_PLACEHOLDER_KEY = "%plugin_folder%";
    public static final String DATABASE_TYPE_KEY = "type";
    public static final String HOST_KEY = "host";
    public static final String PORT_KEY = "port";
    public static final String DATABASE_KEY = "database";
    // #endregion

    // #region Resolver Creation
    /**
     * Creates a resolver for configuration fields.
     *
     * @param context The factory context. Must not be null.
     * @return A {@link ConfigurationFieldResolver} for raw URL providers.
     */
    @Override
    public ConfigurationFieldResolver<?> createResolver(ConfigurationFieldFactoryContext context) {
        Preconditions.checkNotNull(context, "context must not be null");
        LOGGER.atFine().log("Creating resolver for raw URL provider");

        if (context.hasAnnotation(SqlConnectionUrl.class) && context.isSection()) {
            return (ignored) -> {
                ConfigurationSectionHolder section = context.getSection();
                String url = String.format(REMOTE_URI_TEMPLATE,
                                section.getString(DATABASE_TYPE_KEY),
                                section.getString(HOST_KEY),
                                section.getString(PORT_KEY),
                                section.getString(DATABASE_KEY))
                        .replace(PLUGIN_FOLDER_PLACEHOLDER_KEY, PLUGIN.getFolder().getAbsolutePath());
                LOGGER.atFine().log("Resolved SQL connection URL: %s", url);
                return RawURLProvider.of(url);
            };
        }
        return (ignored) -> {
            String url = context.getString().replace(PLUGIN_FOLDER_PLACEHOLDER_KEY, PLUGIN.getFolder().getAbsolutePath());
            LOGGER.atFine().log("Resolved custom URL: %s", url);
            return RawURLProvider.of(url);
        };
    }
    // #endregion

    // #region RawURLProvider Interface
    /**
     * Interface for providing raw URLs.
     */
    public interface RawURLProvider {
        /**
         * Gets the URL.
         *
         * @return The URL string.
         */
        String url();

        /**
         * Creates a new raw URL provider.
         *
         * @param url The URL string. Must not be null.
         * @return A {@link RawURLProvider} instance.
         */
        static RawURLProvider of(String url) {
            Preconditions.checkNotNull(url, "url must not be null");
            return () -> url;
        }
    }
    // #endregion

    // #region Annotation
    /**
     * Annotation for marking fields that represent SQL connection URLs.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SqlConnectionUrl {
    }
    // #endregion
}