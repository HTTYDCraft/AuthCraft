package com.httydcraft.authcraft.utils;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

import java.security.SecureRandom;

public class TOTPUtils {
    private static final SecureRandom random = new SecureRandom();
    private static final int TIME_STEP = 30;
    private static final int DIGITS = 6;

    public static String generateSecret() {
        byte[] buffer = new byte[20];
        random.nextBytes(buffer);
        StringBuilder secret = new StringBuilder();
        for (byte b : buffer) {
            secret.append(String.format("%02x", b));
        }
        return secret.toString();
    }

    public static String getQRCode(String username, String issuer, String secret) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%d&period=%d",
                issuer, username, secret, issuer, DIGITS, TIME_STEP);
    }

    public static boolean verifyCode(String secret, String code) {
        try {
            TimeProvider timeProvider = new SystemTimeProvider();
            CodeGenerator codeGenerator = new DefaultCodeGenerator();
            CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
            verifier.setTimePeriod(TIME_STEP);
            verifier.setAllowedTimePeriodDiscrepancy(1);
            return verifier.isValidCode(secret, code);
        } catch (Exception e) {
            return false;
        }
    }
}