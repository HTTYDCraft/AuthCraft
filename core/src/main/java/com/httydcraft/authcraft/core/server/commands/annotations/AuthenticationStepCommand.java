package com.httydcraft.authcraft.core.server.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// #region Annotation Documentation
/**
 * Marks a command method as part of an authentication step.
 * Specifies the name of the authentication step the command corresponds to.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuthenticationStepCommand {
    /**
     * The name of the authentication step.
     *
     * @return The step name.
     */
    String stepName();
}
// #endregion