package com.httydcraft.authcraft.core.crypto.authme;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.httydcraft.authcraft.api.crypto.HashInput;
import com.httydcraft.authcraft.api.crypto.HashedPassword;
import com.httydcraft.authcraft.core.util.HashUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// #region Class Documentation
/**
 * A cryptographic provider for AuthMe's SHA-256 hashing.
 * Uses a salt and double SHA-256 hashing.
 */
public class AuthMeSha256CryptoProvider implements CryptoProvider {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final MessageDigest MESSAGE_DIGEST = HashUtils.getSHA256();
    private static final int SALT_LENGTH = 16;

    // #endregion

    // #region CryptoProvider Implementation
    /**
     * Hashes the input using AuthMe's SHA-256 with a random salt.
     *
     * @param input The input to hash. Must not be null.
     * @return The hashed password.
     */
    @Override
    public HashedPassword hash(HashInput input) {
        Preconditions.checkNotNull(input, "input must not be null");
        LOGGER.atFine().log("Hashing input with AuthMe SHA-256");
        String salt = SaltUtil.generateHex(SALT_LENGTH);
        HashedPassword hashedPassword = hash(input.getRawInput(), salt);
        LOGGER.atFine().log("Generated hashed password with salt: %s", salt);
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
        LOGGER.atFine().log("Verifying input against hashed password with AuthMe SHA-256");

        String hash = password.getHash();
        String[] line = hash.split("\\$");
        if (line.length != 4) {
            LOGGER.atFine().log("Invalid hash format, expected 4 parts, got: %d", line.length);
            return false;
        }
        String lineSalt = line[2];
        String hashedPassword = hash(input.getRawInput(), lineSalt).getHash();
        boolean result = MessageDigest.isEqual(
                hashedPassword.getBytes(StandardCharsets.UTF_8),
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
        LOGGER.atFine().log("Retrieved identifier: AUTHME_SHA256");
        return "AUTHME_SHA256";
    }
    // #endregion

    // #region Helper Methods
    /**
     * Hashes the input with the given salt using AuthMe's SHA-256 format.
     *
     * @param input The input string to hash. Must not be null.
     * @param salt The salt to use. Must not be null.
     * @return The hashed password.
     */
    private HashedPassword hash(String input, String salt) {
        Preconditions.checkNotNull(input, "input must not be null");
        Preconditions.checkNotNull(salt, "salt must not be null");
        String hash = HashUtils.hashText(HashUtils.hashText(input, MESSAGE_DIGEST) + salt, MESSAGE_DIGEST);
        String result = "$SHA$" + salt + "$" + hash;
        LOGGER.atFine().log("Computed AuthMe SHA-256 hash: %s", result);
        return HashedPassword.of(result, salt, this);
    }
    // #endregion
}