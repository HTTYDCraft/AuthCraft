package com.httydcraft.authcraft.core.server.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// #region Annotation Documentation
/**
 * Marks a command method as requiring Google integration.
 * Indicates that the command interacts with Google-specific functionality.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GoogleUse {
}
// #endregion