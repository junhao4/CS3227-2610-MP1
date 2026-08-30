package cs3227.prototype;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PrototypeApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        var scene = new Scene(loader.load(), 1180, 760);
        scene.getStylesheets().add(getClass().getResource("/styles/prototype.css").toExternalForm());
        stage.setTitle("Student Budget Tracker — UI Prototypes");
        stage.setMinWidth(980);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
