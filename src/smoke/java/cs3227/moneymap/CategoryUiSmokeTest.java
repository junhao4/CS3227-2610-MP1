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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;

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
                    .map(node -> ((HBox) node).getChildren().get(0))
                    .map(node -> ((Label) node).getText())
                    .anyMatch(text -> text.equals("Investments · Income")),
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

            Category archived = service.createCategory(TransactionType.EXPENSE, "Loans");
            service.renameCategory(archived.id(), "Archived investments");
            service.archiveCategory(archived.id());
            Parent lifecycleView = loadView(service);
            stage.setScene(new Scene(lifecycleView, 700, 600));
            stage.show();
            require(requireNode(lifecycleView, "categoryRows", VBox.class).getChildren().stream()
                    .map(node -> ((HBox) node).getChildren().get(0))
                    .map(node -> ((Label) node).getText())
                    .noneMatch(text -> text.equals("Archived investments · Expense")),
                    "Archived category was shown in the active view");
            requireNode(lifecycleView, "archivedCategoriesButton", Button.class).fire();
            require(!requireNode(lifecycleView, "categoryCreationPanel", VBox.class).isVisible(),
                    "Category creation panel was shown in the archived view");
            require(requireNode(lifecycleView, "categoryListTitle", Label.class)
                            .getText().equals("Archived categories"),
                    "Archived view title was not shown");
            require(requireNode(lifecycleView, "categoryRows", VBox.class).getChildren().stream()
                    .map(node -> ((HBox) node).getChildren().get(0))
                    .map(node -> ((Label) node).getText())
                    .anyMatch(text -> text.equals("Archived investments · Expense")),
                    "Archived category was not shown in the archived view");
            chooseManagementAction("Restore");
            requireButtonNamed(categoryRow(lifecycleView, "Archived investments · Expense"), "Manage").fire();
            require(service.categoriesFor(TransactionType.EXPENSE).stream()
                    .anyMatch(category -> category.name().equals("Archived investments")),
                    "Manage dialog did not restore the archived category");
            requireNode(lifecycleView, "activeCategoriesButton", Button.class).fire();

            Category used = service.createCategory(TransactionType.EXPENSE, "Temporary");
            service.createTransaction(TransactionType.EXPENSE, "5.00", service.defaultDate(), used.id(), "Test");
            Parent deletionView = loadView(service);
            stage.setScene(new Scene(deletionView, 700, 600));
            stage.show();
            HBox usedRow = categoryRow(deletionView, "Temporary · Expense");
            require(requireButtonNamed(usedRow, "Manage") != null,
                    "Manage control was not shown for ordinary category");
            require(buttonNamed(usedRow, "Delete") == null, "Delete was not progressively disclosed");
            require(buttonNamed(usedRow, "Reassign") == null, "Reassign was not progressively disclosed");
            Category fallback = service.categoriesFor(TransactionType.EXPENSE).stream()
                    .filter(Category::permanentFallback).findFirst().orElseThrow();
            HBox fallbackRow = categoryRow(deletionView, fallback.name() + " · Expense");
            inspectFallbackManagementThenClose();
            requireButtonNamed(fallbackRow, "Manage").fire();
            inspectUsedManagementThenCancel();
            requireButtonNamed(usedRow, "Manage").fire();
            chooseManagementActionThenChooseAndConfirm("Reassign");
            requireButtonNamed(usedRow, "Manage").fire();
            require(service.transactions().stream()
                            .noneMatch(transaction -> transaction.category().id().equals(used.id())),
                    "Manage dialog did not reassign the used category's transaction");
            chooseManagementActionAndAcceptConfirmation("Delete");
            requireButtonNamed(usedRow, "Manage").fire();
            require(service.allCategories().stream().noneMatch(category -> category.id().equals(used.id())),
                    "Manage dialog did not delete the reassigned category");

            Category blocked = service.createCategory(TransactionType.EXPENSE, "Still used");
            service.createTransaction(TransactionType.EXPENSE, "7.00", service.defaultDate(), blocked.id(), "Blocked");
            Parent blockedDeletionView = loadView(service);
            stage.setScene(new Scene(blockedDeletionView, 700, 600));
            stage.show();
            inspectUsedManagementThenCancel();
            requireButtonNamed(categoryRow(blockedDeletionView, "Still used · Expense"), "Manage").fire();
            require(service.allCategories().stream().anyMatch(category -> category.id().equals(blocked.id())),
                    "Used category was deletable through its management dialog");

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
            require(transactionCategories.getItems().stream()
                    .anyMatch(category -> category.name().equals("Archived investments")),
                    "Restored category was not offered for a transaction");

            TransactionService reloaded = createService(applicationDirectory);
            require(reloaded.categoriesFor(TransactionType.INCOME).stream()
                    .anyMatch(category -> category.name().equals("Investments")),
                    "Income category did not survive restart");
            require(reloaded.categoriesFor(TransactionType.EXPENSE).stream()
                    .anyMatch(category -> category.name().equals("Investments")),
                    "Expense category did not survive restart");
            require(reloaded.categoriesFor(TransactionType.EXPENSE).stream()
                    .anyMatch(category -> category.name().equals("Archived investments")),
                    "Restored category did not survive restart as active");
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

    private static HBox categoryRow(Parent parent, String categoryText) {
        return requireNode(parent, "categoryRows", VBox.class).getChildren().stream()
                .map(HBox.class::cast)
                .filter(row -> ((Label) row.getChildren().getFirst()).getText().equals(categoryText))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Category row was not shown: " + categoryText));
    }

    private static Button buttonNamed(HBox row, String text) {
        return row.getChildren().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> button.getText().equals(text))
                .findFirst()
                .orElse(null);
    }

    private static Button requireButtonNamed(HBox row, String text) {
        Button button = buttonNamed(row, text);
        require(button != null, "Missing category row button: " + text);
        return button;
    }

    /** Schedules a click on the requested action in the next management dialog. */
    private static void chooseManagementAction(String action) {
        Platform.runLater(() -> buttonInOpenDialog(action).fire());
    }

    /** Selects a management action and accepts the confirmation dialog it opens. */
    private static void chooseManagementActionAndAcceptConfirmation(String action) {
        Platform.runLater(() -> {
            buttonInOpenDialog(action).fire();
            Platform.runLater(CategoryUiSmokeTest::acceptOpenDialog);
        });
    }

    /** Selects reassignment, accepts its target choice, then accepts its confirmation. */
    private static void chooseManagementActionThenChooseAndConfirm(String action) {
        Platform.runLater(() -> {
            buttonInOpenDialog(action).fire();
            Platform.runLater(() -> {
                acceptOpenDialog();
                Platform.runLater(CategoryUiSmokeTest::acceptOpenDialog);
            });
        });
    }

    /** Checks the used-category explanation and disabled Delete action before cancelling. */
    private static void inspectUsedManagementThenCancel() {
        Platform.runLater(() -> {
            DialogPane dialogPane = openDialogPane();
            require(dialogPane.getContentText().contains("Reassign them before deleting."),
                    "Used category management dialog did not explain reassignment");
            require(buttonInOpenDialog("Delete").isDisable(),
                    "Used category management dialog enabled deletion");
            ((Button) dialogPane.lookupButton(ButtonType.CANCEL)).fire();
        });
    }

    /** Checks the protected fallback explanation before closing the management dialog. */
    private static void inspectFallbackManagementThenClose() {
        Platform.runLater(() -> {
            DialogPane dialogPane = openDialogPane();
            require(dialogPane.getContentText().contains("permanent fallback"),
                    "Fallback management dialog did not explain its protected state");
            ((Button) dialogPane.lookupButton(ButtonType.CLOSE)).fire();
        });
    }

    /** Accepts the currently shown JavaFX dialog on the next event-loop turn. */
    private static void acceptOpenDialog() {
        ((Button) openDialogPane().lookupButton(ButtonType.OK)).fire();
    }

    /** Finds a button with the requested visible label in the currently open dialog. */
    private static Button buttonInOpenDialog(String text) {
        DialogPane dialogPane = openDialogPane();
        return dialogPane.getButtonTypes().stream()
                .filter(buttonType -> buttonType.getText().equals(text))
                .map(dialogPane::lookupButton)
                .map(Button.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing dialog action: " + text));
    }

    /** Returns the currently shown JavaFX dialog pane. */
    private static DialogPane openDialogPane() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .map(Window::getScene)
                .filter(java.util.Objects::nonNull)
                .map(scene -> scene.lookup(".dialog-pane"))
                .filter(DialogPane.class::isInstance)
                .map(DialogPane.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Expected confirmation dialog was not shown"));
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
