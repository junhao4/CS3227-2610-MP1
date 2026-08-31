package cs3227.moneymap;

import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.TransactionType;
import cs3227.moneymap.persistence.JsonDataRepository;
import cs3227.moneymap.service.TransactionService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

/** Exercises Dashboard calculations, month transitions, budget states, and layout safeguards. */
@SuppressWarnings("unchecked")
public class DashboardUiSmokeTest extends Application {
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 15);

    @Override
    public void start(Stage stage) throws Exception {
        Path applicationDirectory = Files.createTempDirectory("moneymap-dashboard-ui-");
        try {
            TransactionService service = createService(applicationDirectory);
            createFixture(service);
            Parent view = loadView(service);
            stage.setScene(new Scene(view, 980, 680));
            stage.show();
            view.applyCss();
            view.layout();

            assertCurrentMonth(view);
            assertBudgetStates(view);
            assertRecentActivityLimit(view);
            assertLongContentSafeguards(view);
            assertNegativeNetBalance(view);
            assertMonthSwitch(view);
            System.out.println("Verified Dashboard summaries, budget states, month refresh, recent activity, "
                    + "and layout");
        } finally {
            stage.close();
            Platform.exit();
        }
    }

    private static TransactionService createService(Path applicationDirectory) throws Exception {
        return new TransactionService(
                new JsonDataRepository(applicationDirectory),
                Clock.fixed(Instant.parse("2026-08-15T00:00:00Z"), ZoneId.of("Asia/Singapore")),
                UUID::randomUUID);
    }

    private static void createFixture(TransactionService service) {
        Category food = category(service, TransactionType.EXPENSE, "Food");
        Category transport = category(service, TransactionType.EXPENSE, "Transport");
        Category bills = category(service, TransactionType.EXPENSE, "Bills");
        Category entertainment = category(service, TransactionType.EXPENSE, "Entertainment");
        Category health = category(service, TransactionType.EXPENSE, "Health");
        Category shopping = category(service, TransactionType.EXPENSE, "Shopping");
        Category salary = category(service, TransactionType.INCOME, "Salary");

        service.setBudgetOverride(food.id(), YearMonth.from(TODAY), "200.00");
        service.setBudgetOverride(transport.id(), YearMonth.from(TODAY), "100.00");
        service.setBudgetOverride(bills.id(), YearMonth.from(TODAY), "100.00");
        service.setBudgetOverride(entertainment.id(), YearMonth.from(TODAY), "100.00");
        service.setBudgetOverride(health.id(), YearMonth.from(TODAY), "0.00");
        service.createTransaction(TransactionType.INCOME, "1000.00", TODAY, salary.id(), "salary");
        service.createTransaction(TransactionType.EXPENSE, "100.00", TODAY, food.id(), "food");
        service.createTransaction(TransactionType.EXPENSE, "60.00", TODAY.minusDays(1), transport.id(), "transport");
        service.createTransaction(TransactionType.EXPENSE, "81.00", TODAY.minusDays(2), bills.id(), "bills");
        service.createTransaction(TransactionType.EXPENSE, "110.00", TODAY.minusDays(3), entertainment.id(), "concert");
        service.createTransaction(TransactionType.EXPENSE, "25.00", TODAY.minusDays(4), shopping.id(), "shopping");
        service.createTransaction(TransactionType.EXPENSE, "50.00", TODAY.minusMonths(1), shopping.id(),
                "past expense");
        service.createTransaction(TransactionType.INCOME, "250.00", TODAY.plusMonths(1), salary.id(), "future salary");
    }

    private static Parent loadView(TransactionService service) throws Exception {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                DashboardUiSmokeTest.class.getResource("/moneymap/dashboard.fxml")));
        loader.setControllerFactory(type -> new DashboardController(service));
        return loader.load();
    }

    private static void assertCurrentMonth(Parent view) {
        require(label(view, "dashboardTitle").getText().equals("August 2026 at a glance"),
                "Dashboard did not default to the current month");
        require(label(view, "incomeTotal").getText().equals("$1,000.00"),
                "Income summary is incorrect");
        require(label(view, "expenseTotal").getText().equals("$376.00"),
                "Expense summary is incorrect");
        require(label(view, "plannedTotal").getText().equals("$500.00"),
                "Planned budget summary is incorrect");
        require(label(view, "netTotal").getText().equals("$624.00"),
                "Net balance summary is incorrect");
        require(label(view, "heroTitle").getText().equals("You have $124.00 left to spend"),
                "Hero remaining amount is incorrect");
        require(label(view, "heroSubtitle").getText().equals("75% of your planned spending used."),
                "Hero percentage is incorrect");
        require(progress(view, "heroProgress").getStyleClass().contains("budget-progress-warning"),
                "Hero did not use the near-limit state");
    }

    private static void assertBudgetStates(Parent view) {
        assertBudgetRow(view, "Food", "$100.00 / $200.00", "budget-progress", "Within budget");
        assertBudgetRow(view, "Transport", "$60.00 / $100.00", "budget-progress-warning", "Near limit");
        assertBudgetRow(view, "Bills", "$81.00 / $100.00", "budget-progress-over", "Over budget");
        assertBudgetRow(view, "Entertainment", "$110.00 / $100.00", "budget-progress-over", "Over budget");
        assertBudgetRow(view, "Health", "$0.00 / $0.00", "budget-progress", "Within budget");
        HBox unbudgeted = budgetRow(view, "Shopping");
        require(((Label) unbudgeted.getChildren().get(1)).getText().equals("$25.00 spent"),
                "Unbudgeted spending was not included in the Dashboard");
        require(((ProgressBar) unbudgeted.getChildren().get(2)).getAccessibleText().contains("No budget"),
                "Unbudgeted category did not expose its status");
    }

    private static void assertBudgetRow(Parent view, String category, String amount,
                                        String style, String status) {
        HBox row = budgetRow(view, category);
        require(((Label) row.getChildren().get(1)).getText().equals(amount),
                category + " budget amount is incorrect");
        ProgressBar progress = (ProgressBar) row.getChildren().get(2);
        require(progress.getStyleClass().contains(style), category + " budget state is incorrect");
        require(progress.getAccessibleText().contains(status), category + " budget status is missing");
    }

    private static void assertRecentActivityLimit(Parent view) {
        VBox recentRows = node(view, "recentRows", VBox.class);
        require(recentRows.getChildren().size() == 3, "Dashboard did not limit recent activity to three entries");
    }

    private static void assertLongContentSafeguards(Parent view) {
        HBox row = budgetRow(view, "Entertainment");
        Label name = (Label) row.getChildren().get(0);
        Label amount = (Label) row.getChildren().get(1);
        require(name.getTextOverrun() == OverrunStyle.ELLIPSIS && name.getMaxWidth() == 180,
                "Dashboard category labels lack bounded overflow handling");
        require(amount.getTextOverrun() == OverrunStyle.ELLIPSIS && amount.getMaxWidth() == 170,
                "Dashboard amount labels lack bounded overflow handling");
        VBox quickTotals = node(view, "quickTotalsCard", VBox.class);
        require(quickTotals != null && quickTotals.minWidth(-1) >= 190,
                "Quick totals card lacks a responsive minimum width: "
                        + (quickTotals == null ? "missing" : quickTotals.minWidth(-1)));
        require(quickTotals.getStyleClass().contains("category-card")
                        && quickTotals.getStyleClass().contains("quick-totals"),
                "Quick totals card lacks separate category-card and quick-totals CSS classes: "
                        + quickTotals.getStyleClass());
    }

    private static void assertNegativeNetBalance(Parent view) {
        ComboBox<YearMonth> monthSelector = node(view, "monthSelector", ComboBox.class);
        monthSelector.setValue(YearMonth.from(TODAY).minusMonths(1));
        require(label(view, "dashboardTitle").getText().equals("July 2026 at a glance"),
                "Dashboard did not select the earlier transaction month");
        require(label(view, "incomeTotal").getText().equals("$0.00")
                        && label(view, "expenseTotal").getText().equals("$50.00")
                        && label(view, "netTotal").getText().equals("-$50.00"),
                "Dashboard did not display a signed negative net balance");
        require(node(view, "recentRows", VBox.class).getChildren().size() == 1,
                "Dashboard did not refresh recent activity for the earlier month");
    }

    private static void assertMonthSwitch(Parent view) {
        ComboBox<YearMonth> monthSelector = node(view, "monthSelector", ComboBox.class);
        YearMonth future = YearMonth.from(TODAY).plusMonths(1);
        require(monthSelector.getItems().contains(future), "Dashboard month selector omitted a transaction month");
        monthSelector.setValue(future);
        require(label(view, "dashboardTitle").getText().equals("September 2026 at a glance"),
                "Dashboard title did not refresh with the selected month");
        require(label(view, "incomeTotal").getText().equals("$250.00")
                        && label(view, "expenseTotal").getText().equals("$0.00")
                        && label(view, "plannedTotal").getText().equals("$0.00")
                        && label(view, "netTotal").getText().equals("$250.00"),
                "Dashboard totals did not refresh with the selected month");
        require(label(view, "heroSubtitle").getText().equals("No expense budgets are set for September 2026."),
                "Budgetless month did not refresh the hero state");
        require(progress(view, "heroProgress").getStyleClass().stream()
                        .noneMatch(style -> style.startsWith("budget-progress")),
                "Budgetless month retained a stale hero progress state");
        require(node(view, "recentRows", VBox.class).getChildren().size() == 1,
                "Recent activity did not refresh with the selected month");
    }

    private static Category category(TransactionService service, TransactionType type, String name) {
        return service.categoriesFor(type).stream().filter(category -> category.name().equals(name)).findFirst()
                .orElseThrow();
    }

    private static HBox budgetRow(Parent view, String category) {
        VBox rows = node(view, "budgetRows", VBox.class);
        return rows.getChildren().stream().map(HBox.class::cast)
                .filter(row -> ((Label) row.getChildren().getFirst()).getText().equals(category))
                .findFirst().orElseThrow();
    }

    private static Label label(Parent view, String id) {
        return node(view, id, Label.class);
    }

    private static ProgressBar progress(Parent view, String id) {
        return node(view, id, ProgressBar.class);
    }

    private static <T> T node(Parent view, String id, Class<T> type) {
        javafx.scene.Node found = view.lookup("#" + id);
        require(type.isInstance(found), "Missing Dashboard control: " + id);
        return type.cast(found);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
