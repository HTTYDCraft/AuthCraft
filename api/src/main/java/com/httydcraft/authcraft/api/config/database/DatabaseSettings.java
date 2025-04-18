package com.httydcraft.authcraft.api.config.database;

import java.io.File;

import com.httydcraft.authcraft.api.config.database.schema.SchemaSettings;

public interface DatabaseSettings {
    String getConnectionUrl();

    String getUsername();

    String getPassword();

    String getDriverDownloadUrl();

    File getCacheDriverPath();

    boolean isMigrationEnabled();

    SchemaSettings getSchemaSettings();
}
