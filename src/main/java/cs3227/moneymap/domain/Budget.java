package cs3227.moneymap.domain;

import java.time.YearMonth;
import java.util.Objects;
import java.util.UUID;

/**
 * A monthly spending limit for one expense category.
 *
 * @param categoryId identity of the budgeted expense category
 * @param month calendar month covered by a one-month override; null when recurring
 * @param amount non-negative monthly limit, including an explicit zero limit
 * @param repeatsMonthly whether this budget applies every month
 */
public record Budget(UUID categoryId, YearMonth month, MoneyAmount amount, boolean repeatsMonthly) {
    /** Creates a complete budget definition. */
    public Budget {
        Objects.requireNonNull(categoryId, "Budget category is required.");
        Objects.requireNonNull(amount, "Budget amount is required.");
        if (repeatsMonthly && month != null) {
            throw new IllegalArgumentException("Recurring budgets do not use a calendar month.");
        }
        if (!repeatsMonthly && month == null) {
            throw new IllegalArgumentException("One-month budgets require a calendar month.");
        }
    }

    /**
     * Creates a one-month budget override for a fixed calendar month.
     *
     * @param categoryId identity of the budgeted Expense category
     * @param month calendar month covered by this exception
     * @param amount non-negative monthly limit
     */
    public Budget(UUID categoryId, YearMonth month, MoneyAmount amount) {
        this(categoryId, month, amount, false);
    }

    /**
     * Creates a budget that applies every month until it is replaced.
     *
     * @param categoryId identity of the budgeted Expense category
     * @param amount non-negative recurring monthly limit
     * @return recurring category budget
     */
    public static Budget recurring(UUID categoryId, MoneyAmount amount) {
        return new Budget(categoryId, null, amount, true);
    }
}
