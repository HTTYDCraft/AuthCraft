package com.httydcraft.authcraft.core.link.telegram;

import com.httydcraft.lamp.telegram.TelegramActor;
import com.httydcraft.lamp.telegram.dispatch.DispatchSource;
import com.httydcraft.multimessenger.core.identificator.Identificator;
import com.httydcraft.multimessenger.core.message.Message;
import com.httydcraft.multimessenger.telegram.message.TelegramMessage;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapperTemplate;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.impl.UserNumberIdentificator;

// #region Class Documentation
/**
 * Wrapper for Telegram command actors.
 * Extends {@link LinkCommandActorWrapperTemplate} and implements {@link TelegramActor} for Telegram-specific functionality.
 */
public class TelegramCommandActorWrapper extends LinkCommandActorWrapperTemplate<TelegramActor> implements TelegramActor {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code TelegramCommandActorWrapper}.
     *
     * @param actor The Telegram actor to wrap. Must not be null.
     */
    public TelegramCommandActorWrapper(TelegramActor actor) {
        super(actor);
        Preconditions.checkNotNull(actor, "actor must not be null");
        LOGGER.atFine().log("Initialized TelegramCommandActorWrapper for actor: %s", actor.getId());
    }
    // #endregion

    // #region Messaging
    /**
     * Sends a message to the Telegram actor.
     *
     * @param message The message to send. Must not be null.
     */
    @Override
    public void send(Message message) {
        Preconditions.checkNotNull(message, "message must not be null");
        Optional<TelegramMessage> telegramMessage = message.safeAs(TelegramMessage.class);
        if (!telegramMessage.isPresent()) {
            LOGGER.atFine().log("Ignored non-Telegram message");
            return;
        }
        telegramMessage.get().send(
                Identificator.fromObject(actor.getDispatchSource().getChatIdentficator().asObject()),
                TelegramMessage.getDefaultApiProvider(),
                response -> {
                    if (!response.isOk()) {
                        String errorMessage = "Error occurred " + response.description() + ". Error code: " + response.errorCode();
                        TelegramLinkType.getInstance()
                                .newMessageBuilder(errorMessage)
                                .build()
                                .send(Identificator.fromObject(actor.getDispatchSource().getChatIdentficator().asObject()));
                        LOGGER.atWarning().log("Telegram message send failed: %s", errorMessage);
                    } else {
                        LOGGER.atFine().log("Sent Telegram message to actor: %s", actor.getId());
                    }
                });
    }
    // #endregion

    // #region Identification
    /**
     * Gets the user identifier for the Telegram actor.
     *
     * @return The {@link LinkUserIdentificator}.
     */
    @Override
    public LinkUserIdentificator userId() {
        LinkUserIdentificator id = new UserNumberIdentificator(actor.getId());
        LOGGER.atFine().log("Retrieved Telegram user ID: %s", id);
        return id;
    }
    // #endregion

    // #region Telegram Actor Methods
    /**
     * Gets the dispatch source for the Telegram actor.
     *
     * @return The {@link DispatchSource}.
     */
    @Override
    public DispatchSource getDispatchSource() {
        DispatchSource source = actor.getDispatchSource();
        LOGGER.atFine().log("Retrieved Telegram dispatch source");
        return source;
    }
    // #endregion
}