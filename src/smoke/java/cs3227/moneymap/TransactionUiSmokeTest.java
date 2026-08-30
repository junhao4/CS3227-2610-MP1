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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
            assertVisibleLabels(firstView);
            createIncome(firstView, service);
            rejectInvalidAmount(firstView, service);
            createFallbackExpense(firstView, service);

            TransactionService reloadedService = createService(applicationDirectory);
            Parent reloadedView = loadView(reloadedService);
            stage.setScene(new Scene(reloadedView, 900, 700));
            VBox rows = requireNode(reloadedView, "transactionRows", VBox.class);
            require(reloadedService.transactions().size() == 2, "Saved transactions did not reload");
            require(rows.getChildren().size() == 2, "Reloaded transactions were not visible");

            System.out.println("Verified transaction creation, validation, fallback assignment, and reload");
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
        require(requireNode(view, "transactionRows", VBox.class).getChildren().size() == 1,
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
    }

    private static void createFallbackExpense(Parent view, TransactionService service) {
        requireNode(view, "cancelTransactionButton", Button.class).fire();
        requireNode(view, "addTransactionButton", Button.class).fire();
        ComboBox<TransactionType> type = requireNode(view, "transactionTypeComboBox", ComboBox.class);
        ComboBox<Category> category = requireNode(view, "transactionCategoryComboBox", ComboBox.class);
        type.setValue(TransactionType.EXPENSE);
        category.setValue(null);
        requireNode(view, "transactionAmountField", TextField.class).setText("0.00");
        requireNode(view, "transactionDatePicker", DatePicker.class).setValue(TODAY.plusDays(1));
        requireNode(view, "transactionNoteArea", TextArea.class).clear();
        requireNode(view, "saveTransactionButton", Button.class).fire();
        require(service.transactions().size() == 2, "Valid fallback expense was not created");
        require(service.transactions().get(0).category().permanentFallback(),
                "Omitted category did not use the permanent fallback");
        require(service.transactions().get(0).category().type() == TransactionType.EXPENSE,
                "Omitted category used the wrong fallback type");
    }

    private static void assertVisibleLabels(Parent view) {
        requireNode(view, "transactionTypeLabel", Label.class);
        requireNode(view, "transactionAmountLabel", Label.class);
        requireNode(view, "transactionDateLabel", Label.class);
        requireNode(view, "transactionCategoryLabel", Label.class);
        requireNode(view, "transactionNoteLabel", Label.class);
    }

    private static void assertListFirstHierarchy(Parent view) {
        VBox list = requireNode(view, "transactionListSection", VBox.class);
        VBox form = requireNode(view, "transactionForm", VBox.class);
        require(list.getParent() == form.getParent(), "Transaction list and form do not share a page container");
        VBox page = (VBox) list.getParent();
        require(page.getChildren().indexOf(list) < page.getChildren().indexOf(form),
                "Transaction list does not appear before the entry form");
    }

    private static void assertProgressiveDisclosure(Parent view, Stage stage) {
        Button addButton = requireNode(view, "addTransactionButton", Button.class);
        ComboBox<TransactionType> type = requireNode(view, "transactionTypeComboBox", ComboBox.class);
        VBox form = requireNode(view, "transactionForm", VBox.class);
        require(!form.isManaged() && !form.isVisible(), "Transaction form is not initially hidden");
        addButton.fire();
        require(form.isManaged() && form.isVisible(), "Add transaction did not reveal the form");
        require(stage.getScene().getFocusOwner() == type, "Add transaction did not focus the first form control");
        requireNode(view, "cancelTransactionButton", Button.class).fire();
        require(!form.isManaged() && !form.isVisible(), "Cancel did not collapse the form");
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
