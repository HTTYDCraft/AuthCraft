package com.httydcraft.authcraft.api.resource.impl;

import java.io.InputStream;

import com.httydcraft.authcraft.api.resource.Resource;

public class DefaultResource implements Resource {
    private final String name;
    private final InputStream stream;

    public DefaultResource(String name, InputStream stream) {
        this.name = name;
        this.stream = stream;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public InputStream getStream() {
        return stream;
    }
}
