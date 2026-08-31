package cs3227.moneymap.domain;

import java.util.ArrayList;
import java.util.List;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The complete Issue #3 application state persisted by MoneyMap.
 *
 * @param categories all available transaction categories
 * @param transactions all recorded transactions
 * @param budgets all configured monthly expense-category budgets
 */
public record ApplicationState(List<Category> categories, List<Transaction> transactions, List<Budget> budgets) {
    /** Creates an immutable, internally consistent state snapshot. */
    public ApplicationState {
        categories = List.copyOf(Objects.requireNonNull(categories, "Categories are required"));
        transactions = List.copyOf(Objects.requireNonNull(transactions, "Transactions are required"));
        budgets = List.copyOf(Objects.requireNonNull(budgets, "Budgets are required"));
        for (Transaction transaction : transactions) {
            if (!categories.contains(transaction.category())) {
                throw new IllegalArgumentException("Every transaction category must exist in application state.");
            }
        }
        Set<BudgetKey> budgetKeys = new HashSet<>();
        for (Budget budget : budgets) {
            Category category = categories.stream()
                    .filter(candidate -> candidate.id().equals(budget.categoryId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Every budget category must exist in application state."));
            if (category.type() != TransactionType.EXPENSE) {
                throw new IllegalArgumentException("Budgets can be assigned only to expense categories.");
            }
            if (!budgetKeys.add(new BudgetKey(
                    budget.categoryId(), budget.month(), budget.repeatsMonthly()))) {
                throw new IllegalArgumentException("Only one budget may exist for a category and month.");
            }
        }
    }

    /**
     * Creates a state without budgets for compatibility with pre-budget callers.
     *
     * @param categories all available transaction categories
     * @param transactions all recorded transactions
     */
    public ApplicationState(List<Category> categories, List<Transaction> transactions) {
        this(categories, transactions, List.of());
    }

    /**
     * Creates first-launch state with the fixed starter categories and no transactions.
     *
     * @return seeded first-launch state
     */
    public static ApplicationState withStarterCategories() {
        return new ApplicationState(StarterCategoryCatalog.create(), List.of(), List.of());
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
        return new ApplicationState(categories, updated, budgets);
    }

    /**
     * Returns a new state with one existing transaction replaced by the supplied corrected value.
     *
     * @param transaction corrected transaction retaining the existing identity
     * @return updated immutable state
     */
    public ApplicationState withUpdatedTransaction(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction is required");
        boolean exists = transactions.stream().anyMatch(candidate -> candidate.id().equals(transaction.id()));
        if (!exists) {
            throw new IllegalArgumentException("Selected transaction does not exist.");
        }
        return new ApplicationState(categories, transactions.stream()
                .map(candidate -> candidate.id().equals(transaction.id()) ? transaction : candidate)
                .toList(), budgets);
    }

    /**
     * Returns a new state without a deliberately removed transaction.
     *
     * @param transactionId identity of the transaction to remove
     * @return updated immutable state
     */
    public ApplicationState withoutTransaction(java.util.UUID transactionId) {
        Objects.requireNonNull(transactionId, "Transaction is required");
        List<Transaction> updated = transactions.stream()
                .filter(transaction -> !transaction.id().equals(transactionId))
                .toList();
        if (updated.size() == transactions.size()) {
            throw new IllegalArgumentException("Selected transaction does not exist.");
        }
        return new ApplicationState(categories, updated, budgets);
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
        return new ApplicationState(updated, transactions, budgets);
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
        return new ApplicationState(updated, replaceCategoryReferences(replacement), budgets);
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
        return new ApplicationState(updated, replaceCategoryReferences(replacement), budgets);
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
        return new ApplicationState(updated, replaceCategoryReferences(replacement), budgets);
    }

    /** Returns a new state with an unused ordinary category permanently removed.
     *
     * @param category category to remove
     * @return updated immutable state
     */
    public ApplicationState withoutCategory(Category category) {
        Objects.requireNonNull(category, "Category is required");
        if (category.permanentFallback()) {
            throw new IllegalArgumentException("Permanent fallback categories cannot be deleted.");
        }
        if (transactions.stream().anyMatch(transaction -> transaction.category().id().equals(category.id()))) {
            throw new IllegalArgumentException("A used category cannot be deleted.");
        }
        List<Category> updated = categories.stream()
                .filter(candidate -> !candidate.id().equals(category.id()))
                .toList();
        if (updated.size() == categories.size()) {
            throw new IllegalArgumentException("Category does not exist.");
        }
        return new ApplicationState(updated, transactions,
                budgets.stream().filter(budget -> !budget.categoryId().equals(category.id())).toList());
    }

    /** Returns a new state with every source-category transaction moved to the target category.
     *
     * @param source category currently referenced by transactions
     * @param target replacement category with the same transaction type
     * @return updated immutable state
     */
    public ApplicationState withReassignedTransactions(Category source, Category target) {
        Objects.requireNonNull(source, "Source category is required");
        Objects.requireNonNull(target, "Target category is required");
        return new ApplicationState(categories, transactions.stream()
                .map(transaction -> transaction.category().id().equals(source.id())
                        ? new Transaction(transaction.id(), transaction.type(), transaction.amount(),
                                transaction.date(), target, transaction.note())
                        : transaction)
                .toList(), budgets);
    }

    /**
     * Returns a new state with one recurring budget or one calendar-month override replaced.
     *
     * @param budget validated expense-category budget
     * @return state containing the supplied budget
     */
    public ApplicationState withBudget(Budget budget) {
        Objects.requireNonNull(budget, "Budget is required");
        List<Budget> updated = new ArrayList<>(budgets.stream()
                .filter(existing -> !sameBudgetScope(existing, budget))
                .toList());
        updated.add(budget);
        return new ApplicationState(categories, transactions, updated);
    }

    /**
     * Returns a new state with one budget scope removed.
     *
     * @param categoryId expense category identity
     * @param month calendar month for a one-month override; {@code null} for recurring
     * @param repeatsMonthly whether the budget applies every month
     * @return state without the matching budget, or the same budget values when absent
     */
    public ApplicationState withoutBudget(java.util.UUID categoryId, YearMonth month, boolean repeatsMonthly) {
        Objects.requireNonNull(categoryId, "Budget category is required");
        if (repeatsMonthly && month != null) {
            throw new IllegalArgumentException("Recurring budgets do not use a calendar month.");
        }
        if (!repeatsMonthly && month == null) {
            throw new IllegalArgumentException("One-month budgets require a calendar month.");
        }
        List<Budget> updated = budgets.stream()
                .filter(budget -> !(budget.categoryId().equals(categoryId)
                        && budget.repeatsMonthly() == repeatsMonthly
                        && Objects.equals(budget.month(), month)))
                .toList();
        return new ApplicationState(categories, transactions, updated);
    }

    private static boolean sameBudgetScope(Budget first, Budget second) {
        return first.categoryId().equals(second.categoryId())
                && Objects.equals(first.month(), second.month())
                && first.repeatsMonthly() == second.repeatsMonthly();
    }

    private List<Transaction> replaceCategoryReferences(Category replacement) {
        return transactions.stream()
                .map(transaction -> transaction.category().id().equals(replacement.id())
                        ? new Transaction(transaction.id(), transaction.type(), transaction.amount(),
                                transaction.date(), replacement, transaction.note())
                        : transaction)
                .toList();
    }

    private record BudgetKey(java.util.UUID categoryId, YearMonth month, boolean repeatsMonthly) {
    }
}
