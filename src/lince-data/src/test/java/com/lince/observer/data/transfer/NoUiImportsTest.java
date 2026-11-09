package com.lince.observer.data.transfer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AC-8 (LO-349): the transfer contract and strategies stay free of UI code, so lince-server can run them.
 */
class NoUiImportsTest {

    private static final List<String> FORBIDDEN = List.of("import javafx.", "import javax.swing", "import java.awt",
            "Platform.runLater", "JOptionPane");

    @Test
    void transferPackagesHaveNoUiImports() throws IOException {
        for (String dir : List.of("src/main/java/com/lince/observer/data/transfer",
                "src/main/java/com/lince/observer/data/transfer/importers",
                "src/main/java/com/lince/observer/data/transfer/exporters",
                "src/main/java/com/lince/observer/data/bean/transfer")) {
            try (Stream<Path> files = Files.walk(Path.of(dir))) {
                List<Path> sources = files.filter(p -> p.toString().endsWith(".java")).toList();
                assertFalse(sources.isEmpty(), dir);
                for (Path source : sources) {
                    String code = Files.readString(source);
                    for (String forbidden : FORBIDDEN) {
                        assertFalse(code.contains(forbidden), source + " uses " + forbidden);
                    }
                }
            }
        }
    }

    /**
     * lince-server uses lince-data without JavaFX: the module must never gain a JavaFX jar dependency, even a
     * transitive one. JDKs that bundle JavaFX (Liberica Full, used for the desktop build) expose it from the runtime
     * image ({@code jrt:}), which is accepted; a JavaFX class coming from a jar means a Maven dependency brought it in.
     */
    @Test
    void javaFxDoesNotComeFromAMavenDependency() {
        for (String type : List.of("javafx.application.Platform", "javafx.collections.FXCollections",
                "javafx.scene.Node")) {
            Class<?> loaded;
            try {
                loaded = Class.forName(type, false, getClass().getClassLoader());
            } catch (ClassNotFoundException expected) {
                continue;
            }
            CodeSource source = loaded.getProtectionDomain().getCodeSource();
            String location = source == null || source.getLocation() == null ? "" : source.getLocation().toString();
            assertTrue(location.isEmpty() || location.startsWith("jrt:"),
                    type + " is loaded from " + location + ": lince-data must not depend on JavaFX");
        }
    }
}
