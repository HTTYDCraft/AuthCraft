package com.httydcraft.authcraft.utils;

import com.httydcraft.authcraft.AuthCraft;

public class PasswordValidator {
    private final AuthCraft plugin;
    private final int minLength;
    private final int maxLength;

    public PasswordValidator(AuthCraft plugin) {
        this.plugin = plugin;
        this.minLength = plugin.getConfig().getInt("password.min_length", 8);
        this.maxLength = plugin.getConfig().getInt("password.max_length", 32);
    }

    public boolean isValid(String password, String username) {
        if (password == null || password.length() < minLength || password.length() > maxLength || password.equalsIgnoreCase(username)) {
            return false;
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }
}