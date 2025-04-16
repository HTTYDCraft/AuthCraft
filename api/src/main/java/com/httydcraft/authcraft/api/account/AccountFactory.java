package com.httydcraft.authcraft.api.account;

import java.util.UUID;

import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.httydcraft.authcraft.api.type.IdentifierType;

public interface AccountFactory {
    long DEFAULT_TELEGRAM_ID = -1;
    int DEFAULT_VK_ID = -1;

    Account createAccount(String id, IdentifierType identifierType, UUID uuid, String name, CryptoProvider cryptoProvider, String passwordHash, String lastIp);
}