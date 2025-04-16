package com.httydcraft.authcraft.core.hooks.nanolimbo;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import ua.nanit.limbo.server.Command;
import ua.nanit.limbo.server.CommandHandler;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.data.InfoForwarding;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.IntStream;

// #region Class Documentation
/**
 * Provider for NanoLimbo server functionality.
 * Defines methods for creating and starting a limbo server and finding available addresses.
 */
public interface NanoLimboProvider {
    GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    CommandHandler<Command> DUMMY_COMMAND_HANDLER = new CommandHandler<>() {
        /**
         * Gets the collection of registered commands.
         *
         * @return An empty collection.
         */
        @Override
        public Collection<Command> getCommands() {
            LOGGER.atFine().log("Retrieved empty command list");
            return Collections.emptyList();
        }

        /**
         * Registers a command.
         *
         * @param command The command to register. Ignored.
         */
        @Override
        public void register(Command command) {
            LOGGER.atFine().log("Ignored command registration");
        }

        /**
         * Executes a command.
         *
         * @param s The command string. Ignored.
         * @return {@code false}.
         */
        @Override
        public boolean executeCommand(String s) {
            LOGGER.atFine().log("Ignored command execution: %s", s);
            return false;
        }
    };
    InfoForwardingFactory FORWARDING_FACTORY = new InfoForwardingFactory();
    // #endregion

    // #region Forwarding Creation
    /**
     * Creates an {@link InfoForwarding} configuration.
     *
     * @return The configured {@link InfoForwarding}.
     */
    InfoForwarding createForwarding();
    // #endregion

    // #region Class Loader
    /**
     * Gets the class loader for the limbo server.
     *
     * @return The {@link ClassLoader}.
     */
    ClassLoader classLoader();
    // #endregion

    // #region Server Creation
    /**
     * Creates and starts a limbo server on the specified address.
     *
     * @param address The server address. Must not be null.
     */
    default void createAndStartLimbo(SocketAddress address) {
        Preconditions.checkNotNull(address, "address must not be null");
        LimboServer limboServer = new LimboServer(new NanoLimboConfig(address, createForwarding()), DUMMY_COMMAND_HANDLER, classLoader());
        try {
            limboServer.start();
            LOGGER.atInfo().log("Started limbo server on address: %s", address);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Failed to start limbo server on address: %s", address);
        }
    }
    // #endregion

    // #region Address Discovery
    /**
     * Finds an available address from a list of ports.
     *
     * @param ports The array of ports to check. Must not be null.
     * @return An {@link Optional} containing the first available {@link InetSocketAddress}, or empty if none found.
     */
    default Optional<InetSocketAddress> findAvailableAddress(int[] ports) {
        Preconditions.checkNotNull(ports, "ports must not be null");
        Optional<InetSocketAddress> address = IntStream.of(ports)
                .filter(port -> {
                    try (ServerSocket ignored = new ServerSocket(port)) {
                        LOGGER.atFine().log("Port %d is available", port);
                        return true;
                    } catch (IOException e) {
                        LOGGER.atFine().log("Port %d is unavailable", port);
                        return false;
                    }
                })
                .mapToObj(port -> new InetSocketAddress(port))
                .findFirst();
        LOGGER.atFine().log("Found available address: %s", address.map(Object::toString).orElse("none"));
        return address;
    }
    // #endregion
}