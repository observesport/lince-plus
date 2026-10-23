package com.lince.observer.data.transfer.importers;

import com.lince.observer.data.transfer.ImportException;
import com.lince.observer.data.transfer.Lince1Fixtures;
import com.lince.observer.data.transfer.TransferException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LegacyImportSupportTest {

    private static final String PREFIX = "lince-import-";

    @Test
    void spoolCopiesTheUpload() throws Exception {
        File tmp = LegacyImportSupport.spool(new ByteArrayInputStream("abc".getBytes()), ".mdb");
        try {
            assertArrayEquals("abc".getBytes(), Files.readAllBytes(tmp.toPath()));
        } finally {
            FileUtils.deleteQuietly(tmp);
        }
    }

    @Test
    void spoolDeletesTheTempFileWhenTheUploadAborts() {
        Set<String> before = Lince1Fixtures.tempFileNames(PREFIX);
        InputStream aborting = new InputStream() {
            private int read;

            @Override
            public int read() throws IOException {
                if (read++ < 10) {
                    return 'x';
                }
                throw new IOException("connection reset");
            }
        };

        ImportException e = assertThrows(ImportException.class, () -> LegacyImportSupport.spool(aborting, ".mdb"));

        assertEquals(TransferException.Reason.IO, e.getReason());
        assertEquals(Set.of(), Lince1Fixtures.newTempFiles(before, PREFIX), "temp file deleted");
    }
}
