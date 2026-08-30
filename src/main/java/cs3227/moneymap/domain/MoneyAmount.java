package cs3227.moneymap.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * An exact, non-negative SGD amount normalized to two decimal places.
 *
 * @param value exact decimal value
 */
public record MoneyAmount(BigDecimal value) {
    private static final Pattern PLAIN_AMOUNT = Pattern.compile("\\d+(?:\\.\\d{1,2})?");

    /**
     * Creates and validates an amount supplied by the transaction form.
     *
     * @param input plain non-negative decimal input
     * @return normalized exact amount
     */
    public static MoneyAmount parse(String input) {
        if (input == null || !PLAIN_AMOUNT.matcher(input.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Enter a non-negative number into amount with up to two decimal places.");
        }
        return new MoneyAmount(new BigDecimal(input.trim()));
    }

    /** Validates and normalizes an exact decimal amount. */
    public MoneyAmount {
        Objects.requireNonNull(value, "Amount is required");
        if (value.signum() < 0 || value.scale() > 2) {
            throw new IllegalArgumentException("Amount must be a non-negative number with up to two decimal places.");
        }
        value = value.setScale(2, RoundingMode.UNNECESSARY);
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}
