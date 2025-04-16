/* BaseAuthenticationStepFactoryBucket.java */
/**
 * A bucket specifically for holding AuthenticationStepFactory instances.
 * Manages authentication step creation factories.
 */
package com.httydcraft.authcraft.core.bucket;

import com.httydcraft.authcraft.api.bucket.AuthenticationStepFactoryBucket;
import com.httydcraft.authcraft.api.factory.AuthenticationStepFactory;
import com.google.common.flogger.GoogleLogger;

public class BaseAuthenticationStepFactoryBucket extends BaseListBucket<AuthenticationStepFactory> implements AuthenticationStepFactoryBucket {

    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    /**
     * Initializes a bucket for AuthenticationStepFactories.
     */
    public BaseAuthenticationStepFactoryBucket() {
        logger.atInfo().log("BaseAuthenticationStepFactoryBucket instantiated");
    }
}