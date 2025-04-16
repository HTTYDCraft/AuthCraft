package com.httydcraft.authcraft.bangee.scheduler;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.server.scheduler.ServerScheduler;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

// #region Class Documentation
/**
 * BungeeCord-specific wrapper for scheduled tasks.
 * Implements {@link ServerScheduler} to manage task cancellation.
 */
public class BungeeSchedulerWrapper implements ServerScheduler {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ScheduledTask scheduledTask;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BungeeSchedulerWrapper}.
     *
     * @param scheduledTask The BungeeCord scheduled task. Must not be null.
     */
    public BungeeSchedulerWrapper(@NotNull ScheduledTask scheduledTask) {
        this.scheduledTask = Preconditions.checkNotNull(scheduledTask, "scheduledTask must not be null");
        LOGGER.atInfo().log("Initialized BungeeSchedulerWrapper for task ID: %d", scheduledTask.getId());
    }
    // #endregion

    // #region ServerScheduler Implementation
    /**
     * Cancels the scheduled task.
     */
    @Override
    public void cancel() {
        LOGGER.atFine().log("Cancelling task ID: %d", scheduledTask.getId());
        scheduledTask.cancel();
    }
    // #endregion
}