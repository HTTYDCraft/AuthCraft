package com.httydcraft.authcraft;

public class UtilsManager {
    private final AuditLogger auditLogger;
    private final CacheManager cacheManager;
    private final ConnectionLimiter connectionLimiter;
    private final CryptManager cryptManager;
    private final MessageUtils messageUtils;
    private final MojangAuth mojangAuth;
    private final PasswordValidator passwordValidator;
    private final TOTPUtils totpUtils;
    private final CloudflareWarpChecker cloudflareWarpChecker;

    public UtilsManager(AuthCraft plugin) {
        this.auditLogger = new AuditLogger(plugin);
        this.cacheManager = new CacheManager();
        this.messageUtils = new MessageUtils(plugin);
        this.connectionLimiter = new ConnectionLimiter(plugin, messageUtils);
        this.cryptManager = new CryptManager(plugin);
        this.mojangAuth = new MojangAuth(plugin);
        this.passwordValidator = new PasswordValidator();
        this.totpUtils = new TOTPUtils(plugin, cryptManager);
        this.cloudflareWarpChecker = new CloudflareWarpChecker(plugin, auditLogger);
    }

    public AuditLogger getAuditLogger() {
        return auditLogger;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public ConnectionLimiter getConnectionLimiter() {
        return connectionLimiter;
    }

    public CryptManager getCryptManager() {
        return cryptManager;
    }

    public MessageUtils getMessageUtils() {
        return messageUtils;
    }

    public MojangAuth getMojangAuth() {
        return mojangAuth;
    }

    public PasswordValidator getPasswordValidator() {
        return passwordValidator;
    }

    public TOTPUtils getTOTPUtils() {
        return totpUtils;
    }

    public CloudflareWarpChecker getCloudflareWarpChecker() {
        return cloudflareWarpChecker;
    }
}
