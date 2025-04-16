package com.httydcraft.authcraft.api.bucket;

import java.util.Optional;

import com.httydcraft.authcraft.api.crypto.CryptoProvider;

public interface CryptoProviderBucket extends Bucket<CryptoProvider> {

    @Deprecated
    default Optional<CryptoProvider> findCryptoProvider(String identifier) {
        return findFirstByValue(CryptoProvider::getIdentifier, identifier);
    }

    @Deprecated
    default void addCryptoProvider(CryptoProvider cryptoProvider) {
        modifiable().add(cryptoProvider);
    }

}
