package com.lince.observer.data.transfer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * AC-8 / FR-10: the legacy importers and exporters are pure Java, no UI toolkit or dialog.
 */
class NoUiImportsTest {

    private static final List<String> FORBIDDEN = List.of("import javafx.", "import javax.swing", "import java.awt",
            "Platform.runLater", "JOptionPane", "Alert");

    @Test
    void importersAndExportersHaveNoUiDependencies() throws IOException {
        for (String pkg : List.of("importers", "exporters", "legacy")) {
            Path dir = Path.of("src/main/java/com/lince/observer/data/transfer", pkg);
            try (Stream<Path> files = Files.list(dir)) {
                List<Path> sources = files.filter(p -> p.toString().endsWith(".java")).toList();
                assertFalse(sources.isEmpty(), dir.toString());
                for (Path source : sources) {
                    String code = Files.readString(source);
                    for (String forbidden : FORBIDDEN) {
                        assertFalse(code.contains(forbidden), source + " uses " + forbidden);
                    }
                }
            }
        }
    }
}
