package cs3227.moneymap;

import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.TransactionType;
import cs3227.moneymap.persistence.JsonDataRepository;
import cs3227.moneymap.service.TransactionService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

/** Exercises custom category creation, validation, type scoping, and reload in JavaFX. */
@SuppressWarnings("unchecked")
public class CategoryUiSmokeTest extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Path applicationDirectory = Files.createTempDirectory("moneymap-category-ui-");
        try {
            TransactionService service = createService(applicationDirectory);
            createOverflowCategories(service);
            Parent view = loadView(service);
            stage.setScene(new Scene(view, 700, 600));
            stage.show();

            ScrollPane categoryScrollPane = requireNode(view, "categoriesAndBudgetsView", ScrollPane.class);
            require(categoryScrollPane.isFitToWidth() && categoryScrollPane.getContent() != null,
                    "Category view is not contained in a width-fitting scroll pane");
            categoryScrollPane.applyCss();
            categoryScrollPane.layout();
            require(categoryScrollPane.getContent().prefHeight(-1)
                            > categoryScrollPane.getViewportBounds().getHeight(),
                    "Category list did not exceed the scroll viewport");
            require(categoryScrollPane.getVmax() > 0, "Category scroll pane has no vertical scroll range");
            categoryScrollPane.setVvalue(categoryScrollPane.getVmax());
            require(categoryScrollPane.getVvalue() > 0, "Category scroll pane could not move vertically");

            ComboBox<TransactionType> type = requireNode(view, "categoryTypeComboBox", ComboBox.class);
            TextField name = requireNode(view, "categoryNameField", TextField.class);
            Button create = requireNode(view, "createCategoryButton", Button.class);
            type.setValue(TransactionType.INCOME);
            name.setText("  Investments  ");
            create.fire();
            require(service.categoriesFor(TransactionType.INCOME).stream()
                    .anyMatch(category -> category.name().equals("Investments")),
                    "Valid custom category was not created");
            require(requireNode(view, "categoryRows", VBox.class).getChildren().stream()
                    .map(node -> ((Label) node).getText()).anyMatch(text -> text.equals("Investments · Income")),
                    "Created category was not displayed");

            type.setValue(TransactionType.EXPENSE);
            name.setText("Investments");
            create.fire();
            require(service.categoriesFor(TransactionType.EXPENSE).stream()
                    .anyMatch(category -> category.name().equals("Investments")),
                    "Same category name could not be created for the other type");

            name.setText(" investments ");
            create.fire();
            require(requireNode(view, "categoryValidationLabel", Label.class).isVisible(),
                    "Duplicate category did not show validation feedback");
            require(service.categoriesFor(TransactionType.EXPENSE).stream()
                    .filter(category -> category.name().equalsIgnoreCase("Investments")).count() == 1,
                    "Duplicate category was created");

            Parent transactionView = loadTransactionView(service);
            stage.setScene(new Scene(transactionView, 900, 700));
            stage.show();
            requireNode(transactionView, "addTransactionButton", Button.class).fire();
            ComboBox<Category> transactionCategories = requireNode(transactionView,
                    "transactionCategoryComboBox", ComboBox.class);
            require(transactionCategories.getItems().stream()
                    .anyMatch(category -> category.name().equals("Investments")
                            && category.type() == TransactionType.EXPENSE),
                    "Created category was not offered for a compatible transaction");
            require(transactionCategories.getItems().stream()
                    .noneMatch(category -> category.name().equals("Investments")
                            && category.type() == TransactionType.INCOME),
                    "Incompatible category was offered for a transaction");

            TransactionService reloaded = createService(applicationDirectory);
            require(reloaded.categoriesFor(TransactionType.INCOME).stream()
                    .anyMatch(category -> category.name().equals("Investments")),
                    "Income category did not survive restart");
            require(reloaded.categoriesFor(TransactionType.EXPENSE).stream()
                    .anyMatch(category -> category.name().equals("Investments")),
                    "Expense category did not survive restart");
            Category income = reloaded.categoriesFor(TransactionType.INCOME).stream()
                    .filter(category -> category.name().equals("Investments")).findFirst().orElseThrow();
            require(reloaded.categoriesFor(TransactionType.EXPENSE).stream()
                    .noneMatch(category -> category.id().equals(income.id())),
                    "Category identity was not type-specific");
            System.out.println("Verified custom category creation, validation, type scoping, and reload");
        } finally {
            stage.close();
            Platform.exit();
        }
    }

    private static Parent loadView(TransactionService service) throws Exception {
        FXMLLoader loader = new FXMLLoader(CategoryUiSmokeTest.class.getResource(
                "/moneymap/categories-and-budgets.fxml"));
        loader.setControllerFactory(type -> new CategoryController(service));
        return loader.load();
    }

    private static Parent loadTransactionView(TransactionService service) throws Exception {
        FXMLLoader loader = new FXMLLoader(CategoryUiSmokeTest.class.getResource("/moneymap/transactions.fxml"));
        loader.setControllerFactory(type -> new TransactionController(service));
        return loader.load();
    }

    /** Creates enough ordinary categories to exercise the page's vertical overflow path. */
    private static void createOverflowCategories(TransactionService service) {
        for (int index = 0; index < 24; index++) {
            service.createCategory(TransactionType.EXPENSE, "Overflow " + index);
        }
    }

    private static TransactionService createService(Path directory) throws Exception {
        return new TransactionService(new JsonDataRepository(directory),
                Clock.fixed(Instant.parse("2026-08-30T04:00:00Z"), ZoneId.of("Asia/Singapore")), UUID::randomUUID);
    }

    private static <T> T requireNode(Parent parent, String id, Class<T> type) {
        Object node = parent.lookup("#" + id);
        require(type.isInstance(node), "Missing or incorrect control: " + id);
        return type.cast(node);
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
