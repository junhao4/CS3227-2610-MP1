package cs3227.moneymap.ui;

import cs3227.moneymap.domain.Budget;
import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.MoneyAmount;
import cs3227.moneymap.domain.Transaction;
import cs3227.moneymap.domain.TransactionType;
import cs3227.moneymap.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Presents monthly summaries, budget states, and recent activity. */
public class DashboardController {
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");
    private final TransactionService service;

    @FXML private ComboBox<YearMonth> monthSelector;
    @FXML private Label dashboardTitle;
    @FXML private Label heroTitle;
    @FXML private Label heroSubtitle;
    @FXML private ProgressBar heroProgress;
    @FXML private Label incomeTotal;
    @FXML private Label expenseTotal;
    @FXML private Label plannedTotal;
    @FXML private Label netTotal;
    @FXML private VBox budgetRows;
    @FXML private VBox recentRows;

    /** Creates a dashboard backed by the supplied application service.
     *
     * @param service source of transactions, categories, and budgets
     */
    public DashboardController(TransactionService service) {
        this.service = Objects.requireNonNull(service);
    }

    @FXML
    private void initialize() {
        YearMonth current = YearMonth.from(service.defaultDate());
        List<YearMonth> months = service.transactions().stream().map(t -> YearMonth.from(t.date()))
                .distinct().sorted(Comparator.reverseOrder()).toList();
        monthSelector.setItems(FXCollections.observableArrayList(months));
        if (!months.contains(current)) {
            monthSelector.getItems().add(0, current);
        }
        monthSelector.setValue(current);
        monthSelector.valueProperty().addListener((observable, oldValue, newValue) -> refresh(newValue));
        refresh(current);
    }

    private void refresh(YearMonth month) {
        if (month == null) {
            return;
        }
        List<Transaction> transactions = service.findTransactions(month, null, null, "");
        dashboardTitle.setText(month.format(MONTH_FORMAT) + " at a glance");
        BigDecimal income = total(transactions, TransactionType.INCOME);
        BigDecimal expenses = total(transactions, TransactionType.EXPENSE);
        incomeTotal.setText(format(income));
        expenseTotal.setText(format(expenses));
        plannedTotal.setText(format(service.budgetsFor(month).stream().map(b -> b.amount().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        netTotal.setText(formatSigned(income.subtract(expenses)));
        refreshHero(month, expenses);
        refreshBudgets(month);
        refreshRecent(transactions);
    }

    private void refreshHero(YearMonth month, BigDecimal expenses) {
        BigDecimal planned = service.budgetsFor(month).stream().map(b -> b.amount().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = planned.subtract(expenses);
        heroTitle.setText("You have " + format(remaining.max(BigDecimal.ZERO)) + " left to spend");
        heroProgress.getStyleClass().removeAll("budget-progress", "budget-progress-warning", "budget-progress-over");
        if (planned.signum() == 0) {
            heroSubtitle.setText("No expense budgets are set for " + month.format(MONTH_FORMAT) + ".");
            heroProgress.setProgress(0);
            return;
        }
        BigDecimal used = expenses.divide(planned, 4, RoundingMode.HALF_UP);
        heroSubtitle.setText(used.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP)
                + "% of your planned spending used.");
        heroProgress.setProgress(used.max(BigDecimal.ZERO).min(BigDecimal.ONE).doubleValue());
        heroProgress.getStyleClass().add(BudgetProgress.styleFor(used, expenses.compareTo(planned) > 0));
    }

    private void refreshBudgets(YearMonth month) {
        budgetRows.getChildren().clear();
        List<Category> categories = service.categoriesFor(TransactionType.EXPENSE).stream()
                .filter(category -> service.budgetFor(category.id(), month).isPresent())
                .sorted(Comparator.comparing(Category::name))
                .toList();
        for (Category category : categories) {
            Budget budget = service.budgetFor(category.id(), month).orElseThrow();
            BigDecimal spent = service.spendingFor(category.id(), month).value();
            HBox row = new HBox(10);
            row.getStyleClass().add("dashboard-budget-row");
            Label name = new Label(category.name());
            name.getStyleClass().addAll("row-label", "dashboard-category-name");
            name.setMinWidth(90);
            name.setPrefWidth(150);
            name.setMaxWidth(180);
            name.setTextOverrun(OverrunStyle.ELLIPSIS);
            Label amount = new Label(format(spent) + " / " + format(budget.amount().value()));
            amount.getStyleClass().add("dashboard-amount");
            amount.setMinWidth(105);
            amount.setPrefWidth(150);
            amount.setMaxWidth(170);
            amount.setTextOverrun(OverrunStyle.ELLIPSIS);
            BigDecimal ratio = budget.amount().value().signum() == 0 ? BigDecimal.ZERO
                    : spent.divide(budget.amount().value(), 4, RoundingMode.HALF_UP);
            boolean overBudget = service.isOverBudget(category.id(), month);
            String status = BudgetProgress.statusFor(ratio, overBudget);
            ProgressBar progress = new ProgressBar(ratio.doubleValue());
            progress.getStyleClass().add(BudgetProgress.styleFor(ratio, overBudget));
            progress.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(progress, Priority.ALWAYS);
            progress.setAccessibleText(category.name() + " budget status: " + status);
            row.getChildren().addAll(name, amount, progress);
            budgetRows.getChildren().add(row);
        }
        if (budgetRows.getChildren().isEmpty()) {
            budgetRows.getChildren().add(new Label("No budgets are set for this month."));
        }
    }

    private void refreshRecent(List<Transaction> transactions) {
        recentRows.getChildren().clear();
        transactions.stream().limit(3).forEach(transaction -> {
            String description = transaction.note().isBlank() ? transaction.category().name() : transaction.note();
            Label note = new Label(description);
            note.getStyleClass().add(transaction.note().isBlank() ? "muted" : "row-label");
            note.setWrapText(true);
            note.setMaxWidth(Double.MAX_VALUE);
            Label metadata = new Label(transaction.category().name() + " · " + displayType(transaction.type()));
            metadata.getStyleClass().add("muted");
            VBox details = new VBox(3, note, metadata);
            HBox.setHgrow(details, Priority.ALWAYS);
            Label amount = new Label((transaction.type() == TransactionType.INCOME ? "+" : "−")
                    + format(transaction.amount()));
            amount.setMinWidth(130);
            amount.getStyleClass().add(transaction.type() == TransactionType.INCOME
                    ? "amount-income" : "amount-expense");
            HBox row = new HBox(16, details, amount);
            row.getStyleClass().add("ledger-transaction-row");
            recentRows.getChildren().add(row);
        });
        if (recentRows.getChildren().isEmpty()) {
            recentRows.getChildren().add(new Label("No transactions recorded for this month."));
        }
    }

    private static BigDecimal total(List<Transaction> transactions, TransactionType type) {
        return transactions.stream().filter(t -> t.type() == type).map(t -> t.amount().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String format(BigDecimal value) {
        return SgdFormatter.format(new MoneyAmount(value));
    }

    private static String format(MoneyAmount value) {
        return SgdFormatter.format(value);
    }

    private static String displayType(TransactionType type) {
        return type == TransactionType.INCOME ? "Income" : "Expense";
    }

    private static String formatSigned(BigDecimal value) {
        return value.signum() < 0 ? "-" + format(value.negate()) : format(value);
    }

}
