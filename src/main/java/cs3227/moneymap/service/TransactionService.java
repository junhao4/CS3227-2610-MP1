package cs3227.moneymap.service;

import cs3227.moneymap.domain.ApplicationState;
import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.MoneyAmount;
import cs3227.moneymap.domain.StarterCategoryCatalog;
import cs3227.moneymap.domain.Transaction;
import cs3227.moneymap.domain.TransactionType;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/** Creates validated transactions and persists each successful change. */
public final class TransactionService {
    private final DataRepository repository;
    private final Clock clock;
    private final Supplier<UUID> idSupplier;
    private final String startupWarning;
    private ApplicationState state;

    /**
     * Loads initial state and prepares deterministic collaborators for transaction creation.
     *
     * @param repository local state repository
     * @param clock clock used for the default date
     * @param idSupplier unique transaction ID supplier
     * @throws IOException if initial state cannot be loaded safely
     */
    public TransactionService(DataRepository repository, Clock clock, Supplier<UUID> idSupplier) throws IOException {
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
        this.idSupplier = Objects.requireNonNull(idSupplier);
        LoadResult loaded = repository.load();
        state = loaded.state();
        startupWarning = loaded.warning();
    }

    /**
     * Supplies the initial date for transaction entry.
     *
     * @return today's date according to the injected clock
     */
    public LocalDate defaultDate() {
        return LocalDate.now(clock);
    }

    /**
     * Returns categories compatible with a transaction type.
     *
     * @param type selected transaction type
     * @return immutable compatible category list
     */
    public List<Category> categoriesFor(TransactionType type) {
        Objects.requireNonNull(type, "Transaction type is required");
        return state.categories().stream()
                .filter(category -> category.type() == type && !category.archived())
                .toList();
    }

    /** Returns all categories, including archived categories needed by history displays.
     *
     * @return immutable category list
     */
    public List<Category> allCategories() {
        return state.categories();
    }

    /**
     * Validates, persists, and publishes a new user-defined category.
     *
     * @param type category transaction type
     * @param name category display name
     * @return persisted category
     */
    public Category createCategory(TransactionType type, String name) {
        Objects.requireNonNull(type, "Select Income or Expense.");
        String normalizedName = name == null ? "" : name.strip();
        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        if (normalizedName.codePointCount(0, normalizedName.length()) > 40) {
            throw new IllegalArgumentException("Category name must contain 1 to 40 characters.");
        }
        boolean duplicate = categoriesFor(type).stream()
                .anyMatch(category -> category.name().equalsIgnoreCase(normalizedName));
        if (duplicate) {
            throw new IllegalArgumentException("A category with this name already exists for "
                    + displayType(type) + ".");
        }
        Category category = new Category(UUID.randomUUID(), type, normalizedName, false);
        ApplicationState candidate = state.withCategory(category);
        persist(candidate);
        state = candidate;
        return category;
    }

    /** Validates, persists, and publishes a renamed ordinary category.
     *
     * @param categoryId category identity to rename
     * @param name replacement category name
     * @return persisted renamed category
     */
    public Category renameCategory(UUID categoryId, String name) {
        Category category = requireOrdinaryCategory(categoryId);
        String normalizedName = name == null ? "" : name.strip();
        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        if (normalizedName.codePointCount(0, normalizedName.length()) > 40) {
            throw new IllegalArgumentException("Category name must contain 1 to 40 characters.");
        }
        boolean duplicate = state.categories().stream()
                .filter(candidate -> candidate.type() == category.type() && !candidate.id().equals(category.id()))
                .anyMatch(candidate -> candidate.name().equalsIgnoreCase(normalizedName));
        if (duplicate) {
            throw new IllegalArgumentException("A category with this name already exists for "
                    + displayType(category.type()) + ".");
        }
        ApplicationState candidate = state.withRenamedCategory(category, normalizedName);
        persist(candidate);
        state = candidate;
        return categoryIn(candidate, category.id());
    }

    /** Archives an ordinary category while retaining its identity for history.
     *
     * @param categoryId category identity to archive
     * @return persisted archived category
     */
    public Category archiveCategory(UUID categoryId) {
        Category category = requireOrdinaryCategory(categoryId);
        ApplicationState candidate = state.withArchivedCategory(category);
        persist(candidate);
        state = candidate;
        return categoryIn(candidate, category.id());
    }

    /** Restores an archived ordinary category to new-transaction selection.
     *
     * @param categoryId category identity to restore
     * @return persisted active category
     */
    public Category restoreCategory(UUID categoryId) {
        Category category = requireOrdinaryCategory(categoryId);
        if (!category.archived()) {
            throw new IllegalArgumentException("Category is already active.");
        }
        boolean duplicateActiveName = categoriesFor(category.type()).stream()
                .anyMatch(candidate -> candidate.name().equalsIgnoreCase(category.name()));
        if (duplicateActiveName) {
            throw new IllegalArgumentException("Rename this archived category before restoring it.");
        }
        ApplicationState candidate = state.withRestoredCategory(category);
        persist(candidate);
        state = candidate;
        return categoryIn(candidate, category.id());
    }

    /** Permanently deletes an ordinary category that no transaction still references.
     *
     * @param categoryId category identity to delete
     */
    public void deleteCategory(UUID categoryId) {
        Category category = requireOrdinaryCategory(categoryId);
        int transactionCount = transactionsUsing(category);
        if (transactionCount > 0) {
            throw new IllegalArgumentException("Category is used by " + transactionCount + " "
                    + (transactionCount == 1 ? "transaction" : "transactions")
                    + ". Reassign its transactions before deleting it.");
        }
        ApplicationState candidate = state.withoutCategory(category);
        persist(candidate);
        state = candidate;
    }

