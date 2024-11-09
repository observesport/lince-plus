package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.bean.highcharts.HighChartsSerieBean;
import com.lince.observer.data.bean.highcharts.HighChartsWrapper;
import com.lince.observer.data.bean.highcharts.HighChartsSerie;
import com.lince.observer.data.service.AnalysisService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import static org.mockito.Mockito.when;

class AnalysisControllerImplTest {
    @Mock
    private AnalysisService analysisService;

    @InjectMocks
    private AnalysisControllerImpl analysisController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPieStats() {
    }

    @Test
    void testGetPieStats_WithEmptyHighChartsWrapper() {
        // Arrange
        HighChartsWrapper emptyData = new HighChartsWrapper();
        when(analysisService.getRegisterStatsByCategory()).thenReturn(emptyData);

        // Act
        ResponseEntity<String> response = analysisController.getPieStats();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONObject jsonResponse = new JSONObject(response.getBody());
        assertTrue(jsonResponse.length() == 0);
    }

    @Test
    void testGetPieStats_WithSpecialCharactersInSeriesNames() {
        // Arrange
        HighChartsWrapper mockData = new HighChartsWrapper();
        HighChartsSerie mockSerie = new HighChartsSerie();
        HighChartsSerieBean serieBean = new HighChartsSerieBean();
        serieBean.setName("value");
        mockSerie.setDataBean(List.of(serieBean));
        mockData.setSeries(List.of(mockSerie));

        HighChartsWrapper mockDrilldown = new HighChartsWrapper();
        HighChartsSerie mockDrilldownSerie = new HighChartsSerie();
        mockDrilldownSerie.setName("Special & Char's");
        HighChartsSerieBean drilldownSerieBean = new HighChartsSerieBean();
        drilldownSerieBean.setDrilldown("subValue");
        mockDrilldownSerie.setDataBean(List.of(drilldownSerieBean));
        mockDrilldown.setSeries(List.of(mockDrilldownSerie));
        mockData.setDrilldown(List.of(mockDrilldown));

        when(analysisService.getRegisterStatsByCategory()).thenReturn(mockData);

        // Act
        ResponseEntity<String> response = analysisController.getPieStats();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONObject jsonResponse = new JSONObject(response.getBody());
        assertTrue(jsonResponse.has("criteria"));
        assertTrue(jsonResponse.has("Special & Char's"));

        // Check if "criteria" is a JSONArray
        assertTrue(jsonResponse.get("criteria") instanceof JSONArray);
        JSONArray criteriaArray = jsonResponse.getJSONArray("criteria");
        assertEquals(1, criteriaArray.length());
        JSONObject criteriaObject = criteriaArray.getJSONObject(0);
        assertEquals("value", criteriaObject.getString("name"));

        // Check the drilldown data
        assertTrue(jsonResponse.get("Special & Char's") instanceof JSONArray);
        JSONArray drilldownArray = jsonResponse.getJSONArray("Special & Char's");
        assertEquals(1, drilldownArray.length());
        JSONObject drilldownObject = drilldownArray.getJSONObject(0);
        assertEquals("subValue", drilldownObject.getString("drilldown"));
    }

//    @Test
//    void testGetPieStats_WithDrilldownData() {
//        // Arrange
//        HighChartsWrapper mockData = new HighChartsWrapper();
//        HighChartsSerie mockSerie = new HighChartsSerie();
//        mockSerie.setDataBean(new JSONObject("{\"mainCriteria\":\"value\"}"));
//        mockData.setSeries(List.of(mockSerie));
//
//        HighChartsWrapper mockDrilldown = new HighChartsWrapper();
//        HighChartsSerie mockDrilldownSerie = new HighChartsSerie();
//        mockDrilldownSerie.setName("DrilldownCategory");
//        mockDrilldownSerie.setDataBean(new JSONObject("{\"subCriteria\":\"subValue\"}"));
//        mockDrilldown.setSeries(List.of(mockDrilldownSerie));
//        mockData.setDrilldown(List.of(mockDrilldown));
//
//        when(analysisService.getRegisterStatsByCategory()).thenReturn(mockData);
//
//        // Act
//        ResponseEntity<String> response = analysisController.getPieStats();
//
//        // Assert
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        JSONObject jsonResponse = new JSONObject(response.getBody());
//        assertTrue(jsonResponse.has("criteria"));
//        assertTrue(jsonResponse.has("DrilldownCategory"));
//        assertEquals("{\"mainCriteria\":\"value\"}", jsonResponse.getJSONObject("criteria").toString());
//        assertEquals("{\"subCriteria\":\"subValue\"}", jsonResponse.getJSONObject("DrilldownCategory").toString());
//    }
//
//    @Test
//    void testGetPieStats_WithSeriesData() {
//        // Arrange
//        HighChartsWrapper mockData = new HighChartsWrapper();
//        HighChartsSerie mockSerie = new HighChartsSerie();
//        mockSerie.setDataBean(new JSONObject("{\"key\":\"value\"}"));
//        mockData.setSeries(List.of(mockSerie));
//
//        when(analysisService.getRegisterStatsByCategory()).thenReturn(mockData);
//
//        // Act
//        ResponseEntity<String> response = analysisController.getPieStats();
//
//        // Assert
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        JSONObject jsonResponse = new JSONObject(response.getBody());
//        assertTrue(jsonResponse.has("criteria"));
//        assertEquals("{\"key\":\"value\"}", jsonResponse.getJSONObject("criteria").toString());
//    }


}
