package com.httydcraft.authcraft.core.util;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;

import java.util.Random;

// #region Class Documentation
/**
 * Utility class for generating random codes.
 * Supports generating alphanumeric codes of specified length.
 */
public final class RandomCodeFactory {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final char[] LETTERS = "abcdefghijklmnopqrstuvwxyz".toUpperCase().toCharArray();
    private static final char[] NUMBERS = "1234567890".toCharArray();
    private static final Random RANDOM = new Random();

    private RandomCodeFactory() {
        throw new AssertionError("RandomCodeFactory cannot be instantiated");
    }
    // #endregion

    // #region Random Number Generation
    /**
     * Generates a random integer between min and max (inclusive).
     *
     * @param min The minimum value.
     * @param max The maximum value.
     * @return A random integer, or -1 if min >= max.
     */
    public static int random(int min, int max) {
        if (min >= max) {
            LOGGER.atWarning().log("Invalid range: min (%d) >= max (%d)", min, max);
            return -1;
        }
        int result = RANDOM.nextInt(max - min + 1) + min;
        LOGGER.atFine().log("Generated random number: %d in range [%d, %d]", result, min, max);
        return result;
    }
    // #endregion

    // #region Character Generation
    /**
     * Generates a random character from letters or numbers.
     *
     * @return A random letter or number.
     */
    public static char generateRandomCharacter() {
        boolean isLetter = random(0, 1) == 1;
        char[] source = isLetter ? LETTERS : NUMBERS;
        int index = random(0, source.length - 1);
        char result = source[index];
        LOGGER.atFine().log("Generated random character: %c (%s)", result, isLetter ? "letter" : "number");
        return result;
    }

    /**
     * Generates a random character from a specified character set.
     *
     * @param characters The character set to choose from. Must not be null or empty.
     * @return A random character.
     * @throws IllegalArgumentException If characters is empty.
     */
    public static char generateRandomCharacter(String characters) {
        Preconditions.checkNotNull(characters, "characters must not be null");
        if (characters.isEmpty()) {
            LOGGER.atWarning().log("Character set is empty");
            throw new IllegalArgumentException("Character set must not be empty");
        }
        char[] charactersArray = characters.toCharArray();
        int index = random(0, charactersArray.length - 1);
        char result = charactersArray[index];
        LOGGER.atFine().log("Generated random character: %c from custom set", result);
        return result;
    }
    // #endregion

    // #region Code Generation
    /**
     * Generates a random alphanumeric code of specified length.
     *
     * @param length The length of the code. Must be non-negative.
     * @return The generated code, or empty string if length <= 0.
     */
    public static String generateCode(int length) {
        if (length <= 0) {
            LOGGER.atFine().log("Invalid code length: %d, returning empty string", length);
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(generateRandomCharacter());
        }
        String result = stringBuilder.toString();
        LOGGER.atFine().log("Generated code: %s (length: %d)", result, length);
        return result;
    }

    /**
     * Generates a random code of specified length from a custom character set.
     *
     * @param length The length of the code. Must be non-negative.
     * @param characters The character set to use. Must not be null or empty.
     * @return The generated code, or empty string if length <= 0.
     * @throws IllegalArgumentException If characters is empty.
     */
    public static String generateCode(int length, String characters) {
        Preconditions.checkNotNull(characters, "characters must not be null");
        if (length <= 0) {
            LOGGER.atFine().log("Invalid code length: %d, returning empty string", length);
            return "";
        }
        if (characters.isEmpty()) {
            LOGGER.atWarning().log("Character set is empty");
            throw new IllegalArgumentException("Character set must not be empty");
        }
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(generateRandomCharacter(characters));
        }
        String result = stringBuilder.toString();
        LOGGER.atFine().log("Generated code: %s (length: %d, custom set)", result, length);
        return result;
    }
    // #endregion
}