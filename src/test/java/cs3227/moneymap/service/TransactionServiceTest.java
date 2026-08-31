package cs3227.moneymap.service;

import cs3227.moneymap.domain.ApplicationState;
import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.MoneyAmount;
import cs3227.moneymap.domain.Transaction;
import cs3227.moneymap.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
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
    void createCategory_trimsAndPersistsTypeSpecificCategory() throws IOException {
        Category category = service.createCategory(TransactionType.INCOME, "  Investments  ");

        assertEquals("Investments", category.name());
        assertEquals(TransactionType.INCOME, category.type());
        assertTrue(service.categoriesFor(TransactionType.INCOME).contains(category));
        assertEquals(category, repository.savedState.categories().get(repository.savedState.categories().size() - 1));
    }

    @Test
    void createCategory_allowsSameNameAcrossTypesButRejectsDuplicateWithinType() {
        Category income = service.createCategory(TransactionType.INCOME, "Loans");
        Category expense = service.createCategory(TransactionType.EXPENSE, " loans ");

        assertEquals("Loans", income.name());
        assertEquals("loans", expense.name());
        assertThrows(IllegalArgumentException.class,
                () -> service.createCategory(TransactionType.INCOME, " LOANS "));
    }

    @Test
    void createCategory_rejectsBlankAndOverlongNamesWithoutSaving() {
        assertThrows(IllegalArgumentException.class, () -> service.createCategory(TransactionType.EXPENSE, "   "));
        assertThrows(IllegalArgumentException.class,
                () -> service.createCategory(TransactionType.EXPENSE, "a".repeat(41)));
        assertEquals(0, repository.saveCount);
        assertEquals(14, service.categoriesFor(TransactionType.EXPENSE).size()
                + service.categoriesFor(TransactionType.INCOME).size());
    }

    @Test
    void createCategory_repositoryFailure_doesNotPublishCategory() {
        repository.failSave = true;

        assertThrows(PersistenceException.class, () -> service.createCategory(TransactionType.EXPENSE, "Loans"));
        assertTrue(service.categoriesFor(TransactionType.EXPENSE).stream()
                .noneMatch(category -> category.name().equals("Loans")));
    }

    @Test
    void createCategory_canBeUsedByCompatibleTransaction() throws IOException {
        Category category = service.createCategory(TransactionType.EXPENSE, "Credit Cards");

        Transaction transaction = service.createTransaction(
                TransactionType.EXPENSE, "25.00", TODAY, category.id(), "Card payment");

        assertEquals(category, transaction.category());
        assertThrows(IllegalArgumentException.class, () -> service.createTransaction(
                TransactionType.INCOME, "25.00", TODAY, category.id(), "Wrong type"));
    }

    @Test
    void renameCategory_updatesHistoricalReferencesAndRejectsDuplicateNames() throws IOException {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        Transaction transaction = service.createTransaction(
                TransactionType.EXPENSE, "5.00", TODAY, food.id(), "Lunch");

        Category renamed = service.renameCategory(food.id(), "Meals");

        assertEquals("Meals", renamed.name());
        assertEquals(renamed, service.transactions().get(0).category());
        assertThrows(IllegalArgumentException.class, () -> service.renameCategory(renamed.id(), "Transport"));
        assertEquals(2, repository.saveCount);
    }

    @Test
    void renameCategory_appliesNameRulesAndProtectsFallbacks() {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        Category fallback = categoryNamed("Uncategorised", TransactionType.EXPENSE);

        assertThrows(IllegalArgumentException.class, () -> service.renameCategory(food.id(), "   "));
        assertThrows(IllegalArgumentException.class, () -> service.renameCategory(food.id(), "a".repeat(41)));
        assertThrows(IllegalArgumentException.class, () -> service.renameCategory(fallback.id(), "Miscellaneous"));
        assertEquals("Food", categoryNamed("Food", TransactionType.EXPENSE).name());
    }

    @Test
    void archiveCategory_preservesHistoryButExcludesNewTransactionsAndProtectsFallback() throws IOException {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        Transaction transaction = service.createTransaction(
                TransactionType.EXPENSE, "5.00", TODAY, food.id(), "Lunch");

        Category archived = service.archiveCategory(food.id());

        assertTrue(archived.archived());
        assertTrue(service.allCategories().contains(archived));
        assertTrue(service.categoriesFor(TransactionType.EXPENSE).stream()
                .noneMatch(category -> category.id().equals(food.id())));
        assertEquals(transaction.id(), service.transactions().get(0).id());
        assertEquals(archived, service.transactions().get(0).category());
        assertThrows(IllegalArgumentException.class, () -> service.createTransaction(
                TransactionType.EXPENSE, "1.00", TODAY, food.id(), "New"));
        Category fallback = categoryNamed("Uncategorised", TransactionType.EXPENSE);
        assertThrows(IllegalArgumentException.class, () -> service.archiveCategory(fallback.id()));
    }

    @Test
    void restoreCategory_returnsArchivedCategoryToNewTransactionsAndPersists() throws IOException {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        service.archiveCategory(food.id());

        Category restored = service.restoreCategory(food.id());

        assertTrue(!restored.archived());
        assertTrue(service.categoriesFor(TransactionType.EXPENSE).contains(restored));
        assertEquals(restored, repository.savedState.categories().stream()
                .filter(category -> category.id().equals(food.id())).findFirst().orElseThrow());
        assertEquals(2, repository.saveCount);
    }

    @Test
    void restoreCategory_rejectsActiveCategoryAndArchivedNameClashingWithActiveCategory() {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        assertThrows(IllegalArgumentException.class, () -> service.restoreCategory(food.id()));

        Category temporary = service.createCategory(TransactionType.EXPENSE, "Temporary");
        service.archiveCategory(temporary.id());
        service.createCategory(TransactionType.EXPENSE, "Temporary");

        assertThrows(IllegalArgumentException.class, () -> service.restoreCategory(temporary.id()));
    }

    @Test
    void deleteCategory_removesUnusedOrdinaryCategoryAndPersists() throws IOException {
        Category temporary = service.createCategory(TransactionType.EXPENSE, "Temporary");

        service.deleteCategory(temporary.id());

        assertTrue(service.allCategories().stream().noneMatch(category -> category.id().equals(temporary.id())));
        assertTrue(repository.savedState.categories().stream()
                .noneMatch(category -> category.id().equals(temporary.id())));
    }

    @Test
    void deleteCategory_rejectsUsedAndFallbackCategoriesWithoutSaving() throws IOException {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        Category fallback = categoryNamed("Uncategorised", TransactionType.EXPENSE);
        service.createTransaction(TransactionType.EXPENSE, "8.50", TODAY, food.id(), "Lunch");
        int savesBeforeDeletion = repository.saveCount;

        IllegalArgumentException used = assertThrows(IllegalArgumentException.class,
                () -> service.deleteCategory(food.id()));
        assertEquals("Category is used by 1 transaction. Reassign its transactions before deleting it.",
                used.getMessage());
        assertThrows(IllegalArgumentException.class, () -> service.deleteCategory(fallback.id()));
        assertEquals(savesBeforeDeletion, repository.saveCount);
        assertEquals(food, service.transactions().get(0).category());
    }

    @Test
    void reassignTransactions_updatesReferencesThenAllowsDeletionAndPersists() throws IOException {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        Category transport = categoryNamed("Transport", TransactionType.EXPENSE);
        service.createTransaction(TransactionType.EXPENSE, "8.50", TODAY, food.id(), "Lunch");

        int reassigned = service.reassignTransactions(food.id(), transport.id());
        service.deleteCategory(food.id());

        assertEquals(1, reassigned);
        assertEquals(transport, service.transactions().get(0).category());
        assertTrue(service.allCategories().stream().noneMatch(category -> category.id().equals(food.id())));
        assertEquals(3, repository.saveCount);
    }

    @Test
    void reassignTransactions_rejectsInvalidSourceOrTargetWithoutSaving() throws IOException {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        Category transport = categoryNamed("Transport", TransactionType.EXPENSE);
        Category salary = categoryNamed("Salary", TransactionType.INCOME);
        Category fallback = categoryNamed("Uncategorised", TransactionType.EXPENSE);
        service.createTransaction(TransactionType.EXPENSE, "8.50", TODAY, food.id(), "Lunch");
        service.archiveCategory(transport.id());
        int savesBeforeReassignment = repository.saveCount;

        assertThrows(IllegalArgumentException.class, () -> service.reassignTransactions(food.id(), food.id()));
        assertThrows(IllegalArgumentException.class, () -> service.reassignTransactions(food.id(), salary.id()));
        assertThrows(IllegalArgumentException.class, () -> service.reassignTransactions(food.id(), transport.id()));
        assertThrows(IllegalArgumentException.class, () -> service.reassignTransactions(fallback.id(), food.id()));
        assertEquals(savesBeforeReassignment, repository.saveCount);
        assertEquals(food, service.transactions().get(0).category());
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

    @Test
    void updateTransaction_replacesEveryEditableFieldAndPersists() throws IOException {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        Category salary = categoryNamed("Salary", TransactionType.INCOME);
        Transaction original = service.createTransaction(
                TransactionType.EXPENSE, "8.50", TODAY, food.id(), "Lunch");

        Transaction updated = service.updateTransaction(
                original.id(), TransactionType.INCOME, "600.00", TODAY.plusDays(1), salary.id(), "Allowance");

        assertEquals(original.id(), updated.id());
        assertEquals(TransactionType.INCOME, updated.type());
        assertEquals(MoneyAmount.parse("600.00"), updated.amount());
        assertEquals(TODAY.plusDays(1), updated.date());
        assertEquals(salary, updated.category());
        assertEquals("Allowance", updated.note());
        assertEquals(List.of(updated), service.transactions());
        assertEquals(List.of(updated), repository.savedState.transactions());
    }

    @Test
    void updateTransaction_usesCreationValidationWithoutSavingInvalidChanges() throws IOException {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        Transaction original = service.createTransaction(
                TransactionType.EXPENSE, "8.50", TODAY, food.id(), "Lunch");
        int savesBeforeUpdate = repository.saveCount;

        assertThrows(IllegalArgumentException.class, () -> service.updateTransaction(
                original.id(), TransactionType.INCOME, "-1", TODAY, food.id(), "Wrong type"));
        assertThrows(IllegalArgumentException.class, () -> service.updateTransaction(
                UUID.randomUUID(), TransactionType.EXPENSE, "1", TODAY, food.id(), "Missing"));

        assertEquals(savesBeforeUpdate, repository.saveCount);
        assertEquals(original, service.transactions().getFirst());
    }

    @Test
    void updateTransaction_retainsItsExistingArchivedCategoryButRejectsOtherArchivedCategories() throws IOException {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        Category transport = categoryNamed("Transport", TransactionType.EXPENSE);
        Transaction original = service.createTransaction(
                TransactionType.EXPENSE, "8.50", TODAY, food.id(), "Lunch");
        service.archiveCategory(food.id());
        service.archiveCategory(transport.id());

        Transaction updated = service.updateTransaction(
                original.id(), TransactionType.EXPENSE, "9.00", TODAY, food.id(), "Corrected lunch");

        assertTrue(updated.category().archived());
        assertEquals(food.id(), updated.category().id());
        assertThrows(IllegalArgumentException.class, () -> service.updateTransaction(
                original.id(), TransactionType.EXPENSE, "9.00", TODAY, transport.id(), "Wrong archived category"));
    }

    @Test
    void deleteTransaction_removesOnlyConfirmedTransactionAndPersists() throws IOException {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        Transaction first = transaction("10000000-0000-0000-0000-000000000010", TransactionType.EXPENSE,
                "8.50", TODAY, food, "Lunch");
        Transaction second = transaction("10000000-0000-0000-0000-000000000011", TransactionType.EXPENSE,
                "1.20", TODAY.plusDays(1), food, "Drink");
        repository = new FakeRepository(new ApplicationState(service.allCategories(), List.of(second, first)));
        service = new TransactionService(repository, Clock.systemUTC(), UUID::randomUUID);

        service.deleteTransaction(first.id());

        assertEquals(List.of(second), service.transactions());
        assertEquals(List.of(second), repository.savedState.transactions());
    }

    @Test
    void deleteTransaction_missingTransactionDoesNotSave() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteTransaction(UUID.randomUUID()));

        assertEquals(0, repository.saveCount);
    }

    @Test
    void findTransactions_sortsNewestDateFirstWithoutChangingStoredHistory() throws IOException {
        List<Transaction> stored = transactionsForHistoryTests();
        TransactionService historyService = serviceFor(stored);

        List<Transaction> result = historyService.findTransactions(null, null, null, "");

        assertEquals(List.of(stored.get(2), stored.get(3), stored.get(1), stored.get(0)), result);
        assertEquals(result, historyService.findTransactions(null, null, null, "  "));
        assertTrue(historyService.findTransactions(null, null,
                UUID.fromString("00000000-0000-0000-0000-000000000099"), null).isEmpty());
        assertEquals(stored, historyService.transactions());
    }

    @Test
    void findTransactions_filtersByMonthTypeCategoryAndCaseInsensitiveNoteText() throws IOException {
        List<Transaction> stored = transactionsForHistoryTests();
        TransactionService historyService = serviceFor(stored);
        Category food = categoryNamed(historyService, "Food", TransactionType.EXPENSE);

        assertEquals(List.of(stored.get(2), stored.get(3), stored.get(1)),
                historyService.findTransactions(YearMonth.of(2026, 9), null, null, null));
        assertEquals(List.of(stored.get(1)),
                historyService.findTransactions(null, TransactionType.INCOME, null, null));
        assertEquals(List.of(stored.get(3), stored.get(0)),
                historyService.findTransactions(null, null, food.id(), null));
        assertEquals(List.of(stored.get(3), stored.get(0)),
                historyService.findTransactions(null, null, null, "LUNCH"));
    }

    @Test
    void findTransactions_combinesFiltersAndReturnsEmptyResultWithoutMutation() throws IOException {
        List<Transaction> stored = transactionsForHistoryTests();
        TransactionService historyService = serviceFor(stored);
        Category food = categoryNamed(historyService, "Food", TransactionType.EXPENSE);

        assertEquals(List.of(stored.get(3)), historyService.findTransactions(
                YearMonth.of(2026, 9), TransactionType.EXPENSE, food.id(), "group"));
        assertTrue(historyService.findTransactions(YearMonth.of(2025, 1), null, null, null).isEmpty());
        assertEquals(stored, historyService.transactions());
    }

    @Test
    void findTransactions_distinguishesFallbackCategoriesWithTheSameName() throws IOException {
        Category incomeFallback = categoryNamed("Uncategorised", TransactionType.INCOME);
        Category expenseFallback = categoryNamed("Uncategorised", TransactionType.EXPENSE);
        Transaction income = transaction("00000000-0000-0000-0000-000000000010", TransactionType.INCOME,
                "1.00", TODAY, incomeFallback, "Income fallback");
        Transaction expense = transaction("00000000-0000-0000-0000-000000000011", TransactionType.EXPENSE,
                "2.00", TODAY, expenseFallback, "Expense fallback");
        TransactionService historyService = serviceFor(List.of(income, expense));

        assertEquals(List.of(income), historyService.findTransactions(null, null, incomeFallback.id(), null));
        assertEquals(List.of(expense), historyService.findTransactions(null, null, expenseFallback.id(), null));
    }

    @Test
    void setBudget_monthOverrideReplacesSameCalendarMonthAndPersistsExplicitZero() {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        YearMonth august = YearMonth.of(2026, 8);

        service.setBudgetOverride(food.id(), august, "100.00");
        service.setBudgetOverride(food.id(), august, "0");

        assertEquals(1, service.budgetsFor(august).size());
        assertEquals(MoneyAmount.parse("0"), service.budgetFor(food.id(), august).orElseThrow().amount());
        assertEquals(2, repository.saveCount);
        assertEquals(List.of(MoneyAmount.parse("0")), repository.savedState.budgets().stream()
                .map(budget -> budget.amount()).toList());
    }

    @Test
    void setRecurringBudget_appliesEveryMonthUntilMonthOverrideTakesPrecedence() {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        YearMonth august = YearMonth.of(2026, 8);
        YearMonth september = YearMonth.of(2026, 9);

        service.setRecurringBudget(food.id(), "300.00");
        service.setBudgetOverride(food.id(), august, "450.00");

        assertEquals(MoneyAmount.parse("450.00"), service.budgetFor(food.id(), august).orElseThrow().amount());
        assertEquals(MoneyAmount.parse("300.00"), service.budgetFor(food.id(), september).orElseThrow().amount());
        assertTrue(service.budgetFor(food.id(), september).orElseThrow().repeatsMonthly());
        assertEquals(2, repository.saveCount);
    }

    @Test
    void configuredBudgets_exposeRecurringAndMonthOnlyValuesSeparately() {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        YearMonth august = YearMonth.of(2026, 8);

        service.setRecurringBudget(food.id(), "300.00");
        service.setBudgetOverride(food.id(), august, "450.00");

        assertEquals(MoneyAmount.parse("300.00"), service.recurringBudgetFor(food.id()).orElseThrow().amount());
        assertEquals(MoneyAmount.parse("450.00"), service.monthOnlyBudgetFor(food.id(), august)
                .orElseThrow().amount());
    }

    @Test
    void clearBudgetOverride_removesOnlySelectedMonthAndRevealsRecurringValue() {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        YearMonth august = YearMonth.of(2026, 8);

        service.setRecurringBudget(food.id(), "300.00");
        service.setBudgetOverride(food.id(), august, "450.00");
        service.clearBudgetOverride(food.id(), august);

        assertTrue(service.monthOnlyBudgetFor(food.id(), august).isEmpty());
        assertEquals(MoneyAmount.parse("300.00"), service.budgetFor(food.id(), august)
                .orElseThrow().amount());
    }

    @Test
    void clearRecurringBudget_removesEveryMonthValueWithoutRemovingOverrides() {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        YearMonth august = YearMonth.of(2026, 8);

        service.setRecurringBudget(food.id(), "300.00");
        service.setBudgetOverride(food.id(), august, "450.00");
        service.clearRecurringBudget(food.id());

        assertTrue(service.recurringBudgetFor(food.id()).isEmpty());
        assertEquals(MoneyAmount.parse("450.00"), service.budgetFor(food.id(), august)
                .orElseThrow().amount());
    }

    @Test
    void setBudget_rejectsIncomeNegativeAndExcessivePrecisionWithoutSaving() {
        Category income = categoryNamed("Salary", TransactionType.INCOME);
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        YearMonth august = YearMonth.of(2026, 8);

        assertThrows(IllegalArgumentException.class, () -> service.setRecurringBudget(income.id(), "10"));
        assertThrows(IllegalArgumentException.class, () -> service.setBudgetOverride(food.id(), august, "-1"));
        assertThrows(IllegalArgumentException.class, () -> service.setRecurringBudget(food.id(), "1.001"));

        assertEquals(0, repository.saveCount);
        assertTrue(service.budgetFor(food.id(), august).isEmpty());
    }

    @Test
    void budgetCalculations_includeUnbudgetedSpendingAllowOverspendingAndOmitZeroPercentage() {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        Category transport = categoryNamed("Transport", TransactionType.EXPENSE);
        YearMonth august = YearMonth.of(2026, 8);
        service.createTransaction(TransactionType.EXPENSE, "12.50", LocalDate.of(2026, 8, 10), food.id(), "Lunch");
        service.createTransaction(TransactionType.EXPENSE, "7.50", LocalDate.of(2026, 8, 11), transport.id(), "Bus");

        service.setBudgetOverride(food.id(), august, "10.00");
        service.setRecurringBudget(transport.id(), "0.00");

        assertEquals(MoneyAmount.parse("12.50"), service.spendingFor(food.id(), august));
        assertEquals(MoneyAmount.parse("7.50"), service.spendingFor(transport.id(), august));
        assertEquals(new BigDecimal("125.00"), service.percentageUsed(food.id(), august).orElseThrow());
        assertTrue(service.percentageUsed(transport.id(), august).isEmpty());
        assertTrue(service.isOverBudget(food.id(), august));
        assertTrue(service.isOverBudget(transport.id(), august));
    }

    private Category categoryNamed(String name, TransactionType type) {
        return categoryNamed(service, name, type);
    }

    private static Category categoryNamed(TransactionService service, String name, TransactionType type) {
        return service.categoriesFor(type).stream()
                .filter(category -> category.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private TransactionService serviceFor(List<Transaction> transactions) throws IOException {
        ApplicationState initial = ApplicationState.withStarterCategories();
        return new TransactionService(new FakeRepository(new ApplicationState(initial.categories(), transactions)),
                Clock.fixed(Instant.parse("2026-08-30T04:00:00Z"), ZoneId.of("Asia/Singapore")), UUID::randomUUID);
    }

    private List<Transaction> transactionsForHistoryTests() {
        Category food = categoryNamed("Food", TransactionType.EXPENSE);
        Category transport = categoryNamed("Transport", TransactionType.EXPENSE);
        Category salary = categoryNamed("Salary", TransactionType.INCOME);
        return List.of(
                transaction("00000000-0000-0000-0000-000000000001", TransactionType.EXPENSE, "8.50",
                        LocalDate.of(2026, 8, 31), food, "Lunch near campus"),
                transaction("00000000-0000-0000-0000-000000000002", TransactionType.INCOME, "600.00",
                        LocalDate.of(2026, 9, 1), salary, "Part-time pay"),
                transaction("00000000-0000-0000-0000-000000000003", TransactionType.EXPENSE, "1.20",
                        LocalDate.of(2026, 9, 10), transport, "Late bus"),
                transaction("00000000-0000-0000-0000-000000000004", TransactionType.EXPENSE, "12.00",
                        LocalDate.of(2026, 9, 10), food, "Lunch group"));
    }

    private static Transaction transaction(String id, TransactionType type, String amount,
                                           LocalDate date, Category category, String note) {
        return new Transaction(UUID.fromString(id), type, MoneyAmount.parse(amount), date, category, note);
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
