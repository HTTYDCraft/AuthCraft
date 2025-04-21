package com.httydcraft.authcraft.database;

public class PlayerData {
    private final String identifier;
    private final String username;
    private final String password;
    private final String twofaMethod;
    private final String twofaData;
    private final String role;
    private final long lastLogin;

    public PlayerData(String identifier, String username, String password, String twofaMethod, String twofaData, String role, long lastLogin) {
        this.identifier = identifier;
        this.username = username;
        this.password = password;
        this.twofaMethod = twofaMethod;
        this.twofaData = twofaData;
        this.role = role;
        this.lastLogin = lastLogin;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getTwofaMethod() {
        return twofaMethod;
    }

    public String getTwofaData() {
        return twofaData;
    }

    public String getRole() {
        return role;
    }

    public long getLastLogin() {
        return lastLogin;
    }
}
