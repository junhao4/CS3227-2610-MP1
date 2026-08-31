package cs3227.moneymap;

import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.TransactionType;
import cs3227.moneymap.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.Objects;

/** Connects the Categories view to custom category creation and persistence. */
public final class CategoryController {
    private final TransactionService service;

    @FXML
    private ComboBox<TransactionType> categoryTypeComboBox;
    @FXML
    private TextField categoryNameField;
    @FXML
    private Label categoryValidationLabel;
    @FXML
    private VBox categoryRows;

    CategoryController(TransactionService service) {
        this.service = Objects.requireNonNull(service);
    }

    @FXML
    private void initialize() {
        categoryTypeComboBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        categoryTypeComboBox.setConverter(new TransactionTypeConverter());
        categoryTypeComboBox.setValue(TransactionType.EXPENSE);
        renderCategories();
    }

    @FXML
    private void createCategory() {
        categoryValidationLabel.setManaged(false);
        categoryValidationLabel.setVisible(false);
        try {
            service.createCategory(categoryTypeComboBox.getValue(), categoryNameField.getText());
            categoryNameField.clear();
            renderCategories();
            categoryNameField.requestFocus();
        } catch (RuntimeException exception) {
            categoryValidationLabel.setText(messageFor(exception));
            categoryValidationLabel.setManaged(true);
            categoryValidationLabel.setVisible(true);
            categoryNameField.requestFocus();
        }
    }

    /** Displays all current categories grouped by type and sorted by name. */
    private void renderCategories() {
        categoryRows.getChildren().clear();
        for (Category category : service.categoriesFor(TransactionType.EXPENSE).stream()
                .sorted(Comparator.comparing(Category::name, String.CASE_INSENSITIVE_ORDER)).toList()) {
            categoryRows.getChildren().add(categoryLabel(category));
        }
        for (Category category : service.categoriesFor(TransactionType.INCOME).stream()
                .sorted(Comparator.comparing(Category::name, String.CASE_INSENSITIVE_ORDER)).toList()) {
            categoryRows.getChildren().add(categoryLabel(category));
        }
    }

    private static Label categoryLabel(Category category) {
        Label label = new Label(category.name() + " · " + displayType(category.type()));
        label.getStyleClass().add("category-row");
        return label;
    }

    private static String messageFor(RuntimeException exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Check the category details and try again." : exception.getMessage();
    }

    private static String displayType(TransactionType type) {
        return type == TransactionType.INCOME ? "Income" : "Expense";
    }

    private static final class TransactionTypeConverter extends javafx.util.StringConverter<TransactionType> {
        @Override
        public String toString(TransactionType type) {
            return type == null ? "" : displayType(type);
        }

        @Override
        public TransactionType fromString(String value) {
            throw new UnsupportedOperationException("Transaction type is selected from the list.");
        }
    }
}
