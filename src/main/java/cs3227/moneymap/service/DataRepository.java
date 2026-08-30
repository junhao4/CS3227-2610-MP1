package cs3227.moneymap.service;

import cs3227.moneymap.domain.ApplicationState;

import java.io.IOException;

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
}
