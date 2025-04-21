package com.httydcraft.authcraft;

import org.apache.commons.validator.routines.RegexValidator;

public class PasswordValidator {
    private final RegexValidator passwordValidator;

    public PasswordValidator() {
        this.passwordValidator = new RegexValidator("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,16}$");
    }

    public boolean isValidPassword(String password, String username) {
        if (password == null || username == null) {
            return false;
        }
        if (password.equalsIgnoreCase(username)) {
            return false;
        }
        return passwordValidator.isValid(password);
    }
}
