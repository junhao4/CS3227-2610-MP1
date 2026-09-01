package cs3227.moneymap.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import cs3227.moneymap.service.TransactionService;

import java.io.IOException;
import java.util.Objects;

/** Coordinates navigation between the production application areas. */
public class ApplicationController {
    private final TransactionService transactionService;

    @FXML
    private BorderPane shell;

    /** Creates the shell controller with the service shared by every destination view.
     *
     * @param transactionService application service used by shell destinations
     */
    public ApplicationController(TransactionService transactionService) {
        this.transactionService = Objects.requireNonNull(transactionService);
    }

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

    /** Loads a shell destination and supplies the Transactions view with its service dependency. */
    private void loadView(String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(resource)));
            if ("/moneymap/dashboard.fxml".equals(resource)) {
                loader.setControllerFactory(type -> new DashboardController(transactionService));
            }
            if ("/moneymap/transactions.fxml".equals(resource)) {
                loader.setControllerFactory(type -> new TransactionController(transactionService));
            }
            if ("/moneymap/categories-and-budgets.fxml".equals(resource)) {
                loader.setControllerFactory(type -> new CategoryController(transactionService));
            }
            if ("/moneymap/data-and-settings.fxml".equals(resource)) {
                loader.setControllerFactory(type -> new DataAndSettingsController(transactionService));
            }
            Node view = loader.load();
            shell.setCenter(view);
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Could not load application view: " + resource, exception);
        }
    }
}
