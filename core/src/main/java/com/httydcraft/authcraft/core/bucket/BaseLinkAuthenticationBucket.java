/* BaseLinkAuthenticationBucket.java */
/**
 * A specialized bucket for LinkUser-based authentication.
 * Supports bucket operations for link authentication scenarios.
 *
 * @param <T> Type parameter extending LinkUser
 */
package com.httydcraft.authcraft.core.bucket;

import com.httydcraft.authcraft.api.bucket.LinkAuthenticationBucket;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.google.common.flogger.GoogleLogger;

public class BaseLinkAuthenticationBucket<T extends LinkUser> extends BaseListBucket<T> implements LinkAuthenticationBucket<T> {

    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    /**
     * Initializes a bucket for linking and authenticating users.
     */
    public BaseLinkAuthenticationBucket() {
        logger.atInfo().log("BaseLinkAuthenticationBucket instantiated");
    }
}