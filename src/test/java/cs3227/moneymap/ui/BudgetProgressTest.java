package cs3227.moneymap.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class BudgetProgressTest {
    @Test
    void styleUsesTheCategoryCardThresholdsAtExactBoundaries() {
        assertEquals("budget-progress", BudgetProgress.styleFor(ratio("0.50"), false));
        assertEquals("budget-progress-warning", BudgetProgress.styleFor(ratio("0.51"), false));
        assertEquals("budget-progress-warning", BudgetProgress.styleFor(ratio("0.80"), false));
        assertEquals("budget-progress-over", BudgetProgress.styleFor(ratio("0.81"), false));
    }

    @Test
    void statusUsesOverBudgetForExcessSpendingAndZeroBudget() {
        assertEquals("Within budget", BudgetProgress.statusFor(ratio("0.50"), false));
        assertEquals("Near limit", BudgetProgress.statusFor(ratio("0.51"), false));
        assertEquals("Near limit", BudgetProgress.statusFor(ratio("0.80"), false));
        assertEquals("Over budget", BudgetProgress.statusFor(ratio("0.81"), false));
        assertEquals("Over budget", BudgetProgress.statusFor(BigDecimal.ZERO, true));
    }

    private static BigDecimal ratio(String value) {
        return new BigDecimal(value);
    }
}
