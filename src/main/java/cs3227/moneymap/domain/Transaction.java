package cs3227.moneymap.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * An immutable income or expense transaction.
 *
 * @param id stable transaction identity
 * @param type whether money enters or leaves the records
 * @param amount exact non-negative SGD amount
 * @param date transaction calendar date
 * @param category type-compatible category
 * @param note optional note of at most 200 characters
 */
public record Transaction(UUID id, TransactionType type, MoneyAmount amount,
                          LocalDate date, Category category, String note) {
    private static final int MAX_NOTE_LENGTH = 200;

    /** Validates all transaction invariants. */
    public Transaction {
        Objects.requireNonNull(id, "Transaction ID is required");
        Objects.requireNonNull(type, "Transaction type is required");
        Objects.requireNonNull(amount, "Transaction amount is required");
        Objects.requireNonNull(date, "Transaction date is required");
        Objects.requireNonNull(category, "Transaction category is required");
        note = note == null ? "" : note;
        if (note.codePointCount(0, note.length()) > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException("Note must contain at most 200 characters.");
        }
        if (category.type() != type) {
            throw new IllegalArgumentException("Category must match the transaction type.");
        }
    }
}
