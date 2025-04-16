package com.httydcraft.authcraft.core.util;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// #region Class Documentation
/**
 * Utility class for computing file and text hashes.
 * Supports MD5 and SHA-256 algorithms.
 */
public final class HashUtils {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final String MD_5_EXTENSION = ".md5";
    private static final MessageDigest MD5;
    private static final MessageDigest SHA256;

    static {
        try {
            MD5 = MessageDigest.getInstance("MD5");
            SHA256 = MessageDigest.getInstance("SHA-256");
            LOGGER.atFine().log("Initialized MD5 and SHA-256 digests");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.atSevere().withCause(e).log("Failed to initialize hash digests");
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }

    private HashUtils() {
        throw new AssertionError("HashUtils cannot be instantiated");
    }
    // #endregion

    // #region Hashing Methods
    /**
     * Computes a hash of the input text using the specified digest.
     *
     * @param input The text to hash. Must not be null.
     * @param messageDigest The digest algorithm to use. Must not be null.
     * @return The hexadecimal hash string.
     */
    public static String hashText(String input, MessageDigest messageDigest) {
        Preconditions.checkNotNull(input, "input must not be null");
        Preconditions.checkNotNull(messageDigest, "messageDigest must not be null");
        LOGGER.atFine().log("Computing hash for text");

        messageDigest.reset();
        messageDigest.update(input.getBytes());
        byte[] digest = messageDigest.digest();
        String result = String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
        LOGGER.atFine().log("Computed hash: %s", result);
        return result;
    }

    /**
     * Maps a URL to its MD5 checksum URL by appending the .md5 extension.
     *
     * @param url The original URL. Must not be null.
     * @return The MD5 checksum URL.
     * @throws MalformedURLException If the resulting URL is invalid.
     */
    public static URL mapToMd5URL(URL url) throws MalformedURLException {
        Preconditions.checkNotNull(url, "url must not be null");
        LOGGER.atFine().log("Mapping URL to MD5: %s", url);

        URL result = new URL(url.toString() + MD_5_EXTENSION);
        LOGGER.atFine().log("Mapped to MD5 URL: %s", result);
        return result;
    }

    /**
     * Computes the checksum of a file using the specified digest.
     *
     * @param file The file to hash. Must not be null.
     * @param messageDigest The digest algorithm to use. Must not be null.
     * @return The hexadecimal checksum, or null if the file does not exist.
     * @throws IOException If an I/O error occurs while reading the file.
     */
    public static String getFileCheckSum(File file, MessageDigest messageDigest) throws IOException {
        Preconditions.checkNotNull(file, "file must not be null");
        Preconditions.checkNotNull(messageDigest, "messageDigest must not be null");
        LOGGER.atFine().log("Computing checksum for file: %s", file.getAbsolutePath());

        if (!file.exists()) {
            LOGGER.atFine().log("File does not exist: %s", file.getAbsolutePath());
            return null;
        }

        byte[] bytes = Files.readAllBytes(file.toPath());
        String result = new BigInteger(1, messageDigest.digest(bytes)).toString(16);
        LOGGER.atFine().log("Computed file checksum: %s", result);
        return result;
    }
    // #endregion

    // #region Digest Accessors
    /**
     * Gets the MD5 digest instance.
     *
     * @return The MD5 {@link MessageDigest}.
     */
    public static MessageDigest getMD5() {
        LOGGER.atFine().log("Retrieved MD5 digest");
        return MD5;
    }

    /**
     * Gets the SHA-256 digest instance.
     *
     * @return The SHA-256 {@link MessageDigest}.
     */
    public static MessageDigest getSHA256() {
        LOGGER.atFine().log("Retrieved SHA-256 digest");
        return SHA256;
    }
    // #endregion
}