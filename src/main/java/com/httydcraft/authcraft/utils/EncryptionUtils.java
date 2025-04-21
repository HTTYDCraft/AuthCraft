package com.httydcraft.authcraft.utils;

import com.httydcraft.authcraft.AuthCraft;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class for encryption operations (complements CryptManager).
 *
 * @author HttyDCraft
 * @version 1.0.3
 */
public class EncryptionUtils {
    private final AuthCraft plugin;
    private final AuditLogger auditLogger;

    public EncryptionUtils(AuthCraft plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
        this.auditLogger = plugin.getAuditLogger();
    }

    public String encrypt(String data, String key) {
        if (data == null || key == null) {
            auditLogger.log("Cannot encrypt with null data or key");
            return null;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            auditLogger.log("Encryption failed: " + e.getMessage());
            return null;
        }
    }

    public String decrypt(String encryptedData, String key) {
        if (encryptedData == null || key == null) {
            auditLogger.log("Cannot decrypt with null data or key");
            return null;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            auditLogger.log("Decryption failed: " + e.getMessage());
            return null;
        }
    }
}
