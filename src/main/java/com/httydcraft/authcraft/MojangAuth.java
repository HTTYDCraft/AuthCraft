package com.httydcraft.authcraft;

public class MojangAuth {
    private final AuthCraft plugin;

    public MojangAuth(AuthCraft plugin) {
        this.plugin = plugin;
    }

    public boolean verifyPlayer(String username, String uuid) {
        // Simplified: Assume valid for offline mode
        return true;
    }
}
