package com.httydcraft.authcraft.core.server.commands.annotations;

import com.httydcraft.authcraft.core.server.commands.parameters.ArgumentAccount;
import revxrsal.commands.annotation.DistributeOnMethods;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// #region Annotation Documentation
/**
 * Marks a command class as requiring admin privileges.
 * Required for parameters like {@link ArgumentAccount}
 * to prevent usage in non-admin commands.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DistributeOnMethods
public @interface Admin {
}
// #endregion