package com.lince.observer.data.javafx;

import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.TransferException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportAttemptTest {

    @Test
    void successKeepsTheFiles() {
        List<ExportedFile> files = List.of(new ExportedFile("a.csv", "text/csv", new byte[]{1}));

        ExportAttempt.Result result = ExportAttempt.run(() -> files);

        assertFalse(result.failed());
        assertSame(files, result.files());
        assertNull(result.message());
    }

    @Test
    void transferErrorShowsItsOwnMessage() {
        ExportException error = new ExportException(TransferException.Reason.IO, "disk full");

        ExportAttempt.Result result = ExportAttempt.run(() -> {
            throw error;
        });

        assertTrue(result.failed());
        assertFalse(result.unexpected());
        assertSame(error, result.error());
        assertEquals("disk full", result.message());
        assertEquals(List.of(), result.files());
    }

    @Test
    void anyOtherErrorIsCaughtAsUnexpectedWithTheSavingMessage() {
        IllegalStateException error = new IllegalStateException("boom");

        ExportAttempt.Result result = ExportAttempt.run(() -> {
            throw error;
        });

        assertTrue(result.failed());
        assertTrue(result.unexpected());
        assertSame(error, result.error());
        assertEquals(ResourceBundleHelper.getI18NLabel("ERROR_SAVING_FILE"), result.message());
    }
}
