package cs3227.moneymap;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/** Starts the production MoneyMap shell. */
public class MoneyMapApp extends Application {
    private static final double WINDOW_WIDTH = 1180;
    private static final double WINDOW_HEIGHT = 760;
    private static final double MIN_WINDOW_WIDTH = 980;
    private static final double MIN_WINDOW_HEIGHT = 680;

    @Override
    public void start(Stage stage) throws IOException {
        URL mainView = Objects.requireNonNull(
                MoneyMapApp.class.getResource("/moneymap/main.fxml"),
                "The production shell resource is missing"
        );
        URL stylesheet = Objects.requireNonNull(
                MoneyMapApp.class.getResource("/styles/moneymap.css"),
                "The production stylesheet is missing"
        );
        FXMLLoader loader = new FXMLLoader(mainView);
        Scene scene = new Scene(loader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(stylesheet.toExternalForm());

        stage.setTitle("MoneyMap — Student Budget Tracker");
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
