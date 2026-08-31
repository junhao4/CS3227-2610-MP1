package cs3227.moneymap.domain;

import org.junit.jupiter.api.Test;

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
}
