package cs3227.moneymap.service;

import cs3227.moneymap.domain.ApplicationState;

import java.io.IOException;
import java.nio.file.Path;

/** Loads and atomically saves MoneyMap application state. */
public interface DataRepository {
    /**
     * Loads valid state or returns a safely recovered state with a warning.
     *
     * @return load result
     * @throws IOException if the repository cannot read or preserve its files
     */
    LoadResult load() throws IOException;

    /**
     * Saves the complete state atomically.
     *
     * @param state immutable state to persist
     * @throws IOException if the state cannot be saved
     */
    void save(ApplicationState state) throws IOException;

    /**
     * Writes a complete independent backup without changing the active local data file.
     *
     * @param state immutable application state to export
     * @param destination user-selected backup file
     * @throws IOException if the destination cannot receive the backup
     */
    void export(ApplicationState state, Path destination) throws IOException;

    /**
     * Reads and completely validates an independent backup without changing active local data.
     *
     * @param source user-selected backup file
     * @return validated immutable backup state
     * @throws IOException if the backup cannot be read or is invalid
     */
    ApplicationState importBackup(Path source) throws IOException;
}
