package cs3227.moneymap.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionTypeTest {
    @Test
    void values_containsOnlyIncomeAndExpense() {
        assertEquals(2, TransactionType.values().length);
        assertEquals(TransactionType.INCOME, TransactionType.valueOf("INCOME"));
        assertEquals(TransactionType.EXPENSE, TransactionType.valueOf("EXPENSE"));
    }
}
