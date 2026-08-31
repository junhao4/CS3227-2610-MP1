package cs3227.moneymap.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import cs3227.moneymap.domain.ApplicationState;
import cs3227.moneymap.domain.Budget;
import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.MoneyAmount;
import cs3227.moneymap.domain.StarterCategoryCatalog;
import cs3227.moneymap.domain.Transaction;
import cs3227.moneymap.domain.TransactionType;
import cs3227.moneymap.service.DataRepository;
import cs3227.moneymap.service.LoadResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Stores complete MoneyMap state in an atomic, versioned JSON file. */
public final class JsonDataRepository implements DataRepository {
    private static final int CURRENT_VERSION = 1;
    private static final String DATA_DIRECTORY = "data";
    private static final String DATA_FILE = "moneymap.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setStrictness(Strictness.STRICT)
            .create();

    private final Path dataFile;

    /**
     * Creates a repository under the application's stable base directory.
     *
     * @param applicationDirectory directory containing the packaged application or development base
     */
    public JsonDataRepository(Path applicationDirectory) {
        Objects.requireNonNull(applicationDirectory, "Application directory is required");
        dataFile = applicationDirectory.toAbsolutePath().normalize().resolve(DATA_DIRECTORY).resolve(DATA_FILE);
    }

    @Override
    public LoadResult load() throws IOException {
        if (Files.notExists(dataFile)) {
            return recoverTemporaryFileWhenMainIsMissing();
        }
        try {
            ApplicationState state = readState(dataFile);
            Files.deleteIfExists(temporaryFile());
            return LoadResult.success(state);
        } catch (JsonParseException | IllegalArgumentException | NullPointerException | DateTimeException exception) {
            preserveInvalidFile(dataFile);
            Files.deleteIfExists(temporaryFile());
            String warning = "Saved MoneyMap data could not be loaded. The invalid file was preserved as "
                    + corruptFile().getFileName() + ".";
            return LoadResult.recovered(ApplicationState.withStarterCategories(), warning);
        }
    }

    /** Recovers or preserves the only available state after an interrupted first save. */
    private LoadResult recoverTemporaryFileWhenMainIsMissing() throws IOException {
        if (Files.notExists(temporaryFile())) {
            return LoadResult.success(ApplicationState.withStarterCategories());
        }
        try {
            ApplicationState state = readState(temporaryFile());
            replaceMainFile();
            String warning = "MoneyMap recovered an interrupted save.";
            return LoadResult.recovered(state, warning);
        } catch (JsonParseException | IllegalArgumentException | NullPointerException | DateTimeException exception) {
            preserveInvalidFile(temporaryFile());
            String warning = "Saved MoneyMap temporary data could not be recovered. The invalid file was preserved as "
                    + corruptFile().getFileName() + ".";
            return LoadResult.recovered(ApplicationState.withStarterCategories(), warning);
        }
    }

    @Override
    public void save(ApplicationState state) throws IOException {
        Objects.requireNonNull(state, "Application state is required");
        Files.createDirectories(dataFile.getParent());
        String json = GSON.toJson(toPersistedState(state));
        Files.writeString(temporaryFile(), json, StandardCharsets.UTF_8);
        replaceMainFile();
    }

    Path dataFile() {
        return dataFile;
    }

    Path temporaryFile() {
        return dataFile.resolveSibling(DATA_FILE + ".tmp");
    }

    Path corruptFile() {
        return dataFile.resolveSibling(DATA_FILE + ".corrupt");
    }

