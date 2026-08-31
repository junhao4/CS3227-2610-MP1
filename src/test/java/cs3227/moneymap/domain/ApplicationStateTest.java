package cs3227.moneymap.domain;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationStateTest {
    @Test
    void withoutCategory_permanentFallbackRejectedWithoutChangingState() {
        ApplicationState initial = ApplicationState.withStarterCategories();
        Category incomeFallback = StarterCategoryCatalog.fallbackFor(initial.categories(), TransactionType.INCOME);
        Category expenseFallback = StarterCategoryCatalog.fallbackFor(initial.categories(), TransactionType.EXPENSE);

        assertThrows(IllegalArgumentException.class, () -> initial.withoutCategory(incomeFallback));
        assertThrows(IllegalArgumentException.class, () -> initial.withoutCategory(expenseFallback));
        assertEquals(14, initial.categories().size());
    }

    @Test
    void constructor_rejectsIncomeAndDuplicateBudgetsForSameMonth() {
        ApplicationState initial = ApplicationState.withStarterCategories();
        Category salary = StarterCategoryCatalog.fallbackFor(initial.categories(), TransactionType.INCOME);
        Category food = initial.categories().stream().filter(category -> category.name().equals("Food"))
                .findFirst().orElseThrow();
        Budget incomeBudget = new Budget(salary.id(), YearMonth.of(2026, 8), MoneyAmount.parse("10"));
        Budget foodBudget = new Budget(food.id(), YearMonth.of(2026, 8), MoneyAmount.parse("10"));

        assertThrows(IllegalArgumentException.class,
                () -> new ApplicationState(initial.categories(), initial.transactions(), List.of(incomeBudget)));
        assertThrows(IllegalArgumentException.class,
                () -> new ApplicationState(
                        initial.categories(), initial.transactions(), List.of(foodBudget, foodBudget)));
    }
}
