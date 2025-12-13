package com.lince.observer.data.component;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.bean.categories.Criteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LinceCsvExporterTest {

    private static final String TEST_PROJECT_MULTIPLE_RESOURCE = "multipleObserverExample.xml";
    private static final String TEST_PROJECT_SINGLE_RESOURCE = "singleObserverExample.xml";

    private LinceCsvExporter csvExporter;
    private ILinceProject testLinceProject;
    Logger log = LoggerFactory.getLogger(LinceCsvExporterTest.class);

    private ILinceProject loadProjectFromResources(String resourceName) {
        try {
            File file = new ClassPathResource(resourceName).getFile();
            LinceFileImporter linceFileImporter = new LinceFileImporter();
            return linceFileImporter.importLinceProject(file);
        }catch (IOException e) {
            log.error("Error loading project from resource: {}", resourceName, e);
            return null;
        }
    }

    @BeforeEach
    void setUp() {
        testLinceProject = loadProjectFromResources(TEST_PROJECT_MULTIPLE_RESOURCE);
        csvExporter = new LinceCsvExporter();
    }

    @Test
    void testGetFileFormat() {
        assertEquals("csv", csvExporter.getFileFormat());
    }

    @Test
    void testGetFps() {
        loadProjectFromResources(TEST_PROJECT_MULTIPLE_RESOURCE);
        assertEquals(LinceDataConstants.DEFAULT_FPS, csvExporter.getFps(testLinceProject));
    }

    @Test
    void testGet60Fps() {
        loadProjectFromResources(TEST_PROJECT_SINGLE_RESOURCE);
        testLinceProject.getProfiles().get(0).setFps(60);
        assertEquals(60, csvExporter.getFps(testLinceProject));
    }


    @Test
    void testGetDefaultColumnDefinitions() {
        // Arrange
        List<String> expectedColumns = Arrays.asList(
                LinceDataConstants.ColumnType.EVENT_TIME_FRAMES.toString(),
                LinceDataConstants.ColumnType.EVENT_TIME_SECONDS.toString(),
                LinceDataConstants.ColumnType.EVENT_TIME_MS.toString(),
                LinceDataConstants.ColumnType.EVENT_DURATION_FRAMES.toString(),
                LinceDataConstants.ColumnType.EVENT_DURATION_SECONDS.toString(),
                LinceDataConstants.ColumnType.EVENT_DURATION_MS.toString()
        );
        List<Criteria> criteriaList = testLinceProject.getObservationTool();

        // Act
        List<String> columnDefinitions = csvExporter.getDefaultColumnDefinitions(testLinceProject);

        // Assert
        for (String expectedColumn : expectedColumns) {
            assertTrue(columnDefinitions.contains(expectedColumn),
                    "Column definitions should contain: " + expectedColumn);
        }

        for (Criteria criteria : criteriaList) {
            assertTrue(columnDefinitions.contains(criteria.getCode()),
                    "Column definitions should contain criterion: " + criteria.getName());
        }
    }


    @Test
    void testSaveAs() throws IOException {
        // Arrange
        Path tempFile = Files.createTempFile("test-lince-export", ".csv");
        String expectedContent = csvExporter.executeFormatConversion(testLinceProject);

        try {
            // Act
            File savedFile = csvExporter.saveAs(testLinceProject, tempFile.toString());

            // Assert
            assertNotNull(savedFile, "Saved file should not be null");
            assertTrue(savedFile.exists(), "Saved file should exist");
            String fileContent = Files.readString(savedFile.toPath());
            assertEquals(expectedContent, fileContent,
                    "File content should match the result of executeFormatConversion");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExecuteFormatConversion() throws IOException {
        // Arrange
        testLinceProject = loadProjectFromResources(TEST_PROJECT_MULTIPLE_RESOURCE);
        File expectedOutputFile = new ClassPathResource("multipleObserverExample-export-25fps.csv").getFile();
        String expectedContentComma = Files.readString(expectedOutputFile.toPath());

        // Act - Comma separator (default)
        String resultComma = csvExporter.executeFormatConversion(testLinceProject);

        // Assert - Comma separator
        assertNotNull(resultComma, "Result with comma separator should not be null");
        assertEquals(expectedContentComma.trim(), resultComma.trim(),
                "File content should match the result of executeFormatConversion with comma separator");

        // Act - Semicolon separator
        csvExporter.setUseCsvComma(false);
        String resultSemicolon = csvExporter.executeFormatConversion(testLinceProject);

        // Assert - Semicolon separator
        assertNotNull(resultSemicolon, "Result with semicolon separator should not be null");
        assertEquals(expectedContentComma.replace(LinceDataConstants.CSV_CHAR_SEPARATOR_COMMA,
                        LinceDataConstants.CSV_CHAR_SEPARATOR_SEMICOLON).trim(), resultSemicolon.trim(),
                "File content should match the result of executeFormatConversion with semicolon separator");
    }

    @Test
    void testExecuteFormatConversionWith60Fps() throws IOException {
        // Arrange
        testLinceProject = loadProjectFromResources(TEST_PROJECT_SINGLE_RESOURCE);
        if (testLinceProject != null) {
            testLinceProject.getProfiles().get(0).setFps(60);
        }
        File expectedOutputFile = new ClassPathResource("singleObserverExample-export-60fps.csv").getFile();
        String expectedContentComma = Files.readString(expectedOutputFile.toPath());

        // Act - Comma separator (default)
        String resultComma = csvExporter.executeFormatConversion(testLinceProject);

        // Assert - Comma separator
        assertNotNull(resultComma, "Result with comma separator should not be null");
        assertEquals(expectedContentComma.trim(), resultComma.trim(),
                "File content should match the result of executeFormatConversion with comma separator at 60fps");

        // Act - Semicolon separator
        csvExporter.setUseCsvComma(false);
        String resultSemicolon = csvExporter.executeFormatConversion(testLinceProject);

        // Assert - Semicolon separator
        assertNotNull(resultSemicolon, "Result with semicolon separator should not be null");
        assertEquals(expectedContentComma.replace(LinceDataConstants.CSV_CHAR_SEPARATOR_COMMA,
                        LinceDataConstants.CSV_CHAR_SEPARATOR_SEMICOLON).trim(), resultSemicolon.trim(),
                "File content should match the result of executeFormatConversion with semicolon separator at 60fps");
    }

    @Test
    void testExecuteFormatConversionWithSelectedColumns() throws IOException {
        // Arrange
        testLinceProject = loadProjectFromResources(TEST_PROJECT_SINGLE_RESOURCE);
        if (testLinceProject != null) {
            testLinceProject.getProfiles().get(0).setFps(60);
        }

        // Select specific columns
        List<String> selectedColumns = Arrays.asList(
                LinceDataConstants.ColumnType.EVENT_TIME_FRAMES.toString(),
                LinceDataConstants.ColumnType.EVENT_TIME_SECONDS.toString(),
                "CRI0",
                "CRI10"
        );

        // Act
        String result = csvExporter.executeFormatConversion(testLinceProject, selectedColumns);

        // Assert
        assertNotNull(result, "Result should not be null");
        String[] lines = result.split("\r\n|\n");

        // Check header
        String[] headers = lines[0].split(LinceDataConstants.CSV_CHAR_SEPARATOR_COMMA);
        List<String> headerList = Arrays.asList(headers);
        assertEquals(selectedColumns.size(), headerList.size(), "Header should contain only selected columns");
        assertTrue(headerList.containsAll(selectedColumns), "Header should contain all selected columns");

        // Check data rows, avoiding first two rows (header and timestamp)
        for (int i = 2; i < lines.length; i++) {
            String[] columns = lines[i].split(LinceDataConstants.CSV_CHAR_SEPARATOR_COMMA);
            assertEquals(selectedColumns.size(), columns.length, "Each row should have the same number of columns as selected");
        }

        // Check that excluded columns are not present
        assertFalse(result.contains(LinceDataConstants.ColumnType.EVENT_DURATION_FRAMES.toString()),
                "Result should not contain excluded column: " + LinceDataConstants.ColumnType.EVENT_DURATION_FRAMES);
        assertFalse(result.contains(LinceDataConstants.ColumnType.EVENT_DURATION_SECONDS.toString()),
                "Result should not contain excluded column: " + LinceDataConstants.ColumnType.EVENT_DURATION_SECONDS);
    }

    @Test
    void testExportSingleObserverOrderedByTime() {
        // Arrange
        testLinceProject = loadProjectFromResources(TEST_PROJECT_MULTIPLE_RESOURCE);
        assertNotNull(testLinceProject, "Test project should not be null");

        // Act - Export first observer's data (default behavior)
        String result = csvExporter.executeFormatConversion(testLinceProject);

        // Assert
        assertNotNull(result, "Result should not be null");
        String[] lines = result.split("\r\n|\n");
        assertTrue(lines.length > 1, "Result should have header and data rows");

        // Verify that the time values are sorted in ascending order
        // Skip first two lines (header and zero time row)
        double previousTime = -1.0;
        for (int i = 2; i < lines.length; i++) {
            String[] columns = lines[i].split(LinceDataConstants.CSV_CHAR_SEPARATOR_COMMA);
            if (columns.length > 2) {
                // Parse time from TSegundos column (index 2)
                String timeStr = columns[2];
                if (!timeStr.isEmpty()) {
                    // Convert time format "MM:SS" or "HH:MM:SS" to seconds
                    String[] timeParts = timeStr.split(":");
                    double timeInSeconds = 0;
                    if (timeParts.length == 2) {
                        timeInSeconds = Integer.parseInt(timeParts[0]) * 60 +
                                       Double.parseDouble(timeParts[1]);
                    } else if (timeParts.length == 3) {
                        timeInSeconds = Integer.parseInt(timeParts[0]) * 3600 +
                                       Integer.parseInt(timeParts[1]) * 60 +
                                       Double.parseDouble(timeParts[2]);
                    }
                    assertTrue(timeInSeconds >= previousTime,
                            String.format("Time at row %d (%s = %.2f) should be >= previous time (%.2f)",
                                    i, timeStr, timeInSeconds, previousTime));
                    previousTime = timeInSeconds;
                }
            }
        }

        // Verify that only one observer's data is exported
        int dataRowCount = lines.length - 2;
        int expectedRowCount = testLinceProject.getRegister().get(0).getRegisterData().size();
        assertEquals(expectedRowCount, dataRowCount,
                "Export should contain only the first observer's register items");
    }

    @Test
    void testExportSpecificObserverByUUID() {
        // Arrange
        testLinceProject = loadProjectFromResources(TEST_PROJECT_MULTIPLE_RESOURCE);
        assertNotNull(testLinceProject, "Test project should not be null");
        assertTrue(testLinceProject.getRegister().size() > 1,
                "Test project should have multiple observers for this test");

        // Get the UUID of the second observer to verify we can select a specific one
        java.util.UUID secondObserverId = testLinceProject.getRegister().get(1).getId();
        csvExporter.setResearchUUID(secondObserverId);

        // Act - Export only the second observer's data
        String result = csvExporter.executeFormatConversion(testLinceProject);

        // Assert
        assertNotNull(result, "Result should not be null");
        String[] lines = result.split("\r\n|\n");
        assertTrue(lines.length > 1, "Result should have header and data rows");

        // Count data rows (excluding header and zero time row)
        int dataRowCount = lines.length - 2;
        int expectedRowCount = testLinceProject.getRegister().get(1).getRegisterData().size();

        assertEquals(expectedRowCount, dataRowCount,
                "Export should contain only the register items from the selected observer");
    }


}
