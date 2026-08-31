package cs3227.moneymap;

import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.TransactionType;
import cs3227.moneymap.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
    private VBox categoryCreationPanel;
    @FXML
    private Label categoryListTitle;
    @FXML
    private VBox categoryRows;
    @FXML
    private Button activeCategoriesButton;
    @FXML
    private Button archivedCategoriesButton;
    private boolean showingArchived;

    CategoryController(TransactionService service) {
        this.service = Objects.requireNonNull(service);
    }

    @FXML
    private void initialize() {
        categoryTypeComboBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        categoryTypeComboBox.setConverter(new TransactionTypeConverter());
        categoryTypeComboBox.setValue(TransactionType.EXPENSE);
        showActiveCategories();
    }

    /** Shows active categories and the form for creating a new category. */
    @FXML
    private void showActiveCategories() {
        showingArchived = false;
        updateCategoryView();
    }

    /** Shows archived categories and their restore actions. */
    @FXML
    private void showArchivedCategories() {
        showingArchived = true;
        updateCategoryView();
    }

    /** Updates controls and rows for the selected active or archived category view. */
    private void updateCategoryView() {
        categoryCreationPanel.setManaged(!showingArchived);
        categoryCreationPanel.setVisible(!showingArchived);
        categoryListTitle.setText(showingArchived ? "Archived categories" : "Current categories");
        activeCategoriesButton.setDisable(!showingArchived);
        archivedCategoriesButton.setDisable(showingArchived);
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

    /** Displays categories in the selected lifecycle state, grouped by type and sorted by name. */
    private void renderCategories() {
        categoryRows.getChildren().clear();
        for (Category category : service.allCategories().stream()
                .filter(category -> category.type() == TransactionType.EXPENSE
                        && category.archived() == showingArchived)
                .sorted(Comparator.comparing(Category::name, String.CASE_INSENSITIVE_ORDER)).toList()) {
            categoryRows.getChildren().add(categoryLabel(category));
        }
        for (Category category : service.allCategories().stream()
                .filter(category -> category.type() == TransactionType.INCOME
                        && category.archived() == showingArchived)
                .sorted(Comparator.comparing(Category::name, String.CASE_INSENSITIVE_ORDER)).toList()) {
            categoryRows.getChildren().add(categoryLabel(category));
        }
    }

    private HBox categoryLabel(Category category) {
        Label label = new Label(category.name() + " · " + displayType(category.type()));
        label.getStyleClass().add("category-row");
        HBox row = new HBox(8, label);
        Button rename = new Button("Rename");
        rename.setAccessibleText("Rename " + category.name());
        rename.setOnAction(event -> renameCategory(category));
        Button lifecycleAction = new Button(showingArchived ? "Restore" : "Archive");
        lifecycleAction.setId(showingArchived ? "restoreCategoryButton" : "archiveCategoryButton");
        lifecycleAction.setAccessibleText((showingArchived ? "Restore " : "Archive ") + category.name());
        lifecycleAction.setDisable(category.permanentFallback());
        lifecycleAction.setOnAction(event -> {
            if (showingArchived) {
                restoreCategory(category);
            } else {
                archiveCategory(category);
            }
        });
        row.getChildren().addAll(rename, lifecycleAction);
        return row;
    }

    private void renameCategory(Category category) {
        TextInputDialog dialog = new TextInputDialog(category.name());
        dialog.setTitle("Rename category");
        dialog.setHeaderText("Rename " + category.name());
        dialog.setContentText("New name:");
        dialog.showAndWait().ifPresent(name -> {
            try {
                service.renameCategory(category.id(), name);
                renderCategories();
            } catch (RuntimeException exception) {
                showValidation(messageFor(exception));
            }
        });
    }

    private void archiveCategory(Category category) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Archive category");
        confirmation.setHeaderText("Archive " + category.name() + "?");
        confirmation.setContentText("It will remain visible in history but cannot be used for new transactions.");
        confirmation.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
            try {
                service.archiveCategory(category.id());
                renderCategories();
            } catch (RuntimeException exception) {
                showValidation(messageFor(exception));
            }
        });
    }

    private void restoreCategory(Category category) {
        try {
            service.restoreCategory(category.id());
            renderCategories();
        } catch (RuntimeException exception) {
            showValidation(messageFor(exception));
        }
    }

    private void showValidation(String message) {
        categoryValidationLabel.setText(message);
        categoryValidationLabel.setManaged(true);
        categoryValidationLabel.setVisible(true);
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
