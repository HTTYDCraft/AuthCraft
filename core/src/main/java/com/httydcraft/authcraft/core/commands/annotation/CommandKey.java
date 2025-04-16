package com.httydcraft.authcraft.core.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import revxrsal.commands.annotation.DistributeOnMethods;

// #region Annotation Documentation
/**
 * Annotation specifying the configuration key for a command.
 * Used to map commands to their configuration settings.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@DistributeOnMethods
public @interface CommandKey {
    /**
     * The configuration key for the command.
     *
     * @return The configuration key.
     */
    String value();
}
// #endregion