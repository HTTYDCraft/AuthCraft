package com.httydcraft.authcraft.core.util;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.io.BufferedReader;

// #region Class Documentation
/**
 * Utility class for downloading files and verifying checksums.
 * Provides methods to download files from URLs and check their integrity.
 */
public final class DownloadUtil {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    private DownloadUtil() {
        throw new AssertionError("DownloadUtil cannot be instantiated");
    }
    // #endregion

    // #region Download Methods
    /**
     * Downloads a file from a specified URL to a destination file.
     *
     * @param url The URL to download from. Must not be null.
     * @param destinationFile The file to save the downloaded content to. Must not be null.
     * @throws IOException If an I/O error occurs during the download.
     */
    public static void downloadFile(URL url, File destinationFile) throws IOException {
        Preconditions.checkNotNull(url, "url must not be null");
        Preconditions.checkNotNull(destinationFile, "destinationFile must not be null");
        LOGGER.atFine().log("Downloading file from %s to %s", url, destinationFile.getAbsolutePath());

        File absoluteFile = destinationFile.getAbsoluteFile();
        if (!destinationFile.exists()) {
            File parentFile = absoluteFile.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
                LOGGER.atFine().log("Created parent directories: %s", parentFile.getAbsolutePath());
            }
            absoluteFile.createNewFile();
            LOGGER.atFine().log("Created new file: %s", absoluteFile.getAbsolutePath());
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(absoluteFile);
             ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
             FileChannel fileChannel = fileOutputStream.getChannel()) {
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            LOGGER.atInfo().log("Successfully downloaded file to %s", absoluteFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.atSevere().withCause(e).log("Failed to download file from %s to %s", url, absoluteFile.getAbsolutePath());
            throw e;
        }
    }
    // #endregion

    // #region Checksum Methods
    /**
     * Verifies the checksum of a file at the specified URL against a given checksum.
     *
     * @param mappedUrl The URL to check the checksum from. Must not be null.
     * @param fileCheckSum The expected checksum. Must not be null.
     * @return {@code true} if the checksum matches, {@code false} otherwise.
     */
    public static boolean checkSum(URL mappedUrl, String fileCheckSum) {
        return checkSum(mappedUrl, fileCheckSum, false);
    }

    /**
     * Verifies the checksum of a file at the specified URL against a given checksum.
     *
     * @param mappedUrl The URL to check the checksum from. Must not be null.
     * @param fileCheckSum The expected checksum. Must not be null.
     * @param catchInvalidUrl If {@code true}, returns {@code false} for invalid URLs instead of throwing an exception.
     * @return {@code true} if the checksum matches, {@code false} otherwise.
     */
    public static boolean checkSum(URL mappedUrl, String fileCheckSum, boolean catchInvalidUrl) {
        Preconditions.checkNotNull(mappedUrl, "mappedUrl must not be null");
        Preconditions.checkNotNull(fileCheckSum, "fileCheckSum must not be null");
        LOGGER.atFine().log("Checking checksum for URL: %s", mappedUrl);

        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(mappedUrl.openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            boolean result = stringBuilder.toString().equals(fileCheckSum);
            LOGGER.atFine().log("Checksum verification result: %b", result);
            return result;
        } catch (IOException e) {
            if (catchInvalidUrl && e instanceof java.io.FileNotFoundException) {
                LOGGER.atFine().log("Invalid URL caught and ignored: %s", mappedUrl);
                return false;
            }
            LOGGER.atWarning().withCause(e).log("Failed to verify checksum for URL: %s", mappedUrl);
            return false;
        }
    }
    // #endregion
}