package com.httydcraft.authcraft.core.config.message.telegram;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.core.config.message.context.placeholder.PlaceholderProvider;
import com.httydcraft.authcraft.core.config.message.link.context.LinkPlaceholderContext;
import com.httydcraft.authcraft.core.link.telegram.TelegramLinkType;

// #region Class Documentation
/**
 * Context for Telegram-specific message placeholders.
 * Extends link placeholder context with Telegram-specific settings.
 */
public class TelegramMessagePlaceholderContext extends LinkPlaceholderContext {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code TelegramMessagePlaceholderContext}.
     *
     * @param account The account to provide placeholders for. Must not be null.
     */
    public TelegramMessagePlaceholderContext(Account account) {
        super(Preconditions.checkNotNull(account, "account must not be null"),
                TelegramLinkType.getInstance(), "telegram");
        getLinkUser().ifPresent(linkUser -> {
            if (linkUser.isIdentifierDefaultOrNull() || !linkUser.getLinkUserInfo().getIdentificator().isNumber()) {
                LOGGER.atFine().log("Skipping Telegram ID placeholder: invalid identifier");
                return;
            }
            registerPlaceholderProvider(PlaceholderProvider.of(
                    Long.toString(linkUser.getLinkUserInfo().getIdentificator().asNumber()),
                    "%telegram_id%"));
            LOGGER.atFine().log("Registered Telegram ID placeholder");
        });
        LOGGER.atInfo().log("Initialized TelegramMessagePlaceholderContext for account: %s", account.getName());
    }
    // #endregion
}