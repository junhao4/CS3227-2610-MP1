package cs3227.moneymap;

import cs3227.moneymap.persistence.ApplicationDirectoryResolver;
import cs3227.moneymap.persistence.JsonDataRepository;
import cs3227.moneymap.service.TransactionService;
import cs3227.moneymap.ui.ApplicationController;
import cs3227.moneymap.ui.DialogStyler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/** Starts the production MoneyMap shell. */
public class MoneyMapApp extends Application {
    private static final double WINDOW_WIDTH = 1180;
    private static final double WINDOW_HEIGHT = 760;
    private static final double MIN_WINDOW_WIDTH = 980;
    private static final double MIN_WINDOW_HEIGHT = 680;
    private TransactionService transactionService;

    /** Creates an application that resolves and loads its production data repository. */
    public MoneyMapApp() {
    }

    MoneyMapApp(TransactionService transactionService) {
        this.transactionService = Objects.requireNonNull(transactionService);
    }

    @Override
    public void start(Stage stage) throws IOException {
        if (transactionService == null) {
            transactionService = new TransactionService(
                    new JsonDataRepository(ApplicationDirectoryResolver.resolve()),
                    Clock.systemDefaultZone(), UUID::randomUUID);
        }
        URL mainView = Objects.requireNonNull(
                MoneyMapApp.class.getResource("/moneymap/main.fxml"),
                "The production shell resource is missing"
        );
        URL stylesheet = Objects.requireNonNull(
                MoneyMapApp.class.getResource("/styles/moneymap.css"),
                "The production stylesheet is missing"
        );
        FXMLLoader loader = new FXMLLoader(mainView);
        loader.setControllerFactory(type -> new ApplicationController(transactionService));
        Scene scene = new Scene(loader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(stylesheet.toExternalForm());

        stage.setTitle("MoneyMap — Student Budget Tracker");
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();
        showStartupWarningIfPresent();
    }

    private void showStartupWarningIfPresent() {
        if (transactionService.startupWarning() == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("MoneyMap data recovery");
        alert.setHeaderText("Saved data could not be loaded");
        alert.setContentText(transactionService.startupWarning());
        DialogStyler.apply(alert);
        alert.show();
    }

    /**
     * Launches MoneyMap.
     *
     * @param args JavaFX command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
