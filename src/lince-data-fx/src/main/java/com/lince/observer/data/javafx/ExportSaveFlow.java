package com.lince.observer.data.javafx;

import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.util.JavaFXLogHelper;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The one JavaFX save path for exports (LO-349), shared by the export panels ({@link GenericExportComponent}) and
 * the whole-project menu entries: run the exporter, resolve the targets with {@link ExportTargets}, confirm overwriting existing targets other than the chosen file, write, and report.
 * Dialogs go through {@link Ui}, so the flow itself runs without a JavaFX toolkit.
 * <p>
 * Created by Alberto Soto. 22/10/26
 */
public final class ExportSaveFlow {

    private static final Logger log = LoggerFactory.getLogger(ExportSaveFlow.class);

    /**
     * The dialogs the flow needs. {@link #javaFx(Window)} shows JavaFX alerts; tests record the calls.
     */
    public interface Ui {

        /**
         * @param existing targets that already exist, never the file the save dialog returned
         * @return true to overwrite them, false cancels the whole save
         */
        boolean confirmOverwrite(String title, List<File> existing);

        /**
         * @param message    text to show
         * @param unexpected the cause when it is not a {@link TransferException}, to log with its stack trace;
         *                   null for expected failures
         */
        void error(String title, String message, Exception unexpected);

        /**
         * @param written every file written, the chosen one first
         */
        void saved(String title, List<File> written);
    }

    private final Ui ui;

    public ExportSaveFlow(Ui ui) {
        this.ui = Objects.requireNonNull(ui, "ui");
    }

    /**
     * @param owner window the alerts belong to, null for none
     * @return the flow with JavaFX alerts; call it on the JavaFX thread
     */
    public static ExportSaveFlow javaFx(Window owner) {
        return new ExportSaveFlow(new JavaFxUi(owner));
    }

    /**
     * Exports, then saves to {@code chosen}. The save dialog comes first, so cancelling it exports nothing.
     *
     * @param title  dialog title, already translated
     * @param export the exporter call
     * @param chosen file picked in the save dialog, null when it was cancelled
     * @return the files written, empty when cancelled or failed
     */
    public List<File> run(String title, Supplier<List<ExportedFile>> export, File chosen) {
        if (chosen == null) {
            return List.of();
        }
        List<ExportedFile> files = export(title, export);
        return files == null ? List.of() : save(title, chosen, files);
    }

    /**
     * Runs the exporter and shows any failure: a {@link TransferException} shows its own message, any other
     * exception is unexpected and shows {@code ERROR_SAVING_FILE}.
     *
     * @return the exported files, null when the export failed (already reported)
     */
    public List<ExportedFile> export(String title, Supplier<List<ExportedFile>> export) {
        try {
            return export.get();
        } catch (TransferException e) {
            log.warn("{} export: {} {}", title, e.getReason(), e.getMessage());
            ui.error(title, e.getMessage(), null);
        } catch (Exception e) {
            ui.error(title, ResourceBundleHelper.getI18NLabel("ERROR_SAVING_FILE"), e);
        }
        return null;
    }

    /**
     * Writes exported files next to {@code chosen} (see {@link ExportTargets#resolve}), asking before overwriting
     * any existing target other than {@code chosen}, which the save dialog already confirmed.
     *
     * @param chosen file picked in the save dialog, null when it was cancelled
     * @param files  exporter result, at least one
     * @return the files written, empty when cancelled or failed
     */
    public List<File> save(String title, File chosen, List<ExportedFile> files) {
        if (chosen == null) {
            return List.of();
        }
        List<ExportTargets.Target> targets = ExportTargets.resolve(chosen, files);
        List<File> existing = ExportTargets.toConfirm(chosen, targets);
        if (!existing.isEmpty() && !ui.confirmOverwrite(title, existing)) {
            return List.of();
        }
        try {
            ExportTargets.write(targets);
        } catch (Exception e) {
            ui.error(title, ResourceBundleHelper.getI18NLabel("ERROR_SAVING_FILE"), e);
            return List.of();
        }
        List<File> written = targets.stream().map(ExportTargets.Target::file).toList();
        ui.saved(title, written);
        return written;
    }

    private record JavaFxUi(Window owner) implements Ui {

        @Override
        public boolean confirmOverwrite(String title, List<File> existing) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    ResourceBundleHelper.getI18NLabel("OVERWRITE_FILE_PROMPT") + "\n"
                            + existing.stream().map(File::getAbsolutePath).collect(Collectors.joining("\n")),
                    ButtonType.YES, ButtonType.NO);
            if (owner != null) {
                confirm.initOwner(owner);
            }
            confirm.setTitle(title);
            confirm.setHeaderText(null);
            return confirm.showAndWait().filter(ButtonType.YES::equals).isPresent();
        }

        @Override
        public void error(String title, String message, Exception unexpected) {
            if (unexpected != null) {
                JavaFXLogHelper.addLogError(title, unexpected);
            }
            JavaFXLogHelper.showMessage(Alert.AlertType.ERROR, title, message);
        }

        @Override
        public void saved(String title, List<File> written) {
            JavaFXLogHelper.addLogInfo(title + ": " + written.get(0).getAbsolutePath());
            if (written.size() == 1) {
                JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION, title,
                        ResourceBundleHelper.getI18NLabel("FILE_SAVED"));
            } else {
                JavaFXLogHelper.showMessageDialog(Alert.AlertType.INFORMATION, title,
                        ResourceBundleHelper.getI18NLabel("FILES_SAVED") + "\n\n" + written.stream()
                                .map(f -> "  • " + f.getName() + "\n    " + f.getAbsolutePath())
                                .collect(Collectors.joining("\n\n")));
            }
        }
    }
}
