package com.httydcraft.authcraft.scheduler;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.server.scheduler.ServerScheduler;
import org.bukkit.scheduler.BukkitTask;

// #region Class Documentation
/**
 * Spigot-specific wrapper for scheduled tasks.
 * Implements {@link ServerScheduler} to manage task cancellation.
 */
public class SpigotSchedulerWrapper implements ServerScheduler {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final BukkitTask task;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code SpigotSchedulerWrapper}.
     *
     * @param task The Bukkit scheduled task. Must not be null.
     */
    public SpigotSchedulerWrapper(BukkitTask task) {
        this.task = Preconditions.checkNotNull(task, "task must not be null");
        LOGGER.atInfo().log("Initialized SpigotSchedulerWrapper");
    }
    // #endregion

    // #region ServerScheduler Implementation
    /**
     * Cancels the scheduled task.
     */
    @Override
    public void cancel() {
        LOGGER.atFine().log("Cancelling scheduled task");
        task.cancel();
    }
    // #endregion
}