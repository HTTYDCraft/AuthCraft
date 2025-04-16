package com.httydcraft.authcraft.core.link.discord;

import com.httydcraft.multimessenger.core.message.Message;
import com.httydcraft.multimessenger.discord.message.DiscordMessage;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.core.discord.command.actor.BaseJDAButtonActor;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapperTemplate;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.impl.UserNumberIdentificator;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.JDAActor;
import revxrsal.commands.jda.actor.MessageJDAActor;
import revxrsal.commands.jda.actor.SlashCommandJDAActor;
import revxrsal.commands.jda.exception.GuildOnlyCommandException;
import revxrsal.commands.jda.exception.PrivateMessageOnlyCommandException;

import java.util.Optional;

// #region Class Documentation
/**
 * Wrapper for Discord command actors.
 * Extends {@link LinkCommandActorWrapperTemplate} and implements {@link JDAActor} for Discord-specific functionality.
 */
public class DiscordCommandActorWrapper extends LinkCommandActorWrapperTemplate<JDAActor> implements JDAActor {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code DiscordCommandActorWrapper}.
     *
     * @param actor The Discord actor to wrap. Must not be null.
     */
    public DiscordCommandActorWrapper(JDAActor actor) {
        super(actor);
        Preconditions.checkNotNull(actor, "actor must not be null");
        LOGGER.atFine().log("Initialized DiscordCommandActorWrapper for actor: %s", actor.getId());
    }
    // #endregion

    // #region Messaging
    /**
     * Sends a message to the Discord actor.
     *
     * @param message The message to send. Must not be null.
     */
    @Override
    public void send(Message message) {
        Preconditions.checkNotNull(message, "message must not be null");
        Optional<DiscordMessage> discordMessage = message.safeAs(DiscordMessage.class);
        if (!discordMessage.isPresent()) {
            LOGGER.atFine().log("Ignored non-Discord message");
            return;
        }
        DiscordMessage msg = discordMessage.get();
        if (actor instanceof SlashCommandJDAActor slashCommandJDAActor) {
            SlashCommandInteractionEvent event = slashCommandJDAActor.getSlashEvent();
            sendInteractionMessage(msg, event, event.getHook());
            LOGGER.atFine().log("Sent Discord message via slash command to actor: %s", actor.getId());
        } else if (actor instanceof BaseJDAButtonActor buttonActor) {
            ButtonInteractionEvent event = buttonActor.getButtonEvent();
            sendInteractionMessage(msg, event, event.getHook());
            LOGGER.atFine().log("Sent Discord message via button to actor: %s", actor.getId());
        } else if (actor instanceof MessageJDAActor) {
            msg.send(getChannel());
            LOGGER.atFine().log("Sent Discord message via channel to actor: %s", actor.getId());
        } else {
            LOGGER.atWarning().log("Unsupported actor type for sending message: %s", actor.getClass().getSimpleName());
        }
    }

    /**
     * Sends a reply to the Discord actor.
     *
     * @param message The message to send. Must not be null.
     */
    @Override
    public void reply(String message) {
        Preconditions.checkNotNull(message, "message must not be null");
        if (actor instanceof SlashCommandJDAActor slashCommandJDAActor) {
            SlashCommandInteractionEvent event = slashCommandJDAActor.getSlashEvent();
            sendInteractionMessage(message, event, event.getHook());
            LOGGER.atFine().log("Sent reply via slash command: %s", message);
            return;
        }
        if (actor instanceof BaseJDAButtonActor buttonActor) {
            ButtonInteractionEvent event = buttonActor.getButtonEvent();
            sendInteractionMessage(message, event, event.getHook());
            LOGGER.atFine().log("Sent reply via button: %s", message);
            return;
        }
        actor.reply(message);
        LOGGER.atFine().log("Sent reply via default: %s", message);
    }

    /**
     * Sends an error message to the Discord actor.
     *
     * @param message The error message to send. Must not be null.
     */
    @Override
    public void error(String message) {
        Preconditions.checkNotNull(message, "message must not be null");
        reply(message);
        LOGGER.atWarning().log("Sent error to Discord actor: %s", message);
    }

    /**
     * Sends an interaction message to the Discord actor.
     *
     * @param message        The message to send.
     * @param replyCallback  The reply callback.
     * @param interactionHook The interaction hook.
     */
    private void sendInteractionMessage(String message, IReplyCallback replyCallback, InteractionHook interactionHook) {
        Preconditions.checkNotNull(message, "message must not be null");
        Preconditions.checkNotNull(replyCallback, "replyCallback must not be null");
        Preconditions.checkNotNull(interactionHook, "interactionHook must not be null");
        interactionHook.setEphemeral(true);
        if (interactionHook.getInteraction().isAcknowledged()) {
            interactionHook.editOriginal(message).queue();
            LOGGER.atFine().log("Edited interaction message: %s", message);
        } else {
            replyCallback.reply(message).queue();
            LOGGER.atFine().log("Sent interaction reply: %s", message);
        }
    }

    /**
     * Sends a Discord-specific interaction message.
     *
     * @param message        The Discord message to send.
     * @param replyCallback  The reply callback.
     * @param interactionHook The interaction hook.
     */
    private void sendInteractionMessage(DiscordMessage message, IReplyCallback replyCallback, InteractionHook interactionHook) {
        Preconditions.checkNotNull(message, "message must not be null");
        Preconditions.checkNotNull(replyCallback, "replyCallback must not be null");
        Preconditions.checkNotNull(interactionHook, "interactionHook must not be null");
        interactionHook.setEphemeral(true);
        message.send(builder -> {
            if (interactionHook.getInteraction().isAcknowledged()) {
                interactionHook.sendMessage(builder.build()).queue();
                LOGGER.atFine().log("Sent interaction message via sendMessage");
            } else {
                replyCallback.reply(builder.build()).queue();
                LOGGER.atFine().log("Sent interaction message via reply");
            }
        });
    }
    // #endregion

    // #region Identification
    /**
     * Gets the user identifier for the Discord actor.
     *
     * @return The {@link LinkUserIdentificator}.
     */
    @Override
    public LinkUserIdentificator userId() {
        LinkUserIdentificator id = new UserNumberIdentificator(getIdLong());
        LOGGER.atFine().log("Retrieved Discord user ID: %s", id);
        return id;
    }
    // #endregion

    // #region JDA Actor Methods
    /**
     * Gets the ID of the actor as a long.
     *
     * @return The actor's ID.
     */
    @Override
    public long getIdLong() {
        long id = actor.getIdLong();
        LOGGER.atFine().log("Retrieved Discord ID long: %d", id);
        return id;
    }

    /**
     * Gets the ID of the actor as a string.
     *
     * @return The actor's ID.
     */
    @Override
    public @NotNull String getId() {
        String id = actor.getId();
        LOGGER.atFine().log("Retrieved Discord ID: %s", id);
        return id;
    }

    /**
     * Gets the Discord user.
     *
     * @return The {@link User}.
     */
    @Override
    public @NotNull User getUser() {
        User user = actor.getUser();
        LOGGER.atFine().log("Retrieved Discord user: %s", user.getId());
        return user;
    }

    /**
     * Gets the generic event.
     *
     * @return The {@link Event}.
     */
    @Override
    public @NotNull Event getGenericEvent() {
        Event event = actor.getGenericEvent();
        LOGGER.atFine().log("Retrieved Discord generic event: %s", event.getClass().getSimpleName());
        return event;
    }

    /**
     * Gets the message channel.
     *
     * @return The {@link MessageChannel}.
     */
    @Override
    public @NotNull MessageChannel getChannel() {
        MessageChannel channel = actor.getChannel();
        LOGGER.atFine().log("Retrieved Discord channel: %s", channel.getId());
        return channel;
    }

    /**
     * Checks if the event is guild-related.
     *
     * @return {@code true} if guild-related, {@code false} otherwise.
     */
    @Override
    public boolean isGuildEvent() {
        boolean isGuild = actor.isGuildEvent();
        LOGGER.atFine().log("Checked if Discord event is guild-related: %b", isGuild);
        return isGuild;
    }

    /**
     * Gets the Discord guild.
     *
     * @return The {@link Guild}.
     */
    @Override
    public @NotNull Guild getGuild() {
        Guild guild = actor.getGuild();
        LOGGER.atFine().log("Retrieved Discord guild: %s", guild.getId());
        return guild;
    }

    /**
     * Gets the Discord member.
     *
     * @return The {@link Member}.
     */
    @Override
    public @NotNull Member getMember() {
        Member member = actor.getMember();
        LOGGER.atFine().log("Retrieved Discord member: %s", member.getId());
        return member;
    }

    /**
     * Checks if the command is executed in a guild.
     *
     * @param executableCommand The command to check.
     * @return This actor.
     * @throws GuildOnlyCommandException If not in a guild.
     */
    @Override
    public JDAActor checkInGuild(ExecutableCommand executableCommand) throws GuildOnlyCommandException {
        JDAActor result = actor.checkInGuild(executableCommand);
        LOGGER.atFine().log("Checked Discord guild command: %s", executableCommand.getName());
        return result;
    }

    /**
     * Checks if the command is executed in a private message.
     *
     * @param executableCommand The command to check.
     * @return This actor.
     * @throws PrivateMessageOnlyCommandException If not in a private message.
     */
    @Override
    public JDAActor checkNotInGuild(ExecutableCommand executableCommand) throws PrivateMessageOnlyCommandException {
        JDAActor result = actor.checkNotInGuild(executableCommand);
        LOGGER.atFine().log("Checked Discord private command: %s", executableCommand.getName());
        return result;
    }
    // #endregion
}