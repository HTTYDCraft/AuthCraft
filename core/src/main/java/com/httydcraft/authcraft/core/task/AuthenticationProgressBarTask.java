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
import com.httydcraft.authcraft.api.server.message.ServerComponent;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.api.server.scheduler.ServerScheduler;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;
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
            long timeoutMillis = plugin.getConfig().getAuthTime();

            Iterator<Entry<String, ServerBossbar>> iterator = progressBars.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<String, ServerBossbar> entry = iterator.next();
                String accountId = entry.getKey();
                ServerBossbar progressBar = entry.getValue();
                PlayerIdSupplier idSupplier = PlayerIdSupplier.of(accountId);
                Optional<Account> accountOptional = plugin.getAuthenticatingAccountBucket().getAuthenticatingAccount(idSupplier);
                if (!accountOptional.isPresent()) {
                    iterator.remove();
                    progressBar.removeAll();
                    continue;
                }

                Account account = accountOptional.get();
                Optional<ServerPlayer> player = account.getPlayer();
                if (!player.isPresent()) {
                    iterator.remove();
                    progressBar.removeAll();
                    continue;
                }

                long accountTimeElapsedFromEntryMillis = (now -
                        plugin.getAuthenticatingAccountBucket().getEnterTimestampOrZero(idSupplier));

                if (progressBar.players().isEmpty())
                    progressBar.send(player.get());

                float progress = 1.0F - (accountTimeElapsedFromEntryMillis / (float) timeoutMillis);
                if (progress > 1 || progress < 0) {
                    iterator.remove();
                    progressBar.removeAll();
                    continue;
                }

                String formattedDuration = settings.getDurationPlaceholderFormat().format(new Date(timeoutMillis - accountTimeElapsedFromEntryMillis));
                progressBar.progress(progress);
                progressBar.title(ServerComponent.fromJson(settings.getTitle().jsonText().replace("%duration%", formattedDuration)));
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
     */
    @SubscribeEvent
    public void onJoin(AccountJoinEvent e) {
        SecurityAuditLogger.logSuccess("AuthenticationProgressBarTask", null, "Account joined, bossbar created for: " + e.getAccount().getPlayerId());
        registerBossbar(e.getAccount());
    }
    /**
     * Handles player logout events by removing the boss bar.
     */
    @SubscribeEvent
    public void onLogout(PlayerLogoutEvent e) {
        SecurityAuditLogger.logSuccess("AuthenticationProgressBarTask", null, "Player logout, bossbar removed for: " + e.getPlayer().getPlayerId());
        registerBossbar(e.getPlayer());
    }

    private void registerBossbar(PlayerIdSupplier playerIdSupplier) {
        SecurityAuditLogger.logSuccess("AuthenticationProgressBarTask: bossbar event", null, "Bossbar event triggered");
        if (playerIdSupplier == null) {
            SecurityAuditLogger.logFailure("AuthenticationProgressBarTask", null, "PlayerIdSupplier is null on bossbar event");
            return;
        }
        ServerBossbar bossbar = settings.createBossbar();
        if (bossbar == null)
            return;
        progressBars.put(playerIdSupplier.getPlayerId(), bossbar);
    }
    // #endregion
}