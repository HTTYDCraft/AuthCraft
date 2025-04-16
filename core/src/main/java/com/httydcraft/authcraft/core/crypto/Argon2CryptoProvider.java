package com.httydcraft.authcraft.core.crypto;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.password4j.Hash;
import com.password4j.HashBuilder;
import com.password4j.HashChecker;

// #region Class Documentation
/**
 * A cryptographic provider for Argon2 hashing.
 * Extends {@link Password4jCryptoProvider} to implement Argon2-specific hashing.
 */
public class Argon2CryptoProvider extends Password4jCryptoProvider {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code Argon2CryptoProvider}.
     */
    public Argon2CryptoProvider() {
        super("ARGON2");
        LOGGER.atInfo().log("Initialized Argon2CryptoProvider");
    }
    // #endregion

    // #region Hashing Methods
    /**
     * Builds an Argon2 hash using the provided builder.
     *
     * @param builder The hash builder. Must not be null.
     * @return The computed {@link Hash}.
     */
    @Override
    public Hash build(HashBuilder builder) {
        Preconditions.checkNotNull(builder, "builder must not be null");
        LOGGER.atFine().log("Building Argon2 hash");
        return builder.withArgon2();
    }

    /**
     * Verifies an Argon2 hash using the provided checker.
     *
     * @param hashChecker The hash checker. Must not be null.
     * @return {@code true} if the hash matches, {@code false} otherwise.
     */
    @Override
    public boolean build(HashChecker hashChecker) {
        Preconditions.checkNotNull(hashChecker, "hashChecker must not be null");
        LOGGER.atFine().log("Verifying Argon2 hash");
        return hashChecker.withArgon2();
    }
    // #endregion
}