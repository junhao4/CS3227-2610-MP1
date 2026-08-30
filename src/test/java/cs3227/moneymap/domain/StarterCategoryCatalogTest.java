package cs3227.moneymap.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarterCategoryCatalogTest {
    @Test
    void create_returnsExactTypeCompatibleStarterSets() {
        List<Category> categories = StarterCategoryCatalog.create();

        assertEquals(List.of("Food", "Transport", "Bills", "Shopping", "Entertainment", "Health",
                        "Education", "Other Expense", "Uncategorised"),
                namesOf(categories, TransactionType.EXPENSE));
        assertEquals(List.of("Salary", "Allowance", "Gift", "Other Income", "Uncategorised"),
                namesOf(categories, TransactionType.INCOME));
    }

    @Test
    void create_containsTwoDistinctPermanentFallbacks() {
        Category incomeFallback = StarterCategoryCatalog.fallbackFor(
                StarterCategoryCatalog.create(), TransactionType.INCOME);
        Category expenseFallback = StarterCategoryCatalog.fallbackFor(
                StarterCategoryCatalog.create(), TransactionType.EXPENSE);

        assertTrue(incomeFallback.permanentFallback());
        assertTrue(expenseFallback.permanentFallback());
        assertNotEquals(incomeFallback.id(), expenseFallback.id());
    }

    private static List<String> namesOf(List<Category> categories, TransactionType type) {
        return categories.stream().filter(category -> category.type() == type).map(Category::name).toList();
    }
}
