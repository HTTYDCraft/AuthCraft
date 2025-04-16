package com.httydcraft.authcraft.api.resource;

import com.httydcraft.authcraft.api.resource.impl.DefaultResource;

public interface ResourceReader<T extends Resource> {
    static ResourceReader<Resource> defaultReader(ClassLoader classLoader, String resourceName) {
        return () -> new DefaultResource(resourceName, classLoader.getResourceAsStream(resourceName));
    }

    T read();
}
