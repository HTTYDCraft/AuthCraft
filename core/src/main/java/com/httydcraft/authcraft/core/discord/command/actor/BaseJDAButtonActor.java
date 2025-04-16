package com.httydcraft.authcraft.core.discord.command.actor;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.jda.core.actor.BaseActorJDA;

// #region Class Documentation
/**
 * Actor for handling Discord button interactions.
 * Extends {@link BaseActorJDA} to provide button-specific functionality.
 */
public class BaseJDAButtonActor extends BaseActorJDA {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ButtonInteractionEvent event;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseJDAButtonActor}.
     *
     * @param event   The button interaction event. Must not be null.
     * @param handler The command handler. Must not be null.
     */
    public BaseJDAButtonActor(ButtonInteractionEvent event, CommandHandler handler) {
        super(event, handler);
        this.event = Preconditions.checkNotNull(event, "event must not be null");
        Preconditions.checkNotNull(handler, "handler must not be null");
        LOGGER.atFine().log("Initialized BaseJDAButtonActor for user: %s", event.getUser().getId());
    }
    // #endregion

    // #region Reply Methods
    /**
     * Sends a reply to the button interaction.
     *
     * @param message The message to send. Must not be null.
     */
    @Override
    public void reply(@NotNull String message) {
        Preconditions.checkNotNull(message, "message must not be null");
        event.reply(getCommandHandler().getMessagePrefix() + message).queue();
        LOGGER.atFine().log("Sent reply for button interaction: %s", message);
    }

    /**
     * Sends an error message to the button interaction.
     *
     * @param message The error message to send. Must not be null.
     */
    @Override
    public void error(@NotNull String message) {
        Preconditions.checkNotNull(message, "message must not be null");
        event.reply(getCommandHandler().getMessagePrefix() + message).queue();
        LOGGER.atWarning().log("Sent error for button interaction: %s", message);
    }
    // #endregion

    // #region User and Channel Information
    /**
     * Gets the user who triggered the button interaction.
     *
     * @return The {@link User}. Never null.
     */
    @Override
    public @NotNull User getUser() {
        User user = event.getUser();
        LOGGER.atFine().log("Retrieved user: %s", user.getId());
        return user;
    }

    /**
     * Gets the channel where the button interaction occurred.
     *
     * @return The {@link MessageChannel}. Never null.
     */
    @Override
    public @NotNull MessageChannel getChannel() {
        MessageChannel channel = event.getChannel();
        LOGGER.atFine().log("Retrieved channel: %s", channel.getId());
        return channel;
    }
    // #endregion

    // #region Guild Information
    /**
     * Checks if the button interaction occurred in a guild.
     *
     * @return {@code true} if the interaction is from a guild, {@code false} otherwise.
     */
    @Override
    public boolean isGuildEvent() {
        boolean isGuild = event.isFromGuild();
        LOGGER.atFine().log("Checked guild event: %b", isGuild);
        return isGuild;
    }

    /**
     * Gets the guild where the button interaction occurred.
     *
     * @return The {@link Guild}. Never null if {@link #isGuildEvent()} is true.
     * @throws IllegalStateException If the interaction is not from a guild.
     */
    @Override
    public @NotNull Guild getGuild() {
        Guild guild = event.getGuild();
        if (guild == null) {
            LOGGER.atSevere().log("Guild is null for button interaction");
            throw new IllegalStateException("Guild is null");
        }
        LOGGER.atFine().log("Retrieved guild: %s", guild.getId());
        return guild;
    }

    /**
     * Gets the member who triggered the button interaction.
     *
     * @return The {@link Member}. Never null if {@link #isGuildEvent()} is true.
     * @throws IllegalStateException If the interaction is not from a guild.
     */
    @Override
    public @NotNull Member getMember() {
        Member member = event.getMember();
        if (member == null) {
            LOGGER.atSevere().log("Member is null for button interaction");
            throw new IllegalStateException("Member is null");
        }
        LOGGER.atFine().log("Retrieved member: %s", member.getId());
        return member;
    }
    // #endregion

    // #region Event Access
    /**
     * Gets the button interaction event.
     *
     * @return The {@link ButtonInteractionEvent}. Never null.
     */
    public ButtonInteractionEvent getButtonEvent() {
        LOGGER.atFine().log("Retrieved button event");
        return event;
    }
    // #endregion
}