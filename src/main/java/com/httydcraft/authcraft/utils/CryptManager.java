package com.httydcraft.authcraft.utils;

import com.httydcraft.authcraft.AuthCraft;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class CryptManager {
    private final AuthCraft plugin;
    private final byte[] key;

    public CryptManager(AuthCraft plugin) {
        this.plugin = plugin;
        String keyString = plugin.getConfig().getString("encryption.key", generateKey());
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            this.key = sha.digest(keyString.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption key", e);
        }
    }

    private String generateKey() {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            key.append((char) ('a' + (int) (Math.random() * 26)));
        }
        plugin.getConfig().set("encryption.key", key.toString());
        plugin.saveConfig();
        return key.toString();
    }

    public String encrypt(String data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            plugin.getUtilsManager().getAuditLogger().log("Encryption error: " + e.getMessage());
            return null;
        }
    }

    public String decrypt(String encryptedData) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            plugin.getUtilsManager().getAuditLogger().log("Decryption error: " + e.getMessage());
            return null;
        }
    }
}