    /** Reassigns every transaction from one ordinary category to an active compatible category.
     *
     * @param sourceCategoryId category currently assigned to transactions
     * @param targetCategoryId active category that will replace the source
     * @return number of reassigned transactions
     */
    public int reassignTransactions(UUID sourceCategoryId, UUID targetCategoryId) {
        Category source = requireOrdinaryCategory(sourceCategoryId);
        Category target = requireActiveCategory(targetCategoryId);
        if (source.id().equals(target.id())) {
            throw new IllegalArgumentException("Choose a different category for reassignment.");
        }
        if (source.type() != target.type()) {
            throw new IllegalArgumentException("Reassignment requires categories of the same type.");
        }
        int transactionCount = transactionsUsing(source);
        if (transactionCount == 0) {
            throw new IllegalArgumentException("Category has no transactions to reassign.");
        }
        ApplicationState candidate = state.withReassignedTransactions(source, target);
        persist(candidate);
        state = candidate;
        return transactionCount;
    }

    /**
     * Supplies the current transaction history.
     *
     * @return immutable transaction history with newly created entries first
     */
    public List<Transaction> transactions() {
        return state.transactions();
    }

    /**
     * Finds transactions matching all supplied history criteria, newest date first.
     *
     * @param month optional transaction month
     * @param type optional transaction type
     * @param categoryId optional category identity
     * @param noteQuery optional case-insensitive note text
     * @return immutable matching transactions, ordered by date descending
     */
    public List<Transaction> findTransactions(YearMonth month, TransactionType type,
                                              UUID categoryId, String noteQuery) {
        String normalizedQuery = noteQuery == null ? "" : noteQuery.strip().toLowerCase(Locale.ROOT);
        return state.transactions().stream()
                .filter(transaction -> month == null || YearMonth.from(transaction.date()).equals(month))
                .filter(transaction -> type == null || transaction.type() == type)
                .filter(transaction -> categoryId == null || transaction.category().id().equals(categoryId))
                .filter(transaction -> normalizedQuery.isEmpty()
                        || transaction.note().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .sorted(Comparator.comparing(Transaction::date).reversed())
                .toList();
    }

    /**
     * Supplies any recoverable problem encountered while loading local data.
     *
     * @return nullable startup recovery warning
     */
    public String startupWarning() {
        return startupWarning;
    }

    /**
     * Validates, persists, and publishes a transaction.
     *
     * @param type selected transaction type
     * @param amount plain SGD amount input
     * @param date required transaction date
     * @param categoryId optional category identity; null selects the matching fallback
     * @param note optional transaction note
     * @return persisted transaction
     */
    public Transaction createTransaction(TransactionType type, String amount, LocalDate date,
                                         UUID categoryId, String note) {
        Objects.requireNonNull(type, "Select Income or Expense.");
        Objects.requireNonNull(date, "Select a transaction date.");
        Category category = resolveCategory(type, categoryId);
        Transaction transaction = new Transaction(
                idSupplier.get(), type, MoneyAmount.parse(amount), date, category, note);
        ApplicationState candidate = state.withTransaction(transaction);
        persist(candidate);
        state = candidate;
        return transaction;
    }

    /** Resolves an omitted category to the matching fallback and rejects incompatible selections. */
    private Category resolveCategory(TransactionType type, UUID categoryId) {
        if (categoryId == null) {
            return StarterCategoryCatalog.fallbackFor(state.categories(), type);
        }
        Category category = state.categories().stream()
                .filter(candidate -> candidate.id().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Selected category does not exist."));
        if (category.type() != type) {
            throw new IllegalArgumentException("Selected category does not match the transaction type.");
        }
        if (category.archived()) {
            throw new IllegalArgumentException("Archived categories cannot be used for new transactions.");
        }
        return category;
    }

    /** Finds a category and protects both permanent fallback categories from lifecycle changes. */
    private Category requireOrdinaryCategory(UUID categoryId) {
        Objects.requireNonNull(categoryId, "Category is required.");
        Category category = state.categories().stream()
                .filter(candidate -> candidate.id().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Selected category does not exist."));
        if (category.permanentFallback()) {
            throw new IllegalArgumentException("Uncategorised categories cannot be changed.");
        }
        return category;
    }

    /** Finds a category that can receive reassigned transactions. */
    private Category requireActiveCategory(UUID categoryId) {
        Objects.requireNonNull(categoryId, "Replacement category is required.");
        Category category = state.categories().stream()
                .filter(candidate -> candidate.id().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Selected category does not exist."));
        if (category.archived()) {
            throw new IllegalArgumentException("Archived categories cannot receive reassigned transactions.");
        }
        return category;
    }

    /** Counts the transactions currently assigned to a category. */
    private int transactionsUsing(Category category) {
        return (int) state.transactions().stream()
                .filter(transaction -> transaction.category().id().equals(category.id()))
                .count();
    }

    private static Category categoryIn(ApplicationState candidate, UUID categoryId) {
        return candidate.categories().stream()
                .filter(category -> category.id().equals(categoryId))
                .findFirst()
                .orElseThrow();
    }

    /** Saves candidate state before it becomes visible to callers. */
    private void persist(ApplicationState candidate) {
        try {
            repository.save(candidate);
        } catch (IOException exception) {
            throw new PersistenceException("MoneyMap could not save the category or transaction.", exception);
        }
    }

    /** Formats a transaction type for validation feedback. */
    private static String displayType(TransactionType type) {
        return type == TransactionType.INCOME ? "Income" : "Expense";
    }
}
