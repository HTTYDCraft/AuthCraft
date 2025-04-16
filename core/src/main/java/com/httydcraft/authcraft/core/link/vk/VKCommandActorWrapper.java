package com.httydcraft.authcraft.core.link.vk;

import com.httydcraft.lamp.commands.vk.VkActor;
import com.httydcraft.lamp.commands.vk.message.DispatchSource;
import com.httydcraft.multimessenger.core.identificator.Identificator;
import com.httydcraft.multimessenger.core.message.Message;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapperTemplate;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.impl.UserNumberIdentificator;
import com.vk.api.sdk.objects.messages.Conversation;
import com.vk.api.sdk.objects.messages.ConversationPeerType;
import com.vk.api.sdk.objects.users.UserFull;

// #region Class Documentation
/**
 * Wrapper for VK command actors.
 * Extends {@link LinkCommandActorWrapperTemplate} and implements {@link VkActor} for VK-specific functionality.
 */
public class VKCommandActorWrapper extends LinkCommandActorWrapperTemplate<VkActor> implements VkActor {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VKCommandActorWrapper}.
     *
     * @param actor The VK actor to wrap. Must not be null.
     */
    public VKCommandActorWrapper(VkActor actor) {
        super(actor);
        Preconditions.checkNotNull(actor, "actor must not be null");
        LOGGER.atFine().log("Initialized VKCommandActorWrapper for actor: %s", actor.getId());
    }
    // #endregion

    // #region Messaging
    /**
     * Sends a message to the VK actor.
     *
     * @param message The message to send. Must not be null.
     */
    @Override
    public void send(Message message) {
        Preconditions.checkNotNull(message, "message must not be null");
        message.send(Identificator.of(actor.getPeerId()));
        LOGGER.atFine().log("Sent message to VK actor: %s", actor.getPeerId());
    }
    // #endregion

    // #region Identification
    /**
     * Gets the user identifier for the VK actor.
     *
     * @return The {@link LinkUserIdentificator}.
     */
    @Override
    public LinkUserIdentificator userId() {
        LinkUserIdentificator id = new UserNumberIdentificator(actor.getAuthorId());
        LOGGER.atFine().log("Retrieved VK user ID: %s", id);
        return id;
    }
    // #endregion

    // #region VK Actor Methods
    /**
     * Gets the dispatch source for the VK actor.
     *
     * @return The {@link DispatchSource}.
     */
    @Override
    public DispatchSource getDispatchSource() {
        DispatchSource source = actor.getDispatchSource();
        LOGGER.atFine().log("Retrieved VK dispatch source");
        return source;
    }

    /**
     * Gets the VK user information.
     *
     * @return The {@link UserFull}.
     */
    @Override
    public UserFull getUser() {
        UserFull user = actor.getUser();
        LOGGER.atFine().log("Retrieved VK user: %s", user != null ? user.getId() : "null");
        return user;
    }

    /**
     * Gets the VK conversation.
     *
     * @return The {@link Conversation}.
     */
    @Override
    public Conversation getConversation() {
        Conversation conversation = actor.getConversation();
        LOGGER.atFine().log("Retrieved VK conversation");
        return conversation;
    }

    /**
     * Gets the VK conversation type.
     *
     * @return The {@link ConversationPeerType}.
     */
    @Override
    public ConversationPeerType getConversationType() {
        ConversationPeerType type = actor.getConversationType();
        LOGGER.atFine().log("Retrieved VK conversation type: %s", type);
        return type;
    }

    /**
     * Gets the text of the VK message.
     *
     * @return The message text.
     */
    @Override
    public String getText() {
        String text = actor.getText();
        LOGGER.atFine().log("Retrieved VK message text: %s", text);
        return text;
    }

    /**
     * Gets the message payload.
     *
     * @return The message payload.
     */
    @Override
    public String getMessagePayload() {
        String payload = actor.getMessagePayload();
        LOGGER.atFine().log("Retrieved VK message payload: %s", payload);
        return payload;
    }

    /**
     * Gets the conversation ID.
     *
     * @return The conversation ID.
     */
    @Override
    public Integer getConversationId() {
        Integer id = actor.getConversationId();
        LOGGER.atFine().log("Retrieved VK conversation ID: %s", id);
        return id;
    }

    /**
     * Gets the author ID.
     *
     * @return The author ID.
     */
    @Override
    public Integer getAuthorId() {
        Integer id = actor.getAuthorId();
        LOGGER.atFine().log("Retrieved VK author ID: %s", id);
        return id;
    }

    /**
     * Gets the peer ID.
     *
     * @return The peer ID.
     */
    @Override
    public Integer getPeerId() {
        Integer id = actor.getPeerId();
        LOGGER.atFine().log("Retrieved VK peer ID: %s", id);
        return id;
    }
    // #endregion
}