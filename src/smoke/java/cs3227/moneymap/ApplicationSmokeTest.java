package cs3227.moneymap;

import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.TransactionType;
import cs3227.moneymap.persistence.JsonDataRepository;
import cs3227.moneymap.service.TransactionService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.UUID;

/** Verifies the production startup path and every shell navigation mapping. */
public class ApplicationSmokeTest extends Application {
    private static final String EXPECTED_TITLE = "MoneyMap — Student Budget Tracker";

    @Override
    public void start(Stage stage) throws Exception {
        try {
            TransactionService service = new TransactionService(
                    new JsonDataRepository(Files.createTempDirectory("moneymap-application-smoke-")),
                    Clock.fixed(Instant.parse("2026-08-15T00:00:00Z"), ZoneId.of("Asia/Singapore")), UUID::randomUUID);
            Category expense = service.categoriesFor(TransactionType.EXPENSE).get(0);
            Category income = service.categoriesFor(TransactionType.INCOME).get(0);
            service.setBudgetOverride(expense.id(), YearMonth.from(service.defaultDate()), "10.00");
            service.createTransaction(TransactionType.EXPENSE, "20.00", service.defaultDate(), expense.id(), "over");
            service.createTransaction(TransactionType.INCOME, "1.00", service.defaultDate().plusMonths(1),
                    income.id(), "future");
            new MoneyMapApp(service).start(stage);

            require(stage.isShowing(), "The production stage was not shown");
            require(EXPECTED_TITLE.equals(stage.getTitle()), "The production title is incorrect");
            require(stage.getScene() != null, "The production scene was not attached");
            require(
                    stage.getScene().getStylesheets().stream().anyMatch(url -> url.endsWith("/styles/moneymap.css")),
                    "The production stylesheet was not attached"
            );
            require(stage.getScene().getRoot() instanceof BorderPane, "The production shell is not a BorderPane");

            BorderPane shell = (BorderPane) stage.getScene().getRoot();
            assertDestination(shell, "dashboardView");
            assertHeroStyleResetsForBudgetlessMonth(shell, YearMonth.from(service.defaultDate()).plusMonths(1));
            navigateAndAssert(shell, "transactionsButton", "transactionsView");
            navigateAndAssert(shell, "categoriesAndBudgetsButton", "categoriesAndBudgetsView");
            navigateAndAssert(shell, "dataAndSettingsButton", "dataAndSettingsView");
            navigateAndAssert(shell, "dashboardButton", "dashboardView");

            System.out.println("Verified production startup and all four navigation destinations");
        } finally {
            stage.close();
            Platform.exit();
        }
    }

    private static void navigateAndAssert(BorderPane shell, String buttonId, String destinationId) {
        Node control = shell.lookup("#" + buttonId);
        require(control instanceof Button, "Navigation control is missing: " + buttonId);
        ((Button) control).fire();
        assertDestination(shell, destinationId);
    }

    private static void assertDestination(BorderPane shell, String destinationId) {
        Node destination = shell.getCenter();
        require(destination != null, "The shell has no active destination");
        require(destinationId.equals(destination.getId()), "Expected destination " + destinationId
                + " but found " + destination.getId());
    }

    @SuppressWarnings("unchecked")
    private static void assertHeroStyleResetsForBudgetlessMonth(BorderPane shell, YearMonth month) {
        require(shell.getCenter() instanceof ScrollPane, "Dashboard destination is not scrollable");
        ScrollPane dashboard = (ScrollPane) shell.getCenter();
        Node heroNode = dashboard.lookup("#heroProgress");
        Node monthNode = dashboard.lookup("#monthSelector");
        require(heroNode instanceof ProgressBar, "Dashboard hero progress bar is missing");
        require(monthNode instanceof ComboBox, "Dashboard month selector is missing");
        ProgressBar hero = (ProgressBar) heroNode;
        require(hero.getStyleClass().contains("budget-progress-over"),
                "Dashboard hero did not show the over-budget state for the current month");
        ((ComboBox<YearMonth>) monthNode).setValue(month);
        require(hero.getStyleClass().stream().noneMatch(style -> style.startsWith("budget-progress")),
                "Dashboard hero kept a stale progress style for a budgetless month");
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
