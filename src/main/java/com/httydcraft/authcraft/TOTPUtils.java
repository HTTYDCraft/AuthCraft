package com.httydcraft.authcraft;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import java.util.Base64;

import static dev.samstevens.totp.code.HashingAlgorithm.SHA1;

public class TOTPUtils {
    private final AuthCraft plugin;
    private final CryptManager cryptManager;
    private final CodeVerifier verifier;

    public TOTPUtils(AuthCraft plugin, CryptManager cryptManager) {
        this.plugin = plugin;
        this.cryptManager = cryptManager;
        this.verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
    }

    public String generateSecret() {
        String secret = new DefaultSecretGenerator().generate();
        return cryptManager.encrypt(secret);
    }

    public String getQrCodeUrl(String username, String secret) {
        QrData data = new QrData.Builder()
                .label(username)
                .secret(cryptManager.decrypt(secret))
                .issuer("AuthCraft")
                .algorithm(SHA1)
                .digits(6)
                .period(30)
                .build();
        try {
            QrGenerator generator = new ZxingPngQrGenerator();
            byte[] imageData = generator.generate(data);
            String mimeType = generator.getImageMimeType();
            String dataUri = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imageData);
            return dataUri;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to generate QR code: " + e.getMessage());
            return "";
        }
    }

    public boolean verifyCode(String secret, String code) {
        return verifier.isValidCode(cryptManager.decrypt(secret), code);
    }

    // Получить исходный (plaintext) секрет из зашифрованного
    public String getPlainSecret(String encryptedSecret) {
        return cryptManager.decrypt(encryptedSecret);
    }
}
