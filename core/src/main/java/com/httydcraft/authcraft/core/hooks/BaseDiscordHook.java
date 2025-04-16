package com.httydcraft.authcraft.core.hooks;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.concurrent.CompletableFuture;

// #region Class Documentation
/**
 * Base implementation of {@link DiscordHook}.
 * Initializes and provides access to a Discord JDA instance.
 */
public class BaseDiscordHook implements DiscordHook {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private JDA jda;
    // #endregion

    // #region JDA Access
    /**
     * Gets the JDA instance.
     *
     * @return The {@link JDA} instance, or {@code null} if not initialized.
     */
    @Override
    public JDA getJDA() {
        LOGGER.atFine().log("Retrieved JDA instance: %s", jda != null ? "present" : "null");
        return jda;
    }
    // #endregion

    // #region Initialization
    /**
     * Initializes the JDA instance asynchronously.
     *
     * @return A {@link CompletableFuture} containing the initialized {@link JDA}.
     */
    public CompletableFuture<JDA> initialize() {
        LOGGER.atInfo().log("Starting JDA initialization");
        return CompletableFuture.supplyAsync(() -> {
            String botToken = PLUGIN.getConfig().getDiscordSettings().getBotToken();
            Preconditions.checkNotNull(botToken, "botToken must not be null");
            JDABuilder jdaBuilder = JDABuilder.createDefault(botToken)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS);
            try {
                jda = jdaBuilder.build().awaitReady();
                LOGGER.atInfo().log("JDA initialized successfully");
                return jda;
            } catch (InterruptedException e) {
                LOGGER.atSevere().withCause(e).log("Interrupted during JDA initialization");
                Thread.currentThread().interrupt();
                return null;
            }
        }).handle((result, ex) -> {
            if (ex != null) {
                LOGGER.atSevere().withCause(ex).log("Failed to initialize JDA");
            }
            return result;
        });
    }
    // #endregion
}