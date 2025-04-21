package com.httydcraft.authcraft.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MojangAuth {
    public boolean verifyPlayer(String username, String uuid) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    String expectedUuid = uuid.replace("-", "");
                    return response.toString().contains(expectedUuid);
                }
            }
        } catch (Exception e) {
            // Log error if needed
        }
        return false;
    }
}