package com.lince.observer.data.transfer;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportSelectionTest {

    private LinceFileProjectWrapper project;
    private Criteria pase;
    private Criteria zona;
    private Criteria resultado;
    private LinceRegisterWrapper first;
    private LinceRegisterWrapper second;

    @BeforeEach
    void setUp() {
        pase = criteria(1, "Pase", "P");
        zona = criteria(2, "Zona", "Z");
        resultado = criteria(3, "Resultado", "R");
        project = new LinceFileProjectWrapper();
        project.setObservationTool(new ArrayList<>(List.of(pase, zona, resultado)));
        first = register(2);
        second = register(5);
        project.setRegister(new ArrayList<>(List.of(first, second)));
    }

    @Test
    void emptyIdsSelectAllCriteriaInProjectOrder() {
        ExportSelection selection = new ExportSelection(project, first.getId(), List.of());

        assertEquals(List.of(pase, zona, resultado), selection.getCriteria());
        assertSame(project, selection.getProject());
    }

    @Test
    void nullIdsSelectAllCriteria() {
        assertEquals(3, new ExportSelection(project, null, null).getCriteria().size());
    }

    @Test
    void subsetKeepsRequestOrderAndIgnoresDuplicates() {
        ExportSelection selection = new ExportSelection(project, null, List.of("3", "1", "3"));

        assertEquals(List.of(resultado, pase), selection.getCriteria());
    }

    @Test
    void criteriaCanBeSelectedByCodeWhenNoIdMatches() {
        ExportSelection selection = new ExportSelection(project, null, List.of("Z", " 1 "));

        assertEquals(List.of(zona, pase), selection.getCriteria());
    }

    @Test
    void unknownCriteriaIdsAreAllReported() {
        ExportException ex = assertThrows(ExportException.class,
                () -> new ExportSelection(project, null, List.of("1", "99", "nope")));

        assertEquals(ExportException.Reason.UNKNOWN_CRITERIA, ex.getReason());
        assertEquals(List.of("99", "nope"), ex.getOffendingIds());
        assertTrue(ex.getMessage().contains("99"), ex.getMessage());
    }

    @Test
    void researchIdSelectsThatRegister() {
        ExportSelection selection = new ExportSelection(project, second.getId(), List.of());

        assertSame(second, selection.getRegister());
        assertEquals(5, selection.getRows().size());
    }

    @Test
    void researchIdMatchesTheUserProfileRegisterCodeToo() {
        UUID code = UUID.randomUUID();
        second.getUserProfile().setRegisterCode(code);

        assertSame(second, new ExportSelection(project, code, List.of()).getRegister());
    }

    @Test
    void unknownResearchIdIsRejected() {
        UUID unknown = UUID.randomUUID();
        ExportException ex = assertThrows(ExportException.class,
                () -> new ExportSelection(project, unknown, List.of()));

        assertEquals(ExportException.Reason.UNKNOWN_RESEARCH, ex.getReason());
        assertEquals(List.of(unknown.toString()), ex.getOffendingIds());
    }

    @Test
    void nullResearchWithOneRegisterUsesIt() {
        project.setRegister(new ArrayList<>(List.of(second)));

        ExportSelection selection = new ExportSelection(project, null, List.of("2"));

        assertSame(second, selection.getRegister());
        assertEquals(5, selection.getRows().size());
        assertEquals(List.of(zona), selection.getCriteria());
    }

    @Test
    void nullResearchWithSeveralRegistersUsesTheFirst() {
        assertSame(first, new ExportSelection(project, null, List.of()).getRegister());
    }

    @Test
    void projectWithoutRegistersExportsNoRows() {
        project.setRegister(new ArrayList<>());

        ExportSelection selection = new ExportSelection(project, null, List.of());

        assertEquals(null, selection.getRegister());
        assertEquals(List.of(), selection.getRows());
    }

    @Test
    void requireCriteriaFailsOnEmptyInstrument() {
        project.setObservationTool(new ArrayList<>());
        ExportSelection selection = new ExportSelection(project, null, List.of());

        ExportException ex = assertThrows(ExportException.class, selection::requireCriteria);
        assertEquals(ExportException.Reason.EMPTY_SELECTION, ex.getReason());
    }

    private static Criteria criteria(int id, String name, String code) {
        Criteria criteria = new Criteria(id, name);
        criteria.setCode(code);
        Category category = new Category(id * 10 + 1, name + " 1", id);
        category.setCode(code + "1");
        criteria.setInnerCategories(new LinkedList<>(List.of(category)));
        return criteria;
    }

    private static LinceRegisterWrapper register(int rows) {
        LinceRegisterWrapper wrapper = new LinceRegisterWrapper();
        List<RegisterItem> items = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            items.add(new RegisterItem((double) i));
        }
        wrapper.setRegisterData(items);
        return wrapper;
    }
}
