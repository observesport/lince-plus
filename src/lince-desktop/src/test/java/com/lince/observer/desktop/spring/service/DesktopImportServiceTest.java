package com.lince.observer.desktop.spring.service;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ImportResult;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.transfer.ImportTypeDescriptor;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.transfer.ImportException;
import com.lince.observer.data.transfer.ImportOutcome;
import com.lince.observer.data.transfer.LinceImporter;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.math.service.DataHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class DesktopImportServiceTest {

    private DataHubService dataHubService;
    private CurrentProjectProvider projectProvider;
    private FakeImporter lincePlus;
    private FakeImporter register;
    private FakeImporter hoisan;
    private DesktopImportService service;
    private ILinceProject snapshot;

    @BeforeEach
    void setUp() {
        dataHubService = mock(DataHubService.class);
        projectProvider = mock(CurrentProjectProvider.class);
        snapshot = new LinceFileProjectWrapper();
        when(projectProvider.snapshot()).thenReturn(snapshot);
        lincePlus = new FakeImporter("lince-plus", "xml");
        register = new FakeImporter("lince1-register", "rlince");
        hoisan = new FakeImporter("hoisan", "mdb");
        service = new DesktopImportService(List.of(lincePlus, register, hoisan), projectProvider, dataHubService);
    }

    @Test
    void typesListsEveryImporterDescriptorInOrder() {
        assertEquals(List.of("lince-plus", "lince1-register", "hoisan"),
                service.types().stream().map(ImportTypeDescriptor::id).toList());
        assertTrue(service.type("hoisan").isPresent());
        assertTrue(service.type("nope").isEmpty());
    }

    @Test
    void detectsTheTypeFromTheExtension() {
        ImportResult result = service.importFile("observer.RLINCE", stream(), null, null, null);

        assertEquals("lince1-register", result.type());
        assertEquals(1, register.calls);
        assertEquals(0, lincePlus.calls);
    }

    @Test
    void explicitTypeWinsOverDetection() {
        FakeImporter alsoXml = new FakeImporter("other-xml", "xml");
        service = new DesktopImportService(List.of(lincePlus, alsoXml), projectProvider, dataHubService);

        ImportResult result = service.importFile("project.xml", stream(), "other-xml", ImportScope.FULL, Map.of());

        assertEquals("other-xml", result.type());
        assertEquals(1, alsoXml.calls);
        assertEquals(0, lincePlus.calls);
    }

    @Test
    void explicitTypeAcceptsAFileWithoutExtension() {
        ImportResult result = service.importFile("upload", stream(), "hoisan", null, null);

        assertEquals("hoisan", result.type());
    }

    @Test
    void explicitTypeAcceptsAnExtensionNoImporterClaims() {
        ImportResult result = service.importFile("project.dat", stream(), "lince-plus", null, null);

        assertEquals("lince-plus", result.type());
    }

    @Test
    void unknownExtensionWithoutTypeFailsWithUnknownType() {
        ImportException e = assertThrows(ImportException.class,
                () -> service.importFile("data.dat", stream(), null, null, null));
        assertEquals(TransferException.Reason.UNKNOWN_TYPE, e.getReason());
    }

    @Test
    void missingNameWithoutTypeFailsWithUnknownType() {
        ImportException e = assertThrows(ImportException.class,
                () -> service.importFile(null, stream(), " ", null, null));
        assertEquals(TransferException.Reason.UNKNOWN_TYPE, e.getReason());
    }

    @Test
    void unknownExplicitTypeFailsWithUnknownType() {
        ImportException e = assertThrows(ImportException.class,
                () -> service.importFile("project.xml", stream(), "excel", null, null));
        assertEquals(TransferException.Reason.UNKNOWN_TYPE, e.getReason());
        assertEquals(0, lincePlus.calls);
    }

    @Test
    void explicitTypeContradictingAKnownExtensionFailsWithTypeMismatch() {
        ImportException e = assertThrows(ImportException.class,
                () -> service.importFile("project.xml", stream(), "hoisan", null, null));
        assertEquals(TransferException.Reason.TYPE_MISMATCH, e.getReason());
        assertEquals(0, hoisan.calls);
        verifyNoInteractions(dataHubService);
    }

    @Test
    void nullScopeDefaultsToFullAndNullOptionsToEmpty() {
        ImportResult result = service.importFile("project.xml", stream(), null, null, null);

        assertEquals(ImportScope.FULL, lincePlus.lastScope);
        assertEquals(Map.of(), lincePlus.lastOptions);
        assertEquals(ImportScope.FULL, result.scope());
    }

    @Test
    void passesScopeOptionsStreamAndSnapshotToTheImporter() {
        InputStream in = stream();
        Map<String, String> options = Map.of("k", "v");

        ImportResult result = service.importFile("project.xml", in, null, ImportScope.OBSERVATIONS, options);

        assertSame(snapshot, lincePlus.lastTarget);
        assertSame(in, lincePlus.lastStream);
        assertEquals(ImportScope.OBSERVATIONS, lincePlus.lastScope);
        assertEquals(options, lincePlus.lastOptions);
        assertEquals(ImportScope.OBSERVATIONS, result.scope());
    }

    @Test
    void scopeOnlyAppliesToLincePlusOtherTypesAlwaysImportFull() {
        ImportResult result = service.importFile("observer.rlince", stream(), null, ImportScope.OBSERVATIONS, Map.of());

        assertEquals(ImportScope.FULL, register.lastScope);
        assertEquals(ImportScope.FULL, result.scope());
    }

    @Test
    void mapsTheOutcomeToTheResult() {
        lincePlus.outcome = new ImportOutcome(3, 2, 40, 1, List.of("criteria X not found"));

        ImportResult result = service.importFile("project.xml", stream(), null, null, null);

        assertEquals(new ImportResult("lince-plus", ImportScope.FULL, 3, 2, 40, 1, List.of("criteria X not found")), result);
    }

    @Test
    void appliesTheImportedProjectToTheLiveStateAndReloadsProfiles() {
        Criteria criteria = new Criteria();
        LinceRegisterWrapper newRegister = new LinceRegisterWrapper();
        ResearchProfile profile = new ResearchProfile();
        lincePlus.mutation = target -> {
            target.getObservationTool().add(criteria);
            target.getRegister().add(newRegister);
            target.getProfiles().add(profile);
        };

        service.importFile("project.xml", stream(), null, null, null);

        InOrder order = inOrder(dataHubService);
        order.verify(dataHubService).setCriteria(List.of(criteria));
        order.verify(dataHubService).setAllDataRegister(List.of(newRegister));
        order.verify(dataHubService).setUserData(List.of(profile));
        order.verify(dataHubService).reloadResearchProfileDataFromRegister();
    }

    @Test
    void appliesCopiesSoTheLiveStateDoesNotAliasTheSnapshot() {
        service.importFile("project.xml", stream(), null, null, null);

        verify(dataHubService).setAllDataRegister(argThat(list -> list != snapshot.getRegister()));
        verify(dataHubService).setCriteria(argThat(list -> list != snapshot.getObservationTool()));
    }

    @Test
    void importerFailureLeavesTheLiveStateUntouched() {
        lincePlus.failure = new ImportException(TransferException.Reason.UNREADABLE, "bad xml");

        ImportException e = assertThrows(ImportException.class,
                () -> service.importFile("project.xml", stream(), null, null, null));

        assertEquals(TransferException.Reason.UNREADABLE, e.getReason());
        verify(dataHubService, never()).setAllDataRegister(anyList());
        verify(dataHubService, never()).reloadResearchProfileDataFromRegister();
    }

    private static InputStream stream() {
        return new ByteArrayInputStream("<xml/>".getBytes());
    }

    /**
     * Minimal importer: records its last call and optionally mutates the target.
     */
    static final class FakeImporter implements LinceImporter {
        private final ImportTypeDescriptor descriptor;
        int calls;
        ILinceProject lastTarget;
        InputStream lastStream;
        ImportScope lastScope;
        Map<String, String> lastOptions;
        ImportOutcome outcome = new ImportOutcome(0, 0, 0, 0, List.of());
        java.util.function.Consumer<ILinceProject> mutation = target -> { };
        RuntimeException failure;

        FakeImporter(String id, String extension) {
            this.descriptor = new ImportTypeDescriptor(id, id, List.of(extension), List.of());
        }

        @Override
        public ImportTypeDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public ImportOutcome importInto(ILinceProject target, InputStream in, ImportScope scope, Map<String, String> options) {
            calls++;
            lastTarget = target;
            lastStream = in;
            lastScope = scope;
            lastOptions = options;
            if (failure != null) {
                throw failure;
            }
            mutation.accept(target);
            return outcome;
        }
    }
}
