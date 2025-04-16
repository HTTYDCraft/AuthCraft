package com.httydcraft.authcraft.core.crypto;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.httydcraft.authcraft.api.crypto.HashInput;
import com.httydcraft.authcraft.api.crypto.HashedPassword;
import com.password4j.Hash;
import com.password4j.HashBuilder;
import com.password4j.HashChecker;
import com.password4j.Password;

// #region Class Documentation
/**
 * Abstract base class for cryptographic providers using Password4j.
 * Implements the {@link CryptoProvider} interface for hashing and verification.
 */
public abstract class Password4jCryptoProvider implements CryptoProvider {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final String identifier;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code Password4jCryptoProvider}.
     *
     * @param identifier The unique identifier for this provider. Must not be null.
     */
    public Password4jCryptoProvider(String identifier) {
        this.identifier = Preconditions.checkNotNull(identifier, "identifier must not be null");
        LOGGER.atInfo().log("Initialized Password4jCryptoProvider with identifier: %s", identifier);
    }
    // #endregion

    // #region CryptoProvider Implementation
    /**
     * Hashes the input using Password4j with a random salt.
     *
     * @param input The input to hash. Must not be null.
     * @return The hashed password.
     */
    @Override
    public HashedPassword hash(HashInput input) {
        Preconditions.checkNotNull(input, "input must not be null");
        LOGGER.atFine().log("Hashing input for provider: %s", identifier);
        Hash result = build(Password.hash(input.getRawInput()).addRandomSalt());
        HashedPassword hashedPassword = HashedPassword.of(result.getResult(), result.getSalt(), this);
        LOGGER.atFine().log("Generated hashed password for provider: %s", identifier);
        return hashedPassword;
    }

    /**
     * Verifies if the input matches the hashed password.
     *
     * @param input The input to verify. Must not be null.
     * @param password The hashed password to check against. Must not be null.
     * @return {@code true} if the input matches, {@code false} otherwise.
     */
    @Override
    public boolean matches(HashInput input, HashedPassword password) {
        Preconditions.checkNotNull(input, "input must not be null");
        Preconditions.checkNotNull(password, "password must not be null");
        LOGGER.atFine().log("Verifying input against hashed password for provider: %s", identifier);
        boolean result = build(Password.check(input.getRawInput(), password.getHash()));
        LOGGER.atFine().log("Verification result: %b for provider: %s", result, identifier);
        return result;
    }

    /**
     * Gets the unique identifier for this provider.
     *
     * @return The provider identifier.
     */
    @Override
    public String getIdentifier() {
        LOGGER.atFine().log("Retrieved identifier: %s", identifier);
        return identifier;
    }
    // #endregion

    // #region Abstract Methods
    /**
     * Builds a hash using the provided builder.
     *
     * @param builder The hash builder. Must not be null.
     * @return The computed {@link Hash}.
     */
    public abstract Hash build(HashBuilder builder);

    /**
     * Verifies a hash using the provided checker.
     *
     * @param hashChecker The hash checker. Must not be null.
     * @return {@code true} if the hash matches, {@code false} otherwise.
     */
    public abstract boolean build(HashChecker hashChecker);
    // #endregion
}