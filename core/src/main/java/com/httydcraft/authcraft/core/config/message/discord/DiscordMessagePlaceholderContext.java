package com.httydcraft.authcraft.core.config.message.discord;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.core.config.message.link.context.LinkPlaceholderContext;
import com.httydcraft.authcraft.core.link.discord.DiscordLinkType;

// #region Class Documentation
/**
 * Context for Discord-specific message placeholders.
 * Extends link placeholder context with Discord-specific settings.
 */
public class DiscordMessagePlaceholderContext extends LinkPlaceholderContext {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code DiscordMessagePlaceholderContext}.
     *
     * @param account The account to provide placeholders for. Must not be null.
     */
    public DiscordMessagePlaceholderContext(Account account) {
        super(Preconditions.checkNotNull(account, "account must not be null"),
                DiscordLinkType.getInstance(), "discord");
        LOGGER.atFine().log("Initialized DiscordMessagePlaceholderContext for account: %s", account.getName());
    }
    // #endregion
}