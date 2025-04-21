package com.httydcraft.authcraft.utils;

import com.httydcraft.authcraft.AuthCraft;

public class UtilsManager {
    private final MessageUtils messageUtils;
    private final PasswordValidator passwordValidator;
    private final AuditLogger auditLogger;
    private final CryptManager cryptManager;
    private final CacheManager cacheManager;

    public UtilsManager(AuthCraft plugin) {
        this.messageUtils = new MessageUtils(plugin);
        this.passwordValidator = new PasswordValidator(plugin);
        this.auditLogger = new AuditLogger(plugin);
        this.cryptManager = new CryptManager(plugin);
        this.cacheManager = new CacheManager();
    }

    public MessageUtils getMessageUtils() {
        return messageUtils;
    }

    public PasswordValidator getPasswordValidator() {
        return passwordValidator;
    }

    public AuditLogger getAuditLogger() {
        return auditLogger;
    }

    public CryptManager getCryptManager() {
        return cryptManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }
}