/* BaseAuthenticationTaskBucket.java */
/**
 * A specialized bucket for holding AuthenticationTask instances.
 * Provides container and management for authentication tasks.
 */
package com.httydcraft.authcraft.core.bucket;

import com.httydcraft.authcraft.api.bucket.AuthenticationTaskBucket;
import com.httydcraft.authcraft.api.model.AuthenticationTask;
import com.google.common.flogger.GoogleLogger;

public class BaseAuthenticationTaskBucket extends BaseListBucket<AuthenticationTask> implements AuthenticationTaskBucket {

    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    /**
     * Initializes a bucket for AuthenticationTasks.
     */
    public BaseAuthenticationTaskBucket() {
        logger.atInfo().log("BaseAuthenticationTaskBucket instantiated");
    }
}