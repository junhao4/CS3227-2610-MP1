package cs3227.prototype;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

/** Switches between the compact category landing view and its budget-management detail. */
public class BudgetDisclosurePrototypeController {
    @FXML
    private VBox budgetManagerView;
    @FXML
    private VBox categoryLandingView;

    @FXML
    private void showBudgetManager() {
        categoryLandingView.setManaged(false);
        categoryLandingView.setVisible(false);
        budgetManagerView.setManaged(true);
        budgetManagerView.setVisible(true);
    }

    @FXML
    private void showCategories() {
        budgetManagerView.setManaged(false);
        budgetManagerView.setVisible(false);
        categoryLandingView.setManaged(true);
        categoryLandingView.setVisible(true);
    }
}
