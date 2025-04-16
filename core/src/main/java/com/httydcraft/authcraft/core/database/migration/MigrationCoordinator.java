package com.httydcraft.authcraft.core.database.migration;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.database.migration.Migrator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// #region Class Documentation
/**
 * Coordinates database migrations for a specific entity type.
 * Manages a list of migrators and applies them in order.
 *
 * @param <T>  The entity type.
 * @param <ID> The ID type of the entity.
 */
public class MigrationCoordinator<T, ID> {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final List<Migrator<T, ID>> migrators = new ArrayList<>();
    // #endregion

    // #region Migration Execution
    /**
     * Applies all registered migrators to the database.
     *
     * @param connectionSource The database connection source. Must not be null.
     * @param dao             The DAO for the entity. Must not be null.
     * @throws SQLException If a database error occurs.
     */
    public void migrate(ConnectionSource connectionSource, Dao<? extends T, ID> dao) throws SQLException {
        Preconditions.checkNotNull(connectionSource, "connectionSource must not be null");
        Preconditions.checkNotNull(dao, "dao must not be null");
        try {
            for (Migrator<T, ID> migrator : migrators) {
                migrator.tryToMigrate(connectionSource, dao);
                LOGGER.atFine().log("Applied migrator: %s", migrator.getClass().getSimpleName());
            }
            LOGGER.atInfo().log("Completed migration for %s", dao.getDataClass().getSimpleName());
        } catch (SQLException e) {
            LOGGER.atSevere().withCause(e).log("Migration failed for %s", dao.getDataClass().getSimpleName());
            throw e;
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Unexpected error during migration for %s", dao.getDataClass().getSimpleName());
            throw new SQLException("Unexpected migration error", e);
        }
    }
    // #endregion

    // #region Migrator Management
    /**
     * Adds a migrator to the coordinator.
     *
     * @param migrator The migrator to add. Must not be null.
     */
    public void add(Migrator<T, ID> migrator) {
        Preconditions.checkNotNull(migrator, "migrator must not be null");
        migrators.add(migrator);
        LOGGER.atFine().log("Added migrator: %s", migrator.getClass().getSimpleName());
    }

    /**
     * Removes a migrator from the coordinator.
     *
     * @param migrator The migrator to remove. Must not be null.
     */
    public void remove(Migrator<T, ID> migrator) {
        Preconditions.checkNotNull(migrator, "migrator must not be null");
        migrators.remove(migrator);
        LOGGER.atFine().log("Removed migrator: %s", migrator.getClass().getSimpleName());
    }

    /**
     * Gets the list of registered migrators.
     *
     * @return An unmodifiable list of {@link Migrator}s.
     */
    public List<Migrator<T, ID>> getMigrators() {
        return Collections.unmodifiableList(migrators);
    }
    // #endregion
}