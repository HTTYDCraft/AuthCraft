package com.httydcraft.authcraft.core.task;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.bossbar.BossBarSettings;
import com.httydcraft.authcraft.api.event.AccountJoinEvent;
import com.httydcraft.authcraft.api.event.PlayerLogoutEvent;
import com.httydcraft.authcraft.api.model.AuthenticationTask;
import com.httydcraft.authcraft.api.model.PlayerIdSupplier;
import com.httydcraft.authcraft.api.server.bossbar.ServerBossbar;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.server.scheduler.ServerScheduler;
import io.github.revxrsal.eventbus.SubscribeEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

// #region Class Documentation
/**
 * Task for managing authentication progress bars.
 * Displays a boss bar to players indicating authentication time remaining.
 */
public class AuthenticationProgressBarTask implements AuthenticationTask {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final Map<String, ServerBossbar> progressBars;
    private final BossBarSettings settings;
    private final ServerScheduler proxyScheduler;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code AuthenticationProgressBarTask}.
     *
     * @param plugin The AuthPlugin instance. Must not be null.
     */
    public AuthenticationProgressBarTask(AuthPlugin plugin) {
        Preconditions.checkNotNull(plugin, "plugin must not be null");
        this.progressBars = new HashMap<>();
        this.settings = plugin.getConfig().getBossBarSettings();
        this.proxyScheduler = plugin.getCore().schedule(() -> {
            long now = System.currentTimeMillis();
            Iterator<Entry<String, ServerBossbar>> iterator = progressBars.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, ServerBossbar> entry = iterator.next();
                String playerId = entry.getKey();
                ServerBossbar bossbar = entry.getValue();
                Account account = plugin.getAuthenticatingAccountBucket()
                        .getAuthenticatingAccountNullable(PlayerIdSupplier.of(playerId));
                if (account == null || !plugin.getAuthenticatingAccountBucket().isAuthenticating(account)) {
                    bossbar.removeAll();
                    iterator.remove();
                    LOGGER.atFine().log("Removed boss bar for player ID: %s due to invalid account", playerId);
                    continue;
                }
                long authenticationTimeout = account.getAuthenticationTimeout();
                float progress = (float) (authenticationTimeout - now) / settings.getTimeout();
                if (progress <= 0) {
                    bossbar.removeAll();
                    iterator.remove();
                    account.getPlayer().ifPresent(player -> player.kick(
                            settings.getMessages().getMessage("timeout", new Date(authenticationTimeout))));
                    LOGGER.atFine().log("Timed out and kicked player ID: %s", playerId);
                    continue;
                }
                bossbar.progress(Math.max(0, Math.min(1, progress)));
                LOGGER.atFine().log("Updated boss bar progress for player ID: %s, progress: %f", playerId, progress);
            }
        }, 0, 1, TimeUnit.SECONDS);
        plugin.getEventBus().register(this);
        LOGGER.atInfo().log("Initialized AuthenticationProgressBarTask");
    }
    // #endregion

    // #region AuthenticationTask Implementation
    /**
     * Stops the task by canceling the scheduler and removing all boss bars.
     */
    @Override
    public void stop() {
        proxyScheduler.cancel();
        for (ServerBossbar bossbar : progressBars.values()) {
            bossbar.removeAll();
        }
        progressBars.clear();
        LOGGER.atInfo().log("Stopped AuthenticationProgressBarTask and cleared all boss bars");
    }
    // #endregion

    // #region Event Handlers
    /**
     * Handles account join events by creating a boss bar for the player.
     *
     * @param event The account join event. Must not be null.
     */
    @SubscribeEvent
    public void onAccountJoin(AccountJoinEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        Account account = event.getAccount();
        String playerId = account.getPlayerId();
        if (progressBars.containsKey(playerId)) {
            LOGGER.atFine().log("Boss bar already exists for player ID: %s", playerId);
            return;
        }
        Optional<ServerPlayer> playerOptional = account.getPlayer();
        if (!playerOptional.isPresent()) {
            LOGGER.atFine().log("No player found for account: %s", playerId);
            return;
        }
        ServerPlayer player = playerOptional.get();
        ServerBossbar bossbar = settings.createBossBar(
                settings.getMessages().getMessage("authenticating", new Date(account.getAuthenticationTimeout())));
        bossbar.addPlayer(player);
        progressBars.put(playerId, bossbar);
        LOGGER.atFine().log("Created boss bar for player ID: %s", playerId);
    }

    /**
     * Handles player logout events by removing the boss bar.
     *
     * @param event The player logout event. Must not be null.
     */
    @SubscribeEvent
    public void onPlayerLogout(PlayerLogoutEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        String playerId = event.getPlayerId();
        ServerBossbar bossbar = progressBars.remove(playerId);
        if (bossbar != null) {
            bossbar.removeAll();
            LOGGER.atFine().log("Removed boss bar for player ID: %s due to logout", playerId);
        }
    }
    // #endregion
}