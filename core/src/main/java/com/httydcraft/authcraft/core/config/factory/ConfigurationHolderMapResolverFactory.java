package com.httydcraft.authcraft.core.config.factory;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.configuration.context.ConfigurationFieldFactoryContext;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.configuration.resolver.field.ConfigurationFieldResolver;
import com.httydcraft.configuration.resolver.field.ConfigurationFieldResolverFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// #region Class Documentation
/**
 * Factory for resolving configuration fields into a map of configuration holders.
 * Supports dynamic instantiation of configuration holders based on constructor signatures.
 */
public class ConfigurationHolderMapResolverFactory implements ConfigurationFieldResolverFactory {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Resolver Creation
    /**
     * Creates a resolver for configuration fields.
     *
     * @param factoryContext The factory context. Must not be null.
     * @return A {@link ConfigurationFieldResolver} for mapping configuration holders.
     */
    @Override
    public ConfigurationFieldResolver<?> createResolver(ConfigurationFieldFactoryContext factoryContext) {
        Preconditions.checkNotNull(factoryContext, "factoryContext must not be null");
        LOGGER.atFine().log("Creating resolver for configuration field");

        return (context) -> {
            ConfigurationSectionHolder rootSectionHolder = context.path()[0].equals("self") ? context.configuration() : context.getSection();
            Map<String, Object> map = rootSectionHolder.keys().stream()
                    .collect(Collectors.toMap(Function.identity(), (key) -> {
                        ConfigurationSectionHolder sectionHolder = rootSectionHolder.section(key);
                        return newConfigurationHolder(context.getGeneric(0), key, sectionHolder);
                    }));
            ConfigurationHolderMap<?> holderMap = new ConfigurationHolderMap<>(map);
            LOGGER.atFine().log("Resolved %d configuration holders", map.size());
            return holderMap;
        };
    }
    // #endregion

    // #region Helper Methods
    /**
     * Creates a new configuration holder instance.
     *
     * @param clazz         The class to instantiate. Must not be null.
     * @param key           The configuration key.
     * @param sectionHolder The configuration section holder. Must not be null.
     * @return The instantiated configuration holder.
     * @throws IllegalArgumentException if no valid constructor is found.
     */
    private Object newConfigurationHolder(Class<?> clazz, String key, ConfigurationSectionHolder sectionHolder) {
        Preconditions.checkNotNull(clazz, "clazz must not be null");
        Preconditions.checkNotNull(sectionHolder, "sectionHolder must not be null");

        LOGGER.atFine().log("Creating configuration holder for class: %s, key: %s", clazz.getSimpleName(), key);
        try {
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (constructor.getParameterCount() == 1 && ConfigurationSectionHolder.class.isAssignableFrom(constructor.getParameterTypes()[0])) {
                    return constructor.newInstance(sectionHolder);
                }
                if (constructor.getParameterCount() >= 2 && String.class == constructor.getParameterTypes()[0] &&
                        ConfigurationSectionHolder.class.isAssignableFrom(constructor.getParameterTypes()[1])) {
                    return constructor.newInstance(key, sectionHolder);
                }
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.atSevere().withCause(e).log("Failed to create configuration holder for class: %s", clazz.getSimpleName());
        }
        throw new IllegalArgumentException("Cannot create class " + clazz.getSimpleName() + " because it doesn`t have valid constructor");
    }
    // #endregion

    // #region Configuration Holder Map
    /**
     * A map implementation for holding configuration holders.
     *
     * @param <V> The type of the configuration holder.
     */
    public static class ConfigurationHolderMap<V> extends HashMap<String, V> {
        /**
         * Default constructor.
         */
        public ConfigurationHolderMap() {
        }

        /**
         * Constructs a new map with the given entries.
         *
         * @param map The map to copy. Must not be null.
         */
        public ConfigurationHolderMap(Map<String, V> map) {
            super(Preconditions.checkNotNull(map, "map must not be null"));
            LOGGER.atFine().log("Initialized ConfigurationHolderMap with %d entries", map.size());
        }
    }
    // #endregion
}