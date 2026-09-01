package cs3227.moneymap.ui;

import cs3227.moneymap.domain.ApplicationState;
import cs3227.moneymap.persistence.JsonDataRepository;
import cs3227.moneymap.service.TransactionService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

/** Verifies malformed local data can be preserved and replaced through Data and Settings import. */
public class RecoveryUiSmokeTest extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Path applicationDirectory = Files.createTempDirectory("moneymap-recovery-ui-");
        try {
            JsonDataRepository repository = new JsonDataRepository(applicationDirectory);
            Path dataFile = applicationDirectory.resolve("data/moneymap.json");
            Path corruptFile = applicationDirectory.resolve("data/moneymap.json.corrupt");
            Files.createDirectories(dataFile.getParent());
            Files.writeString(dataFile, "{not-json", StandardCharsets.UTF_8);
            TransactionService service = new TransactionService(repository, fixedClock(), UUID::randomUUID);

            require(service.startupWarning() != null && service.startupWarning().contains("preserved"),
                    "Malformed local data did not produce clear recovery warning");
            require(service.transactions().isEmpty(), "Malformed local data did not start safely");
            require(Files.exists(corruptFile), "Malformed local data was not preserved");

            Path backup = Files.createTempDirectory("moneymap-recovery-backup-").resolve("backup.json");
            new JsonDataRepository(Files.createTempDirectory("moneymap-recovery-exporter-"))
                    .export(ApplicationState.withStarterCategories(), backup);
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/moneymap/data-and-settings.fxml")));
            loader.setControllerFactory(type -> new DataAndSettingsController(service));
            Parent view = loader.load();
            DataAndSettingsController controller = loader.getController();
            stage.setScene(new Scene(view, 800, 600));
            stage.show();

            controller.importFrom(backup);

            Label feedback = (Label) view.lookup("#importFeedback");
            require(feedback != null && feedback.getText().contains("Backup imported"),
                    "Recovery import did not show success feedback");
            require(repository.load().state().equals(ApplicationState.withStarterCategories()),
                    "Recovery import did not replace safe starter state");
            System.out.println("Verified malformed-data recovery and subsequent backup import");
        } finally {
            stage.close();
            Platform.exit();
        }
    }

    private static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-08-15T00:00:00Z"), ZoneId.of("Asia/Singapore"));
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
