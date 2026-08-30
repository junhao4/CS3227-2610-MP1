package cs3227.moneymap;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Objects;

/** Coordinates navigation between the production application areas. */
public class ApplicationController {
    @FXML
    private BorderPane shell;

    @FXML
    private void initialize() {
        showDashboard();
    }

    @FXML
    void showDashboard() {
        loadView("/moneymap/dashboard.fxml");
    }

    @FXML
    void showTransactions() {
        loadView("/moneymap/transactions.fxml");
    }

    @FXML
    void showCategoriesAndBudgets() {
        loadView("/moneymap/categories-and-budgets.fxml");
    }

    @FXML
    void showDataAndSettings() {
        loadView("/moneymap/data-and-settings.fxml");
    }

    private void loadView(String resource) {
        try {
            Node view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(resource)));
            shell.setCenter(view);
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Could not load application view: " + resource, exception);
        }
    }
}
