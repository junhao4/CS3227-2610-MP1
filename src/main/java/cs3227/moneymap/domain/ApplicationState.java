package cs3227.moneymap.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The complete Issue #3 application state persisted by MoneyMap.
 *
 * @param categories all available transaction categories
 * @param transactions all recorded transactions
 */
public record ApplicationState(List<Category> categories, List<Transaction> transactions) {
    /** Creates an immutable, internally consistent state snapshot. */
    public ApplicationState {
        categories = List.copyOf(Objects.requireNonNull(categories, "Categories are required"));
        transactions = List.copyOf(Objects.requireNonNull(transactions, "Transactions are required"));
        for (Transaction transaction : transactions) {
            if (!categories.contains(transaction.category())) {
                throw new IllegalArgumentException("Every transaction category must exist in application state.");
            }
        }
    }

    /**
     * Creates first-launch state with the fixed starter categories and no transactions.
     *
     * @return seeded first-launch state
     */
    public static ApplicationState withStarterCategories() {
        return new ApplicationState(StarterCategoryCatalog.create(), List.of());
    }

    /**
     * Returns a new state with the transaction inserted at the beginning of the visible history.
     *
     * @param transaction validated transaction to add
     * @return updated immutable state
     */
    public ApplicationState withTransaction(Transaction transaction) {
        List<Transaction> updated = new ArrayList<>(transactions.size() + 1);
        updated.add(transaction);
        updated.addAll(transactions);
        return new ApplicationState(categories, updated);
    }

    /**
     * Returns a new state with the supplied category appended to the category list.
     *
     * @param category validated category to add
     * @return updated immutable state
     */
    public ApplicationState withCategory(Category category) {
        List<Category> updated = new ArrayList<>(categories.size() + 1);
        updated.addAll(categories);
        updated.add(category);
        return new ApplicationState(updated, transactions);
    }
}
