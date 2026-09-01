package cs3227.moneymap.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTest {
    private static final Category FOOD = new Category(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            TransactionType.EXPENSE, "Food", false);
    private static final Category SALARY = new Category(
            UUID.fromString("00000000-0000-0000-0000-000000000002"),
            TransactionType.INCOME, "Salary", false);

    @Test
    void constructor_validPastPresentAndFutureDates_accepted() {
        assertDoesNotThrow(() -> transaction(LocalDate.of(2000, 1, 1), ""));
        assertDoesNotThrow(() -> transaction(LocalDate.of(2026, 8, 30), "Lunch"));
        assertDoesNotThrow(() -> transaction(LocalDate.of(2100, 12, 31), "Planned expense"));
    }

    @Test
    void constructor_missingDate_rejected() {
        assertThrows(NullPointerException.class, () -> transaction(null, ""));
    }

    @Test
    void constructor_optionalAndTwoHundredCodePointNotes_accepted() {
        assertEquals("", transaction(LocalDate.now(), null).note());
        assertDoesNotThrow(() -> transaction(LocalDate.now(), "a".repeat(200)));
        assertDoesNotThrow(() -> transaction(LocalDate.now(), "😀".repeat(200)));
    }

    @Test
    void constructor_noteOverTwoHundredCodePoints_rejected() {
        assertThrows(IllegalArgumentException.class,
                () -> transaction(LocalDate.now(), "😀".repeat(201)));
    }

    @Test
    void constructor_incompatibleCategory_rejected() {
        assertThrows(IllegalArgumentException.class, () -> new Transaction(
                UUID.randomUUID(), TransactionType.INCOME, MoneyAmount.parse("1"),
                LocalDate.now(), FOOD, ""));
        assertDoesNotThrow(() -> new Transaction(
                UUID.randomUUID(), TransactionType.INCOME, MoneyAmount.parse("1"),
                LocalDate.now(), SALARY, ""));
    }

    @Test
    void constructor_amountAboveMaximumTransactionValue_rejected() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Transaction(
                UUID.randomUUID(), TransactionType.EXPENSE, MoneyAmount.parse("10000000"),
                LocalDate.now(), FOOD, "Too large"));
        assertEquals("Amount must be between $0.00 and $9,999,999.99.", exception.getMessage());
    }

    @Test
    void constructor_maximumTransactionValue_accepted() {
        assertDoesNotThrow(() -> new Transaction(
                UUID.randomUUID(), TransactionType.EXPENSE, MoneyAmount.parse("9999999.99"),
                LocalDate.now(), FOOD, "Maximum value"));
    }

    private static Transaction transaction(LocalDate date, String note) {
        return new Transaction(UUID.randomUUID(), TransactionType.EXPENSE,
                MoneyAmount.parse("12.34"), date, FOOD, note);
    }
}
