package cs3227.moneymap.ui;

import cs3227.moneymap.domain.MoneyAmount;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formats exact monetary values using MoneyMap's fixed SGD presentation. */
final class SgdFormatter {
    private SgdFormatter() {
    }

    static String format(MoneyAmount amount) {
        DecimalFormat formatter = new DecimalFormat("'$'#,##0.00",
                DecimalFormatSymbols.getInstance(Locale.US));
        return formatter.format(amount.value());
    }
}
