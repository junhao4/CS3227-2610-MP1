package cs3227.moneymap.ui;

import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

import java.net.URL;
import java.util.Objects;

/** Applies the shared MoneyMap presentation to application-owned JavaFX dialogs. */
public final class DialogStyler {
    private static final String STYLESHEET = stylesheetUrl().toExternalForm();
    private static final String DIALOG_STYLE_CLASS = "moneymap-dialog";
    private static final String DANGER_STYLE_CLASS = "danger-dialog";

    private DialogStyler() {
    }

    /** Attaches the production stylesheet and base dialog class.
     *
     * @param dialog application-owned dialog to style
     */
    public static void apply(Dialog<?> dialog) {
        DialogPane dialogPane = Objects.requireNonNull(dialog).getDialogPane();
        if (!dialogPane.getStylesheets().contains(STYLESHEET)) {
            dialogPane.getStylesheets().add(STYLESHEET);
        }
        if (!dialogPane.getStyleClass().contains(DIALOG_STYLE_CLASS)) {
            dialogPane.getStyleClass().add(DIALOG_STYLE_CLASS);
        }
    }

    /** Applies the base theme and marks an irreversible confirmation as destructive.
     *
     * @param dialog irreversible application-owned dialog to style
     */
    public static void applyDanger(Dialog<?> dialog) {
        apply(dialog);
        if (!dialog.getDialogPane().getStyleClass().contains(DANGER_STYLE_CLASS)) {
            dialog.getDialogPane().getStyleClass().add(DANGER_STYLE_CLASS);
        }
    }

    private static URL stylesheetUrl() {
        return Objects.requireNonNull(
                DialogStyler.class.getResource("/styles/moneymap.css"),
                "The production stylesheet is missing"
        );
    }
}
