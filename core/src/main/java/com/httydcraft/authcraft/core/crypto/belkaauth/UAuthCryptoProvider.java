package com.httydcraft.authcraft.core.crypto.belkaauth;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.httydcraft.authcraft.api.crypto.HashInput;
import com.httydcraft.authcraft.api.crypto.HashedPassword;

// #region Class Documentation
/**
 * A cryptographic provider for UAuth hashing.
 * Uses a fixed salt and UAuth-specific hashing logic.
 */
public class UAuthCryptoProvider implements CryptoProvider {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final String SALT = "saharPidor";

    // #endregion

    // #region CryptoProvider Implementation
    /**
     * Hashes the input using UAuth-specific hashing.
     *
     * @param input The input to hash. Must not be null.
     * @return The hashed password.
     */
    @Override
    public HashedPassword hash(HashInput input) {
        Preconditions.checkNotNull(input, "input must not be null");
        LOGGER.atFine().log("Hashing input with UAuth");
        String hashed = UAuthUtil.getHash(SALT, input.getRawInput());
        HashedPassword hashedPassword = HashedPassword.of(hashed, SALT, this);
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
        LOGGER.atFine().log("Verifying input against hashed password with UAuth");
        String hashedInput = hash(input).getHash();
        boolean result = hashedInput != null && hashedInput.equals(password.getHash());
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
        LOGGER.atFine().log("Retrieved identifier: UAUTH_UNSAFE");
        return "UAUTH_UNSAFE";
    }
    // #endregion
}