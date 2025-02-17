package com.lince.observer.math.service;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.SceneWrapper;
import com.lince.observer.data.service.AnalysisService;
import com.lince.observer.math.LinceServiceTestConfig;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Created by Alberto Soto. 17/2/25
 *
 *     TODO uncomment tests
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LinceServiceTestConfig.class)
class AnalysisServiceTest {

    private static final Logger log = LoggerFactory.getLogger(AnalysisServiceTest.class);
    @Autowired
    private AnalysisService analysisService;

    @Mock
    private List<RegisterItem> mockRegisterItems;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testSaveAndGetOne() {
        // Collect all IDs first
        List<Integer> idsToDelete = analysisService.getAllObservations().stream()
                .map(RegisterItem::getId)
                .toList();

        // Delete observations
        idsToDelete.forEach(id -> {
            boolean deleted = analysisService.deleteObservationById(id);
            assertTrue(deleted, "Failed to delete existing observation: " + id);
        });

        // Verify that all previous entries are deleted
        assertTrue(analysisService.getAllObservations().isEmpty(), "Failed to clear all existing observations");

        // Arrange
        List<RegisterItem> realRegisterItems = new ArrayList<>();
        Criteria criteria = new Criteria(100,"Criteria3");
        Category category = new Category(101,"Category3",criteria.getId());
        criteria.setInnerCategories(new LinkedList<>(List.of(category)));
        realRegisterItems.add(new RegisterItem(1.0, category));

        // Act
        for (RegisterItem item : realRegisterItems) {
            boolean saved = analysisService.saveObservation(item);
            assertTrue(saved, "Failed to save observation: " + item);
        }
        List<RegisterItem> result = analysisService.getAllObservations();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1.0, result.get(0).getVideoTime());
        assertTrue(result.get(0).getRegister().stream().anyMatch(cat -> cat.getName().equals("Category3")));
    }

    @Test
    void testSaveObservationWithVideoTimeAndCategories() {
        Double videoTime = 15.0;
        Category category1 = mock(Category.class);
        Category category2 = mock(Category.class);

        boolean result = analysisService.saveObservation(videoTime, category1, category2);

        assertTrue(result);
    }

    @Test
    void testSaveObservationWithRegisterItem() {
        RegisterItem item = mock(RegisterItem.class);

        boolean result = analysisService.saveObservation(item);

        assertTrue(result);
    }

    @Test
    void testSaveObservationWithSceneWrapper() {
        SceneWrapper sceneWrapper = mock(SceneWrapper.class);

        boolean result = analysisService.saveObservation(sceneWrapper);

        assertTrue(result);
    }


    @Test
    void testConvertSysMoment() {
        assertEquals(3.14, analysisService.convertSysMoment(3.1415926535), 0.001);
        assertEquals(0.00, analysisService.convertSysMoment(0.001), 0.001);
        assertEquals(100.00, analysisService.convertSysMoment(100.0), 0.001);
    }

    @Test
    void testGetFrequency() {
        assertEquals(50.0, analysisService.getFrequency(5.0, 10.0), 0.001);
        assertEquals(0.0, analysisService.getFrequency(0.0, 10.0), 0.001);
        assertNull(analysisService.getFrequency(null, 10.0));
        assertNull(analysisService.getFrequency(5.0, null));
    }

    @Test
    void testGetSortedObservations() {
        // Mock the getAllObservations method
        AnalysisService spyService = spy(analysisService);
        List<RegisterItem> unsortedList = new ArrayList<>();
        doReturn(unsortedList).when(spyService).getAllObservations();

        List<RegisterItem> sortedList = spyService.getSortedObservations();

        // Verify that the list is sorted
        assertTrue(isSorted(sortedList));
    }

    private boolean isSorted(List<RegisterItem> list) {
        if (list.size() <= 1) {
            return true;
        }
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i - 1).compareTo(list.get(i)) > 0) {
                return false;
            }
        }
        return true;
    }

    @Test
    void testGetClusteredObservationsByCriteria() {
        Criteria criteria = mock(Criteria.class);
        LinkedList<Category> categories = new LinkedList<>();
        // Add some mock categories
        when(criteria.getInnerCategories()).thenReturn(categories);

        List<RegisterItem> userSceneData = new ArrayList<>();
        // Add some mock RegisterItems

        List<Pair<CategoryData, Double>> result = analysisService.getClusteredObservationsByCriteria(criteria, userSceneData);

        assertNotNull(result);
        // Add more specific assertions based on your expected behavior
    }

    @Test
    void testGetTotals() {
        List<Pair<CategoryData, Double>> data = new ArrayList<>();
        data.add(new Pair<>(mock(CategoryData.class), 5.0));
        data.add(new Pair<>(mock(CategoryData.class), 10.0));
        data.add(new Pair<>(mock(CategoryData.class), 15.0));

        double total = analysisService.getTotals(data);
        assertEquals(30.0, total, 0.001);
    }
}
