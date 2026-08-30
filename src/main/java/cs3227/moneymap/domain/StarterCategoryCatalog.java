package cs3227.moneymap.domain;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Supplies MoneyMap's fixed starter categories and permanent fallbacks. */
public final class StarterCategoryCatalog {
    private static final List<String> EXPENSE_NAMES = List.of(
            "Food", "Transport", "Bills", "Shopping", "Entertainment", "Health",
            "Education", "Other Expense", "Uncategorised");
    private static final List<String> INCOME_NAMES = List.of(
            "Salary", "Allowance", "Gift", "Other Income", "Uncategorised");

    private StarterCategoryCatalog() {
    }

    /**
     * Returns a new immutable list containing the exact fixed starter set.
     *
     * @return all Income and Expense starter categories
     */
    public static List<Category> create() {
        List<Category> categories = new ArrayList<>();
        addCategories(categories, TransactionType.EXPENSE, EXPENSE_NAMES);
        addCategories(categories, TransactionType.INCOME, INCOME_NAMES);
        return List.copyOf(categories);
    }

    /**
     * Finds the permanent fallback matching the requested transaction type.
     *
     * @param categories categories to search
     * @param type required fallback type
     * @return matching permanent Uncategorised category
     */
    public static Category fallbackFor(List<Category> categories, TransactionType type) {
        return categories.stream()
                .filter(category -> category.type() == type && category.permanentFallback())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing Uncategorised category for " + type));
    }

    private static void addCategories(List<Category> categories, TransactionType type, List<String> names) {
        for (String name : names) {
            String identity = "starter:" + type + ":" + name;
            UUID id = UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
            categories.add(new Category(id, type, name, "Uncategorised".equals(name)));
        }
    }
}
