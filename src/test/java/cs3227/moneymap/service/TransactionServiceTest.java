package cs3227.moneymap.service;

import cs3227.moneymap.domain.ApplicationState;
import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.StarterCategoryCatalog;
import cs3227.moneymap.domain.Transaction;
import cs3227.moneymap.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionServiceTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 30);
    private static final UUID TRANSACTION_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private FakeRepository repository;
    private TransactionService service;

    @BeforeEach
    void setUp() throws IOException {
        repository = new FakeRepository(ApplicationState.withStarterCategories());
        Clock clock = Clock.fixed(Instant.parse("2026-08-30T04:00:00Z"), ZoneId.of("Asia/Singapore"));
        service = new TransactionService(repository, clock, () -> TRANSACTION_ID);
    }

    @Test
    void defaultDate_returnsTodayFromInjectedClock() {
        assertEquals(TODAY, service.defaultDate());
    }

    @Test
    void categoriesFor_returnsOnlyMatchingType() {
        assertTrue(service.categoriesFor(TransactionType.INCOME).stream()
                .allMatch(category -> category.type() == TransactionType.INCOME));
        assertTrue(service.categoriesFor(TransactionType.EXPENSE).stream()
                .allMatch(category -> category.type() == TransactionType.EXPENSE));
    }

    @Test
    void createTransaction_validIncome_persistsAndPublishesTransaction() throws IOException {
        Category salary = categoryNamed("Salary", TransactionType.INCOME);

        Transaction created = service.createTransaction(
                TransactionType.INCOME, "600.00", TODAY, salary.id(), "Allowance");

        assertEquals(TRANSACTION_ID, created.id());
        assertEquals(salary, created.category());
        assertEquals(List.of(created), service.transactions());
        assertEquals(List.of(created), repository.savedState.transactions());
    }

    @Test
    void createTransaction_omittedCategory_usesTypeSpecificPermanentFallback() throws IOException {
        Transaction income = service.createTransaction(
                TransactionType.INCOME, "0", TODAY.minusDays(1), null, null);

        assertEquals(TransactionType.INCOME, income.category().type());
        assertTrue(income.category().permanentFallback());
    }

    @Test
    void createTransaction_incompatibleCategory_rejectedWithoutSave() {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);

        assertThrows(IllegalArgumentException.class, () -> service.createTransaction(
                TransactionType.INCOME, "1", TODAY.plusDays(1), food.id(), ""));
        assertEquals(0, repository.saveCount);
        assertTrue(service.transactions().isEmpty());
    }

    @Test
    void createTransaction_repositoryFailure_doesNotPublishTransaction() {
        repository.failSave = true;

        assertThrows(PersistenceException.class, () -> service.createTransaction(
                TransactionType.EXPENSE, "8.50", TODAY, null, "Lunch"));
        assertTrue(service.transactions().isEmpty());
    }

    private Category categoryNamed(String name, TransactionType type) {
        return service.categoriesFor(type).stream()
                .filter(category -> category.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private static final class FakeRepository implements DataRepository {
        private final ApplicationState loadedState;
        private ApplicationState savedState;
        private int saveCount;
        private boolean failSave;

        private FakeRepository(ApplicationState loadedState) {
            this.loadedState = loadedState;
        }

        @Override
        public LoadResult load() {
            return LoadResult.success(loadedState);
        }

        @Override
        public void save(ApplicationState state) throws IOException {
            saveCount++;
            if (failSave) {
                throw new IOException("simulated save failure");
            }
            savedState = state;
        }
    }
}
