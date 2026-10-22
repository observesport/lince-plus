package com.lince.observer.data.javafx;

import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.TransferException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportSaveFlowTest {

    private static final String TITLE = "Theme 5";

    @TempDir
    Path dir;

    /**
     * Records every dialog the flow asks for; answers overwrite confirmations with {@link #accept}.
     */
    private static final class RecordingUi implements ExportSaveFlow.Ui {
        boolean accept = true;
        final List<List<File>> confirmations = new ArrayList<>();
        final List<String> errors = new ArrayList<>();
        final List<Exception> causes = new ArrayList<>();
        final List<List<File>> saved = new ArrayList<>();

        @Override
        public boolean confirmOverwrite(String title, List<File> existing) {
            assertEquals(TITLE, title);
            confirmations.add(existing);
            return accept;
        }

        @Override
        public void error(String title, String message, Exception unexpected) {
            assertEquals(TITLE, title);
            errors.add(message);
            causes.add(unexpected);
        }

        @Override
        public void saved(String title, List<File> written) {
            assertEquals(TITLE, title);
            saved.add(written);
        }
    }

    private final RecordingUi ui = new RecordingUi();
    private final ExportSaveFlow flow = new ExportSaveFlow(ui);

    private static ExportedFile file(String name, String content) {
        return new ExportedFile(name, "text/plain", content.getBytes(StandardCharsets.UTF_8));
    }

    private static String read(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    @Test
    void runWritesTheExportToTheChosenFileAndReportsIt() throws IOException {
        File chosen = dir.resolve("out.csv").toFile();

        List<File> written = flow.run(TITLE, () -> List.of(file("theme5.csv", "a;b")), chosen);

        assertEquals(List.of(chosen), written);
        assertEquals("a;b", read(chosen));
        assertEquals(List.of(List.of(chosen)), ui.saved);
        assertTrue(ui.errors.isEmpty());
        assertTrue(ui.confirmations.isEmpty(), "nothing existed, nothing to confirm");
    }

    @Test
    void runWithoutAChosenFileExportsNothing() {
        AtomicBoolean called = new AtomicBoolean();

        List<File> written = flow.run(TITLE, () -> {
            called.set(true);
            return List.of(file("a.csv", "x"));
        }, null);

        assertTrue(written.isEmpty());
        assertFalse(called.get(), "cancelling the save dialog exports nothing");
        assertTrue(ui.saved.isEmpty());
        assertTrue(ui.errors.isEmpty());
    }

    @Test
    void transferErrorShowsItsMessageAndWritesNothing() {
        File chosen = dir.resolve("out.csv").toFile();
        ExportException error = new ExportException(TransferException.Reason.IO, "no criteria");

        List<File> written = flow.run(TITLE, () -> {
            throw error;
        }, chosen);

        assertTrue(written.isEmpty());
        assertFalse(chosen.exists());
        assertEquals(List.of("no criteria"), ui.errors);
        assertNull(ui.causes.get(0), "an expected failure is not logged as an error");
        assertTrue(ui.saved.isEmpty());
    }

    @Test
    void unexpectedErrorShowsTheGenericMessageAndPassesTheCause() {
        IllegalStateException boom = new IllegalStateException("boom");

        List<ExportedFile> files = flow.export(TITLE, () -> {
            throw boom;
        });

        assertNull(files);
        assertEquals(List.of(ResourceBundleHelper.getI18NLabel("ERROR_SAVING_FILE")), ui.errors);
        assertSame(boom, ui.causes.get(0));
    }

    @Test
    void exportReturnsTheFilesWithoutAnyDialog() {
        List<ExportedFile> exported = List.of(file("a.csv", "x"));

        assertSame(exported, flow.export(TITLE, () -> exported));
        assertTrue(ui.errors.isEmpty());
        assertTrue(ui.saved.isEmpty());
    }

    @Test
    void existingSiblingIsConfirmedAndDecliningWritesNothing() throws IOException {
        File chosen = dir.resolve("mine.txt").toFile();
        File sibling = dir.resolve("vvt.vvt").toFile();
        Files.writeString(sibling.toPath(), "old");
        ui.accept = false;

        List<File> written = flow.save(TITLE, chosen, List.of(file("theme6.txt", "t"), file("vvt.vvt", "new")));

        assertTrue(written.isEmpty());
        assertEquals(List.of(List.of(sibling)), ui.confirmations);
        assertFalse(chosen.exists());
        assertEquals("old", read(sibling));
        assertTrue(ui.saved.isEmpty());
    }

    @Test
    void acceptingTheConfirmationWritesEveryTarget() throws IOException {
        File chosen = dir.resolve("mine.txt").toFile();
        File sibling = dir.resolve("vvt.vvt").toFile();
        Files.writeString(sibling.toPath(), "old");

        List<File> written = flow.save(TITLE, chosen, List.of(file("theme6.txt", "t"), file("vvt.vvt", "new")));

        assertEquals(List.of(chosen, sibling), written);
        assertEquals("new", read(sibling));
        assertEquals(List.of(written), ui.saved);
    }

    @Test
    void theChosenFileItselfIsNotConfirmedAgain() throws IOException {
        File chosen = dir.resolve("out.csv").toFile();
        Files.writeString(chosen.toPath(), "old");

        flow.save(TITLE, chosen, List.of(file("theme5.csv", "new")));

        assertTrue(ui.confirmations.isEmpty(), "the save dialog already confirmed it");
        assertEquals("new", read(chosen));
    }

    @Test
    void writeFailureShowsTheGenericMessage() {
        File chosen = dir.resolve("missing-folder").resolve("out.csv").toFile();

        List<File> written = flow.save(TITLE, chosen, List.of(file("theme5.csv", "x")));

        assertTrue(written.isEmpty());
        assertEquals(List.of(ResourceBundleHelper.getI18NLabel("ERROR_SAVING_FILE")), ui.errors);
        assertInstanceOf(IOException.class, ui.causes.get(0));
        assertTrue(ui.saved.isEmpty());
    }
}
