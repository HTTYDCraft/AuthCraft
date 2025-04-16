package com.httydcraft.authcraft.core.crypto;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.httydcraft.authcraft.api.crypto.HashInput;
import com.httydcraft.authcraft.api.crypto.HashedPassword;
import org.mindrot.jbcrypt.BCrypt;

// #region Class Documentation
/**
 * A cryptographic provider using BCrypt hashing.
 * Implements the {@link CryptoProvider} interface for secure password hashing.
 */
public class BcryptCryptoProvider implements CryptoProvider {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    // #endregion

    // #region CryptoProvider Implementation
    /**
     * Hashes the input using BCrypt with a generated salt.
     *
     * @param input The input to hash. Must not be null.
     * @return The hashed password.
     */
    @Override
    public HashedPassword hash(HashInput input) {
        Preconditions.checkNotNull(input, "input must not be null");
        LOGGER.atFine().log("Hashing input with BCrypt");
        String salt = BCrypt.gensalt();
        String hashed = BCrypt.hashpw(input.getRawInput(), salt);
        HashedPassword hashedPassword = HashedPassword.of(hashed, salt, this);
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
        LOGGER.atFine().log("Verifying input against hashed password with BCrypt");
        boolean result = BCrypt.checkpw(input.getRawInput(), password.getHash());
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
        LOGGER.atFine().log("Retrieved identifier: BCRYPT");
        return "BCRYPT";
    }
    // #endregion
}