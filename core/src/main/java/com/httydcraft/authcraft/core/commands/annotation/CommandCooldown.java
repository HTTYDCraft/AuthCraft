package com.httydcraft.authcraft.core.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

// #region Annotation Documentation
/**
 * Annotation specifying a cooldown period for a command.
 * Prevents the command from being executed until the cooldown expires.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandCooldown {
    /**
     * The default cooldown value in milliseconds.
     */
    int DEFAULT_VALUE = 3000;

    /**
     * The cooldown duration.
     *
     * @return The cooldown duration.
     */
    long value();

    /**
     * The time unit for the cooldown duration.
     *
     * @return The time unit (default: milliseconds).
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;
}
// #endregion