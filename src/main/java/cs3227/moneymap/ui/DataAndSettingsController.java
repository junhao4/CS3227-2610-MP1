package cs3227.moneymap.ui;

import cs3227.moneymap.service.PersistenceException;
import cs3227.moneymap.service.TransactionService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/** Connects the Data and Settings view to local-backup export and import. */
public final class DataAndSettingsController {
    private static final DateTimeFormatter BACKUP_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TransactionService transactionService;

    @FXML
    private Button exportButton;

    @FXML
    private Button importButton;

    @FXML
    private Label exportFeedback;

    @FXML
    private Label importFeedback;
    DataAndSettingsController(TransactionService transactionService) {
        this.transactionService = Objects.requireNonNull(transactionService);
    }

    /** Opens the native save dialog for a complete MoneyMap backup. */
    @FXML
    private void chooseBackupDestination() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export MoneyMap backup");
        chooser.setInitialFileName("moneymap-backup-" + BACKUP_DATE.format(transactionService.defaultDate())
                + ".json");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MoneyMap backup (*.json)", "*.json"));
        File destination = chooser.showSaveDialog(exportButton.getScene().getWindow());
        if (destination == null) {
            showFeedback("Export cancelled.", "muted");
            return;
        }
        exportTo(destination.toPath());
    }

    /** Exports to a chosen path and presents user-readable success or failure feedback. */
    void exportTo(Path destination) {
        try {
            transactionService.exportBackup(destination);
            showFeedback("Backup exported to " + destination.getFileName() + ".", "positive");
        } catch (PersistenceException exception) {
            showFeedback("Backup could not be exported. Check the selected location and try again.",
                    "validation-error");
        }
    }

    /** Opens the native file dialog and asks before replacing all current local data. */
    @FXML
    private void chooseBackupSource() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import MoneyMap backup");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MoneyMap backup (*.json)", "*.json"));
        File source = chooser.showOpenDialog(importButton.getScene().getWindow());
        if (source == null) {
            showImportFeedback("Import cancelled.", "muted");
            return;
        }
        promptBeforeReplacing(source.toPath());
    }

    /** Validates the entire selected backup before presenting a destructive confirmation dialog. */
    private void promptBeforeReplacing(Path source) {
        try {
            transactionService.validateBackup(source);
        } catch (PersistenceException exception) {
            showImportFeedback("Backup could not be imported. Choose a valid compatible backup and try again.",
                    "validation-error");
            return;
        }
        ButtonType replace = new ButtonType("Replace data", ButtonBar.ButtonData.OK_DONE);
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Replace MoneyMap data?");
        confirmation.setHeaderText("Replace all current MoneyMap data?");
        confirmation.setContentText("This replaces categories, transactions, and budgets. It cannot be undone.");
        confirmation.getButtonTypes().setAll(replace, ButtonType.CANCEL);
        DialogStyler.applyDanger(confirmation);
        confirmation.showAndWait().filter(replace::equals).ifPresent(ignored -> importFrom(source));
    }

    /** Performs the confirmed replacement and exposes a clear success or failure result. */
    void importFrom(Path source) {
        try {
            transactionService.importBackup(source);
            showImportFeedback("Backup imported. Current MoneyMap data was replaced.", "positive");
        } catch (PersistenceException exception) {
            showImportFeedback("Backup could not be imported. Current data was not changed.", "validation-error");
        }
    }

    /** Shows status text without using colour as the only feedback channel. */
    private void showFeedback(String message, String styleClass) {
        exportFeedback.setText(message);
        exportFeedback.setVisible(true);
        exportFeedback.setManaged(true);
        exportFeedback.getStyleClass().setAll(styleClass);
    }

    /** Shows import status text without relying on colour as the only feedback channel. */
    private void showImportFeedback(String message, String styleClass) {
        importFeedback.setText(message);
        importFeedback.setVisible(true);
        importFeedback.setManaged(true);
        importFeedback.getStyleClass().setAll(styleClass);
    }
}
