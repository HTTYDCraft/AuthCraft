package com.httydcraft.authcraft.database;

import com.zaxxer.hikari.HikariConfig;

import java.sql.SQLException;

public interface Database {
    HikariConfig configureHikari();
    void initializeTables() throws SQLException;
    void backup() throws SQLException;
}
