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
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
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
        Button manage = new Button("Manage");
        manage.setAccessibleText("Manage " + category.name());
        manage.setOnAction(event -> manageCategory(category));
        row.getChildren().add(manage);
        return row;
    }

    /** Reveals only the management actions that are currently valid for the category. */
    private void manageCategory(Category category) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage category");
        dialog.setHeaderText(category.name() + " · " + displayType(category.type()));
        if (category.permanentFallback()) {
            dialog.setContentText("Uncategorised is a permanent fallback category and cannot be changed.");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
            return;
        }

        int transactionCount = transactionsUsing(category);
        dialog.setContentText(transactionCount == 0
                ? "This category is unused and can be deleted."
                : "This category is used by " + transactionCount + " "
                + (transactionCount == 1 ? "transaction" : "transactions")
                + ". Reassign them before deleting.");
        ButtonType rename = new ButtonType("Rename");
        ButtonType lifecycle = new ButtonType(showingArchived ? "Restore" : "Archive");
        ButtonType reassign = new ButtonType("Reassign");
        ButtonType delete = new ButtonType("Delete");
        dialog.getDialogPane().getButtonTypes().addAll(rename, lifecycle);
        if (transactionCount > 0) {
            dialog.getDialogPane().getButtonTypes().add(reassign);
        }
        dialog.getDialogPane().getButtonTypes().addAll(delete, ButtonType.CANCEL);
        if (transactionCount > 0) {
            ((Button) dialog.getDialogPane().lookupButton(delete)).setDisable(true);
        }
        dialog.showAndWait().ifPresent(action -> handleManagementAction(
                action, rename, lifecycle, reassign, delete, category));
    }

    /** Dispatches the category management action after its dialog has closed. */
    private void handleManagementAction(ButtonType action, ButtonType rename, ButtonType lifecycle,
                                        ButtonType reassign, ButtonType delete, Category category) {
        if (action == rename) {
            renameCategory(category);
        } else if (action == lifecycle) {
            if (showingArchived) {
                restoreCategory(category);
            } else {
                archiveCategory(category);
            }
        } else if (action == reassign) {
            reassignCategory(category);
        } else if (action == delete) {
            deleteCategory(category);
        }
    }

    /** Counts transactions assigned to the category for the state-aware management dialog. */
    private int transactionsUsing(Category category) {
        return (int) service.transactions().stream()
                .filter(transaction -> transaction.category().id().equals(category.id()))
                .count();
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

    /** Moves every transaction to a compatible active category after explicit confirmation. */
    private void reassignCategory(Category source) {
        List<Category> targets = service.categoriesFor(source.type()).stream()
                .filter(category -> !category.id().equals(source.id()))
                .toList();
        if (targets.isEmpty()) {
            showValidation("No compatible category is available for reassignment.");
            return;
        }
        ChoiceDialog<Category> selection = new ChoiceDialog<>(targets.getFirst(), targets);
        selection.setTitle("Reassign transactions");
        selection.setHeaderText("Reassign transactions from " + source.name());
        selection.setContentText("Move all transactions to:");
        selection.showAndWait().ifPresent(target -> confirmReassignment(source, target));
    }

    /** Confirms and performs the irreversible transaction-category reassignment. */
    private void confirmReassignment(Category source, Category target) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm reassignment");
        confirmation.setHeaderText("Move transactions from " + source.name() + " to " + target.name() + "?");
        confirmation.setContentText("This changes the category shown for every transaction currently using "
                + source.name() + ".");
        confirmation.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
            try {
                int reassigned = service.reassignTransactions(source.id(), target.id());
                showValidation(reassigned + " " + (reassigned == 1 ? "transaction was" : "transactions were")
                        + " reassigned to " + target.name() + ".");
            } catch (RuntimeException exception) {
                showValidation(messageFor(exception));
            }
        });
    }

    /** Confirms and permanently deletes an unused ordinary category. */
    private void deleteCategory(Category category) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete category");
        confirmation.setHeaderText("Permanently delete " + category.name() + "?");
        confirmation.setContentText("This cannot be undone. Categories used by transactions must be reassigned first.");
        confirmation.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
            try {
                service.deleteCategory(category.id());
                renderCategories();
                showValidation(category.name() + " was deleted.");
            } catch (RuntimeException exception) {
                showValidation(messageFor(exception));
            }
        });
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
