package com.httydcraft.authcraft.core.link;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Maps;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.core.link.discord.DiscordLinkType;
import com.httydcraft.authcraft.core.link.google.GoogleLinkType;
import com.httydcraft.authcraft.core.link.telegram.TelegramLinkType;
import com.httydcraft.authcraft.core.link.vk.VKLinkType;
import com.httydcraft.authcraft.api.provider.LinkTypeProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// #region Class Documentation
/**
 * Base implementation of {@link LinkTypeProvider}.
 * Manages a collection of link types for different platforms.
 */
public class BaseLinkTypeProvider implements LinkTypeProvider {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final Map<String, LinkType> linkTypeMap = new HashMap<>();
    // #endregion

    // #region Factory Method
    /**
     * Creates a provider with all supported link types.
     *
     * @return A configured {@link BaseLinkTypeProvider}.
     */
    public static BaseLinkTypeProvider allLinks() {
        BaseLinkTypeProvider provider = new BaseLinkTypeProvider();
        provider.putLinkType(GoogleLinkType.getInstance());
        provider.putLinkType(VKLinkType.getInstance());
        provider.putLinkType(TelegramLinkType.getInstance());
        provider.putLinkType(DiscordLinkType.getInstance());
        LOGGER.atInfo().log("Created BaseLinkTypeProvider with all link types");
        return provider;
    }
    // #endregion

    // #region Link Type Management
    /**
     * Adds a link type to the provider.
     *
     * @param linkType The link type to add. Must not be null.
     */
    @Override
    public void putLinkType(LinkType linkType) {
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        putLinkType(linkType.getName(), linkType);
    }

    /**
     * Adds a link type with a specific name to the provider.
     *
     * @param name     The name of the link type. Must not be null.
     * @param linkType The link type to add. Must not be null.
     */
    @Override
    public void putLinkType(String name, LinkType linkType) {
        Preconditions.checkNotNull(name, "name must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        linkTypeMap.put(name, linkType);
        LOGGER.atFine().log("Added link type: %s", name);
    }

    /**
     * Gets a link type by name.
     *
     * @param name The name of the link type. Must not be null.
     * @return An {@link Optional} containing the {@link LinkType}, or empty if not found.
     */
    @Override
    public Optional<LinkType> getLinkType(String name) {
        Preconditions.checkNotNull(name, "name must not be null");
        LinkType linkType = linkTypeMap.get(name);
        LOGGER.atFine().log("Retrieved link type: %s, found: %b", name, linkType != null);
        return Optional.ofNullable(linkType);
    }

    /**
     * Gets all registered link types.
     *
     * @return An immutable collection of {@link LinkType}s.
     */
    @Override
    public Collection<LinkType> getLinkTypes() {
        LOGGER.atFine().log("Retrieved %d link types");
        return linkTypeMap.values();
    }

    // #endregion
}