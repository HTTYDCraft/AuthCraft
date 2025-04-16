package com.httydcraft.authcraft.core.util;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Logger;

// #region Class Documentation
/**
 * Utility class for loading and registering JDBC drivers.
 * Supports loading drivers from file paths or URLs.
 */
public final class DriverUtil {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();

    private DriverUtil() {
        throw new AssertionError("DriverUtil cannot be instantiated");
    }
    // #endregion

    // #region Driver Loading Methods
    /**
     * Loads a JDBC driver from a file path.
     *
     * @param driverPath The file path to the driver. Must not be null.
     * @param classLoader The class loader to use. Must not be null.
     * @return {@code true} if the driver was loaded successfully, {@code false} otherwise.
     */
    public static boolean loadDriver(File driverPath, ClassLoader classLoader) {
        Preconditions.checkNotNull(driverPath, "driverPath must not be null");
        Preconditions.checkNotNull(classLoader, "classLoader must not be null");
        LOGGER.atFine().log("Loading driver from file: %s", driverPath.getAbsolutePath());

        try {
            return loadDriver(driverPath.toURI().toURL(), classLoader);
        } catch (MalformedURLException e) {
            LOGGER.atWarning().withCause(e).log("Invalid URL for driver file: %s", driverPath.getAbsolutePath());
            return false;
        }
    }

    /**
     * Loads a JDBC driver from a URL.
     *
     * @param driverUrl The URL to the driver. Must not be null.
     * @param classLoader The class loader to use. Must not be null.
     * @return {@code true} if the driver was loaded successfully, {@code false} otherwise.
     */
    public static boolean loadDriver(URL driverUrl, ClassLoader classLoader) {
        Preconditions.checkNotNull(driverUrl, "driverUrl must not be null");
        Preconditions.checkNotNull(classLoader, "classLoader must not be null");
        LOGGER.atFine().log("Loading driver from URL: %s", driverUrl);

        try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{driverUrl}, classLoader)) {
            ServiceLoader<Driver> drivers = ServiceLoader.load(Driver.class, urlClassLoader);
            Iterator<Driver> iterator = drivers.iterator();
            boolean loaded = false;
            while (iterator.hasNext()) {
                try {
                    Driver driver = iterator.next();
                    Driver newDriver = (Driver) Class.forName(driver.getClass().getName(), true, urlClassLoader)
                            .getDeclaredConstructor()
                            .newInstance();
                    DriverManager.registerDriver(new DelegatingDriver(newDriver));
                    loaded = true;
                    LOGGER.atFine().log("Registered driver: %s", driver.getClass().getName());
                } catch (Exception e) {
                    LOGGER.atFine().log("Skipping invalid driver: %s", e.getMessage());
                }
            }
            LOGGER.atInfo().log("Driver loading %s: %b", driverUrl, loaded);
            return loaded;
        } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException
                 | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.atWarning().withCause(e).log("Failed to load driver from URL: %s", driverUrl);
            return false;
        }
    }
    // #endregion

    // #region DelegatingDriver
    /**
     * A wrapper class that delegates JDBC driver operations to the underlying driver.
     */
    static class DelegatingDriver implements Driver {
        private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
        private final Driver driver;

        /**
         * Constructs a new {@code DelegatingDriver}.
         *
         * @param driver The underlying driver. Must not be null.
         */
        public DelegatingDriver(Driver driver) {
            this.driver = Preconditions.checkNotNull(driver, "driver must not be null");
            LOGGER.atFine().log("Initialized DelegatingDriver for %s", driver.getClass().getName());
        }

        /**
         * Connects to the database using the specified URL and properties.
         *
         * @param url The database URL.
         * @param info The connection properties.
         * @return A {@link Connection} object.
         * @throws SQLException If a database access error occurs.
         */
        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            LOGGER.atFine().log("Connecting to database: %s", url);
            return driver.connect(url, info);
        }

        /**
         * Checks if the driver accepts the specified URL.
         *
         * @param url The database URL.
         * @return {@code true} if the URL is accepted, {@code false} otherwise.
         * @throws SQLException If a database access error occurs.
         */
        @Override
        public boolean acceptsURL(String url) throws SQLException {
            boolean accepted = driver.acceptsURL(url);
            LOGGER.atFine().log("URL %s accepted: %b", url, accepted);
            return accepted;
        }

        /**
         * Gets the driver's property information.
         *
         * @param url The database URL.
         * @param info The connection properties.
         * @return An array of {@link DriverPropertyInfo}.
         * @throws SQLException If a database access error occurs.
         */
        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            LOGGER.atFine().log("Retrieving property info for URL: %s", url);
            return driver.getPropertyInfo(url, info);
        }

        /**
         * Gets the driver's major version.
         *
         * @return The major version.
         */
        @Override
        public int getMajorVersion() {
            int version = driver.getMajorVersion();
            LOGGER.atFine().log("Major version: %d", version);
            return version;
        }

        /**
         * Gets the driver's minor version.
         *
         * @return The minor version.
         */
        @Override
        public int getMinorVersion() {
            int version = driver.getMinorVersion();
            LOGGER.atFine().log("Minor version: %d", version);
            return version;
        }

        /**
         * Checks if the driver is JDBC compliant.
         *
         * @return {@code true} if JDBC compliant, {@code false} otherwise.
         */
        @Override
        public boolean jdbcCompliant() {
            boolean compliant = driver.jdbcCompliant();
            LOGGER.atFine().log("JDBC compliant: %b", compliant);
            return compliant;
        }

        /**
         * Gets the parent logger for the driver.
         *
         * @return The parent {@link Logger}.
         * @throws SQLFeatureNotSupportedException If the feature is not supported.
         */
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            LOGGER.atFine().log("Retrieving parent logger");
            return driver.getParentLogger();
        }
    }
    // #endregion
}