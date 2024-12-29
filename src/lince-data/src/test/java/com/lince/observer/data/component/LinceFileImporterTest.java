package com.lince.observer.data.component;

import com.lince.observer.data.ILinceProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
/**
 *
 */
public class LinceFileImporterTest {
    private LinceFileImporter linceFileImporter;

    @BeforeEach
    void setUp() {
        linceFileImporter = new LinceFileImporter();
    }

    @Test
    void testImportLinceProject() throws IOException {
        // Arrange
        File file = new ClassPathResource("multipleObserverExample.xml").getFile();

        // Act
        ILinceProject result = linceFileImporter.importLinceProject(file);

        // Assert
        assertNotNull(result, "The imported project should not be null");
        assertNotNull(result.getObservationTool(), "The observation tool should not be null");
        assertNotNull(result.getRegister(), "The register should not be null");
        assertNotNull(result.getProfiles(), "The profiles should not be null");
        assertNotNull(result.getVideoPlayList(), "The video playlist should not be null");

    }

    @Test
    void testImportLinceProjectWithNonExistentFile() {
        // Arrange
        File nonExistentFile = new File("non_existent_file.xml");

        // Act
        ILinceProject result = linceFileImporter.importLinceProject(nonExistentFile);

        // Assert
        assertNull(result, "The result should be null for a non-existent file");
    }
}
