package cs3227.moneymap;

import cs3227.moneymap.domain.Category;
import cs3227.moneymap.domain.Budget;
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
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.time.YearMonth;

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
    private Button newCategoryButton;
    private Dialog<Void> categoryCreationDialog;
    @FXML
    private VBox categoryLandingView;
    @FXML
    private VBox budgetManagerView;
    @FXML
    private VBox budgetDetailPanel;
    @FXML
    private VBox budgetEditorPanel;
    @FXML
    private Label budgetEditorTitle;
    @FXML
    private TextField budgetAmountField;
    @FXML
    private Label budgetValidationLabel;
    @FXML
    private VBox budgetRows;
    @FXML
    private VBox budgetListPanel;
    @FXML
    private Label budgetListTitle;
    @FXML
    private Label budgetDetailMonthLabel;
    @FXML
    private Label budgetAppliedSummaryLabel;
    @FXML
    private Label recurringBudgetValueLabel;
    @FXML
    private Label monthBudgetLabel;
    @FXML
    private Label monthBudgetValueLabel;
    @FXML
    private Button changeRecurringBudgetButton;
    @FXML
    private Button changeMonthBudgetButton;
    @FXML
    private Button removeRecurringBudgetButton;
    @FXML
    private Button removeMonthBudgetButton;
    @FXML
    private Label budgetEditPromptLabel;
    @FXML
    private Label categoryListTitle;
    @FXML
    private FlowPane categoryRows;
    @FXML
    private Label incomeListTitle;
    @FXML
    private FlowPane incomeCategoryRows;
    @FXML
    private Button activeCategoriesButton;
    @FXML
    private Button archivedCategoriesButton;
    private boolean showingArchived;
    private Category selectedBudgetCategory;
    private boolean editingRecurringBudget;
    private YearMonth budgetEditorMonth;
    @FXML
    private Button previousBudgetMonthButton;
    @FXML
    private Button nextBudgetMonthButton;
    @FXML
    private Label budgetMonthValueLabel;

    CategoryController(TransactionService service) {
        this.service = Objects.requireNonNull(service);
    }

    @FXML
    private void initialize() {
        categoryTypeComboBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        categoryTypeComboBox.setConverter(new TransactionTypeConverter());
        categoryTypeComboBox.setValue(TransactionType.EXPENSE);
        budgetEditorMonth = YearMonth.from(service.defaultDate());
        updateBudgetMonthLabel();
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

    /** Reveals the category form only after the user requests a new category. */
    @FXML
    private void showCategoryCreation() {
        if (categoryCreationDialog != null) {
            if (!categoryCreationDialog.isShowing()) {
                hideCategoryCreation();
            } else {
                return;
            }
        }
        if (categoryCreationDialog != null) {
            return;
        }
        categoryLandingView.getChildren().remove(categoryCreationPanel);
        categoryCreationPanel.setManaged(true);
        categoryCreationPanel.setVisible(true);
        categoryCreationDialog = new Dialog<>();
        categoryCreationDialog.setTitle("New category");
        categoryCreationDialog.setHeaderText("Create an income or expense category");
        categoryCreationDialog.getDialogPane().setContent(categoryCreationPanel);
        categoryCreationDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        DialogStyler.apply(categoryCreationDialog);
        categoryCreationDialog.setOnCloseRequest(event -> hideCategoryCreation());
        categoryCreationDialog.setOnHidden(event -> {
            categoryCreationDialog = null;
            restoreCategoryCreationPanel();
        });
        categoryCreationDialog.show();
        categoryNameField.requestFocus();
    }

    /** Closes the modal category form without changing saved state. */
    @FXML
    private void hideCategoryCreation() {
        if (categoryCreationDialog != null) {
            Dialog<Void> dialog = categoryCreationDialog;
            categoryCreationDialog = null;
            dialog.setOnHidden(null);
            dialog.getDialogPane().setContent(null);
            dialog.hide();
            restoreCategoryCreationPanel();
        }
    }

    /** Restores the detached form to its hidden FXML location after the dialog closes. */
    private void restoreCategoryCreationPanel() {
        if (!categoryLandingView.getChildren().contains(categoryCreationPanel)) {
            categoryLandingView.getChildren().add(2, categoryCreationPanel);
        }
        categoryCreationPanel.setManaged(false);
        categoryCreationPanel.setVisible(false);
    }

    /** Updates controls and rows for the selected active or archived category view. */
    private void updateCategoryView() {
        showCategories();
        categoryCreationPanel.setManaged(false);
        categoryCreationPanel.setVisible(false);
        newCategoryButton.setManaged(!showingArchived);
        newCategoryButton.setVisible(!showingArchived);
        categoryListTitle.setText(showingArchived ? "Archived expense categories" : "Expense categories");
        incomeListTitle.setText(showingArchived ? "Archived income categories" : "Income categories");
        activeCategoriesButton.setDisable(!showingArchived);
        archivedCategoriesButton.setDisable(showingArchived);
        renderCategories();
    }

    /** Reveals focused budget management without competing with category controls. */
    @FXML
    private void showBudgetManager() {
        categoryLandingView.setManaged(false);
        categoryLandingView.setVisible(false);
        budgetManagerView.setManaged(true);
        budgetManagerView.setVisible(true);
        budgetListPanel.setManaged(true);
        budgetListPanel.setVisible(true);
        selectedBudgetCategory = null;
        budgetEditorMonth = YearMonth.from(service.defaultDate());
        updateBudgetMonthLabel();
        renderBudgets();
    }

    /** Opens the budget editor for the expense category whose Manage action was selected. */
    private void showFocusedBudgetManager(Category category) {
        categoryLandingView.setManaged(false);
        categoryLandingView.setVisible(false);
        budgetManagerView.setManaged(true);
        budgetManagerView.setVisible(true);
        budgetListPanel.setManaged(false);
        budgetListPanel.setVisible(false);
        selectedBudgetCategory = category;
        budgetEditorMonth = YearMonth.from(service.defaultDate());
        updateBudgetMonthLabel();
        showBudgetEditor(category);
    }

    /** Returns to the category landing view after closing the contextual editor. */
    @FXML
    private void showCategories() {
        categoryLandingView.setManaged(true);
        categoryLandingView.setVisible(true);
        budgetManagerView.setManaged(false);
        budgetManagerView.setVisible(false);
        budgetListPanel.setManaged(true);
        budgetListPanel.setVisible(true);
        hideBudgetEditor();
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
            hideCategoryCreation();
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
        incomeCategoryRows.getChildren().clear();
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
            incomeCategoryRows.getChildren().add(categoryLabel(category));
        }
    }

    /** Refreshes effective budget rows for the month explicitly chosen for viewing. */
    private void renderBudgets() {
        if (budgetRows == null || budgetEditorMonth == null) {
            return;
        }
        budgetRows.getChildren().clear();
        YearMonth month = budgetEditorMonth;
        budgetListTitle.setText("Budgets for " + displayMonth(month));
        for (Category category : service.categoriesFor(TransactionType.EXPENSE).stream()
                .sorted(Comparator.comparing(Category::name, String.CASE_INSENSITIVE_ORDER)).toList()) {
            Budget budget = service.budgetFor(category.id(), month).orElse(null);
            Budget recurring = service.recurringBudgetFor(category.id(), month).orElse(null);
            Label name = new Label(category.name());
            name.getStyleClass().add("section-title");
            Label spent = new Label("Spent in " + displayMonth(month) + ": "
                    + cardMoney(service.spendingFor(category.id(), month)));
            constrainCardLabel(spent);
            spent.getStyleClass().add("metric-value");
            Label amount = new Label(budget == null ? "No budget set for " + displayMonth(month)
                    : "Applied budget for " + displayMonth(month) + ": " + cardMoney(budget.amount()));
            constrainCardLabel(amount);
            amount.getStyleClass().add(budget == null ? "muted" : "row-label");
            Button manage = new Button(budget == null ? "Set budget" : "Manage");
            manage.getStyleClass().add("text-button");
            manage.setOnAction(event -> showBudgetEditor(category));
            HBox row = new HBox(12, name);
            Label spacer = new Label();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            row.getChildren().addAll(spacer, manage);
            row.getStyleClass().add("budget-table-row");
            Label status = new Label(budgetStatus(category, month, budget));
            constrainCardLabel(status);
            status.getStyleClass().add(budget == null ? "muted" : service.isOverBudget(category.id(), month)
                    ? "danger" : "positive");
            ProgressBar progress = new ProgressBar(budget == null || budget.amount().value().signum() == 0
                    ? 0 : service.percentageUsed(category.id(), month).orElse(java.math.BigDecimal.ZERO)
                    .divide(java.math.BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP).doubleValue());
            progress.setMaxWidth(Double.MAX_VALUE);
            progress.getStyleClass().add(progressStyle(category.id(), month));
            VBox budgetRow = new VBox(8, row, spent, amount, status, progress);
            budgetRow.getStyleClass().add("category-card");
            if (budget != null && !budget.repeatsMonthly() && recurring != null) {
                Label monthlyDefault = new Label("Monthly budget: " + SgdFormatter.format(recurring.amount())
                        + " every month");
                constrainCardLabel(monthlyDefault);
                monthlyDefault.getStyleClass().add("muted");
                budgetRow.getChildren().add(monthlyDefault);
            }
            budgetRows.getChildren().add(budgetRow);
        }
    }

    /** Reveals budget fields for one category only after the user chooses to manage it. */
    private void showBudgetEditor(Category category) {
        selectedBudgetCategory = category;
        YearMonth month = selectedBudgetMonth();
        Budget recurring = service.recurringBudgetFor(category.id(), month).orElse(null);
        Budget monthOnly = service.monthOnlyBudgetFor(category.id(), month).orElse(null);
        budgetEditorTitle.setText("Manage " + category.name() + " budget");
        budgetDetailMonthLabel.setText("For " + displayMonth(month));
        budgetAppliedSummaryLabel.setText(appliedBudgetSummary(month, recurring, monthOnly));
        recurringBudgetValueLabel.setText(recurring == null ? "No every-month budget" :
                SgdFormatter.format(recurring.amount()) + " every month");
        changeRecurringBudgetButton.setText(recurring == null ? "Set" : "Change");
        setBudgetActionVisibility(removeRecurringBudgetButton,
                recurring != null || service.hasRecurringBudgetFrom(category.id(), month));
        monthBudgetLabel.setText("One-time override for " + displayMonth(month));
        monthBudgetValueLabel.setText(monthOnly == null ? "No one-time budget" :
                SgdFormatter.format(monthOnly.amount()) + " this month only");
        changeMonthBudgetButton.setText(monthOnly == null ? "Set" : "Change");
        setBudgetActionVisibility(removeMonthBudgetButton, monthOnly != null);
        budgetDetailPanel.setManaged(true);
        budgetDetailPanel.setVisible(true);
        hideBudgetForm();
    }

    /** Hides the contextual budget editor. */
    @FXML
    private void hideBudgetEditor() {
        selectedBudgetCategory = null;
        if (budgetDetailPanel != null) {
            budgetDetailPanel.setManaged(false);
            budgetDetailPanel.setVisible(false);
        }
        hideBudgetForm();
    }

    /** Moves the budget view to the previous calendar month. */
    @FXML
    private void showPreviousBudgetMonth() {
        budgetEditorMonth = selectedBudgetMonth().minusMonths(1);
        updateBudgetMonthLabel();
        if (selectedBudgetCategory == null) {
            renderBudgets();
        } else {
            showBudgetEditor(selectedBudgetCategory);
        }
    }

    /** Moves the budget view to the next calendar month. */
    @FXML
    private void showNextBudgetMonth() {
        budgetEditorMonth = selectedBudgetMonth().plusMonths(1);
        updateBudgetMonthLabel();
        if (selectedBudgetCategory == null) {
            renderBudgets();
        } else {
            showBudgetEditor(selectedBudgetCategory);
        }
    }

    /** Updates the visible month label after a month navigation action. */
    private void updateBudgetMonthLabel() {
        budgetMonthValueLabel.setText(displayMonth(budgetEditorMonth));
    }

    /** Reveals a single amount field for the every-month value. */
    @FXML
    private void editRecurringBudget() {
        showBudgetForm(true);
    }

    /** Reveals a single amount field for the month selected above. */
    @FXML
    private void editMonthBudget() {
        showBudgetForm(false);
    }

    /** Removes the selected category's every-month budget and refreshes its summary. */
    @FXML
    private void removeRecurringBudget() {
        clearSelectedBudget(true);
    }

    /** Removes the selected category's one-month override and refreshes its summary. */
    @FXML
    private void removeMonthBudget() {
        clearSelectedBudget(false);
    }

    /** Removes one budget scope while keeping the other scope, if configured. */
    private void clearSelectedBudget(boolean recurring) {
        try {
            Category category = Objects.requireNonNull(selectedBudgetCategory, "Select a budget category.");
            YearMonth month = selectedBudgetMonth();
            if (recurring) {
                service.clearRecurringBudget(category.id(), month);
            } else {
                service.clearBudgetOverride(category.id(), month);
            }
            renderBudgets();
            showBudgetEditor(category);
            showBudgetValidation(recurring ? "Recurring monthly budget was removed."
                    : "Budget for " + month + " was removed.");
        } catch (RuntimeException exception) {
            showBudgetValidation(messageFor(exception));
        }
    }

    /** Keeps a remove action out of the tab order when its budget scope is absent. */
    private static void setBudgetActionVisibility(Button button, boolean visible) {
        button.setManaged(visible);
        button.setVisible(visible);
    }

    /** Shows only the currently requested budget input. */
    private void showBudgetForm(boolean recurring) {
        editingRecurringBudget = recurring;
        Budget existing = recurring ? service.recurringBudgetFor(selectedBudgetCategory.id(), selectedBudgetMonth())
                .orElse(null)
                : service.monthOnlyBudgetFor(selectedBudgetCategory.id(), selectedBudgetMonth()).orElse(null);
        budgetEditPromptLabel.setText(recurring ? "Every-month amount" :
                "Amount for " + displayMonth(selectedBudgetMonth()) + " only");
        budgetAmountField.setText(existing == null ? "" : existing.amount().toString());
        budgetValidationLabel.setManaged(false);
        budgetValidationLabel.setVisible(false);
        budgetEditorPanel.setManaged(true);
        budgetEditorPanel.setVisible(true);
        budgetAmountField.requestFocus();
    }

    /** Hides the amount field while retaining the selected category's budget summary. */
    @FXML
    private void hideBudgetForm() {
        if (budgetEditorPanel != null) {
            budgetEditorPanel.setManaged(false);
            budgetEditorPanel.setVisible(false);
        }
    }

    /** Validates and persists one expense category's budget for the selected calendar month. */
    @FXML
    private void setBudget() {
        budgetValidationLabel.setManaged(false);
        budgetValidationLabel.setVisible(false);
        try {
            Category category = Objects.requireNonNull(selectedBudgetCategory, "Select a budget category.");
            Budget budget = editingRecurringBudget
                    ? service.setRecurringBudget(category.id(), selectedBudgetMonth(), budgetAmountField.getText())
                    : service.setBudgetOverride(category.id(), selectedBudgetMonth(), budgetAmountField.getText());
            budgetAmountField.clear();
            renderBudgets();
            showBudgetEditor(category);
            showBudgetValidation(budget.repeatsMonthly()
                    ? "Recurring monthly budget was saved."
                    : "Budget for " + budget.month() + " was saved.");
        } catch (RuntimeException exception) {
            showBudgetValidation(messageFor(exception));
        }
    }

    /** Returns the calendar month currently selected for the focused budget view. */
    private YearMonth selectedBudgetMonth() {
        return Objects.requireNonNull(budgetEditorMonth, "Select a month to view.");
    }

    /** Formats a calendar month for a clear user-facing heading. */
    private static String displayMonth(YearMonth month) {
        return month.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
                + " " + month.getYear();
    }

    /** Explains which configured value actually applies to the selected month. */
    private String appliedBudgetSummary(YearMonth month, Budget recurring, Budget monthOnly) {
        if (monthOnly != null) {
            return "Applied budget for " + displayMonth(month) + ": " + SgdFormatter.format(monthOnly.amount())
                    + " (one-time override)";
        }
        if (recurring != null) {
            return "Applied budget for " + displayMonth(month) + ": " + SgdFormatter.format(recurring.amount())
                    + " (recurring every month)";
        }
        return "Applied budget for " + displayMonth(month) + ": none";
    }

    /** Shows the amount left, or the amount over budget, for the selected month. */
    private String budgetStatus(Category category, YearMonth month, Budget budget) {
        if (budget == null) {
            return "Set a budget to see what is left";
        }
        java.math.BigDecimal difference = budget.amount().value()
                .subtract(service.spendingFor(category.id(), month).value());
        if (difference.signum() < 0) {
            return SgdFormatter.format(new cs3227.moneymap.domain.MoneyAmount(difference.abs())) + " over budget";
        }
        return SgdFormatter.format(new cs3227.moneymap.domain.MoneyAmount(difference)) + " left";
    }

    private VBox categoryLabel(Category category) {
        Label label = new Label(category.name());
        label.getStyleClass().add("section-title");
        VBox card = new VBox(10, label);
        boolean expense = category.type() == TransactionType.EXPENSE;
        card.setPrefWidth(250);
        card.setMinWidth(250);
        card.setMaxWidth(250);
        double cardHeight = expense ? 250 : 150;
        card.setMinHeight(cardHeight);
        card.setPrefHeight(cardHeight);
        card.setMaxHeight(cardHeight);
        card.getStyleClass().addAll("category-card", "category-summary-card",
                expense ? "expense-summary-card" : "income-summary-card");
        if (expense) {
            addExpenseSummary(card, category, YearMonth.from(service.defaultDate()));
        } else {
            Label description = new Label("Income category");
            description.getStyleClass().add("muted");
            card.getChildren().add(description);
        }
        Button manage = new Button("Manage");
        manage.getStyleClass().addAll("text-button", "manage-action");
        manage.setAccessibleText("Manage " + category.name());
        manage.setOnAction(event -> manageCategory(category));
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        card.getChildren().addAll(spacer, manage);
        return card;
    }

    /** Adds the compact prototype-style spending, budget, status, and progress content. */
    private void addExpenseSummary(VBox card, Category category, YearMonth month) {
        java.math.BigDecimal spent = service.spendingFor(category.id(), month).value();
        Budget budget = service.budgetFor(category.id(), month).orElse(null);
        Label spending = new Label(cardMoney(new cs3227.moneymap.domain.MoneyAmount(spent)) + " spent");
        constrainCardLabel(spending);
        spending.getStyleClass().add("metric-value");
        Label budgetSummary = new Label(budget == null ? "No budget set" : budget.amount().value().signum() == 0
                ? "$0.00 budget" : "of " + cardMoney(budget.amount()) + " budget · "
                + cardMoney(new cs3227.moneymap.domain.MoneyAmount(
                budget.amount().value().subtract(spent).max(java.math.BigDecimal.ZERO))) + " left");
        budgetSummary.setWrapText(true);
        constrainCardLabel(budgetSummary);
        budgetSummary.getStyleClass().add(budget == null ? "muted" : "positive");
        card.getChildren().addAll(spending, budgetSummary);
        if (budget != null && service.isOverBudget(category.id(), month)) {
            Label over = new Label(cardMoney(new cs3227.moneymap.domain.MoneyAmount(
                    spent.subtract(budget.amount().value()))) + " over budget");
            constrainCardLabel(over);
            over.getStyleClass().add("danger");
            card.getChildren().add(over);
        }
        java.math.BigDecimal percentage = budget == null || budget.amount().value().signum() == 0
                ? java.math.BigDecimal.ZERO : service.percentageUsed(category.id(), month)
                .orElse(java.math.BigDecimal.ZERO);
        ProgressBar progress = new ProgressBar(percentage.min(java.math.BigDecimal.valueOf(100))
                .divide(java.math.BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP).doubleValue());
        progress.setMinWidth(210);
        progress.setPrefWidth(210);
        progress.setMaxWidth(210);
        progress.setMinHeight(9);
        progress.setPrefHeight(9);
        progress.setMaxHeight(9);
        progress.getStyleClass().add(progressStyle(category.id(), month));
        card.getChildren().add(progress);
    }

    /** Keeps long monetary summaries inside the fixed-width prototype card. */
    private static void constrainCardLabel(Label label) {
        label.setWrapText(false);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setMaxWidth(Double.MAX_VALUE);
    }

    /** Uses the compact dollar notation reserved for the prototype-style cards. */
    private static String cardMoney(cs3227.moneymap.domain.MoneyAmount amount) {
        String formatted = SgdFormatter.format(amount);
        return formatted.startsWith("S$") ? "$" + formatted.substring(2) : formatted;
    }

    /** Selects the prototype progress colour thresholds for a category month. */
    private String progressStyle(java.util.UUID categoryId, YearMonth month) {
        if (service.isOverBudget(categoryId, month)) {
            return "budget-progress-over";
        }
        java.math.BigDecimal percentage = service.percentageUsed(categoryId, month)
                .orElse(java.math.BigDecimal.ZERO);
        java.math.BigDecimal ratio = percentage.divide(java.math.BigDecimal.valueOf(100), 4,
                java.math.RoundingMode.HALF_UP);
        return BudgetProgress.styleFor(ratio, service.isOverBudget(categoryId, month));
    }

    /** Reveals only the management actions that are currently valid for the category. */
    private void manageCategory(Category category) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage category");
        dialog.setHeaderText(category.name() + " · " + displayType(category.type()));
        DialogStyler.apply(dialog);
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
        ButtonType budgetAction = category.type() == TransactionType.EXPENSE
                ? new ButtonType(service.budgetFor(category.id(), YearMonth.from(service.defaultDate())).isPresent()
                ? "Edit budget" : "Set budget") : null;
        ButtonType reassign = new ButtonType("Reassign");
        ButtonType delete = new ButtonType("Delete");
        dialog.getDialogPane().getButtonTypes().addAll(rename, lifecycle);
        if (budgetAction != null) {
            dialog.getDialogPane().getButtonTypes().add(budgetAction);
        }
        if (transactionCount > 0) {
            dialog.getDialogPane().getButtonTypes().add(reassign);
        }
        dialog.getDialogPane().getButtonTypes().addAll(delete, ButtonType.CANCEL);
        if (transactionCount > 0) {
            ((Button) dialog.getDialogPane().lookupButton(delete)).setDisable(true);
        }
        dialog.showAndWait().ifPresent(action -> handleManagementAction(
                action, rename, lifecycle, budgetAction, reassign, delete, category));
    }

    /** Dispatches the category management action after its dialog has closed. */
    private void handleManagementAction(ButtonType action, ButtonType rename, ButtonType lifecycle,
                                        ButtonType budgetAction, ButtonType reassign, ButtonType delete,
                                        Category category) {
        if (action == rename) {
            renameCategory(category);
        } else if (action == lifecycle) {
            if (showingArchived) {
                restoreCategory(category);
            } else {
                archiveCategory(category);
            }
        } else if (action == budgetAction) {
            showFocusedBudgetManager(category);
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
        DialogStyler.apply(dialog);
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
        DialogStyler.apply(confirmation);
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
        DialogStyler.apply(selection);
        selection.showAndWait().ifPresent(target -> confirmReassignment(source, target));
    }

    /** Confirms and performs the irreversible transaction-category reassignment. */
    private void confirmReassignment(Category source, Category target) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm reassignment");
        confirmation.setHeaderText("Move transactions from " + source.name() + " to " + target.name() + "?");
        confirmation.setContentText("This changes the category shown for every transaction currently using "
                + source.name() + ".");
        DialogStyler.apply(confirmation);
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
        DialogStyler.applyDanger(confirmation);
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

    private void showBudgetValidation(String message) {
        budgetValidationLabel.setText(message);
        budgetValidationLabel.setManaged(true);
        budgetValidationLabel.setVisible(true);
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
