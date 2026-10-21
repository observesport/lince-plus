package com.lince.observer.desktop.spring.service;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ExportRequest;
import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.LinceExporter;
import com.lince.observer.data.transfer.TransferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DesktopExportServiceTest {

    private CurrentProjectProvider projectProvider;
    private ILinceProject project;
    private LinceRegisterWrapper first;
    private LinceRegisterWrapper second;
    private Criteria criteria;
    private FakeExporter csv;
    private FakeExporter lince1;
    private DesktopExportService service;

    @BeforeEach
    void setUp() {
        first = register();
        second = register();
        criteria = new Criteria();
        criteria.setId(7);
        criteria.setCode("C7");
        project = new LinceFileProjectWrapper();
        project.setRegister(new ArrayList<>(List.of(first, second)));
        project.setObservationTool(new ArrayList<>(List.of(criteria)));
        projectProvider = mock(CurrentProjectProvider.class);
        when(projectProvider.current()).thenReturn(project);
        csv = new FakeExporter("csv", true);
        lince1 = new FakeExporter("lince1-register", false);
        service = new DesktopExportService(List.of(csv, lince1), projectProvider);
    }

    @Test
    void typesListsEveryExporterDescriptorInOrder() {
        assertEquals(List.of("csv", "lince1-register"),
                service.types().stream().map(ExportTypeDescriptor::id).toList());
        assertTrue(service.type("csv").isPresent());
        assertTrue(service.type("nope").isEmpty());
    }

    @Test
    void unknownTypeFailsWithUnknownType() {
        ExportException e = assertThrows(ExportException.class,
                () -> service.export(new ExportRequest("excel", null, null, null)));
        assertEquals(TransferException.Reason.UNKNOWN_TYPE, e.getReason());
    }

    @Test
    void nullRequestFailsWithUnknownType() {
        ExportException e = assertThrows(ExportException.class, () -> service.export(null));
        assertEquals(TransferException.Reason.UNKNOWN_TYPE, e.getReason());
    }

    @Test
    void nullResearchUsesTheProviderSelection() {
        when(projectProvider.currentResearchId()).thenReturn(second.getUserProfile().getRegisterCode());

        service.export(new ExportRequest("csv", null, null, null));

        assertSame(second, csv.lastSelection.getRegister());
        assertSame(project, csv.lastSelection.getProject());
    }

    @Test
    void nullResearchWithoutProviderSelectionUsesTheFirstRegister() {
        when(projectProvider.currentResearchId()).thenReturn(null);

        service.export(new ExportRequest("csv", null, null, null));

        assertSame(first, csv.lastSelection.getRegister());
    }

    @Test
    void staleProviderSelectionFallsBackToTheFirstRegister() {
        when(projectProvider.currentResearchId()).thenReturn(UUID.randomUUID());

        service.export(new ExportRequest("csv", null, null, null));

        assertSame(first, csv.lastSelection.getRegister());
    }

    @Test
    void explicitResearchIsUsedAndUnknownOnesFail() {
        service.export(new ExportRequest("csv", second.getId(), null, null));
        assertSame(second, csv.lastSelection.getRegister());

        UUID unknown = UUID.randomUUID();
        ExportException e = assertThrows(ExportException.class,
                () -> service.export(new ExportRequest("csv", unknown, null, null)));
        assertEquals(TransferException.Reason.UNKNOWN_RESEARCH, e.getReason());
        assertEquals(List.of(unknown.toString()), e.getOffendingIds());
    }

    @Test
    void criteriaIdsAreResolvedAndUnknownOnesFail() {
        service.export(new ExportRequest("csv", null, List.of("C7"), null));
        assertEquals(List.of(7), csv.lastSelection.getCriteriaIds());

        ExportException e = assertThrows(ExportException.class,
                () -> service.export(new ExportRequest("csv", null, List.of("99"), null)));
        assertEquals(TransferException.Reason.UNKNOWN_CRITERIA, e.getReason());
        assertEquals(List.of("99"), e.getOffendingIds());
    }

    @Test
    void emptySelectionFailsWhenTheTypeSupportsCriteriaSelection() {
        project.setObservationTool(new ArrayList<>());

        ExportException e = assertThrows(ExportException.class,
                () -> service.export(new ExportRequest("csv", null, null, null)));
        assertEquals(TransferException.Reason.EMPTY_SELECTION, e.getReason());
        assertNull(csv.lastSelection);
    }

    @Test
    void emptySelectionIsAllowedWhenTheTypeDoesNotSelectCriteria() {
        project.setObservationTool(new ArrayList<>());

        List<ExportedFile> files = service.export(new ExportRequest("lince1-register", null, null, null));

        assertEquals(1, files.size());
    }

    @Test
    void passesOptionsThroughAndReturnsTheExporterFiles() {
        Map<String, String> options = Map.of("separator", "semicolon");
        csv.result = List.of(new ExportedFile("a.txt", "text/plain", new byte[]{1}),
                new ExportedFile("vvt.vvt", "text/plain", new byte[]{2}));

        List<ExportedFile> files = service.export(new ExportRequest("csv", null, null, options));

        assertEquals(options, csv.lastOptions);
        assertEquals(csv.result, files);
    }

    private static LinceRegisterWrapper register() {
        LinceRegisterWrapper register = new LinceRegisterWrapper();
        register.setId(UUID.randomUUID());
        register.getUserProfile().setRegisterCode(UUID.randomUUID());
        return register;
    }

    static final class FakeExporter implements LinceExporter {
        private final ExportTypeDescriptor descriptor;
        ExportSelection lastSelection;
        Map<String, String> lastOptions;
        List<ExportedFile> result;

        FakeExporter(String id, boolean criteriaSelection) {
            this.descriptor = new ExportTypeDescriptor(id, id, "txt", "text/plain", criteriaSelection, false, List.of());
            this.result = List.of(new ExportedFile(id + ".txt", "text/plain", new byte[]{42}));
        }

        @Override
        public ExportTypeDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public List<ExportedFile> export(ExportSelection selection, Map<String, String> options) {
            lastSelection = selection;
            lastOptions = options;
            return result;
        }
    }
}
