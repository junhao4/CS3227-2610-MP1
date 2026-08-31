package cs3227.moneymap;

import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.Transaction;
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
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.UUID;

/** Exercises the real transaction FXML, controller, service, and JSON repository together. */
@SuppressWarnings({"unchecked", "rawtypes"})
public class TransactionUiSmokeTest extends Application {
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 30);

    @Override
    public void start(Stage stage) throws Exception {
        Path applicationDirectory = Files.createTempDirectory("moneymap-transaction-ui-");
        try {
            TransactionService service = createService(applicationDirectory);
            Parent firstView = loadView(service);
            stage.setScene(new Scene(firstView, 900, 700));
            stage.show();

            assertListFirstHierarchy(firstView);
            assertProgressiveDisclosure(firstView, stage);
            require(((DatePicker) firstView.lookup("#transactionDatePicker")).getValue().equals(TODAY),
                    "The transaction date did not default to today");
            assertLedgerDisclosure(firstView);
            assertVisibleLabels(firstView);
            createIncome(firstView, service);
            rejectInvalidAmount(firstView, service);
            createFallbackExpense(firstView, service);
            createFallbackIncome(firstView, service);
            assertHistoryFilters(firstView);
            assertEditingAndConfirmedDeletion(firstView, service);

            TransactionService reloadedService = createService(applicationDirectory);
            Parent reloadedView = loadView(reloadedService);
            stage.setScene(new Scene(reloadedView, 900, 700));
            VBox rows = requireNode(reloadedView, "transactionRows", VBox.class);
            require(reloadedService.transactions().size() == 2, "Edited and deleted transactions did not reload");
            require(displayedTransactionRows(rows).size() == 2, "Reloaded transactions were not visible");

            System.out.println("Verified transaction creation, history filters, validation, fallback assignment, "
                    + "editing, confirmed/cancelled deletion, and reload");
        } finally {
            stage.close();
            Platform.exit();
        }
    }

    private static void createIncome(Parent view, TransactionService service) {
        requireNode(view, "addTransactionButton", Button.class).fire();
        ComboBox<TransactionType> type = requireNode(view, "transactionTypeComboBox", ComboBox.class);
        ComboBox<Category> category = requireNode(view, "transactionCategoryComboBox", ComboBox.class);
        type.setValue(TransactionType.INCOME);
        require(category.getItems().stream().allMatch(item -> item.type() == TransactionType.INCOME),
                "Income selection exposed an incompatible category");
        category.setValue(category.getItems().stream()
                .filter(item -> item.name().equals("Salary")).findFirst().orElseThrow());
        requireNode(view, "transactionAmountField", TextField.class).setText("600.00");
        requireNode(view, "transactionNoteArea", TextArea.class).setText("Allowance");
        requireNode(view, "saveTransactionButton", Button.class).fire();
        require(service.transactions().size() == 1, "Valid income was not created");
        require(displayedTransactionRows(requireNode(view, "transactionRows", VBox.class)).size() == 1,
                "Created income was not displayed");
        VBox form = requireNode(view, "transactionForm", VBox.class);
        require(!form.isManaged() && !form.isVisible(), "Successful creation did not collapse the form");
    }

    private static void rejectInvalidAmount(Parent view, TransactionService service) {
        requireNode(view, "addTransactionButton", Button.class).fire();
        requireNode(view, "transactionAmountField", TextField.class).setText("-0.01");
        requireNode(view, "saveTransactionButton", Button.class).fire();
        require(service.transactions().size() == 1, "Invalid amount created a transaction");
        Label feedback = requireNode(view, "transactionValidationLabel", Label.class);
        require(feedback.isVisible() && !feedback.getText().isBlank(), "Validation feedback was not visible");
        require(requireNode(view, "transactionForm", VBox.class).isVisible(),
                "Validation failure unexpectedly collapsed the form");
        requireNode(view, "transactionAmountField", TextField.class).setText("10000000");
        requireNode(view, "saveTransactionButton", Button.class).fire();
        require(service.transactions().size() == 1, "Over-limit amount created a transaction");
        require(feedback.isVisible() && feedback.getText().contains("9,999,999.99"),
                "Over-limit amount did not show the maximum-value feedback");
    }

    private static void createFallbackExpense(Parent view, TransactionService service) {
        requireNode(view, "cancelTransactionButton", Button.class).fire();
        requireNode(view, "addTransactionButton", Button.class).fire();
        ComboBox<TransactionType> type = requireNode(view, "transactionTypeComboBox", ComboBox.class);
        ComboBox<Category> category = requireNode(view, "transactionCategoryComboBox", ComboBox.class);
        type.setValue(TransactionType.EXPENSE);
        category.setValue(null);
        requireNode(view, "transactionAmountField", TextField.class).setText("0.00");
        requireNode(view, "transactionDatePicker", DatePicker.class).setValue(TODAY);
        requireNode(view, "transactionNoteArea", TextArea.class).clear();
        requireNode(view, "saveTransactionButton", Button.class).fire();
        require(service.transactions().size() == 2, "Valid fallback expense was not created");
        require(service.transactions().get(0).category().permanentFallback(),
                "Omitted category did not use the permanent fallback");
        require(service.transactions().get(0).category().type() == TransactionType.EXPENSE,
                "Omitted category used the wrong fallback type");
    }

    /** Creates an Income transaction that exercises the Income fallback category. */
    private static void createFallbackIncome(Parent view, TransactionService service) {
        requireNode(view, "addTransactionButton", Button.class).fire();
        ComboBox<TransactionType> type = requireNode(view, "transactionTypeComboBox", ComboBox.class);
        type.setValue(TransactionType.INCOME);
        requireNode(view, "transactionCategoryComboBox", ComboBox.class).setValue(null);
        requireNode(view, "transactionAmountField", TextField.class).setText("50.00");
        requireNode(view, "transactionDatePicker", DatePicker.class).setValue(TODAY.plusDays(2));
        requireNode(view, "transactionNoteArea", TextArea.class).setText("Fallback income");
        requireNode(view, "saveTransactionButton", Button.class).fire();
        require(service.transactions().size() == 3, "Valid fallback income was not created");
        require(service.transactions().get(0).category().permanentFallback(),
                "Omitted income category did not use the permanent fallback");
        require(service.transactions().get(0).category().type() == TransactionType.INCOME,
                "Omitted income category used the wrong fallback type");
    }

    private static void assertVisibleLabels(Parent view) {
        requireNode(view, "transactionTypeLabel", Label.class);
        requireNode(view, "transactionAmountLabel", Label.class);
        requireNode(view, "transactionDateLabel", Label.class);
        requireNode(view, "transactionCategoryLabel", Label.class);
        requireNode(view, "transactionNoteLabel", Label.class);
        requireNode(view, "transactionMonthFilterLabel", Label.class);
        requireNode(view, "transactionTypeFilterLabel", Label.class);
        requireNode(view, "transactionCategoryFilterLabel", Label.class);
        requireNode(view, "transactionNoteSearchLabel", Label.class);
    }

    /** Verifies that search is immediate while less-frequent filters are progressively disclosed. */
    private static void assertLedgerDisclosure(Parent view) {
        VBox filterPanel = requireNode(view, "transactionFilterPanel", VBox.class);
        require(!filterPanel.isVisible() && !filterPanel.isManaged(), "Filters were visible before being requested");
        Button filterToggle = requireNode(view, "transactionFilterToggleButton", Button.class);
        filterToggle.fire();
        require(filterPanel.isVisible() && filterPanel.isManaged(), "Filter control did not reveal filters");
        filterToggle.fire();
        require(!filterPanel.isVisible() && !filterPanel.isManaged(), "Filter control did not hide filters");
    }

    /** Verifies the real history controls filter display data without changing saved transactions. */
    private static void assertHistoryFilters(Parent view) {
        VBox rows = requireNode(view, "transactionRows", VBox.class);
        ComboBox<YearMonth> monthFilter = requireNode(view, "transactionMonthFilter", ComboBox.class);
        ComboBox<TransactionType> typeFilter = requireNode(view, "transactionTypeFilter", ComboBox.class);
        ComboBox<Category> categoryFilter = requireNode(view, "transactionCategoryFilter", ComboBox.class);
        TextField noteSearch = requireNode(view, "transactionNoteSearchField", TextField.class);

        require(displayedTransactionRows(rows).size() == 3, "History did not show all saved transactions");
        require(dailyGroups(rows).size() == 2, "Transactions on the same date were not grouped together");
        HBox newestRow = displayedTransactionRows(rows).getFirst();
        VBox newestDescription = (VBox) newestRow.getChildren().getFirst();
        require(((Label) newestDescription.getChildren().getFirst()).getText().equals("Fallback income"),
                "History is not ordered with the newest date first");
        require(((Label) newestDescription.getChildren().getFirst()).isWrapText(),
                "Transaction notes do not wrap in the daily ledger");
        require(((Label) rows.getChildren().getFirst()).getText().contains("2026"),
                "Daily ledger does not display readable date headings");

        monthFilter.setValue(YearMonth.from(TODAY));
        require(displayedTransactionRows(rows).size() == 2, "Month filter did not narrow the history");
        monthFilter.setValue(YearMonth.from(TODAY.plusDays(2)));
        require(displayedTransactionRows(rows).size() == 1, "Month filter did not show the selected month");
        requireNode(view, "clearTransactionFiltersButton", Button.class).fire();

        Category incomeFallback = categoryFilter.getItems().stream()
                .filter(category -> category.permanentFallback() && category.type() == TransactionType.INCOME)
                .findFirst()
                .orElseThrow();
        Category expenseFallback = categoryFilter.getItems().stream()
                .filter(category -> category.permanentFallback() && category.type() == TransactionType.EXPENSE)
                .findFirst()
                .orElseThrow();
        require(categoryFilter.getConverter().toString(incomeFallback).equals("Uncategorised (Income)"),
                "Income fallback category filter is not type-qualified");
        require(categoryFilter.getConverter().toString(expenseFallback).equals("Uncategorised (Expense)"),
                "Expense fallback category filter is not type-qualified");
        categoryFilter.setValue(incomeFallback);
        require(displayedTransactionRows(rows).size() == 1, "Income fallback filter did not select its own record");
        categoryFilter.setValue(expenseFallback);
        require(displayedTransactionRows(rows).size() == 1, "Expense fallback filter did not select its own record");
        requireNode(view, "clearTransactionFiltersButton", Button.class).fire();

        typeFilter.setValue(TransactionType.INCOME);
        require(displayedTransactionRows(rows).size() == 2, "Type filter did not narrow the history");
        categoryFilter.setValue(categoryFilter.getItems().stream()
                .filter(category -> category.name().equals("Salary")).findFirst().orElseThrow());
        require(displayedTransactionRows(rows).size() == 1, "Category filter did not combine with type filter");
        noteSearch.setText("ALLOWANCE");
        require(displayedTransactionRows(rows).size() == 1,
                "Case-insensitive note search did not retain the matching entry");

        noteSearch.setText("does not exist");
        VBox emptyState = requireNode(view, "transactionEmptyState", VBox.class);
        Label emptyTitle = requireNode(view, "transactionEmptyTitle", Label.class);
        require(emptyState.isVisible() && emptyTitle.getText().equals("No matching transactions"),
                "Empty filtered history did not explain that no records matched");

        requireNode(view, "clearTransactionFiltersButton", Button.class).fire();
        require(displayedTransactionRows(rows).size() == 3 && noteSearch.getText().isEmpty()
                        && monthFilter.getValue() == null && typeFilter.getValue() == null
                        && categoryFilter.getValue() == null,
                "Clear filters did not restore the complete transaction history");
    }

    /** Exercises type-aware edits plus both cancellation and confirmation of permanent deletion. */
    private static void assertEditingAndConfirmedDeletion(Parent view, TransactionService service) {
        VBox rows = requireNode(view, "transactionRows", VBox.class);
        Category salary = service.categoriesFor(TransactionType.INCOME).stream()
                .filter(category -> category.name().equals("Salary")).findFirst().orElseThrow();
        service.archiveCategory(salary.id());
        requireNode(view, "transactionTypeFilter", ComboBox.class).setValue(TransactionType.INCOME);
        requireNode(view, "clearTransactionFiltersButton", Button.class).fire();
        HBox salaryRow = transactionRowWithNote(rows, "Allowance");
        menuButtonNamed(salaryRow).getItems().getFirst().fire();
        require(requireNode(view, "transactionFormTitle", Label.class).getText().equals("Edit transaction"),
                "Edit did not identify the correction form");
        ComboBox<TransactionType> type = requireNode(view, "transactionTypeComboBox", ComboBox.class);
        ComboBox<Category> category = requireNode(view, "transactionCategoryComboBox", ComboBox.class);
        require(category.getValue().archived(), "Edit did not retain its archived historical category");
        requireNode(view, "transactionNoteArea", TextArea.class).setText("Corrected allowance");
        requireNode(view, "saveTransactionButton", Button.class).fire();
        require(service.transactions().stream().filter(transaction -> transaction.note().equals("Corrected allowance"))
                        .findFirst().orElseThrow().category().archived(),
                "Saving an edit changed an archived historical category");

        salaryRow = transactionRowWithNote(rows, "Corrected allowance");
        menuButtonNamed(salaryRow).getItems().getFirst().fire();
        type.setValue(TransactionType.EXPENSE);
        require(category.getItems().stream().allMatch(item -> item.type() == TransactionType.EXPENSE),
                "Changing type during edit exposed incompatible categories");
        category.setValue(category.getItems().stream()
                .filter(item -> item.name().equals("Food")).findFirst().orElseThrow());
        requireNode(view, "transactionAmountField", TextField.class).setText("15.00");
        requireNode(view, "transactionDatePicker", DatePicker.class).setValue(TODAY.plusDays(3));
        requireNode(view, "transactionNoteArea", TextArea.class).setText("Edited income to expense");
        requireNode(view, "saveTransactionButton", Button.class).fire();
        Transaction edited = service.transactions().stream()
                .filter(transaction -> transaction.note().equals("Edited income to expense"))
                .findFirst().orElseThrow();
        require(edited.type() == TransactionType.EXPENSE,
                "Edited transaction type was not saved");
        require(edited.category().name().equals("Food"),
                "Edited transaction category was not saved");

        HBox editedRow = displayedTransactionRows(rows).getFirst();
        cancelDeletion(editedRow);
        require(service.transactions().size() == 3, "Cancelling deletion removed a transaction");

        HBox oldestRow = displayedTransactionRows(rows).getLast();
        confirmDeletion(oldestRow);
        require(service.transactions().size() == 2, "Confirming deletion did not remove a transaction");
        require(displayedTransactionRows(rows).size() == 2, "Confirmed deletion did not refresh the transaction list");
    }

    private static HBox transactionRowWithNote(VBox rows, String note) {
        return displayedTransactionRows(rows).stream()
                .filter(row -> row.lookupAll(".label").stream().filter(Label.class::isInstance).map(Label.class::cast)
                        .anyMatch(label -> label.getText().equals(note)))
                .findFirst().orElseThrow(() -> new IllegalStateException("Transaction row was not shown: " + note));
    }

    /** Opens a delete confirmation and uses its standard Cancel action. */
    private static void cancelDeletion(HBox row) {
        Platform.runLater(() -> ((Button) openDialogPane().lookupButton(ButtonType.CANCEL)).fire());
        menuButtonNamed(row).getItems().get(1).fire();
    }

    /** Opens a delete confirmation and uses its standard OK action. */
    private static void confirmDeletion(HBox row) {
        Platform.runLater(() -> ((Button) openDialogPane().lookupButton(ButtonType.OK)).fire());
        menuButtonNamed(row).getItems().get(1).fire();
    }

    private static MenuButton menuButtonNamed(HBox row) {
        return row.lookupAll(".menu-button").stream().filter(MenuButton.class::isInstance)
                .map(MenuButton.class::cast).filter(button -> button.getText().equals("⋯")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Transaction more-actions button was not shown"));
    }

    private static java.util.List<HBox> displayedTransactionRows(VBox rows) {
        return dailyGroups(rows).stream().flatMap(group -> group.getChildren().stream())
                .filter(HBox.class::isInstance).map(HBox.class::cast).toList();
    }

    private static java.util.List<VBox> dailyGroups(VBox rows) {
        return rows.getChildren().stream().filter(VBox.class::isInstance).map(VBox.class::cast)
                .filter(group -> group.getStyleClass().contains("daily-ledger-group")).toList();
    }

    private static DialogPane openDialogPane() {
        return Window.getWindows().stream().filter(Window::isShowing)
                .flatMap(window -> window.getScene() == null ? java.util.stream.Stream.empty()
                        : window.getScene().getRoot().lookupAll(".dialog-pane").stream())
                .filter(DialogPane.class::isInstance).map(DialogPane.class::cast).findFirst()
                .orElseThrow(() -> new IllegalStateException("Expected a confirmation dialog"));
    }

    private static void assertListFirstHierarchy(Parent view) {
        VBox list = requireNode(view, "transactionListSection", VBox.class);
        VBox ledger = requireNode(view, "transactionLedger", VBox.class);
        HBox actions = requireNode(view, "transactionLedgerActions", HBox.class);
        VBox form = requireNode(view, "transactionForm", VBox.class);
        require(list.getParent() == ledger, "Transaction list is not contained by the ledger view");
        require(actions.getParent() == ledger, "Transaction actions are not contained by the ledger view");
        require(ledger.getChildren().indexOf(actions) < ledger.getChildren().indexOf(list),
                "Transaction actions do not appear above the ledger");
        require(!form.isVisible() && !form.isManaged(), "Transaction form is visible with the ledger initially");
    }

    private static void assertProgressiveDisclosure(Parent view, Stage stage) {
        Button addButton = requireNode(view, "addTransactionButton", Button.class);
        ComboBox<TransactionType> type = requireNode(view, "transactionTypeComboBox", ComboBox.class);
        VBox header = requireNode(view, "transactionHistoryHeader", VBox.class);
        VBox ledger = requireNode(view, "transactionLedger", VBox.class);
        VBox form = requireNode(view, "transactionForm", VBox.class);
        require(header.isVisible() && ledger.isVisible(), "Transaction ledger is not initially visible");
        require(!form.isManaged() && !form.isVisible(), "Transaction form is not initially hidden");
        addButton.fire();
        require(form.isManaged() && form.isVisible(), "Add transaction did not show the focused form view");
        require(!header.isVisible() && !ledger.isVisible(), "Add transaction did not replace the ledger view");
        require(stage.getScene().getFocusOwner() == type, "Add transaction did not focus the first form control");
        requireNode(view, "backToTransactionsButton", Button.class).fire();
        require(!form.isManaged() && !form.isVisible(), "Back did not hide the focused form view");
        require(header.isVisible() && ledger.isVisible(), "Back did not restore the ledger view");
    }

    private static Parent loadView(TransactionService service) throws Exception {
        FXMLLoader loader = new FXMLLoader(TransactionUiSmokeTest.class.getResource("/moneymap/transactions.fxml"));
        loader.setControllerFactory(type -> new TransactionController(service));
        return loader.load();
    }

    private static TransactionService createService(Path applicationDirectory) throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-08-30T04:00:00Z"), ZoneId.of("Asia/Singapore"));
        return new TransactionService(new JsonDataRepository(applicationDirectory), clock, UUID::randomUUID);
    }

    @SuppressWarnings("unchecked")
    private static <T> T requireNode(Parent parent, String id, Class<T> type) {
        Object node = parent.lookup("#" + id);
        require(type.isInstance(node), "Missing or incorrect control: " + id);
        return (T) node;
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
