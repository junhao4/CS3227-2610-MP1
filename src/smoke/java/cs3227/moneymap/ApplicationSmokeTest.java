package cs3227.moneymap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/** Verifies the production startup path and every shell navigation mapping. */
public class ApplicationSmokeTest extends Application {
    private static final String EXPECTED_TITLE = "MoneyMap — Student Budget Tracker";

    @Override
    public void start(Stage stage) throws Exception {
        try {
            new MoneyMapApp().start(stage);

            require(stage.isShowing(), "The production stage was not shown");
            require(EXPECTED_TITLE.equals(stage.getTitle()), "The production title is incorrect");
            require(stage.getScene() != null, "The production scene was not attached");
            require(
                    stage.getScene().getStylesheets().stream().anyMatch(url -> url.endsWith("/styles/moneymap.css")),
                    "The production stylesheet was not attached"
            );
            require(stage.getScene().getRoot() instanceof BorderPane, "The production shell is not a BorderPane");

            BorderPane shell = (BorderPane) stage.getScene().getRoot();
            assertDestination(shell, "dashboardView");
            navigateAndAssert(shell, "transactionsButton", "transactionsView");
            navigateAndAssert(shell, "categoriesAndBudgetsButton", "categoriesAndBudgetsView");
            navigateAndAssert(shell, "dataAndSettingsButton", "dataAndSettingsView");
            navigateAndAssert(shell, "dashboardButton", "dashboardView");

            System.out.println("Verified production startup and all four navigation destinations");
        } finally {
            stage.close();
            Platform.exit();
        }
    }

    private static void navigateAndAssert(BorderPane shell, String buttonId, String destinationId) {
        Node control = shell.lookup("#" + buttonId);
        require(control instanceof Button, "Navigation control is missing: " + buttonId);
        ((Button) control).fire();
        assertDestination(shell, destinationId);
    }

    private static void assertDestination(BorderPane shell, String destinationId) {
        Node destination = shell.getCenter();
        require(destination != null, "The shell has no active destination");
        require(destinationId.equals(destination.getId()), "Expected destination " + destinationId
                + " but found " + destination.getId());
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
