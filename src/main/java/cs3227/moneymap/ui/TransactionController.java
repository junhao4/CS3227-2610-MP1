package cs3227.moneymap.ui;

import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.Transaction;
import cs3227.moneymap.domain.TransactionType;
import cs3227.moneymap.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private VBox transactionHistoryHeader;
    @FXML
    private VBox transactionLedger;
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
    @FXML
    private Label transactionFormTitle;
    @FXML
    private Button saveTransactionButton;
    @FXML
    private VBox transactionFilterPanel;
    @FXML
    private Button transactionFilterToggleButton;
    private Transaction selectedTransaction;

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
        setFilterPanelVisible(false);
        updateNoteCount("");
        renderTransactions();
    }

    @FXML
    private void saveTransaction() {
        clearValidation();
        try {
            Category selectedCategory = transactionCategoryComboBox.getValue();
            if (selectedTransaction == null) {
                service.createTransaction(transactionTypeComboBox.getValue(), transactionAmountField.getText(),
                        transactionDatePicker.getValue(), selectedCategory == null ? null : selectedCategory.id(),
                        transactionNoteArea.getText());
            } else {
                service.updateTransaction(selectedTransaction.id(), transactionTypeComboBox.getValue(),
                        transactionAmountField.getText(), transactionDatePicker.getValue(),
                        selectedCategory == null ? null : selectedCategory.id(), transactionNoteArea.getText());
            }
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
        selectedTransaction = null;
        transactionFormTitle.setText("Add transaction");
        saveTransactionButton.setText("Save transaction");
        clearCompletedFormFields();
        clearValidation();
        setFormVisible(true);
        transactionsScrollPane.setVvalue(1.0);
        transactionTypeComboBox.requestFocus();
    }

    @FXML
    private void cancelTransaction() {
        selectedTransaction = null;
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

        transactionMonthFilter.valueProperty().addListener((observable, previous, selected) -> refreshHistoryView());
        transactionTypeFilter.valueProperty().addListener((observable, previous, selected) -> refreshHistoryView());
        transactionCategoryFilter.valueProperty().addListener((observable, previous, selected) -> refreshHistoryView());
        transactionNoteSearchField.textProperty().addListener((observable, previous, text) -> refreshHistoryView());
    }

    /** Returns all categories in a predictable order for the history filter. */
    private List<Category> allCategories() {
        return List.of(TransactionType.values()).stream()
                .flatMap(type -> service.allCategories().stream()
                        .filter(category -> category.type() == type))
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

    /** Shows or hides the less-frequent history filters while keeping search immediately available. */
    @FXML
    private void toggleTransactionFilters() {
        setFilterPanelVisible(!transactionFilterPanel.isVisible());
    }

    /** Refreshes the history and the active-filter cue after a query control changes. */
    private void refreshHistoryView() {
        renderTransactions();
        updateFilterToggleLabel();
    }

    /** Applies progressive disclosure to the filter controls. */
    private void setFilterPanelVisible(boolean visible) {
        transactionFilterPanel.setManaged(visible);
        transactionFilterPanel.setVisible(visible);
        updateFilterToggleLabel();
    }

    /** Shows whether any of the filters beyond the visible search field are active. */
    private void updateFilterToggleLabel() {
        int activeFilters = (transactionMonthFilter.getValue() == null ? 0 : 1)
                + (transactionTypeFilter.getValue() == null ? 0 : 1)
                + (transactionCategoryFilter.getValue() == null ? 0 : 1);
        transactionFilterToggleButton.setText(activeFilters == 0 ? "Filter & sort" : "Filter & sort · "
                + activeFilters + " active");
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
        setFormVisible(false);
        transactionsScrollPane.setVvalue(0.0);
        addTransactionButton.requestFocus();
    }

    /** Switches between the focused add/edit view and the transaction ledger without changing its query. */
    private void setFormVisible(boolean visible) {
        transactionForm.setManaged(visible);
        transactionForm.setVisible(visible);
        transactionHistoryHeader.setManaged(!visible);
        transactionHistoryHeader.setVisible(!visible);
        transactionLedger.setManaged(!visible);
        transactionLedger.setVisible(!visible);
    }

    /** Renders transactions that match the current history-filter controls. */
    private void renderTransactions() {
        transactionRows.getChildren().clear();
        Category selectedCategory = transactionCategoryFilter.getValue();
        List<Transaction> results = service.findTransactions(
                transactionMonthFilter.getValue(), transactionTypeFilter.getValue(),
                selectedCategory == null ? null : selectedCategory.id(), transactionNoteSearchField.getText());
        LocalDate previousDate = null;
        VBox dailyGroup = null;
        for (Transaction transaction : results) {
            if (!transaction.date().equals(previousDate)) {
                Label dateHeading = new Label(displayDate(transaction));
                dateHeading.getStyleClass().add("ledger-date-heading");
                transactionRows.getChildren().add(dateHeading);
                dailyGroup = new VBox();
                dailyGroup.getStyleClass().add("daily-ledger-group");
                transactionRows.getChildren().add(dailyGroup);
                previousDate = transaction.date();
            }
            dailyGroup.getChildren().add(createTransactionRow(transaction));
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
        Label note = new Label(transaction.note().isBlank() ? "No note" : transaction.note());
        note.getStyleClass().add(transaction.note().isBlank() ? "muted" : "row-label");
        note.setWrapText(true);
        note.setMaxWidth(Double.MAX_VALUE);
        Label metadata = new Label(transaction.category().name() + " · " + displayType(transaction.type()));
        metadata.getStyleClass().add("muted");
        VBox description = new VBox(3, note, metadata);
        HBox.setHgrow(description, Priority.ALWAYS);
        description.setMaxWidth(Double.MAX_VALUE);

        Label amount = new Label((transaction.type() == TransactionType.INCOME ? "+" : "−")
                + SgdFormatter.format(transaction.amount()));
        amount.setMinWidth(130);
        amount.getStyleClass().add(transaction.type() == TransactionType.INCOME
                ? "amount-income" : "amount-expense");
        MenuItem edit = new MenuItem("Edit transaction");
        edit.setOnAction(event -> editTransaction(transaction));
        MenuItem delete = new MenuItem("Delete transaction");
        delete.setOnAction(event -> confirmDeleteTransaction(transaction));
        MenuButton moreActions = new MenuButton("⋯");
        moreActions.getItems().addAll(edit, delete);
        moreActions.getStyleClass().add("more-action");
        moreActions.setAccessibleText("More actions for transaction dated " + displayDate(transaction));
        HBox row = new HBox(16, description, amount, moreActions);
        row.getStyleClass().add("ledger-transaction-row");
        return row;
    }

    /** Opens the standard transaction form with the persisted values selected for correction. */
    private void editTransaction(Transaction transaction) {
        selectedTransaction = transaction;
        transactionFormTitle.setText("Edit transaction");
        saveTransactionButton.setText("Save changes");
        transactionTypeComboBox.setValue(transaction.type());
        transactionAmountField.setText(transaction.amount().value().toPlainString());
        transactionDatePicker.setValue(transaction.date());
        if (transaction.category().archived()) {
            List<Category> categories = new ArrayList<>(service.categoriesFor(transaction.type()));
            categories.add(transaction.category());
            transactionCategoryComboBox.setItems(FXCollections.observableArrayList(categories));
        }
        transactionCategoryComboBox.setValue(transaction.category());
        transactionNoteArea.setText(transaction.note());
        clearValidation();
        setFormVisible(true);
        transactionsScrollPane.setVvalue(1.0);
        transactionTypeComboBox.requestFocus();
    }

    /** Requests explicit confirmation before permanently removing a transaction. */
    private void confirmDeleteTransaction(Transaction transaction) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete transaction?");
        confirmation.setHeaderText("Permanently delete this transaction?");
        confirmation.setContentText(transaction.date() + " — " + SgdFormatter.format(transaction.amount())
                + " in " + transaction.category().name() + ". This cannot be undone.");
        DialogStyler.applyDanger(confirmation);
        confirmation.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
            try {
                service.deleteTransaction(transaction.id());
                if (selectedTransaction != null && selectedTransaction.id().equals(transaction.id())) {
                    cancelTransaction();
                }
                refreshMonthFilter();
                renderTransactions();
            } catch (RuntimeException exception) {
                showValidation(messageFor(exception));
            }
        });
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

    /** Formats transaction dates for quick ledger scanning without hiding the year. */
    private static String displayDate(Transaction transaction) {
        return transaction.date().format(DateTimeFormatter.ofPattern("d MMM uuuu"));
    }

    private static String displayType(TransactionType type) {
        return type == TransactionType.INCOME ? "Income" : "Expense";
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
