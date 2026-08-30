package cs3227.moneymap.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyAmountTest {
    @ParameterizedTest
    @CsvSource({
            "0, 0.00",
            "0.0, 0.00",
            "0.00, 0.00",
            "12, 12.00",
            "12.3, 12.30",
            "12.34, 12.34",
            "' 12.34 ', 12.34",
            "999999999999999999999999.99, 999999999999999999999999.99"
    })
    void parse_validPlainSgdAmount_returnsExactTwoDecimalValue(String input, String expected) {
        assertEquals(new BigDecimal(expected), MoneyAmount.parse(input).value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "-0.00", "-0.01", "1.234", "abc", "S$1.00", "1,000.00", "1e2", ".50", "1."})
    void parse_invalidAmount_throwsValidationException(String input) {
        assertThrows(IllegalArgumentException.class, () -> MoneyAmount.parse(input));
    }

    @Test
    void parse_null_throwsValidationException() {
        assertThrows(IllegalArgumentException.class, () -> MoneyAmount.parse(null));
    }
}
