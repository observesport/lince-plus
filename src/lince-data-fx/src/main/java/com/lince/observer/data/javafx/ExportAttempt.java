package com.lince.observer.data.javafx;

import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.transfer.TransferException;

import java.util.List;
import java.util.function.Supplier;

/**
 * Runs an exporter for the JavaFX export panel and turns any failure into the message the panel shows (LO-349).
 * A {@link TransferException} shows its own message; any other exception is unexpected and shows
 * {@code ERROR_SAVING_FILE}. No UI here.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public final class ExportAttempt {

    /**
     * @param files   exported files, empty on failure
     * @param error   the failure, null on success
     * @param message text to show the user, null on success
     */
    public record Result(List<ExportedFile> files, Exception error, String message) {

        public boolean failed() {
            return error != null;
        }

        /**
         * @return true when the failure is not a {@link TransferException}, so it must be logged with its stack trace
         */
        public boolean unexpected() {
            return error != null && !(error instanceof TransferException);
        }
    }

    private ExportAttempt() {
    }

    /**
     * @param export the exporter call
     * @return its files, or the failure and the message to show; never throws
     */
    public static Result run(Supplier<List<ExportedFile>> export) {
        try {
            return new Result(export.get(), null, null);
        } catch (TransferException e) {
            return new Result(List.of(), e, e.getMessage());
        } catch (Exception e) {
            return new Result(List.of(), e, ResourceBundleHelper.getI18NLabel("ERROR_SAVING_FILE"));
        }
    }
}
