package com.lince.observer.data.transfer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AC-8 / FR-10 (LO-349): lince-legacy is pure Java, no UI toolkit or dialog, so lince-server and headless REST can
 * use it. The only Swing classes allowed are the tree model ones the legacy instrument is built on
 * ({@code javax.swing.tree.*}, {@code javax.swing.event.Tree*}); they live in the JDK {@code java.desktop} module and
 * need no display.
 */
class NoUiImportsTest {

    private static final Path MAIN = Path.of("src/main/java");
    private static final List<String> FORBIDDEN = List.of("import javafx.", "import java.awt", "Platform.runLater",
            "JOptionPane", "Alert");
    private static final List<String> ALLOWED_SWING = List.of("import javax.swing.tree.", "import javax.swing.event.Tree");

    @Test
    void moduleHasNoUiDependencies() throws IOException {
        List<Path> sources;
        try (Stream<Path> files = Files.walk(MAIN)) {
            sources = files.filter(p -> p.toString().endsWith(".java")).toList();
        }
        assertTrue(sources.size() > 30, "expected the whole legacy module under " + MAIN);
        List<String> violations = new ArrayList<>();
        for (Path source : sources) {
            for (String line : Files.readAllLines(source)) {
                String code = line.strip();
                for (String forbidden : FORBIDDEN) {
                    if (code.contains(forbidden)) {
                        violations.add(source + " uses " + forbidden);
                    }
                }
                if (code.startsWith("import javax.swing") && ALLOWED_SWING.stream().noneMatch(code::startsWith)) {
                    violations.add(source + ": " + code);
                }
            }
        }
        assertTrue(violations.isEmpty(), String.join("\n", violations));
    }

    @Test
    void forbiddenListCatchesSwingOutsideTheTreeModel() {
        String table = "import javax.swing.table.AbstractTableModel;";
        assertFalse(ALLOWED_SWING.stream().anyMatch(table::startsWith));
        assertTrue(ALLOWED_SWING.stream().anyMatch("import javax.swing.tree.DefaultMutableTreeNode;"::startsWith));
    }
}
