package com.httydcraft.authcraft.core.server.commands.annotations;

import com.httydcraft.authcraft.api.account.Account;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// #region Annotation Documentation
/**
 * Marks a command parameter as an authentication account.
 * Used to indicate that the parameter represents an {@link Account}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticationAccount {
}
// #endregion