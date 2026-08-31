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

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Connects the Transactions view to creation, persistence, and history review. */
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
    private ComboBox<YearMonth> transactionMonthFilter;
    @FXML
    private ComboBox<TransactionType> transactionTypeFilter;
    @FXML
    private ComboBox<Category> transactionCategoryFilter;
    @FXML
    private TextField transactionNoteSearchField;
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
    private Label transactionEmptyTitle;
    @FXML
    private Label transactionEmptyMessage;
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
        configureHistoryFilters();
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
            refreshMonthFilter();
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

    /** Configures history inputs to rerender the list when their query changes. */
    private void configureHistoryFilters() {
        List<Category> categories = allCategories();
        transactionMonthFilter.setConverter(new YearMonthConverter());
        transactionTypeFilter.setItems(FXCollections.observableArrayList(TransactionType.values()));
        transactionTypeFilter.setConverter(new TransactionTypeConverter());
        transactionCategoryFilter.setItems(FXCollections.observableArrayList(categories));
        transactionCategoryFilter.setConverter(new CategoryConverter(categories));
        refreshMonthFilter();

        transactionMonthFilter.valueProperty().addListener((observable, previous, selected) -> renderTransactions());
        transactionTypeFilter.valueProperty().addListener((observable, previous, selected) -> renderTransactions());
        transactionCategoryFilter.valueProperty().addListener((observable, previous, selected) -> renderTransactions());
        transactionNoteSearchField.textProperty().addListener((observable, previous, text) -> renderTransactions());
    }

    /** Returns all categories in a predictable order for the history filter. */
    private List<Category> allCategories() {
        return List.of(TransactionType.values()).stream()
                .flatMap(type -> service.categoriesFor(type).stream())
                .sorted(Comparator.comparing(Category::name))
                .toList();
    }

    /** Refreshes the available transaction months while preserving a valid selection. */
    private void refreshMonthFilter() {
        YearMonth selectedMonth = transactionMonthFilter.getValue();
        List<YearMonth> months = service.transactions().stream()
                .map(transaction -> YearMonth.from(transaction.date()))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
        transactionMonthFilter.getItems().setAll(months);
        transactionMonthFilter.setValue(months.contains(selectedMonth) ? selectedMonth : null);
    }

    /** Restores the unfiltered transaction-history display. */
    @FXML
    private void clearTransactionFilters() {
        transactionMonthFilter.setValue(null);
        transactionTypeFilter.setValue(null);
        transactionCategoryFilter.setValue(null);
        transactionNoteSearchField.clear();
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

    /** Renders transactions that match the current history-filter controls. */
    private void renderTransactions() {
        transactionRows.getChildren().clear();
        Category selectedCategory = transactionCategoryFilter.getValue();
        List<Transaction> results = service.findTransactions(
                transactionMonthFilter.getValue(), transactionTypeFilter.getValue(),
                selectedCategory == null ? null : selectedCategory.id(), transactionNoteSearchField.getText());
        for (Transaction transaction : results) {
            transactionRows.getChildren().add(createTransactionRow(transaction));
        }
        boolean empty = results.isEmpty();
        transactionEmptyState.setManaged(empty);
        transactionEmptyState.setVisible(empty);
        boolean hasTransactions = !service.transactions().isEmpty();
        transactionEmptyTitle.setText(hasTransactions ? "No matching transactions" : "No transactions recorded");
        transactionEmptyMessage.setText(hasTransactions
                ? "Try clearing a filter or searching for a different note."
                : "Use Add transaction to record your first income or expense.");
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

    /** Formats history categories and distinguishes otherwise identical names. */
    private static final class CategoryConverter extends StringConverter<Category> {
        private final List<Category> categories;

        private CategoryConverter(List<Category> categories) {
            this.categories = List.copyOf(categories);
        }

        @Override
        public String toString(Category category) {
            if (category == null) {
                return "";
            }
            return hasDuplicateName(category)
                    ? category.name() + " (" + displayType(category.type()) + ")"
                    : category.name();
        }

        @Override
        public Category fromString(String value) {
            throw new UnsupportedOperationException("Categories are selected from the list.");
        }

        /** Returns whether a distinct category shares the display name. */
        private boolean hasDuplicateName(Category category) {
            return categories.stream().anyMatch(candidate -> !candidate.id().equals(category.id())
                    && candidate.name().equals(category.name()));
        }

        private static String displayType(TransactionType type) {
            return type == TransactionType.INCOME ? "Income" : "Expense";
        }
    }

    private static final class YearMonthConverter extends StringConverter<YearMonth> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM uuuu");

        @Override
        public String toString(YearMonth month) {
            return month == null ? "" : FORMATTER.format(month);
        }

        @Override
        public YearMonth fromString(String value) {
            throw new UnsupportedOperationException("Months are selected from the list.");
        }
    }
}
