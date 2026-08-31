package cs3227.moneymap.domain;

import java.time.YearMonth;
import java.util.Objects;
import java.util.UUID;

/**
 * A monthly spending limit for one expense category.
 *
 * @param categoryId identity of the budgeted expense category
 * @param month calendar month covered by a one-month override, or the effective
 *              start month of a versioned recurring budget
 * @param amount non-negative monthly limit, including an explicit zero limit
 * @param repeatsMonthly whether this budget applies every month
 * @param active whether this recurring version supplies a budget; one-month
 *              overrides are always active
 */
public record Budget(UUID categoryId, YearMonth month, MoneyAmount amount, boolean repeatsMonthly, boolean active) {
    /** Creates a complete budget definition. */
    public Budget {
        Objects.requireNonNull(categoryId, "Budget category is required.");
        Objects.requireNonNull(amount, "Budget amount is required.");
        if (!repeatsMonthly && month == null) {
            throw new IllegalArgumentException("One-month budgets require a calendar month.");
        }
        if (!repeatsMonthly && !active) {
            throw new IllegalArgumentException("One-month budgets must be active.");
        }
    }

    /** Creates an active budget definition for compatibility with existing callers.
     *
     * @param categoryId identity of the budgeted category
     * @param month one-month scope or recurring effective month
     * @param amount non-negative monthly limit
     * @param repeatsMonthly whether this budget applies every month
     */
    public Budget(UUID categoryId, YearMonth month, MoneyAmount amount, boolean repeatsMonthly) {
        this(categoryId, month, amount, repeatsMonthly, true);
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

    /** Creates an active recurring budget beginning in the supplied calendar month.
     *
     * @param categoryId identity of the budgeted category
     * @param effectiveFrom first month receiving the recurring value
     * @param amount non-negative monthly limit
     * @return active recurring budget
     */
    public static Budget recurring(UUID categoryId, YearMonth effectiveFrom, MoneyAmount amount) {
        Objects.requireNonNull(effectiveFrom, "Recurring budget start month is required.");
        return new Budget(categoryId, effectiveFrom, amount, true);
    }

    /** Creates a recurring stop marker that prevents a prior value from applying onward.
     *
     * @param categoryId identity of the budgeted category
     * @param effectiveFrom first month without the recurring value
     * @return inactive recurring stop marker
     */
    public static Budget recurringStop(UUID categoryId, YearMonth effectiveFrom) {
        Objects.requireNonNull(effectiveFrom, "Recurring budget stop month is required.");
        return new Budget(categoryId, effectiveFrom, MoneyAmount.parse("0"), true, false);
    }
}
