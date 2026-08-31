package cs3227.moneymap;

import cs3227.moneymap.domain.ApplicationState;
import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.TransactionType;
import cs3227.moneymap.persistence.JsonDataRepository;
import cs3227.moneymap.service.TransactionService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

/** Exercises Data and Settings backup export through its real FXML, controller, service, and JSON repository. */
public class DataAndSettingsUiSmokeTest extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Path applicationDirectory = Files.createTempDirectory("moneymap-export-ui-");
        Path backupDirectory = Files.createTempDirectory("moneymap-export-backup-");
        try {
            TransactionService service = createService(applicationDirectory);
            Category food = service.categoriesFor(TransactionType.EXPENSE).stream()
                    .filter(category -> category.name().equals("Food")).findFirst().orElseThrow();
            service.createTransaction(TransactionType.EXPENSE, "12.50", service.defaultDate(), food.id(), "Lunch");
            service.setBudgetOverride(food.id(), YearMonth.from(service.defaultDate()), "50.00");

            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/moneymap/data-and-settings.fxml")));
            loader.setControllerFactory(type -> new DataAndSettingsController(service));
            Parent view = loader.load();
            DataAndSettingsController controller = loader.getController();
            stage.setScene(new Scene(view, 800, 600));
            stage.show();

            Button exportButton = requireNode(view, "exportButton", Button.class);
            Label feedback = requireNode(view, "exportFeedback", Label.class);
            require("Export backup…".equals(exportButton.getText()), "Export control is not visibly labelled");
            require(exportButton.getAccessibleText().contains("complete"), "Export control lacks accessible text");

            Path backupFile = backupDirectory.resolve("data").resolve("moneymap.json");
            controller.exportTo(backupFile);

            assertFeedback(feedback, "Backup exported", "positive");
            ApplicationState local = new JsonDataRepository(applicationDirectory).load().state();
            assertEquals(local, new JsonDataRepository(backupDirectory).load().state(),
                    "Exported backup differs from current local state");

            controller.exportTo(applicationDirectory);

            assertFeedback(feedback, "could not be exported", "validation-error");
            assertEquals(local, new JsonDataRepository(applicationDirectory).load().state(),
                    "Failed export altered current local data");
            System.out.println("Verified Data and Settings backup export success and failure feedback");
        } finally {
            stage.close();
            Platform.exit();
        }
    }

    private static TransactionService createService(Path applicationDirectory) throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-08-15T00:00:00Z"), ZoneId.of("Asia/Singapore"));
        return new TransactionService(new JsonDataRepository(applicationDirectory), clock, UUID::randomUUID);
    }

    private static <T> T requireNode(Parent root, String id, Class<T> type) {
        Object node = root.lookup("#" + id);
        if (!type.isInstance(node)) {
            throw new IllegalStateException("Missing " + type.getSimpleName() + ": " + id);
        }
        return type.cast(node);
    }

    private static void assertFeedback(Label feedback, String expectedText, String styleClass) {
        require(feedback.isVisible() && feedback.isManaged(), "Export feedback was not displayed");
        require(feedback.getText().contains(expectedText), "Unexpected export feedback: " + feedback.getText());
        require(feedback.getStyleClass().contains(styleClass), "Export feedback lacks style: " + styleClass);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException(message);
        }
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
