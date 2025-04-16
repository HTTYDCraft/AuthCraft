/* BaseCryptoProviderBucket.java */
/**
 * A specialized bucket for holding CryptoProvider instances.
 * Provides a type-safe container with bucket management capabilities.
 */
package com.httydcraft.authcraft.core.bucket;

import com.httydcraft.authcraft.api.bucket.CryptoProviderBucket;
import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.google.common.flogger.GoogleLogger;

public class BaseCryptoProviderBucket extends BaseListBucket<CryptoProvider> implements CryptoProviderBucket {

    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    /**
     * Initializes a bucket specifically for CryptoProviders.
     */
    public BaseCryptoProviderBucket() {
        logger.atInfo().log("BaseCryptoProviderBucket instantiated");
    }
}
