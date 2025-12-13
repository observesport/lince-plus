package com.lince.observer.legacy;


import com.lince.observer.data.LegacyToolException;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Alberto Soto. 17/2/25
 */
class RegistroTest {

    private List<LegacyToolException> exceptions;
    private Registro registro;
    private List<Criterio> criterios;

    @BeforeEach
    void setUp() throws Exception {
        exceptions = new ArrayList<>();

        // Load test files for export tests
        File linceRegister = new File(getClass().getResource("/lince1/import/Registro_prueba.rlince").getFile());
        File linceInstrument = new File(getClass().getResource("/lince1/import/Instrumento_prueba.ilince").getFile());

        InstrumentoObservacional.loadInstance(linceInstrument);
        registro = Registro.cargarRegistro(linceRegister, exceptions);

        if (registro != null) {
            criterios = Arrays.asList(InstrumentoObservacional.getInstance().getCriterios());
        }
    }

    @Test
    void testCargarRegistroSuccess() throws Exception {
        // Arrange
        File linceRegister = new  File(getClass().getResource("/lince1/import/Registro_prueba.rlince").getFile());
        File linceInstrument = new File(getClass().getResource("/lince1/import/Instrumento_prueba.ilince").getFile());

        // Act
        InstrumentoObservacional.loadInstance(linceInstrument);
        Registro result = Registro.cargarRegistro(linceRegister, exceptions);

        // Assert
        assertNotNull(result);
        if (!exceptions.isEmpty()) {
            System.out.println("[DEBUG_LOG] Found " + exceptions.size() + " exceptions:");
            for (LegacyToolException ex : exceptions) {
                System.out.println("[DEBUG_LOG] Exception: " + ex.getMessage());
                if (ex.getCategoria() != null) {
                    System.out.println("[DEBUG_LOG] Category: " + ex.getCategoria().getNombre());
                }
            }
        }
        assertTrue(exceptions.isEmpty(), "Expected no exceptions during loading");
        assertTrue(result.getRowCount() > 0);
    }

    @Test
    void testCargarInstrumentoSuccess() throws Exception {
        // Arrange
        File linceFile = new File(getClass().getResource("/lince1/instrumentoObservacional-adv.ilince").getFile());

        // Act
        InstrumentoObservacional.loadInstance(linceFile);
        InstrumentoObservacional result = InstrumentoObservacional.getInstance();

        // Assert
        assertNotNull(result);
        assertTrue(exceptions.isEmpty());
    }

    @Test
    void testCargarRegistroFileNotFoundException() throws RegistroException {
        // Arrange
        File nonExistentFile = new File("nonexistent.xml");

        // Act
        Registro result = Registro.cargarRegistro(nonExistentFile, exceptions);

        // Assert
        assertNull(result);
        assertFalse(exceptions.isEmpty());
        assertEquals(1, exceptions.size());
        assertTrue(exceptions.get(0).getCause() instanceof FileNotFoundException);
        assertTrue(exceptions.get(0).getMessage().contains("Error loading file"));
    }





}
