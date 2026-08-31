package cs3227.moneymap;

import cs3227.moneymap.service.PersistenceException;
import cs3227.moneymap.service.TransactionService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/** Connects the Data and Settings view to complete local-backup export. */
public final class DataAndSettingsController {
    private static final DateTimeFormatter BACKUP_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TransactionService transactionService;

    @FXML
    private Button exportButton;

    @FXML
    private Label exportFeedback;

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

    /** Shows status text without using colour as the only feedback channel. */
    private void showFeedback(String message, String styleClass) {
        exportFeedback.setText(message);
        exportFeedback.setVisible(true);
        exportFeedback.setManaged(true);
        exportFeedback.getStyleClass().setAll(styleClass);
    }
}
