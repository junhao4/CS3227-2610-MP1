package cs3227.prototype;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.List;

public class PrototypeSmokeTest extends Application {
    private static final List<String> PROTOTYPES = List.of(
            "/fxml/main.fxml",
            "/fxml/dashboard-a.fxml",
            "/fxml/dashboard-b.fxml",
            "/fxml/transactions-a.fxml",
            "/fxml/transactions-b.fxml",
            "/fxml/categories-a.fxml",
            "/fxml/categories-b.fxml",
            "/fxml/settings.fxml"
    );

    @Override
    public void start(Stage stage) throws Exception {
        for (String prototype : PROTOTYPES) {
            FXMLLoader.load(getClass().getResource(prototype));
            System.out.println("Loaded " + prototype);
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
