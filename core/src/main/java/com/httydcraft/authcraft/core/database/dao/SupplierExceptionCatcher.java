package com.httydcraft.authcraft.core.database.dao;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;

// #region Class Documentation
/**
 * Utility class for catching exceptions from suppliers.
 * Executes suppliers and handles exceptions with a default value or rethrowing.
 */
public class SupplierExceptionCatcher {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    // #endregion

    // #region Exception Handling
    /**
     * Executes a supplier and catches exceptions, returning a default value.
     *
     * @param <T>                The return type.
     * @param <V>                The exception type.
     * @param supplierWithException The supplier to execute. Must not be null.
     * @param def                The default value to return on exception.
     * @return The supplier result or the default value.
     */
    public <T, V extends Throwable> T execute(SupplierWithException<T, V> supplierWithException, T def) {
        Preconditions.checkNotNull(supplierWithException, "supplierWithException must not be null");
        try {
            T result = supplierWithException.get();
            LOGGER.atFine().log("Supplier executed successfully");
            return result;
        } catch (Throwable e) {
            processException(e);
            return def;
        }
    }

    /**
     * Executes a supplier and catches exceptions, returning null on exception.
     *
     * @param <T>                The return type.
     * @param <V>                The exception type.
     * @param supplierWithException The supplier to execute. Must not be null.
     * @return The supplier result or null.
     */
    public <T, V extends Throwable> T execute(SupplierWithException<T, V> supplierWithException) {
        return execute(supplierWithException, null);
    }

    /**
     * Processes an exception by logging it.
     *
     * @param throwable The exception to process. Must not be null.
     */
    public void processException(Throwable throwable) {
        Preconditions.checkNotNull(throwable, "throwable must not be null");
        LOGGER.atWarning().withCause(throwable).log("Caught exception in supplier");
    }
    // #endregion

    // #region Supplier Interface
    /**
     * Functional interface for suppliers that may throw exceptions.
     *
     * @param <T> The return type.
     * @param <V> The exception type.
     */
    public interface SupplierWithException<T, V extends Throwable> {
        /**
         * Gets the result.
         *
         * @return The result.
         * @throws V If an error occurs.
         */
        T get() throws V;
    }
    // #endregion
}