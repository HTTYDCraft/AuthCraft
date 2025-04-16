package com.httydcraft.authcraft.core.config.message.link.context;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.core.config.message.context.account.BaseAccountPlaceholderContext;
import com.httydcraft.authcraft.core.config.message.context.placeholder.PlaceholderProvider;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;

import java.util.Optional;

// #region Class Documentation
/**
 * Context for link-specific placeholders.
 * Extends account placeholder context with link user information.
 */
public class LinkPlaceholderContext extends BaseAccountPlaceholderContext {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private LinkUser linkUser;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code LinkPlaceholderContext}.
     *
     * @param account   The account to provide placeholders for. Must not be null.
     * @param linkType  The link type. Must not be null.
     * @param linkName  The link name for placeholders. Must not be null.
     */
    public LinkPlaceholderContext(Account account, LinkType linkType, String linkName) {
        super(Preconditions.checkNotNull(account, "account must not be null"));
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(linkName, "linkName must not be null");

        linkUser = account.findFirstLinkUser((user) -> user.getLinkType().equals(linkType)).orElse(null);
        if (linkUser != null) {
            registerPlaceholderProvider(PlaceholderProvider.of(
                    linkUser.getLinkUserInfo().getIdentificator().asString(),
                    "%" + linkName + "_id%"));
            LOGGER.atFine().log("Registered link user placeholder for %s_id", linkName);
        }
        LOGGER.atInfo().log("Initialized LinkPlaceholderContext for link: %s", linkName);
    }
    // #endregion

    // #region Getter
    /**
     * Gets the associated link user, if present.
     *
     * @return An {@link Optional} containing the link user, or empty if none.
     */
    public Optional<LinkUser> getLinkUser() {
        return Optional.ofNullable(linkUser);
    }
    // #endregion
}