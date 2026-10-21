package com.lince.observer.data.javafx;

import com.lince.observer.data.bean.transfer.ExportedFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportTargetsTest {

    @TempDir
    Path dir;

    private static ExportedFile file(String name, String content) {
        return new ExportedFile(name, "text/plain", content.getBytes());
    }

    @Test
    void singleFileGoesToTheChosenFileWithItsExtension() {
        File chosen = dir.resolve("out").toFile();

        List<ExportTargets.Target> targets = ExportTargets.resolve(chosen, List.of(file("hoisan.mdb", "x")));

        assertEquals(1, targets.size());
        assertEquals(dir.resolve("out.mdb").toFile(), targets.get(0).file());
        assertFalse(targets.get(0).fixedName());
    }

    @Test
    void chosenExtensionIsKeptCaseInsensitively() {
        File chosen = dir.resolve("data.SDS").toFile();

        assertEquals(chosen, ExportTargets.resolve(chosen, List.of(file("gseq-event.sds", "x"))).get(0).file());
    }

    @Test
    void siblingWithTheSameBaseNameFollowsTheChosenName() {
        File chosen = dir.resolve("match.csv").toFile();

        List<ExportTargets.Target> targets = ExportTargets.resolve(chosen,
                List.of(file("theme5.csv", "r"), file("theme5.vvt", "i")));

        assertEquals(dir.resolve("match.csv").toFile(), targets.get(0).file());
        assertEquals(dir.resolve("match.vvt").toFile(), targets.get(1).file());
        assertFalse(targets.get(1).fixedName());
    }

    @Test
    void siblingWithAnotherNameKeepsItsFixedNameInTheChosenFolder() {
        File chosen = dir.resolve("match.txt").toFile();

        List<ExportTargets.Target> targets = ExportTargets.resolve(chosen,
                List.of(file("theme6.txt", "r"), file("vvt.vvt", "i")));

        assertEquals(dir.resolve("vvt.vvt").toFile(), targets.get(1).file());
        assertTrue(targets.get(1).fixedName());
    }

    @Test
    void existingFixedNamesAreReportedForTheOverwritePrompt() throws Exception {
        Files.writeString(dir.resolve("vvt.vvt"), "old");
        List<ExportTargets.Target> targets = ExportTargets.resolve(dir.resolve("a.txt").toFile(),
                List.of(file("theme6.txt", "r"), file("vvt.vvt", "i")));

        assertEquals(List.of(dir.resolve("vvt.vvt").toFile()), ExportTargets.existingFixedNames(targets));
    }

    @Test
    void writeStoresTheExactBytes() throws Exception {
        List<ExportTargets.Target> targets = ExportTargets.resolve(dir.resolve("a.csv").toFile(),
                List.of(file("theme5.csv", "ré"), file("theme5.vvt", "i")));

        ExportTargets.write(targets);

        assertArrayEquals("ré".getBytes(), Files.readAllBytes(dir.resolve("a.csv")));
        assertArrayEquals("i".getBytes(), Files.readAllBytes(dir.resolve("a.vvt")));
    }
}
