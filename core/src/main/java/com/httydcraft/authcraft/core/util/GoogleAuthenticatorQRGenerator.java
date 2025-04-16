package com.httydcraft.authcraft.core.util;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import org.apache.http.client.utils.URIBuilder;

// #region Class Documentation
/**
 * Utility class for generating Google Authenticator QR code URLs.
 * Creates otpauth URIs for TOTP authentication.
 * <p>
 * Source: <a href="https://github.com/wstrange/GoogleAuth/blob/master/src/main/java/com/warrenstrange/googleauth/GoogleAuthenticatorQRGenerator.java">
 * Google Authenticator QR Generator</a>
 */
public final class GoogleAuthenticatorQRGenerator {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    private GoogleAuthenticatorQRGenerator() {
        throw new AssertionError("GoogleAuthenticatorQRGenerator cannot be instantiated");
    }
    // #endregion

    // #region Label Formatting
    /**
     * Formats a label for the otpauth URI.
     * The label includes an optional issuer and a mandatory account name, separated by a colon.
     *
     * @param issuer The issuer name, or null if not specified.
     * @param accountName The account name. Must not be null or empty.
     * @return The formatted label.
     * @throws IllegalArgumentException If accountName is empty or issuer contains a colon.
     */
    private static String formatLabel(String issuer, String accountName) {
        Preconditions.checkNotNull(accountName, "accountName must not be null");
        if (accountName.trim().isEmpty()) {
            LOGGER.atWarning().log("Account name is empty");
            throw new IllegalArgumentException("Account name must not be empty.");
        }
        if (issuer != null && issuer.contains(":")) {
            LOGGER.atWarning().log("Issuer contains invalid colon: %s", issuer);
            throw new IllegalArgumentException("Issuer cannot contain the ':' character.");
        }

        StringBuilder sb = new StringBuilder();
        if (issuer != null) {
            sb.append(issuer).append(":");
            LOGGER.atFine().log("Added issuer to label: %s", issuer);
        }
        sb.append(accountName);
        String result = sb.toString();
        LOGGER.atFine().log("Formatted label: %s", result);
        return result;
    }
    // #endregion

    // #region URI Generation
    /**
     * Generates an otpauth TOTP URI for Google Authenticator.
     * The URI includes the secret, label, and optional issuer.
     *
     * @param issuer The issuer name, or null if not specified.
     * @param accountName The account name. Must not be null or empty.
     * @param secret The TOTP secret. Must not be null.
     * @return The otpauth TOTP URI.
     * @throws IllegalArgumentException If accountName is empty, secret is null, or issuer contains a colon.
     */
    public static String getOtpAuthTotpURL(String issuer, String accountName, String secret) {
        Preconditions.checkNotNull(accountName, "accountName must not be null");
        Preconditions.checkNotNull(secret, "secret must not be null");
        LOGGER.atFine().log("Generating otpauth URI for account: %s", accountName);

        URIBuilder uri = new URIBuilder()
                .setScheme("otpauth")
                .setHost("totp")
                .setPath("/" + formatLabel(issuer, accountName))
                .setParameter("secret", secret);

        if (issuer != null) {
            if (issuer.contains(":")) {
                LOGGER.atWarning().log("Issuer contains invalid colon: %s", issuer);
                throw new IllegalArgumentException("Issuer cannot contain the ':' character.");
            }
            uri.setParameter("issuer", issuer);
            LOGGER.atFine().log("Added issuer parameter: %s", issuer);
        }

        String result = uri.toString();
        LOGGER.atInfo().log("Generated otpauth URI: %s", result);
        return result;
    }
    // #endregion
}