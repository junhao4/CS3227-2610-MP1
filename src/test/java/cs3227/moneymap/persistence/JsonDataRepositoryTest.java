package cs3227.moneymap.persistence;

import cs3227.moneymap.domain.ApplicationState;
import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.MoneyAmount;
import cs3227.moneymap.domain.Transaction;
import cs3227.moneymap.domain.TransactionType;
import cs3227.moneymap.service.LoadResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonDataRepositoryTest {
    @TempDir
    Path applicationDirectory;

    @Test
    void load_firstLaunch_returnsSeededStateWithoutCreatingFile() throws IOException {
        JsonDataRepository repository = new JsonDataRepository(applicationDirectory);

        LoadResult result = repository.load();

        assertEquals(14, result.state().categories().size());
        assertTrue(result.state().transactions().isEmpty());
        assertTrue(result.optionalWarning().isEmpty());
        assertFalse(Files.exists(repository.dataFile()));
    }

    @Test
    void saveThenLoad_validState_preservesVersionAndAllTransactionFields() throws IOException {
        JsonDataRepository repository = new JsonDataRepository(applicationDirectory);
        ApplicationState initial = ApplicationState.withStarterCategories();
        Category food = initial.categories().stream()
                .filter(category -> category.name().equals("Food"))
                .findFirst()
                .orElseThrow();
        Transaction transaction = new Transaction(
                UUID.fromString("20000000-0000-0000-0000-000000000001"), TransactionType.EXPENSE,
                MoneyAmount.parse("8.50"), LocalDate.of(2026, 8, 30), food, "Lunch");
        ApplicationState saved = initial.withTransaction(transaction);

        repository.save(saved);
        LoadResult loaded = repository.load();

        assertEquals(saved, loaded.state());
        String json = Files.readString(repository.dataFile(), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"version\": 1"));
        assertTrue(json.contains("\"amount\": \"8.50\""));
        assertFalse(Files.exists(repository.temporaryFile()));
    }

    @Test
    void saveThenLoad_preservesCustomCategories() throws IOException {
        JsonDataRepository repository = new JsonDataRepository(applicationDirectory);
        ApplicationState initial = ApplicationState.withStarterCategories();
        Category custom = new Category(UUID.randomUUID(), TransactionType.EXPENSE, "Credit Cards", false);

        repository.save(initial.withCategory(custom));

        assertEquals(custom, repository.load().state().categories().stream()
                .filter(category -> category.id().equals(custom.id())).findFirst().orElseThrow());
    }

    @Test
    void saveThenLoad_preservesRenamedAndArchivedCategoriesAndReferences() throws IOException {
        JsonDataRepository repository = new JsonDataRepository(applicationDirectory);
        ApplicationState initial = ApplicationState.withStarterCategories();
        Category food = initial.categories().stream()
                .filter(category -> category.name().equals("Food"))
                .findFirst()
                .orElseThrow();
        Category renamed = new Category(food.id(), food.type(), "Meals", false, true);
        Transaction transaction = new Transaction(
                UUID.fromString("20000000-0000-0000-0000-000000000002"), TransactionType.EXPENSE,
                MoneyAmount.parse("8.50"), LocalDate.of(2026, 8, 30), renamed, "Lunch");
        ApplicationState saved = new ApplicationState(
                initial.categories().stream()
                        .map(category -> category.id().equals(food.id()) ? renamed : category).toList(),
                java.util.List.of(transaction));

        repository.save(saved);

        assertEquals(saved, repository.load().state());
        assertTrue(repository.load().state().categories().stream()
                .filter(category -> category.id().equals(food.id())).findFirst().orElseThrow().archived());
    }

    @Test
    void load_malformedJson_preservesCorruptFileAndReturnsSafeSeededState() throws IOException {
        JsonDataRepository repository = new JsonDataRepository(applicationDirectory);
        Files.createDirectories(repository.dataFile().getParent());
        Files.writeString(repository.dataFile(), "{not-json", StandardCharsets.UTF_8);

        LoadResult result = repository.load();

        assertTrue(result.optionalWarning().orElseThrow().contains("could not be loaded"));
        assertEquals(14, result.state().categories().size());
        assertTrue(result.state().transactions().isEmpty());
        assertFalse(Files.exists(repository.dataFile()));
        assertTrue(Files.exists(repository.corruptFile()));
    }

    @Test
    void load_unsupportedFutureVersion_preservesFileAndRecovers() throws IOException {
        JsonDataRepository repository = new JsonDataRepository(applicationDirectory);
        Files.createDirectories(repository.dataFile().getParent());
        Files.writeString(repository.dataFile(), "{\"version\":2,\"categories\":[],\"transactions\":[]}",
                StandardCharsets.UTF_8);

        LoadResult result = repository.load();

        assertTrue(result.optionalWarning().isPresent());
        assertTrue(Files.exists(repository.corruptFile()));
    }

    @Test
    void load_validMainWithOrphanTemporaryFile_usesMainAndRemovesTemporaryFile() throws IOException {
        JsonDataRepository repository = new JsonDataRepository(applicationDirectory);
        repository.save(ApplicationState.withStarterCategories());
        Files.writeString(repository.temporaryFile(), "incomplete", StandardCharsets.UTF_8);

        LoadResult result = repository.load();

        assertEquals(14, result.state().categories().size());
        assertTrue(result.optionalWarning().isEmpty());
        assertFalse(Files.exists(repository.temporaryFile()));
    }

    @Test
    void load_validTemporaryFileWithoutMain_recoversInterruptedFirstSave() throws IOException {
        JsonDataRepository repository = new JsonDataRepository(applicationDirectory);
        ApplicationState saved = stateWithExpense();
        repository.save(saved);
        Files.move(repository.dataFile(), repository.temporaryFile());

        LoadResult result = repository.load();

        assertEquals(saved, result.state());
        assertTrue(result.optionalWarning().orElseThrow().contains("recovered"));
        assertTrue(Files.exists(repository.dataFile()));
        assertFalse(Files.exists(repository.temporaryFile()));
    }

    @Test
    void load_invalidTemporaryFileWithoutMain_preservesItAndReturnsSafeSeededState() throws IOException {
        JsonDataRepository repository = new JsonDataRepository(applicationDirectory);
        Files.createDirectories(repository.dataFile().getParent());
        Files.writeString(repository.temporaryFile(), "{not-json", StandardCharsets.UTF_8);

        LoadResult result = repository.load();

        assertEquals(14, result.state().categories().size());
        assertTrue(result.state().transactions().isEmpty());
        assertTrue(result.optionalWarning().orElseThrow().contains("could not be recovered"));
        assertFalse(Files.exists(repository.dataFile()));
        assertFalse(Files.exists(repository.temporaryFile()));
        assertTrue(Files.exists(repository.corruptFile()));
    }

    @Test
    void load_invalidDateInMainFile_preservesItAndReturnsSafeSeededState() throws IOException {
        JsonDataRepository repository = new JsonDataRepository(applicationDirectory);
        repository.save(stateWithExpense());
        replaceSavedDate(repository.dataFile());

        LoadResult result = repository.load();

        assertEquals(14, result.state().categories().size());
        assertTrue(result.state().transactions().isEmpty());
        assertTrue(result.optionalWarning().orElseThrow().contains("could not be loaded"));
        assertFalse(Files.exists(repository.dataFile()));
        assertTrue(Files.exists(repository.corruptFile()));
    }

    @Test
    void load_invalidDateInTemporaryFile_preservesItAndReturnsSafeSeededState() throws IOException {
        JsonDataRepository repository = new JsonDataRepository(applicationDirectory);
        repository.save(stateWithExpense());
        Files.move(repository.dataFile(), repository.temporaryFile());
        replaceSavedDate(repository.temporaryFile());

        LoadResult result = repository.load();

        assertEquals(14, result.state().categories().size());
        assertTrue(result.state().transactions().isEmpty());
        assertTrue(result.optionalWarning().orElseThrow().contains("could not be recovered"));
        assertFalse(Files.exists(repository.dataFile()));
        assertFalse(Files.exists(repository.temporaryFile()));
        assertTrue(Files.exists(repository.corruptFile()));
    }

    @Test
    void save_temporaryWriteFails_preservesPreviousValidFile() throws IOException {
        JsonDataRepository repository = new JsonDataRepository(applicationDirectory);
        repository.save(ApplicationState.withStarterCategories());
        String validJson = Files.readString(repository.dataFile(), StandardCharsets.UTF_8);
        Files.createDirectory(repository.temporaryFile());

        assertThrows(IOException.class, () -> repository.save(ApplicationState.withStarterCategories()));

        assertEquals(validJson, Files.readString(repository.dataFile(), StandardCharsets.UTF_8));
    }

    private static ApplicationState stateWithExpense() {
        ApplicationState initial = ApplicationState.withStarterCategories();
        Category food = initial.categories().stream()
                .filter(category -> category.name().equals("Food"))
                .findFirst()
                .orElseThrow();
        Transaction transaction = new Transaction(
                UUID.fromString("30000000-0000-0000-0000-000000000001"), TransactionType.EXPENSE,
                MoneyAmount.parse("8.50"), LocalDate.of(2026, 8, 30), food, "Lunch");
        return initial.withTransaction(transaction);
    }

    private static void replaceSavedDate(Path savedFile) throws IOException {
        String json = Files.readString(savedFile, StandardCharsets.UTF_8);
        Files.writeString(savedFile, json.replace("\"date\": \"2026-08-30\"", "\"date\": \"not-a-date\""),
                StandardCharsets.UTF_8);
    }
}