    /** Promotes a completely written temporary document to the main data file. */
    private void replaceMainFile() throws IOException {
        try {
            Files.move(temporaryFile(), dataFile,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile(), dataFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Reads and validates a persisted document before exposing it to the application. */
    private ApplicationState readState(Path source) throws IOException {
        PersistedState persisted = GSON.fromJson(Files.readString(source, StandardCharsets.UTF_8),
                PersistedState.class);
        return fromPersistedState(persisted);
    }

    /** Retains invalid persisted content under a free recovery filename. */
    private void preserveInvalidFile(Path source) throws IOException {
        Path destination = availableCorruptFile();
        Files.move(source, destination);
    }

    /** Finds a recovery filename without overwriting a file from an earlier failed load. */
    private Path availableCorruptFile() {
        Path candidate = corruptFile();
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = dataFile.resolveSibling(DATA_FILE + ".corrupt-" + suffix);
            suffix++;
        }
        return candidate;
    }

    /** Converts immutable application state into the versioned persistence representation. */
    private static PersistedState toPersistedState(ApplicationState state) {
        List<PersistedCategory> categories = state.categories().stream()
                .map(category -> new PersistedCategory(category.id().toString(), category.type().name(),
                        category.name(), category.permanentFallback(), category.archived()))
                .toList();
        List<PersistedTransaction> transactions = state.transactions().stream()
                .map(transaction -> new PersistedTransaction(transaction.id().toString(), transaction.type().name(),
                        transaction.amount().toString(), transaction.date().toString(),
                        transaction.category().id().toString(), transaction.note()))
                .toList();
        List<PersistedBudget> budgets = state.budgets().stream()
                .map(budget -> new PersistedBudget(budget.categoryId().toString(),
                        budget.month() == null ? null : budget.month().toString(), budget.amount().toString(),
                        budget.repeatsMonthly(), budget.active()))
                .toList();
        return new PersistedState(CURRENT_VERSION, categories, transactions, budgets);
    }

    /** Validates a parsed persistence document and reconstructs immutable application state. */
    private static ApplicationState fromPersistedState(PersistedState persisted) {
        Objects.requireNonNull(persisted, "Saved JSON must contain an object.");
        if (persisted.version() != CURRENT_VERSION) {
            throw new IllegalArgumentException("Unsupported MoneyMap data version: " + persisted.version());
        }
        Objects.requireNonNull(persisted.categories(), "Saved categories are required.");
        Objects.requireNonNull(persisted.transactions(), "Saved transactions are required.");
        List<PersistedBudget> persistedBudgets = persisted.budgets() == null ? List.of() : persisted.budgets();

        List<Category> categories = new ArrayList<>();
        Map<UUID, Category> categoriesById = new HashMap<>();
        for (PersistedCategory item : persisted.categories()) {
            Objects.requireNonNull(item, "Saved category cannot be null.");
            Category category = new Category(UUID.fromString(item.id()), TransactionType.valueOf(item.type()),
                    item.name(), item.permanentFallback(), item.archived());
            if (categoriesById.put(category.id(), category) != null) {
                throw new IllegalArgumentException("Duplicate saved category ID.");
            }
            categories.add(category);
        }
        validateFallbacks(categories);

        List<Transaction> transactions = new ArrayList<>();
        Set<UUID> transactionIds = new HashSet<>();
        for (PersistedTransaction item : persisted.transactions()) {
            Objects.requireNonNull(item, "Saved transaction cannot be null.");
            UUID id = UUID.fromString(item.id());
            if (!transactionIds.add(id)) {
                throw new IllegalArgumentException("Duplicate saved transaction ID.");
            }
            UUID categoryId = UUID.fromString(item.categoryId());
            Category category = categoriesById.get(categoryId);
            if (category == null) {
                throw new IllegalArgumentException("Saved transaction refers to an unknown category.");
            }
            transactions.add(new Transaction(id, TransactionType.valueOf(item.type()),
                    MoneyAmount.parse(item.amount()), LocalDate.parse(item.date()), category, item.note()));
        }
        List<Budget> budgets = new ArrayList<>();
        for (PersistedBudget item : persistedBudgets) {
            Objects.requireNonNull(item, "Saved budget cannot be null.");
            budgets.add(new Budget(UUID.fromString(item.categoryId()),
                    item.month() == null ? null : YearMonth.parse(item.month()), MoneyAmount.parse(item.amount()),
                    item.repeatsMonthly(), item.active() == null || item.active()));
        }
        return new ApplicationState(categories, transactions, budgets);
    }

    /** Ensures each transaction type retains its required permanent fallback category. */
    private static void validateFallbacks(List<Category> categories) {
        for (TransactionType type : TransactionType.values()) {
            Category fallback = StarterCategoryCatalog.fallbackFor(categories, type);
            if (!"Uncategorised".equals(fallback.name())) {
                throw new IllegalArgumentException("Fallback category must be named Uncategorised.");
            }
        }
    }

    private record PersistedState(int version, List<PersistedCategory> categories,
                                  List<PersistedTransaction> transactions, List<PersistedBudget> budgets) {
    }

    private record PersistedCategory(String id, String type, String name, boolean permanentFallback, boolean archived) {
    }

    private record PersistedTransaction(String id, String type, String amount, String date,
                                        String categoryId, String note) {
    }

    private record PersistedBudget(String categoryId, String month, String amount, boolean repeatsMonthly,
                                   Boolean active) {
    }
}
