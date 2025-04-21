package com.httydcraft.authcraft;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class CryptManager {
    private final AuthCraft plugin;
    private final SecretKeySpec key;

    public CryptManager(AuthCraft plugin) {
        this.plugin = plugin;
        String keyStr = plugin.getConfig().getString("encryption.key", generateDefaultKey());
        this.key = createKey(keyStr);
    }

    private String generateDefaultKey() {
        String defaultKey = "default_authcraft_key_16";
        plugin.getConfig().set("encryption.key", defaultKey);
        plugin.saveConfig();
        return defaultKey;
    }

    private SecretKeySpec createKey(String keyStr) {
        try {
            byte[] keyBytes = keyStr.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);
            byte[] key16 = new byte[16];
            System.arraycopy(keyBytes, 0, key16, 0, 16);
            return new SecretKeySpec(key16, "AES");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create encryption key: " + e.getMessage());
            return null;
        }
    }

    public String encrypt(String data) {
        if (key == null) {
            return data;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            plugin.getLogger().warning("Encryption failed: " + e.getMessage());
            return data;
        }
    }

    public String decrypt(String encryptedData) {
        if (key == null) {
            return encryptedData;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            plugin.getLogger().warning("Decryption failed: " + e.getMessage());
            return encryptedData;
        }
    }
}
