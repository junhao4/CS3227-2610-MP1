package cs3227.moneymap.ui;

import java.math.BigDecimal;

/** Provides the shared category-card budget-state policy for production views. */
final class BudgetProgress {
    private static final BigDecimal WARNING_THRESHOLD = new BigDecimal("0.50");
    private static final BigDecimal OVER_THRESHOLD = new BigDecimal("0.80");

    private BudgetProgress() {
    }

    /** Returns the display status for a spending-to-budget ratio.
     *
     * @param ratio spending divided by budget
     * @param overBudget whether the budget is explicitly exceeded, including a zero budget
     * @return user-visible budget status
     */
    static String statusFor(BigDecimal ratio, boolean overBudget) {
        if (overBudget || ratio.compareTo(OVER_THRESHOLD) > 0) {
            return "Over budget";
        }
        return ratio.compareTo(WARNING_THRESHOLD) > 0 ? "Near limit" : "Within budget";
    }

    /** Returns the stylesheet class for a spending-to-budget ratio.
     *
     * @param ratio spending divided by budget
     * @param overBudget whether the budget is explicitly exceeded, including a zero budget
     * @return stylesheet class for the progress bar
     */
    static String styleFor(BigDecimal ratio, boolean overBudget) {
        return switch (statusFor(ratio, overBudget)) {
        case "Over budget" -> "budget-progress-over";
        case "Near limit" -> "budget-progress-warning";
        default -> "budget-progress";
        };
    }
}
