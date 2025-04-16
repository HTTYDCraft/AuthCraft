package com.httydcraft.authcraft.core.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// #region Annotation Documentation
/**
 * Annotation indicating an error message key for missing or invalid arguments.
 * Used to customize error messages for command execution.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ConfigurationArgumentError {
    /**
     * The message key for the error.
     *
     * @return The message key.
     */
    String value();
}
// #endregion