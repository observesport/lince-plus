package com.lince.observer.data.transfer.importers;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.transfer.ImportTypeDescriptor;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.user.UserProfile;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.transfer.ImportException;
import com.lince.observer.data.transfer.ImportOutcome;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.TransferFixtures;
import com.lince.observer.data.util.LinceFileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinceProjectImporterTest {

    private LinceProjectImporter importer;

    @BeforeEach
    void setUp() {
        importer = new LinceProjectImporter();
    }

    @Test
    void descriptorDetectsXml() {
        ImportTypeDescriptor descriptor = importer.descriptor();
        assertEquals("lince-plus", descriptor.id());
        assertEquals(List.of("xml"), descriptor.extensions());
        assertTrue(importer.supports("project.XML"));
        assertFalse(importer.supports("register.rlince"));
    }

    @Test
    void fullReplacesInstrumentAndRegistersAndRebuildsUserProfiles() {
        LinceFileProjectWrapper source = TransferFixtures.load(TransferFixtures.MULTIPLE);
        LinceFileProjectWrapper target = projectWith(List.of(criteria(99, "OLD", "OLD-1")), registerOf("OLD-1"));
        ResearchProfile research = new ResearchProfile();
        research.setFps(60);
        research.getUserProfiles().add(new UserProfile());
        target.getProfiles().add(research);

        ImportOutcome outcome = importer.importInto(target, TransferFixtures.open(TransferFixtures.MULTIPLE),
                ImportScope.FULL, Map.of());

        assertEquals(codes(source.getObservationTool()), codes(target.getObservationTool()));
        assertEquals(ids(source.getRegister()), ids(target.getRegister()));
        assertEquals(1, target.getProfiles().size());
        assertSame(research, target.getProfiles().get(0), "the loaded research profile is kept, as the FX menu does");
        assertEquals(60, target.getProfiles().get(0).getFps());
        List<UserProfile> users = target.getProfiles().get(0).getUserProfiles();
        assertEquals(source.getRegister().size(), users.size());
        for (int i = 0; i < users.size(); i++) {
            LinceRegisterWrapper register = target.getRegister().get(i);
            assertSame(register.getUserProfile(), users.get(i));
            assertEquals(register.getRegisterData().size(), users.get(i).getRegisterAmount());
        }
        assertEquals(source.getObservationTool().size(), outcome.criteriaCount());
        assertEquals(source.getRegister().size(), outcome.registerCount());
        assertEquals(observations(source), outcome.observationCount());
        assertEquals(1, outcome.profileCount());
        assertTrue(outcome.warnings().isEmpty());
    }

    @Test
    void fullOnEmptyProjectCreatesResearchProfileAndIgnoresVideos() {
        LinceFileProjectWrapper target = new LinceFileProjectWrapper();
        target.setProfiles(null);

        ImportOutcome outcome = importer.importInto(target, TransferFixtures.open(TransferFixtures.SINGLE),
                ImportScope.FULL, Map.of());

        assertEquals(1, target.getProfiles().size());
        assertEquals(1, outcome.profileCount());
        assertTrue(target.getVideoPlayList().isEmpty());
        assertTrue(target.getYoutubeVideoPlayList().isEmpty());
        assertEquals(TransferFixtures.load(TransferFixtures.SINGLE).getObservationTool().size(), outcome.criteriaCount());
    }

    @Test
    void observationsKeepInstrumentAndAppendRegistersWithFreshIds() {
        LinceFileProjectWrapper source = TransferFixtures.load(TransferFixtures.MULTIPLE);
        LinceFileProjectWrapper target = TransferFixtures.load(TransferFixtures.MULTIPLE);
        List<Criteria> toolBefore = target.getObservationTool();
        int registersBefore = target.getRegister().size();
        int usersBefore = target.getProfiles().get(0).getUserProfiles().size();
        Set<UUID> idsBefore = new HashSet<>(ids(target.getRegister()));

        ImportOutcome outcome = importer.importInto(target, TransferFixtures.open(TransferFixtures.MULTIPLE),
                ImportScope.OBSERVATIONS, Map.of());

        assertSame(toolBefore, target.getObservationTool());
        assertEquals(registersBefore + source.getRegister().size(), target.getRegister().size());
        List<LinceRegisterWrapper> added = target.getRegister().subList(registersBefore, target.getRegister().size());
        for (LinceRegisterWrapper register : added) {
            assertFalse(idsBefore.contains(register.getId()), "imported registers get a new id");
            assertEquals(register.getId(), register.getUserProfile().getRegisterCode());
        }
        assertEquals(usersBefore + source.getRegister().size(), target.getProfiles().get(0).getUserProfiles().size());
        assertEquals(observations(source), outcome.observationCount());
        assertEquals(target.getRegister().size(), outcome.registerCount());
        assertEquals(toolBefore.size(), outcome.criteriaCount());
        assertTrue(outcome.warnings().isEmpty(), outcome.warnings().toString());
    }

    @Test
    void observationsWithDifferentInstrumentImportWhatMapsAndWarn() throws Exception {
        LinceFileProjectWrapper source = projectWith(List.of(criteria(1, "C1", "A", "B")),
                registerOf("A", "A,X", "Y"));
        LinceFileProjectWrapper target = projectWith(List.of(criteria(5, "C1", "A", "B")));

        ImportOutcome outcome = importer.importInto(target, toStream(source), ImportScope.OBSERVATIONS, Map.of());

        assertEquals(1, target.getRegister().size());
        List<RegisterItem> items = target.getRegister().get(0).getRegisterData();
        assertEquals(2, items.size(), "the row with only unknown categories is skipped");
        assertEquals(List.of("A"), categoryCodes(items.get(0)));
        assertEquals(List.of("A"), categoryCodes(items.get(1)), "unknown category X is dropped");
        assertEquals(2, outcome.observationCount());
        assertEquals(3, outcome.warnings().size(), outcome.warnings().toString());
        assertTrue(outcome.warnings().stream().anyMatch(w -> w.contains("'X'")));
        assertTrue(outcome.warnings().stream().anyMatch(w -> w.contains("'Y'")));
        assertTrue(outcome.warnings().stream().anyMatch(w -> w.contains("1 observation")));
    }

    @Test
    void observationsKeepRowsThatHadNoCategory() throws Exception {
        LinceRegisterWrapper register = registerOf("A");
        register.getRegisterData().add(new RegisterItem(5.0));
        LinceFileProjectWrapper source = projectWith(List.of(criteria(1, "C1", "A")), register);
        LinceFileProjectWrapper target = projectWith(List.of(criteria(1, "C1", "A")));

        ImportOutcome outcome = importer.importInto(target, toStream(source), ImportScope.OBSERVATIONS, Map.of());

        assertEquals(2, outcome.observationCount(), "addDataRegister kept rows without categories");
        assertTrue(outcome.warnings().isEmpty(), outcome.warnings().toString());
    }

    @Test
    void observationsIntoAnUnmodifiableRegisterListWork() throws Exception {
        LinceFileProjectWrapper target = TransferFixtures.load(TransferFixtures.MULTIPLE);
        int before = target.getRegister().size();
        target.setRegister(List.copyOf(target.getRegister()));

        importer.importInto(target, TransferFixtures.open(TransferFixtures.MULTIPLE), ImportScope.OBSERVATIONS, Map.of());

        assertTrue(target.getRegister().size() > before);
    }

    @Test
    void observationsUseTheLoadedInstrumentCategories() throws Exception {
        LinceFileProjectWrapper source = projectWith(List.of(criteria(1, "C1", "A", "B")), registerOf("A,B"));
        Criteria loaded = criteria(5, "C1", "A", "B");
        LinceFileProjectWrapper target = projectWith(List.of(loaded));

        importer.importInto(target, toStream(source), ImportScope.OBSERVATIONS, Map.of());

        List<Category> row = target.getRegister().get(0).getRegisterData().get(0).getRegister();
        assertSame(loaded.getInnerCategories().get(0), row.get(0), "category A is the loaded instrument's instance");
        assertSame(loaded.getInnerCategories().get(1), row.get(1));
    }

    @Test
    void observationsKeepInformationNodeValues() throws Exception {
        Criteria info = new Criteria(7, "Notes");
        info.setCode("INFO");
        info.setInformationNode(true);
        LinceFileProjectWrapper source = projectWith(List.of(info), registerOf("INFO"));
        LinceFileProjectWrapper target = projectWith(List.of(info));

        source.getRegister().get(0).getRegisterData().get(0).getRegister().get(0).setNodeInformation("free text");

        ImportOutcome outcome = importer.importInto(target, toStream(source), ImportScope.OBSERVATIONS, Map.of());

        assertEquals(1, outcome.observationCount());
        assertTrue(outcome.warnings().isEmpty());
        Category value = target.getRegister().get(0).getRegisterData().get(0).getRegister().get(0);
        assertEquals("free text", value.getNodeInformation(), "the observed value of an information node is kept");
    }

    @Test
    void observationsWithoutInstrumentFail() {
        LinceFileProjectWrapper target = new LinceFileProjectWrapper();

        ImportException e = assertThrows(ImportException.class, () -> importer.importInto(target,
                TransferFixtures.open(TransferFixtures.SINGLE), ImportScope.OBSERVATIONS, Map.of()));

        assertEquals(TransferException.Reason.INSTRUMENT_REQUIRED, e.getReason());
        assertTrue(target.getRegister().isEmpty());
    }

    @Test
    void fullWithNoInstrumentAndNoObservationsIsRefusedAndLeavesTheTargetUntouched() throws Exception {
        LinceFileProjectWrapper target = TransferFixtures.load(TransferFixtures.SINGLE);
        List<String> tool = codes(target.getObservationTool());
        List<UUID> registers = ids(target.getRegister());
        int rows = observations(target);
        LinceFileProjectWrapper emptyRegisters = projectWith(List.of(), new LinceRegisterWrapper(), new LinceRegisterWrapper());

        for (ByteArrayInputStream content : List.of(
                new ByteArrayInputStream("<linceProject/>".getBytes(StandardCharsets.UTF_8)),
                toStream(emptyRegisters))) {
            ImportException e = assertThrows(ImportException.class,
                    () -> importer.importInto(target, content, ImportScope.FULL, Map.of()));
            assertEquals(TransferException.Reason.EMPTY_PROJECT, e.getReason());
            assertEquals("The file holds no instrument and no observations", e.getMessage());
        }

        assertEquals(tool, codes(target.getObservationTool()));
        assertEquals(registers, ids(target.getRegister()));
        assertEquals(rows, observations(target));
    }

    @Test
    void fullWithInstrumentButNoObservationsIsAccepted() throws Exception {
        LinceFileProjectWrapper target = TransferFixtures.load(TransferFixtures.SINGLE);
        LinceFileProjectWrapper source = projectWith(List.of(criteria(7, "NEW", "NEW-1")), new LinceRegisterWrapper());

        ImportOutcome outcome = importer.importInto(target, toStream(source), ImportScope.FULL, Map.of());

        assertEquals(List.of("NEW"), codes(target.getObservationTool()));
        assertEquals(0, observations(target));
        assertEquals(1, outcome.criteriaCount());
    }

    @Test
    void observationsFromAnEmptyFileImportNothingWithoutError() {
        LinceFileProjectWrapper target = TransferFixtures.load(TransferFixtures.SINGLE);
        int rows = observations(target);

        ImportOutcome outcome = importer.importInto(target,
                new ByteArrayInputStream("<linceProject/>".getBytes(StandardCharsets.UTF_8)), ImportScope.OBSERVATIONS, Map.of());

        assertEquals(rows, observations(target));
        assertEquals(0, outcome.observationCount());
        assertTrue(outcome.warnings().isEmpty());
    }

    @Test
    void unreadableContentFails() {
        LinceFileProjectWrapper target = TransferFixtures.load(TransferFixtures.SINGLE);
        for (String content : List.of("not xml", "<other/>", "<?xml version=\"1.0\"?><!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]><linceProject>&e;</linceProject>")) {
            ImportException e = assertThrows(ImportException.class, () -> importer.importInto(target,
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), ImportScope.FULL, Map.of()));
            assertEquals(TransferException.Reason.UNREADABLE, e.getReason(), content);
        }
        assertNotEquals(0, target.getObservationTool().size(), "target untouched on failure");
    }

    // ---- helpers

    private static ByteArrayInputStream toStream(LinceFileProjectWrapper project) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new LinceFileHelper().writeProject(out, project);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private static LinceFileProjectWrapper projectWith(List<Criteria> tool, LinceRegisterWrapper... registers) {
        LinceFileProjectWrapper project = new LinceFileProjectWrapper();
        project.setObservationTool(new ArrayList<>(tool));
        project.setRegister(new ArrayList<>(List.of(registers)));
        return project;
    }

    private static Criteria criteria(int id, String code, String... categoryCodes) {
        Criteria criteria = new Criteria(id, code);
        criteria.setCode(code);
        LinkedList<Category> categories = new LinkedList<>();
        int next = id * 10;
        for (String categoryCode : categoryCodes) {
            Category category = new Category(++next, categoryCode, id);
            category.setCode(categoryCode);
            categories.add(category);
        }
        criteria.setInnerCategories(categories);
        return criteria;
    }

    /**
     * One register with one row per argument; each argument is a comma separated list of category codes.
     */
    private static LinceRegisterWrapper registerOf(String... rows) {
        LinceRegisterWrapper register = new LinceRegisterWrapper();
        double time = 1;
        int id = 1000;
        for (String row : rows) {
            RegisterItem item = new RegisterItem(time++);
            for (String code : row.split(",")) {
                Category category = new Category(++id, code, 0);
                category.setCode(code);
                item.pushCategory(category);
            }
            register.getRegisterData().add(item);
        }
        return register;
    }

    private static List<String> categoryCodes(RegisterItem item) {
        return item.getRegister().stream().map(Category::getCode).toList();
    }

    private static List<String> codes(List<Criteria> tool) {
        return tool.stream().map(Criteria::getCode).toList();
    }

    private static List<UUID> ids(List<LinceRegisterWrapper> registers) {
        return registers.stream().map(LinceRegisterWrapper::getId).toList();
    }

    private static int observations(LinceFileProjectWrapper project) {
        return project.getRegister().stream().mapToInt(r -> r.getRegisterData().size()).sum();
    }
}
