package com.lince.observer.math.service;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.SceneWrapper;
import com.lince.observer.data.service.AnalysisService;
import com.lince.observer.data.service.CategoryService;
import com.lince.observer.data.service.ProfileService;
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
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LinceServiceTestConfig.class)
class AnalysisServiceTest {

    private static final Logger log = LoggerFactory.getLogger(AnalysisServiceTest.class);
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private DataHubService dataHubService;

    @Mock
    private List<RegisterItem> mockRegisterItems;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        analysisService.getAllObservations().clear();
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

    /**
     * Tests moment generation keeps all information and labelling is valid
     */
    @Test
    void testSceneWrapperToSaveObservationHoldsValidTime() {
        double timeValue = 20.371392;
        SceneWrapper sceneWrapper = new SceneWrapper();
        sceneWrapper.setMoment(timeValue);
        boolean result = analysisService.saveObservation(sceneWrapper);
        assertTrue(result);

        List<RegisterItem> observations = analysisService.getAllObservations();
        assertFalse(observations.isEmpty(), "No observations were saved");
        assertTrue(observations.get(0).getVideoTime().equals(timeValue),
                "Video time doesn't match the expected value");
        assertEquals("00:00:20.371", observations.get(0).getVideoTimeTxt());
    }


    /**
     * Tests that 2 registers on same exact time and one additional on another moment generates 2 registers.
     */
    @Test
    void testTimeCollisionsOnSameSecondHoldsValidInformation() {
        Double timeValue = 20.371392;
        SceneWrapper sceneWrapper = new SceneWrapper();
        sceneWrapper.setMoment(timeValue);
        boolean result = analysisService.saveObservation(sceneWrapper);
        assertTrue(result);
        sceneWrapper = new SceneWrapper();
        sceneWrapper.setMoment(timeValue );
        analysisService.saveObservation(sceneWrapper);
        sceneWrapper = new SceneWrapper();
        sceneWrapper.setMoment(timeValue + 0.05 ); //still same second
        analysisService.saveObservation(sceneWrapper);
        assertEquals(2, analysisService.getAllObservations().size());
    }


    /**
     * A register holding observations without id (old or imported files) used to send generateID into
     * infinite recursion (StackOverflowError) on the next save of a new moment.
     */
    @Test
    void testSaveNewObservationWithNullIdRowsInRegister() {
        List<RegisterItem> register = analysisService.getAllObservations();
        register.add(registerItem(null, 1.0));
        register.add(registerItem(7, 2.0));
        register.add(registerItem(null, 3.0));

        boolean saved = assertDoesNotThrow(() -> analysisService.saveObservation(registerItem(null, 4.0)));

        assertTrue(saved);
        List<RegisterItem> result = analysisService.getAllObservations();
        assertEquals(4, result.size());
        List<Integer> ids = result.stream().map(RegisterItem::getId).toList();
        assertTrue(ids.stream().allMatch(java.util.Objects::nonNull), "Every observation must have an id: " + ids);
        assertEquals(4, ids.stream().distinct().count(), "Ids must be distinct: " + ids);
        assertEquals(7, result.get(1).getId(), "Existing ids must be kept");
    }

    /**
     * generateID used to call itself for every row without id before assigning anything, recursing until
     * StackOverflowError ("Error executing action saveRegister" in production).
     */
    @Test
    void testGenerateIdWithNullIdRowsDoesNotRecurse() {
        List<RegisterItem> register = analysisService.getAllObservations();
        register.add(registerItem(null, 1.0));
        register.add(registerItem(7, 2.0));
        register.add(registerItem(null, 3.0));
        var service = new AnalysisServiceImpl(categoryService, profileService, dataHubService) {
            @Override
            public List<RegisterItem> getAllObservations() {
                return dataHubService.getCurrentDataRegister(); // raw register, no repair on read
            }

            Integer nextId() {
                return generateID();
            }
        };

        Integer next = assertDoesNotThrow(service::nextId);

        assertEquals(10, next, "Rows without id take 8 and 9, the next free id is 10");
        assertEquals(List.of(8, 7, 9), register.stream().map(RegisterItem::getId).toList());
    }

    /**
     * Rows without id get the next free ids in list order, so repeated reads agree.
     */
    @Test
    void testMissingIdsAreAssignedDeterministicallyOnRead() {
        List<RegisterItem> register = analysisService.getAllObservations();
        register.add(registerItem(null, 1.0));
        register.add(registerItem(4, 2.0));
        register.add(registerItem(null, 3.0));

        List<Integer> firstRead = analysisService.getAllObservations().stream().map(RegisterItem::getId).toList();
        List<Integer> secondRead = analysisService.getAllObservations().stream().map(RegisterItem::getId).toList();

        assertEquals(List.of(5, 4, 6), firstRead);
        assertEquals(firstRead, secondRead);
    }

    /**
     * A save on a moment that already has a row replaces it, and keeps the stored id when the incoming item has none.
     */
    @Test
    void testSameMomentSaveWithoutIdKeepsStoredId() {
        analysisService.getAllObservations().add(registerItem(5, 10.0));

        RegisterItem incoming = registerItem(null, 10.0);
        incoming.setName("replaced");
        assertTrue(analysisService.saveObservation(incoming));

        List<RegisterItem> result = analysisService.getAllObservations();
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getId());
        assertEquals("replaced", result.get(0).getName());
    }

    /**
     * pushRegister compared stored ids with equals() and threw NPE on a stored row without id.
     */
    @Test
    void testPushRegisterWithNullIdStoredRowDoesNotThrow() {
        List<RegisterItem> register = new ArrayList<>();
        register.add(registerItem(null, 1.0));

        boolean pushed = assertDoesNotThrow(() -> ((AnalysisServiceImpl) analysisService).pushRegister(register, 5.0));
        assertTrue(pushed);
        assertEquals(2, register.size());
    }

    @Test
    void testDeleteByIdWithNullIdRowsInRegister() {
        List<RegisterItem> register = analysisService.getAllObservations();
        register.add(registerItem(null, 1.0));
        register.add(registerItem(9, 2.0));

        assertTrue(analysisService.deleteObservationById(9));
        assertEquals(1, analysisService.getAllObservations().size());
    }

    private static RegisterItem registerItem(Integer id, double videoTime) {
        RegisterItem item = new RegisterItem();
        item.setId(id);
        item.setVideoTime(videoTime);
        // distinct save dates: pushRegister also matches rows by save date
        item.setSaveDate(new java.util.Date((long) (videoTime * 1000)));
        return item;
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
