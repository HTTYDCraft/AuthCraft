package com.httydcraft.authcraft.core.commands.validator;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import com.httydcraft.authcraft.api.config.message.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.CommandCondition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

// #region Class Documentation
/**
 * A command condition that enforces a cooldown period for commands annotated with {@link CommandCooldown}.
 * Prevents command execution if the cooldown has not expired.
 *
 * @param <T> The type of the message content.
 */
public class CommandCooldownCondition<T> implements CommandCondition {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final Map<Integer, Long> cooldownMap = new HashMap<>();
    private final Messages<T> messages;
    private final Function<T, RuntimeException> messageException;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code CommandCooldownCondition}.
     *
     * @param messages        The messages configuration for cooldown errors. Must not be null.
     * @param messageException The exception factory for cooldown errors. Must not be null.
     */
    public CommandCooldownCondition(Messages<T> messages, Function<T, RuntimeException> messageException) {
        this.messages = Preconditions.checkNotNull(messages, "messages must not be null");
        this.messageException = Preconditions.checkNotNull(messageException, "messageException must not be null");
        LOGGER.atInfo().log("Initialized CommandCooldownCondition");
    }
    // #endregion

    // #region Command Condition
    /**
     * Tests if the command can be executed based on its cooldown status.
     *
     * @param commandActor      The actor executing the command. Must not be null.
     * @param executableCommand The command being executed. Must not be null.
     * @param list              The command arguments. Must not be null.
     * @throws RuntimeException if the cooldown has not expired.
     */
    @Override
    public void test(@NotNull CommandActor commandActor, @NotNull ExecutableCommand executableCommand, @NotNull @Unmodifiable List<String> list) {
        Preconditions.checkNotNull(commandActor, "commandActor must not be null");
        Preconditions.checkNotNull(executableCommand, "executableCommand must not be null");
        Preconditions.checkNotNull(list, "list must not be null");

        if (!executableCommand.hasAnnotation(CommandCooldown.class)) {
            LOGGER.atFine().log("No cooldown annotation for command: %s", executableCommand.getName());
            return;
        }

        CommandCooldown commandCooldown = executableCommand.getAnnotation(CommandCooldown.class);
        long cooldownInMillis = commandCooldown.unit().toMillis(commandCooldown.value());
        int commandId = executableCommand.getId();

        if (!cooldownMap.containsKey(commandId)) {
            cooldownMap.put(commandId, System.currentTimeMillis());
            LOGGER.atFine().log("Initialized cooldown for commandId: %d", commandId);
            return;
        }

        long timestampDelta = System.currentTimeMillis() - cooldownMap.get(commandId);
        if (timestampDelta < cooldownInMillis) {
            LOGGER.atFine().log("Cooldown active for commandId: %d, remaining: %d ms", commandId, cooldownInMillis - timestampDelta);
            throw messageException.apply(messages.getMessage("command-cooldown"));
        }

        cooldownMap.put(commandId, System.currentTimeMillis());
        LOGGER.atFine().log("Updated cooldown for commandId: %d", commandId);
    }
    // #endregion
}