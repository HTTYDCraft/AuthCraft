package com.httydcraft.authcraft.core.config.resolver;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.configuration.context.ConfigurationFieldResolverContext;
import com.httydcraft.configuration.holder.ConfigurationSectionHolder;
import com.httydcraft.configuration.resolver.field.ConfigurationFieldResolver;

import java.util.Arrays;

// #region Class Documentation
/**
 * Resolver for server component fields.
 * Supports deserialization of JSON, legacy, and plain components.
 */
public class ServerComponentFieldResolver implements ConfigurationFieldResolver<ServerComponent> {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Field Resolution
    /**
     * Resolves a server component from a configuration context.
     *
     * @param context The resolver context. Must not be null.
     * @return The deserialized {@link ServerComponent}.
     * @throws IllegalArgumentException if the component type is invalid.
     */
    @Override
    public ServerComponent resolveField(ConfigurationFieldResolverContext context) {
        Preconditions.checkNotNull(context, "context must not be null");
        if (context.isSection()) {
            ConfigurationSectionHolder sectionHolder = context.getSection();
            String componentType = sectionHolder.getString("type");
            LOGGER.atFine().log("Resolving component of type: %s", componentType);
            switch (componentType) {
                case "json":
                    return ServerComponent.fromJson(sectionHolder.getString("value"));
                case "legacy":
                    return ServerComponent.fromLegacy(sectionHolder.getString("value"));
                case "plain":
                    return ServerComponent.fromPlain(sectionHolder.getString("value"));
                default:
                    LOGGER.atSevere().log("Invalid component type: %s at path: %s", componentType, Arrays.toString(context.path()));
                    throw new IllegalArgumentException(
                            "Illegal component type in " + Arrays.toString(context.path()) + ":" + componentType + ", available: json,legacy,plain");
            }
        }
        ServerComponent component = ServerComponent.fromLegacy(context.getString());
        LOGGER.atFine().log("Resolved legacy component from string");
        return component;
    }
    // #endregion
}