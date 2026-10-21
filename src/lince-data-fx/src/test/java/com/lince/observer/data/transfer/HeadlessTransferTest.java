package com.lince.observer.data.transfer;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.transfer.exporters.Theme5Exporter;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The legacy model extends Swing tree/table models; the REST import/export path must still work without a
 * display. Surefire runs this module with {@code -Djava.awt.headless=true} (see the module pom), and the test
 * fails if that ever stops being the case.
 */
class HeadlessTransferTest {

    @Test
    void lince1RegisterImportAndTheme5ExportRunHeadless() {
        assertTrue(GraphicsEnvironment.isHeadless(), "tests must run with -Djava.awt.headless=true");
        LegacyBridge bridge = new LegacyBridge();

        ILinceProject project = Lince1Fixtures.project(bridge);
        List<ExportedFile> files = new Theme5Exporter(bridge).export(ExportSelection.of(project, null, List.of("1")), Map.of());

        assertEquals(1, project.getRegister().size());
        assertFalse(project.getRegister().get(0).getRegisterData().isEmpty());
        assertEquals(2, files.size());
        assertTrue(files.get(0).content().length > "TIME;EVENT\r\n".length());
    }
}
