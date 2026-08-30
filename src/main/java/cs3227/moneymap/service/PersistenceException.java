package cs3227.moneymap.service;

/** Reports a local persistence failure without publishing an unsaved change. */
public class PersistenceException extends RuntimeException {
    /**
     * Creates a user-facing persistence failure with its filesystem cause.
     *
     * @param message safe explanation for the user
     * @param cause underlying I/O failure
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
