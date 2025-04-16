package com.httydcraft.authcraft.core.crypto;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.httydcraft.authcraft.api.crypto.HashInput;
import com.httydcraft.authcraft.api.crypto.HashedPassword;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// #region Class Documentation
/**
 * A cryptographic provider using Java's MessageDigest.
 * Supports hashing without salt for backward compatibility.
 */
public class MessageDigestCryptoProvider implements CryptoProvider {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final String FORMAT = "%032x";
    private final String identifier;
    private final MessageDigest digest;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code MessageDigestCryptoProvider}.
     *
     * @param identifier The unique identifier for this provider. Must not be null.
     * @param digest The message digest algorithm to use. Must not be null.
     */
    public MessageDigestCryptoProvider(String identifier, MessageDigest digest) {
        this.identifier = Preconditions.checkNotNull(identifier, "identifier must not be null");
        this.digest = Preconditions.checkNotNull(digest, "digest must not be null");
        LOGGER.atInfo().log("Initialized MessageDigestCryptoProvider with identifier: %s", identifier);
    }
    // #endregion

    // #region CryptoProvider Implementation
    /**
     * Hashes the input using the configured MessageDigest.
     *
     * @param input The input to hash. Must not be null.
     * @return The hashed password.
     */
    @Override
    public HashedPassword hash(HashInput input) {
        Preconditions.checkNotNull(input, "input must not be null");
        LOGGER.atFine().log("Hashing input for provider: %s", identifier);
        String hashed = hash(input.getRawInput());
        HashedPassword hashedPassword = HashedPassword.of(hashed, this);
        LOGGER.atFine().log("Generated hashed password: %s", hashed);
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
        boolean result = MessageDigest.isEqual(
                hash(input.getRawInput()).getBytes(StandardCharsets.UTF_8),
                password.getHash().getBytes(StandardCharsets.UTF_8));
        LOGGER.atFine().log("Verification result: %b", result);
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

    // #region Helper Methods
    /**
     * Computes the hash of the input string using the configured digest.
     *
     * @param input The input string to hash. Must not be null.
     * @return The hexadecimal hash string.
     */
    private String hash(String input) {
        Preconditions.checkNotNull(input, "input must not be null");
        String result = String.format(FORMAT, new BigInteger(1, digest.digest(input.getBytes(StandardCharsets.UTF_8))));
        LOGGER.atFine().log("Computed hash: %s", result);
        return result;
    }
    // #endregion
}