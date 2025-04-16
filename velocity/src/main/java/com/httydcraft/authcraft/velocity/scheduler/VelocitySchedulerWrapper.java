package com.httydcraft.authcraft.velocity.scheduler;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.server.scheduler.ServerScheduler;
import com.velocitypowered.api.scheduler.ScheduledTask;

// #region Class Documentation
/**
 * Velocity-specific wrapper for scheduled tasks.
 * Implements {@link ServerScheduler} to manage task cancellation.
 */
public class VelocitySchedulerWrapper implements ServerScheduler {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final ScheduledTask scheduledTask;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code VelocitySchedulerWrapper}.
     *
     * @param scheduledTask The Velocity scheduled task. Must not be null.
     */
    public VelocitySchedulerWrapper(ScheduledTask scheduledTask) {
        this.scheduledTask = Preconditions.checkNotNull(scheduledTask, "scheduledTask must not be null");
        LOGGER.atInfo().log("Initialized VelocitySchedulerWrapper");
    }
    // #endregion

    // #region ServerScheduler Implementation
    /**
     * Cancels the scheduled task.
     */
    @Override
    public void cancel() {
        LOGGER.atFine().log("Cancelling scheduled task");
        scheduledTask.cancel();
    }
    // #endregion
}