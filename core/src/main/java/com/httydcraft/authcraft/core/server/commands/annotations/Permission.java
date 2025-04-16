package com.httydcraft.authcraft.core.server.commands.annotations;

import revxrsal.commands.annotation.DistributeOnMethods;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// #region Annotation Documentation
/**
 * Specifies a permission required for a command class.
 * Applied to command classes to restrict access based on the specified permission.
 */
@DistributeOnMethods
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Permission {
    /**
     * The permission string required to execute the command.
     *
     * @return The permission value.
     */
    String value();
}
// #endregion