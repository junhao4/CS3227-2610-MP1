package cs3227.prototype;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class PrototypeController {
    @FXML
    private BorderPane shell;

    @FXML
    private void initialize() {
        showDashboardA();
    }

    @FXML
    private void showDashboardA() {
        load("/fxml/dashboard-a.fxml");
    }

    @FXML
    private void showDashboardB() {
        load("/fxml/dashboard-b.fxml");
    }

    @FXML
    private void showTransactionsA() {
        load("/fxml/transactions-a.fxml");
    }

    @FXML
    private void showTransactionsB() {
        load("/fxml/transactions-b.fxml");
    }

    @FXML
    private void showTransactionsC() {
        load("/fxml/transactions-c.fxml");
    }

    @FXML
    private void showCategoriesA() {
        load("/fxml/categories-a.fxml");
    }

    @FXML
    private void showCategoriesB() {
        load("/fxml/categories-b.fxml");
    }

    @FXML
    private void showCategoriesC() {
        load("/fxml/categories-c.fxml");
    }

    @FXML
    private void showSettings() {
        load("/fxml/settings.fxml");
    }

    private void load(String resource) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(resource));
            shell.setCenter(view);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load prototype: " + resource, exception);
        }
    }
}
