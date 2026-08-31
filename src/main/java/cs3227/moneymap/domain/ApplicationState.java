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

    /** Returns a new state with one ordinary category renamed.
     *
     * @param category category to rename
     * @param name replacement display name
     * @return updated immutable state
     */
    public ApplicationState withRenamedCategory(Category category, String name) {
        Objects.requireNonNull(category, "Category is required");
        List<Category> updated = categories.stream()
                .map(candidate -> candidate.id().equals(category.id())
                        ? new Category(candidate.id(), candidate.type(), name, candidate.permanentFallback(),
                                candidate.archived())
                        : candidate)
                .toList();
        Category replacement = updated.stream()
                .filter(candidate -> candidate.id().equals(category.id()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Category does not exist."));
        return new ApplicationState(updated, replaceCategoryReferences(replacement));
    }

    /** Returns a new state with one ordinary category archived.
     *
     * @param category category to archive
     * @return updated immutable state
     */
    public ApplicationState withArchivedCategory(Category category) {
        Objects.requireNonNull(category, "Category is required");
        List<Category> updated = categories.stream()
                .map(candidate -> candidate.id().equals(category.id())
                        ? new Category(candidate.id(), candidate.type(), candidate.name(),
                                candidate.permanentFallback(), true)
                        : candidate)
                .toList();
        Category replacement = updated.stream()
                .filter(candidate -> candidate.id().equals(category.id()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Category does not exist."));
        return new ApplicationState(updated, replaceCategoryReferences(replacement));
    }

    /** Returns a new state with one ordinary category restored to active use.
     *
     * @param category category to restore
     * @return updated immutable state
     */
    public ApplicationState withRestoredCategory(Category category) {
        Objects.requireNonNull(category, "Category is required");
        List<Category> updated = categories.stream()
                .map(candidate -> candidate.id().equals(category.id())
                        ? new Category(candidate.id(), candidate.type(), candidate.name(),
                                candidate.permanentFallback(), false)
                        : candidate)
                .toList();
        Category replacement = updated.stream()
                .filter(candidate -> candidate.id().equals(category.id()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Category does not exist."));
        return new ApplicationState(updated, replaceCategoryReferences(replacement));
    }

    private List<Transaction> replaceCategoryReferences(Category replacement) {
        return transactions.stream()
                .map(transaction -> transaction.category().id().equals(replacement.id())
                        ? new Transaction(transaction.id(), transaction.type(), transaction.amount(),
                                transaction.date(), replacement, transaction.note())
                        : transaction)
                .toList();
    }
}
