package com.httydcraft.authcraft.core.crypto.authme;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;

import java.security.SecureRandom;
import java.util.Random;

// #region Class Documentation
/**
 * Utility class for generating random strings for AuthMe.
 * Generates strings with digits, letters, or hexadecimal characters.
 * <p>
 * Source: <a href="https://github.com/AuthMe/AuthMeReloaded/blob/master/src/main/java/fr/xephi/authme/util/RandomStringUtils.java">
 * AuthMe RandomStringUtils</a>
 */
public final class SaltUtil {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final char[] CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final Random RANDOM = new SecureRandom();
    private static final int NUM_INDEX = 10;
    private static final int LOWER_ALPHANUMERIC_INDEX = 36;
    private static final int HEX_MAX_INDEX = 16;

    private SaltUtil() {
        throw new AssertionError("SaltUtil cannot be instantiated");
    }
    // #endregion

    // #region String Generation
    /**
     * Generates a random alphanumeric string of the given length (0-9, a-z).
     *
     * @param length The length of the string. Must be non-negative.
     * @return The generated string.
     * @throws IllegalArgumentException If length is negative.
     */
    public static String generate(int length) {
        String result = generateString(length, LOWER_ALPHANUMERIC_INDEX);
        LOGGER.atFine().log("Generated alphanumeric string of length %d: %s", length, result);
        return result;
    }

    /**
     * Generates a random hexadecimal string of the given length (0-9, a-f).
     *
     * @param length The length of the string. Must be non-negative.
     * @return The generated hexadecimal string.
     * @throws IllegalArgumentException If length is negative.
     */
    public static String generateHex(int length) {
        String result = generateString(length, HEX_MAX_INDEX);
        LOGGER.atFine().log("Generated hexadecimal string of length %d: %s", length, result);
        return result;
    }

    /**
     * Generates a random numeric string of the given length (0-9).
     *
     * @param length The length of the string. Must be non-negative.
     * @return The generated numeric string.
     * @throws IllegalArgumentException If length is negative.
     */
    public static String generateNum(int length) {
        String result = generateString(length, NUM_INDEX);
        LOGGER.atFine().log("Generated numeric string of length %d: %s", length, result);
        return result;
    }

    /**
     * Generates a random string with digits and letters (0-9, a-z, A-Z).
     *
     * @param length The length of the string. Must be non-negative.
     * @return The generated string.
     * @throws IllegalArgumentException If length is negative.
     */
    public static String generateLowerUpper(int length) {
        String result = generateString(length, CHARS.length);
        LOGGER.atFine().log("Generated lower-upper string of length %d: %s", length, result);
        return result;
    }

    /**
     * Generates a random string from the character set up to the specified index.
     *
     * @param length The length of the string. Must be non-negative.
     * @param maxIndex The maximum index in the character set.
     * @return The generated string.
     * @throws IllegalArgumentException If length is negative.
     */
    private static String generateString(int length, int maxIndex) {
        Preconditions.checkArgument(length >= 0, "Length must be non-negative but was %s", length);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            sb.append(CHARS[RANDOM.nextInt(maxIndex)]);
        }
        return sb.toString();
    }
    // #endregion
}