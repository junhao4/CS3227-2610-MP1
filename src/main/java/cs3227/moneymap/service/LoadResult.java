package cs3227.moneymap.service;

import cs3227.moneymap.domain.ApplicationState;

import java.util.Objects;
import java.util.Optional;

/**
 * A loaded state plus an optional recoverable startup warning.
 *
 * @param state safe state available to the application
 * @param warning nullable recovery warning for the user
 */
public record LoadResult(ApplicationState state, String warning) {
    /** Validates the loaded state. */
    public LoadResult {
        Objects.requireNonNull(state, "Loaded state is required");
    }

    /**
     * Creates a normal successful load result.
     *
     * @param state loaded state
     * @return result without a warning
     */
    public static LoadResult success(ApplicationState state) {
        return new LoadResult(state, null);
    }

    /**
     * Creates a recovered load result.
     *
     * @param state safe replacement state
     * @param warning recovery details for the user
     * @return result containing the warning
     */
    public static LoadResult recovered(ApplicationState state, String warning) {
        return new LoadResult(state, Objects.requireNonNull(warning));
    }

    /**
     * Returns the warning without exposing nullable handling to callers.
     *
     * @return optional recovery warning
     */
    public Optional<String> optionalWarning() {
        return Optional.ofNullable(warning);
    }
}
