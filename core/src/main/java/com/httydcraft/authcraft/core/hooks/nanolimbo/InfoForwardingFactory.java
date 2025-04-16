package com.httydcraft.authcraft.core.hooks.nanolimbo;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import ua.nanit.limbo.server.data.InfoForwarding;
import ua.nanit.limbo.server.data.InfoForwarding.Type;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// #region Class Documentation
/**
 * Factory for creating {@link InfoForwarding} instances with different configurations.
 * Uses reflection to initialize fields in a controlled manner.
 */
public class InfoForwardingFactory {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Factory Methods
    /**
     * Creates an {@link InfoForwarding} instance with no forwarding.
     *
     * @return The configured {@link InfoForwarding}.
     */
    public InfoForwarding none() {
        Map<String, Object> map = Collections.singletonMap("type", Type.NONE);
        InfoForwarding forwarding = createForwarding(map);
        LOGGER.atFine().log("Created NONE InfoForwarding");
        return forwarding;
    }

    /**
     * Creates an {@link InfoForwarding} instance with legacy forwarding.
     *
     * @return The configured {@link InfoForwarding}.
     */
    public InfoForwarding legacy() {
        Map<String, Object> map = Collections.singletonMap("type", Type.LEGACY);
        InfoForwarding forwarding = createForwarding(map);
        LOGGER.atFine().log("Created LEGACY InfoForwarding");
        return forwarding;
    }

    /**
     * Creates an {@link InfoForwarding} instance with modern forwarding.
     *
     * @param secretKey The secret key for modern forwarding. Must not be null.
     * @return The configured {@link InfoForwarding}.
     */
    public InfoForwarding modern(byte[] secretKey) {
        Preconditions.checkNotNull(secretKey, "secretKey must not be null");
        Map<String, Object> map = new HashMap<>();
        map.put("type", Type.MODERN);
        map.put("secretKey", secretKey);
        InfoForwarding forwarding = createForwarding(map);
        LOGGER.atFine().log("Created MODERN InfoForwarding");
        return forwarding;
    }

    /**
     * Creates an {@link InfoForwarding} instance with BungeeGuard forwarding.
     *
     * @param tokens The collection of tokens. Must not be null.
     * @return The configured {@link InfoForwarding}.
     */
    public InfoForwarding bungeeGuard(Collection<String> tokens) {
        Preconditions.checkNotNull(tokens, "tokens must not be null");
        Map<String, Object> map = new HashMap<>();
        map.put("type", Type.BUNGEE_GUARD);
        map.put("tokens", new ArrayList<>(tokens));
        InfoForwarding forwarding = createForwarding(map);
        LOGGER.atFine().log("Created BUNGEE_GUARD InfoForwarding with %d tokens", tokens.size());
        return forwarding;
    }
    // #endregion

    // #region Helper Methods
    /**
     * Creates an {@link InfoForwarding} instance by setting fields via reflection.
     *
     * @param map The map of field names to values. Must not be null.
     * @return The configured {@link InfoForwarding}.
     */
    private InfoForwarding createForwarding(Map<String, Object> map) {
        Preconditions.checkNotNull(map, "map must not be null");
        InfoForwarding forwarding = new InfoForwarding();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            try {
                Field field = InfoForwarding.class.getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(forwarding, entry.getValue());
                LOGGER.atFine().log("Set field %s in InfoForwarding", entry.getKey());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOGGER.atWarning().withCause(e).log("Failed to set field %s in InfoForwarding", entry.getKey());
            }
        }
        return forwarding;
    }
    // #endregion
}