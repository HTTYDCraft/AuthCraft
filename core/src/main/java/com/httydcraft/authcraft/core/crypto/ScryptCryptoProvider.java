package com.httydcraft.authcraft.core.crypto;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.password4j.Hash;
import com.password4j.HashBuilder;
import com.password4j.HashChecker;

// #region Class Documentation
/**
 * A cryptographic provider for SCrypt hashing.
 * Extends {@link Password4jCryptoProvider} to implement SCrypt-specific hashing.
 */
public class ScryptCryptoProvider extends Password4jCryptoProvider {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code ScryptCryptoProvider}.
     */
    public ScryptCryptoProvider() {
        super("SCRYPT");
        LOGGER.atInfo().log("Initialized ScryptCryptoProvider");
    }
    // #endregion

    // #region Hashing Methods
    /**
     * Builds a SCrypt hash using the provided builder.
     *
     * @param builder The hash builder. Must not be null.
     * @return The computed {@link Hash}.
     */
    @Override
    public Hash build(HashBuilder builder) {
        Preconditions.checkNotNull(builder, "builder must not be null");
        LOGGER.atFine().log("Building SCrypt hash");
        return builder.withScrypt();
    }

    /**
     * Verifies a SCrypt hash using the provided checker.
     *
     * @param hashChecker The hash checker. Must not be null.
     * @return {@code true} if the hash matches, {@code false} otherwise.
     */
    @Override
    public boolean build(HashChecker hashChecker) {
        Preconditions.checkNotNull(hashChecker, "hashChecker must not be null");
        LOGGER.atFine().log("Verifying SCrypt hash");
        return hashChecker.withScrypt();
    }
    // #endregion
}