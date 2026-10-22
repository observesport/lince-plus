package com.lince.observer.data.legacy.datos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Created by Alberto Soto. 17/2/25
 */
class ControladorArchivosTest {

    private ControladorArchivos controladorArchivos;

    @BeforeEach
    void setUp() {
        controladorArchivos = ControladorArchivos.getInstance();
    }

    @Test
    void testGetInstance() {
        ControladorArchivos instance1 = ControladorArchivos.getInstance();
        ControladorArchivos instance2 = ControladorArchivos.getInstance();
        assertSame(instance1, instance2, "getInstance should return the same instance");
    }

    @Test
    void testGuardarYCargar(@TempDir Path tempDir) throws Exception {
        File testFile = tempDir.resolve("test.xml").toFile();
        String testData = "Test data";

        controladorArchivos.guardar(testFile, testData);
        Object loadedData = controladorArchivos.cargar(testFile);

        assertEquals(testData, loadedData, "Loaded data should match saved data");
    }

    @Test
    void testGuardarFileNotFoundException(@TempDir Path tempDir) {
        File nonExistentDir = tempDir.resolve("non-existent-dir").toFile();
        File testFile = new File(nonExistentDir, "test.xml");

        assertThrows(RuntimeException.class, () -> {
            controladorArchivos.guardar(testFile, "Test data");
        });
    }

    @Test
    void testCargarFileNotFoundException(@TempDir Path tempDir) {
        File nonExistentFile = tempDir.resolve("non-existent-file.xml").toFile();

        assertThrows(FileNotFoundException.class, () -> {
            controladorArchivos.cargar(nonExistentFile);
        });
    }
}
