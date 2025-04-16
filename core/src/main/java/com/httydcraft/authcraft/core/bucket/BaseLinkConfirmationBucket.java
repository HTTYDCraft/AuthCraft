/* BaseLinkConfirmationBucket.java */
/**
 * A bucket specifically for holding LinkConfirmationUsers.
 * Facilitates bucket operations for link confirmation handling.
 */
package com.httydcraft.authcraft.core.bucket;

import com.httydcraft.authcraft.api.bucket.LinkConfirmationBucket;
import com.httydcraft.authcraft.api.link.user.confirmation.LinkConfirmationUser;
import com.google.common.flogger.GoogleLogger;

public class BaseLinkConfirmationBucket extends BaseListBucket<LinkConfirmationUser> implements LinkConfirmationBucket {

    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    /**
     * Initializes a bucket for LinkConfirmationUsers.
     */
    public BaseLinkConfirmationBucket() {
        logger.atInfo().log("BaseLinkConfirmationBucket instantiated");
    }
}