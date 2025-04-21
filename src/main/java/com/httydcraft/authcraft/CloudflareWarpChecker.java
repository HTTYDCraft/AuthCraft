package com.httydcraft.authcraft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CloudflareWarpChecker {
    private final AuthCraft plugin;
    private final AuditLogger auditLogger;

    public CloudflareWarpChecker(AuthCraft plugin, AuditLogger auditLogger) {
        this.plugin = plugin;
        this.auditLogger = auditLogger;
    }

    public boolean isUsingWarp(String ip) {
        try {
            URL url = new URL("https://1.1.1.1/cdn-cgi/trace");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("X-Forwarded-For", ip);
            conn.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("warp=") && line.equals("warp=on")) {
                    reader.close();
                    return true;
                }
            }
            reader.close();
            return false;
        } catch (Exception e) {
            auditLogger.log("Failed to check Cloudflare Warp for IP " + ip + ": " + e.getMessage());
            return false; // Fallback to allow connection
        }
    }
}
