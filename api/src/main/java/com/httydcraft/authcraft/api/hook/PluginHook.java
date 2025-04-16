package com.httydcraft.authcraft.api.hook;

import com.httydcraft.authcraft.api.util.Castable;

/**
 * Object that provides access to the api
 *
 * @author User
 */
public interface PluginHook extends Castable<PluginHook> {
    boolean canHook();
}
