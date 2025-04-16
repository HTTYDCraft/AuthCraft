package com.httydcraft.authcraft.core.command;

import com.httydcraft.lamp.telegram.core.TelegramHandler;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.core.commands.MessengerCommandRegistry;
import com.httydcraft.authcraft.core.commands.MessengerLinkCommandTemplate;
import com.httydcraft.authcraft.core.commands.TelegramLinkCommand;
import com.httydcraft.authcraft.core.hooks.TelegramPluginHook;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.core.link.telegram.TelegramCommandActorWrapper;
import com.httydcraft.authcraft.core.link.telegram.TelegramLinkType;
import revxrsal.commands.CommandHandler;

// #region Class Documentation
/**
 * Registry for Telegram commands, extending {@link MessengerCommandRegistry}.
 * Initializes the Telegram bot, registers commands, and sets up the updates listener.
 */
public class TelegramCommandRegistry extends MessengerCommandRegistry {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    private static final TelegramPluginHook TELEGRAM_HOOK = PLUGIN.getHook(TelegramPluginHook.class);
    private static final CommandHandler COMMAND_HANDLER = new TelegramHandler(TELEGRAM_HOOK.getTelegramBot());
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code TelegramCommandRegistry} and initializes the Telegram bot.
     */
    public TelegramCommandRegistry() {
        super(COMMAND_HANDLER, TelegramLinkType.getInstance());
        LOGGER.atInfo().log("Initializing TelegramCommandRegistry");

        COMMAND_HANDLER.registerContextResolver(LinkCommandActorWrapper.class, context -> {
            Preconditions.checkNotNull(context, "context must not be null");
            return new TelegramCommandActorWrapper(context.actor());
        });
        registerCommands();
        startBot();
    }
    // #endregion

    // #region Bot Initialization
    /**
     * Starts the Telegram bot and sets up the updates listener.
     */
    private void startBot() {
        LOGGER.atInfo().log("Starting Telegram bot");
        TELEGRAM_HOOK.getTelegramBot().setUpdatesListener(new TelegramCommandUpdatesListener(), exception -> {
            Preconditions.checkNotNull(exception, "exception must not be null");
            LOGGER.atSevere().withCause(exception).log("Error in Telegram bot updates listener");

            if (exception.response() == null) {
                return;
            }
            if (exception.response().errorCode() == 409) {
                TELEGRAM_HOOK.getTelegramBot().removeGetUpdatesListener();
                LOGGER.atSevere().log("Telegram bot disabled due to multiple bot instances (error 409)");
                System.err.println("Telegram bot disabled because you are already running another bot instance!");
                System.err.println("Please use another token if you need to run multiple bot instances");
            }
        });
    }
    // #endregion

    // #region Command Creation
    /**
     * Creates a Telegram-specific link command.
     *
     * @return A new instance of {@link TelegramLinkCommand}.
     */
    @Override
    protected MessengerLinkCommandTemplate createLinkCommand() {
        LOGGER.atFine().log("Creating TelegramLinkCommand");
        return new TelegramLinkCommand(TelegramLinkType.getInstance().getLinkMessages());
    }
    // #endregion
}