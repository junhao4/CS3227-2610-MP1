package cs3227.moneymap;

import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.MoneyAmount;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Objects;
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
            Category overbudgetCategory = service.createCategory(TransactionType.EXPENSE, "Overbudget smoke");
            service.createTransaction(TransactionType.EXPENSE, "20.00", service.defaultDate(),
                    overbudgetCategory.id(), "Over-budget smoke data");
            service.setRecurringBudget(overbudgetCategory.id(), "10.00");
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
            FlowPane categoryCards = requireNode(view, "categoryRows", FlowPane.class);
            require(categoryCards.getChildren().stream().allMatch(node -> node instanceof VBox
                            && node.getStyleClass().contains("category-card")),
                    "Categories were not rendered as prototype-style cards");
            require(categoryCards.getChildren().stream().map(VBox.class::cast)
                            .flatMap(card -> card.getChildren().stream())
                            .anyMatch(ProgressBar.class::isInstance),
                    "Expense category cards did not include budget progress bars");
            require(categoryCards.getChildren().stream().map(VBox.class::cast)
                            .allMatch(card -> card.getStyleClass().contains("expense-summary-card")
                                    && card.getPrefHeight() == 250),
                    "Expense category cards did not share a stable height");
            VBox overbudgetCard = categoryCards.getChildren().stream().map(VBox.class::cast)
                    .filter(card -> card.getChildren().stream().filter(Label.class::isInstance)
                            .map(Label.class::cast).anyMatch(label -> label.getText().contains("over budget")))
                    .findFirst().orElseThrow(() -> new IllegalStateException("Over-budget card was not rendered"));
            require(overbudgetCard.getChildren().stream().filter(ProgressBar.class::isInstance)
                            .map(ProgressBar.class::cast).anyMatch(progress -> progress.isVisible()
                                    && progress.getPrefHeight() > 0),
                    "Over-budget card did not retain a visible progress bar");
            require(overbudgetCard.getChildren().stream().filter(Button.class::isInstance)
                            .map(Button.class::cast).anyMatch(button -> button.getStyleClass()
                                    .contains("manage-action")),
                    "Manage action was not marked for bold card styling");

            Button manageBudgets = requireNode(view, "manageBudgetsButton", Button.class);
            manageBudgets.fire();
            require(!requireNode(view, "categoryLandingView", VBox.class).isVisible(),
                    "Category landing view remained visible while managing budgets");
            require(requireNode(view, "budgetManagerView", VBox.class).isVisible(),
                    "Budget manager was not progressively disclosed");
            requireNode(view, "viewBudgetMonthPicker", DatePicker.class);
            HBox foodBudgetRow = (HBox) budgetRow(view, "Food").getChildren().getFirst();
            requireButtonNamed(foodBudgetRow, "Set budget").fire();

            TextField budgetAmount = requireNode(view, "budgetAmountField", TextField.class);
            Button saveBudget = requireNode(view, "setBudgetButton", Button.class);
            Category food = service.categoriesFor(TransactionType.EXPENSE).stream()
                    .filter(category -> category.name().equals("Food")).findFirst().orElseThrow();
            requireNode(view, "budgetDetailPanel", VBox.class);
            requireButtonNamed(requireNode(view, "recurringBudgetRow", HBox.class), "Set").fire();
            budgetAmount.setText("0.00");
            saveBudget.fire();
            require(service.budgetFor(food.id(), YearMonth.of(2026, 8)).orElseThrow().amount()
                            .equals(MoneyAmount.parse("0")),
                    "Explicit zero budget was not saved from the category view");
            require(service.budgetFor(food.id(), YearMonth.of(2026, 9)).orElseThrow().repeatsMonthly(),
                    "Recurring budget did not apply to a different calendar month");
            Button removeRecurring = requireNode(view, "removeRecurringBudgetButton", Button.class);
            require(removeRecurring.isVisible(), "Configured recurring budget did not expose a remove action");
            removeRecurring.fire();
            require(service.recurringBudgetFor(food.id()).isEmpty(),
                    "Recurring budget could not be removed from the focused editor");
            require(service.budgetFor(food.id(), YearMonth.of(2026, 8)).isEmpty(),
                    "Removing the recurring budget left an unexpected effective budget");
            require(!removeRecurring.isVisible(), "Removed recurring budget kept its remove action visible");
            requireButtonNamed(requireNode(view, "recurringBudgetRow", HBox.class), "Set").fire();
            budgetAmount.setText("0.00");
            saveBudget.fire();
            requireButtonNamed(requireNode(view, "monthBudgetRow", HBox.class), "Set").fire();
            budgetAmount.setText("1.001");
            saveBudget.fire();
            require(requireNode(view, "budgetValidationLabel", Label.class).isVisible(),
                    "Invalid budget did not show validation feedback");
            require(service.budgetFor(food.id(), YearMonth.of(2026, 8)).orElseThrow().amount()
                            .equals(MoneyAmount.parse("0")),
                    "Invalid budget changed the persisted budget");
            budgetAmount.setText("100.00");
            saveBudget.fire();
            require(service.budgetFor(food.id(), YearMonth.of(2026, 8)).orElseThrow().amount()
                            .equals(MoneyAmount.parse("100.00")),
                    "Month-only budget did not override the recurring budget");
            require(service.budgetFor(food.id(), YearMonth.of(2026, 9)).orElseThrow().amount()
                            .equals(MoneyAmount.parse("0")),
                    "Month-only budget changed the recurring budget");
            Button removeMonth = requireNode(view, "removeMonthBudgetButton", Button.class);
            require(removeMonth.isVisible(), "Configured month-only budget did not expose a remove action");
            removeMonth.fire();
            require(service.monthOnlyBudgetFor(food.id(), YearMonth.of(2026, 8)).isEmpty(),
                    "Month-only budget could not be removed from the focused editor");
            require(service.budgetFor(food.id(), YearMonth.of(2026, 8)).orElseThrow().amount()
                            .equals(MoneyAmount.parse("0")),
                    "Removing a month-only budget did not reveal the recurring value");
            require(!removeMonth.isVisible(), "Removed month-only budget kept its remove action visible");
            requireButtonNamed(requireNode(view, "monthBudgetRow", HBox.class), "Set").fire();
            budgetAmount.setText("100.00");
            saveBudget.fire();
            require(budgetRow(view, "Food").getStyleClass().contains("category-card"),
                    "Budget item did not use the prototype card presentation");
            require(budgetRow(view, "Food").getChildren().stream()
                            .filter(Label.class::isInstance).map(Label.class::cast)
                            .anyMatch(label -> label.getText().contains("Spent in August 2026:")),
                    "Budget card did not identify spending for the selected month");
            require(budgetRow(view, "Food").getChildren().stream()
                            .filter(Label.class::isInstance).map(Label.class::cast)
                            .anyMatch(label -> label.getText().contains("Applied budget for August 2026:")),
                    "Budget card did not identify the applied budget scope");
            require(budgetRow(view, "Food").getChildren().stream()
                            .filter(Label.class::isInstance).map(Label.class::cast)
                            .anyMatch(label -> label.getText().equals("Monthly budget: S$0.00 every month")),
                    "Budget list did not show the monthly default beneath a month-only budget");
            requireNode(view, "backToCategoriesButton", Button.class).fire();
            require(requireNode(view, "categoryLandingView", VBox.class).isVisible(),
                    "Back to categories did not restore the category landing view");

            require(!requireNode(view, "categoryCreationPanel", VBox.class).isVisible(),
                    "Category creation form was not progressively disclosed");
            requireNode(view, "newCategoryButton", Button.class).fire();
            DialogPane categoryDialog = openCategoryDialog();
            ((Button) categoryDialog.lookupButton(ButtonType.CANCEL)).fire();
            require(!hasOpenCategoryDialog(), "Category dialog did not close from its Cancel action");
            requireNode(view, "newCategoryButton", Button.class).fire();
            categoryDialog = openCategoryDialog();
            categoryDialog.getScene().getWindow().hide();
            require(!hasOpenCategoryDialog(), "Category dialog did not close from its window close action");
            requireNode(view, "newCategoryButton", Button.class).fire();
            categoryDialog = openCategoryDialog();
            ComboBox<TransactionType> type = requireNode(categoryDialog, "categoryTypeComboBox", ComboBox.class);
            TextField name = requireNode(categoryDialog, "categoryNameField", TextField.class);
            Button create = requireNode(categoryDialog, "createCategoryButton", Button.class);
            type.setValue(TransactionType.INCOME);
            name.setText("  Investments  ");
            create.fire();
            require(service.categoriesFor(TransactionType.INCOME).stream()
                    .anyMatch(category -> category.name().equals("Investments")),
                    "Valid custom category was not created");
            require(categoryRow(view, "Investments").getStyleClass().contains("category-card"),
                    "Created category was not displayed");
            VBox incomeCard = (VBox) categoryRow(view, "Investments");
            require(incomeCard.getStyleClass().contains("income-summary-card")
                            && incomeCard.getPrefHeight() < overbudgetCard.getPrefHeight(),
                    "Income category cards were not compact compared with expense cards");

            requireNode(view, "newCategoryButton", Button.class).fire();
            categoryDialog = openCategoryDialog();
            type = requireNode(categoryDialog, "categoryTypeComboBox", ComboBox.class);
            name = requireNode(categoryDialog, "categoryNameField", TextField.class);
            create = requireNode(categoryDialog, "createCategoryButton", Button.class);
            type.setValue(TransactionType.EXPENSE);
            name.setText("Investments");
            create.fire();
            require(service.categoriesFor(TransactionType.EXPENSE).stream()
                    .anyMatch(category -> category.name().equals("Investments")),
                    "Same category name could not be created for the other type");

            requireNode(view, "newCategoryButton", Button.class).fire();
            categoryDialog = openCategoryDialog();
            name = requireNode(categoryDialog, "categoryNameField", TextField.class);
            create = requireNode(categoryDialog, "createCategoryButton", Button.class);
            name.setText(" investments ");
            create.fire();
            require(requireNode(categoryDialog, "categoryValidationLabel", Label.class).isVisible(),
                    "Duplicate category did not show validation feedback");
            require(service.categoriesFor(TransactionType.EXPENSE).stream()
                    .filter(category -> category.name().equalsIgnoreCase("Investments")).count() == 1,
                    "Duplicate category was created");
            ((Button) categoryDialog.lookupButton(ButtonType.CANCEL)).fire();
            chooseManagementAction("Set budget");
            requireButtonNamed(categoryRow(view, "Investments"), "Manage").fire();
            require(requireNode(view, "budgetManagerView", VBox.class).isVisible(),
                    "Manage dialog did not open the focused budget view");
            require(requireNode(view, "budgetDetailPanel", VBox.class).isVisible(),
                    "Focused budget view did not select the managed category");
            require(requireNode(view, "budgetEditorTitle", Label.class).getText().contains("Investments"),
                    "Focused budget view did not identify the selected category");
            requireNode(view, "backToCategoriesButton", Button.class).fire();

            Category archived = service.createCategory(TransactionType.EXPENSE, "Loans");
            service.renameCategory(archived.id(), "Archived investments");
            service.archiveCategory(archived.id());
            Parent lifecycleView = loadView(service);
            stage.setScene(new Scene(lifecycleView, 700, 600));
            stage.show();
            require(requireNode(lifecycleView, "categoryRows", FlowPane.class).getChildren().stream()
                    .map(node -> ((VBox) node).getChildren().get(0))
                    .map(node -> ((Label) node).getText())
                    .noneMatch(text -> text.equals("Archived investments")),
                    "Archived category was shown in the active view");
            requireNode(lifecycleView, "archivedCategoriesButton", Button.class).fire();
            require(!requireNode(lifecycleView, "categoryCreationPanel", VBox.class).isVisible(),
                    "Category creation panel was shown in the archived view");
            require(requireNode(lifecycleView, "categoryListTitle", Label.class)
                            .getText().equals("Archived expense categories"),
                    "Archived view title was not shown");
            require(requireNode(lifecycleView, "categoryRows", FlowPane.class).getChildren().stream()
                    .map(node -> ((VBox) node).getChildren().get(0))
                    .map(node -> ((Label) node).getText())
                    .anyMatch(text -> text.equals("Archived investments")),
                    "Archived category was not shown in the archived view");
            chooseManagementAction("Restore");
            requireButtonNamed(categoryRow(lifecycleView, "Archived investments"), "Manage").fire();
            require(service.categoriesFor(TransactionType.EXPENSE).stream()
                    .anyMatch(category -> category.name().equals("Archived investments")),
                    "Manage dialog did not restore the archived category");
            requireNode(lifecycleView, "activeCategoriesButton", Button.class).fire();

            Category used = service.createCategory(TransactionType.EXPENSE, "Temporary");
            service.createTransaction(TransactionType.EXPENSE, "5.00", service.defaultDate(), used.id(), "Test");
            Parent deletionView = loadView(service);
            stage.setScene(new Scene(deletionView, 700, 600));
            stage.show();
            Parent usedRow = categoryRow(deletionView, "Temporary");
            require(requireButtonNamed(usedRow, "Manage") != null,
                    "Manage control was not shown for ordinary category");
            require(buttonNamed(usedRow, "Delete") == null, "Delete was not progressively disclosed");
            require(buttonNamed(usedRow, "Reassign") == null, "Reassign was not progressively disclosed");
            Category fallback = service.categoriesFor(TransactionType.EXPENSE).stream()
                    .filter(Category::permanentFallback).findFirst().orElseThrow();
            Parent fallbackRow = categoryRow(deletionView, fallback.name());
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
            requireButtonNamed(categoryRow(blockedDeletionView, "Still used"), "Manage").fire();
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
            require(reloaded.budgetFor(food.id(), YearMonth.of(2026, 8)).orElseThrow().amount()
                            .equals(MoneyAmount.parse("100")),
                    "Month-only budget did not survive restart");
            require(reloaded.budgetFor(food.id(), YearMonth.of(2026, 9)).orElseThrow().repeatsMonthly(),
                    "Recurring budget did not survive restart");
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

    private static DialogPane openCategoryDialog() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .map(Window::getScene)
                .filter(Objects::nonNull)
                .map(scene -> scene.getRoot())
                .filter(root -> root instanceof DialogPane
                        && root.lookup("#categoryCreationPanel") != null)
                .map(DialogPane.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("New category dialog was not shown"));
    }

    private static boolean hasOpenCategoryDialog() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .map(Window::getScene)
                .filter(java.util.Objects::nonNull)
                .map(scene -> scene.getRoot())
                .anyMatch(root -> root instanceof DialogPane && root.lookup("#categoryCreationPanel") != null);
    }

    private static Parent categoryRow(Parent parent, String categoryText) {
        return java.util.stream.Stream.concat(
                        requireNode(parent, "categoryRows", FlowPane.class).getChildren().stream(),
                        requireNode(parent, "incomeCategoryRows", FlowPane.class).getChildren().stream())
                .map(Parent.class::cast)
                .filter(row -> ((Label) row.getChildrenUnmodifiable().getFirst()).getText().equals(categoryText))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Category row was not shown: " + categoryText));
    }

    /** Finds a budget row by the category label shown at the start of its main line. */
    private static VBox budgetRow(Parent parent, String categoryName) {
        return requireNode(parent, "budgetRows", VBox.class).getChildren().stream()
                .map(VBox.class::cast)
                .filter(row -> ((HBox) row.getChildren().getFirst()).getChildren().stream()
                        .filter(Label.class::isInstance).map(Label.class::cast)
                        .anyMatch(label -> label.getText().equals(categoryName)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Budget row was not shown: " + categoryName));
    }

    private static Button buttonNamed(Parent row, String text) {
        return row.getChildrenUnmodifiable().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> button.getText().equals(text))
                .findFirst()
                .orElse(null);
    }

    private static Button requireButtonNamed(Parent row, String text) {
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
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .map(Window::getScene)
                .filter(java.util.Objects::nonNull)
                .map(scene -> scene.lookup(".dialog-pane"))
                .filter(DialogPane.class::isInstance)
                .map(DialogPane.class::cast)
                .filter(dialogPane -> dialogPane.getButtonTypes().stream()
                        .anyMatch(buttonType -> buttonType.getText().equals(text)))
                .map(dialogPane -> dialogPane.getButtonTypes().stream()
                        .filter(buttonType -> buttonType.getText().equals(text))
                        .findFirst()
                        .map(dialogPane::lookupButton)
                        .orElse(null))
                .filter(Button.class::isInstance)
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
                .reduce((first, second) -> second)
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
