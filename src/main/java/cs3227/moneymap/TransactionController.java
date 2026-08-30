package cs3227.moneymap;

import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.Transaction;
import cs3227.moneymap.domain.TransactionType;
import cs3227.moneymap.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.Objects;

/** Connects the transaction-entry view to validated creation and persistence. */
public final class TransactionController {
    private final TransactionService service;

    @FXML
    private Button addTransactionButton;
    @FXML
    private ScrollPane transactionsScrollPane;
    @FXML
    private ComboBox<TransactionType> transactionTypeComboBox;
    @FXML
    private TextField transactionAmountField;
    @FXML
    private DatePicker transactionDatePicker;
    @FXML
    private ComboBox<Category> transactionCategoryComboBox;
    @FXML
    private TextArea transactionNoteArea;
    @FXML
    private Label noteCharacterCountLabel;
    @FXML
    private Label transactionValidationLabel;
    @FXML
    private VBox transactionRows;
    @FXML
    private VBox transactionEmptyState;
    @FXML
    private VBox transactionForm;

    TransactionController(TransactionService service) {
        this.service = Objects.requireNonNull(service);
    }

    @FXML
    private void initialize() {
        transactionTypeComboBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        transactionTypeComboBox.setConverter(new TransactionTypeConverter());
        transactionTypeComboBox.valueProperty()
                .addListener((observable, previous, selected) -> updateCategories(selected));
        transactionTypeComboBox.setValue(TransactionType.EXPENSE);
        transactionDatePicker.setValue(service.defaultDate());
        transactionNoteArea.textProperty().addListener((observable, previous, note) -> updateNoteCount(note));
        updateNoteCount("");
        renderTransactions();
    }

    @FXML
    private void saveTransaction() {
        clearValidation();
        try {
            Category selectedCategory = transactionCategoryComboBox.getValue();
            service.createTransaction(transactionTypeComboBox.getValue(), transactionAmountField.getText(),
                    transactionDatePicker.getValue(), selectedCategory == null ? null : selectedCategory.id(),
                    transactionNoteArea.getText());
            clearCompletedFormFields();
            renderTransactions();
            hideTransactionForm();
        } catch (RuntimeException exception) {
            showValidation(messageFor(exception));
        }
    }

    @FXML
    private void focusTransactionForm() {
        transactionForm.setManaged(true);
        transactionForm.setVisible(true);
        transactionsScrollPane.setVvalue(1.0);
        transactionTypeComboBox.requestFocus();
    }

    @FXML
    private void cancelTransaction() {
        clearCompletedFormFields();
        clearValidation();
        hideTransactionForm();
    }

    private void updateCategories(TransactionType selectedType) {
        transactionCategoryComboBox.setValue(null);
        if (selectedType == null) {
            transactionCategoryComboBox.getItems().clear();
            return;
        }
        transactionCategoryComboBox.setItems(FXCollections.observableArrayList(service.categoriesFor(selectedType)));
    }

    private void updateNoteCount(String note) {
        int count = note == null ? 0 : note.codePointCount(0, note.length());
        noteCharacterCountLabel.setText(count + " / 200");
    }

    private void clearCompletedFormFields() {
        transactionAmountField.clear();
        transactionCategoryComboBox.setValue(null);
        transactionNoteArea.clear();
        transactionDatePicker.setValue(service.defaultDate());
    }

    private void hideTransactionForm() {
        transactionForm.setManaged(false);
        transactionForm.setVisible(false);
        transactionsScrollPane.setVvalue(0.0);
        addTransactionButton.requestFocus();
    }

    private void renderTransactions() {
        transactionRows.getChildren().clear();
        for (Transaction transaction : service.transactions()) {
            transactionRows.getChildren().add(createTransactionRow(transaction));
        }
        boolean empty = service.transactions().isEmpty();
        transactionEmptyState.setManaged(empty);
        transactionEmptyState.setVisible(empty);
    }

    /** Builds one list row using the transaction's persisted display values. */
    private HBox createTransactionRow(Transaction transaction) {
        Label date = new Label(transaction.date().toString());
        date.getStyleClass().add("row-label");
        Label note = new Label(transaction.note().isBlank() ? "No note" : transaction.note());
        note.getStyleClass().add("muted");
        VBox description = new VBox(3, date, note);
        HBox.setHgrow(description, Priority.ALWAYS);

        Label category = new Label(transaction.category().name());
        category.setMinWidth(150);
        Label amount = new Label((transaction.type() == TransactionType.INCOME ? "+" : "−")
                + SgdFormatter.format(transaction.amount()));
        amount.setMinWidth(120);
        amount.getStyleClass().add(transaction.type() == TransactionType.INCOME
                ? "amount-income" : "amount-expense");
        HBox row = new HBox(18, description, category, amount);
        row.getStyleClass().add("table-row");
        return row;
    }

    private void showValidation(String message) {
        transactionValidationLabel.setText(message);
        transactionValidationLabel.setManaged(true);
        transactionValidationLabel.setVisible(true);
    }

    private void clearValidation() {
        transactionValidationLabel.setText("");
        transactionValidationLabel.setManaged(false);
        transactionValidationLabel.setVisible(false);
    }

    private static String messageFor(RuntimeException exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Check the transaction details and try again."
                : exception.getMessage();
    }

    private static final class TransactionTypeConverter extends StringConverter<TransactionType> {
        @Override
        public String toString(TransactionType type) {
            if (type == null) {
                return "";
            }
            return type == TransactionType.INCOME ? "Income" : "Expense";
        }

        @Override
        public TransactionType fromString(String value) {
            throw new UnsupportedOperationException("Transaction type is selected from the list.");
        }
    }
}
