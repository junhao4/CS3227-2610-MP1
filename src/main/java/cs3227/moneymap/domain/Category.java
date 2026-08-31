package cs3227.moneymap.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * A transaction category with an explicit income or expense type.
 *
 * @param id stable category identity
 * @param type compatible transaction type
 * @param name user-facing category name
 * @param permanentFallback whether this is a protected Uncategorised fallback
 * @param archived whether the category is unavailable for new transactions
 */
public record Category(UUID id, TransactionType type, String name, boolean permanentFallback, boolean archived) {
    /** Creates an active category, retained for callers using the original model shape.
     *
     * @param id stable category identity
     * @param type compatible transaction type
     * @param name user-facing category name
     * @param permanentFallback whether this is a protected fallback
     */
    public Category(UUID id, TransactionType type, String name, boolean permanentFallback) {
        this(id, type, name, permanentFallback, false);
    }

    /** Validates the category identity and display name. */
    public Category {
        Objects.requireNonNull(id, "Category ID is required");
        Objects.requireNonNull(type, "Category type is required");
        Objects.requireNonNull(name, "Category name is required");
        if (name.isBlank() || name.codePointCount(0, name.length()) > 40) {
            throw new IllegalArgumentException("Category name must contain 1 to 40 characters.");
        }
        if (permanentFallback && archived) {
            throw new IllegalArgumentException("Permanent fallback categories cannot be archived.");
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
