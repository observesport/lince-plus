package com.lince.observer.data.transfer.importers;

import com.lince.observer.data.ILinceProject;
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
import com.lince.observer.data.transfer.LinceImporter;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.util.LinceFileHelper;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Imports a Lince PLUS project file ({@code .xml}) into a loaded project (LO-349).
 * <ul>
 *     <li>{@link ImportScope#FULL} ("Import Lince PLUS project"): replaces the instrument and the registers with the
 *     file's ones and rebuilds the user profiles of the first research profile from the registers, as the former
 *     {@code LinceFileHelperFx.addBaseProjectInfo} followed by {@code reloadResearchProfileDataFromRegister()} did
 *     (this class replaces it).
 *     The research profile itself (fps, names) is kept; videos are never imported. A file with no criteria and no
 *     observations is refused ({@code EMPTY_PROJECT}) instead of emptying the loaded project.</li>
 *     <li>{@link ImportScope#OBSERVATIONS} ("Import observer"): keeps the instrument and appends the file's registers
 *     with fresh ids, registering each observer in the first research profile, as the former
 *     {@code LinceFileHelperFx.addObservations} did through {@code DataHubServiceBase.addDataRegister}.
 *     Rows reference the loaded instrument's categories. Categories whose code is not in it are dropped and reported
 *     as warnings; a row whose categories were all dropped is skipped, a row that had none is kept.</li>
 * </ul>
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Component
public class LinceProjectImporter implements LinceImporter {

    public static final String TYPE_ID = "lince-plus";

    private static final ImportTypeDescriptor DESCRIPTOR =
            new ImportTypeDescriptor(TYPE_ID, "Lince PLUS project", List.of("xml"), List.of());

    private final LinceFileHelper fileHelper = new LinceFileHelper();

    @Override
    public ImportTypeDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * @param in project XML; not closed
     * @return the parsed project
     * @throws ImportException UNREADABLE when the content is not a Lince PLUS project
     */
    public LinceFileProjectWrapper read(InputStream in) {
        try {
            return fileHelper.readProject(in);
        } catch (JAXBException | RuntimeException e) {
            throw new ImportException(TransferException.Reason.UNREADABLE, "Not a readable Lince PLUS project", e);
        }
    }

    @Override
    public ImportOutcome importInto(ILinceProject target, InputStream in, ImportScope scope, Map<String, String> options) {
        LinceFileProjectWrapper source = read(in);
        return merge(target, source, scope);
    }

    /**
     * Merges an already parsed project into {@code target}.
     *
     * @param target project receiving the data
     * @param source parsed file
     * @param scope  what to merge
     * @return counts and warnings
     * @throws ImportException INSTRUMENT_REQUIRED for {@link ImportScope#OBSERVATIONS} into a project without instrument;
     *                         EMPTY_PROJECT for {@link ImportScope#FULL} from a source with no criteria and no register
     *                         rows, before the target is touched
     */
    public ImportOutcome merge(ILinceProject target, ILinceProject source, ImportScope scope) {
        if (scope == ImportScope.OBSERVATIONS) {
            return mergeObservations(target, source);
        }
        return mergeFull(target, source);
    }

    private ImportOutcome mergeFull(ILinceProject target, ILinceProject source) {
        List<LinceRegisterWrapper> registers = new ArrayList<>(nonNull(source.getRegister()));
        if (nonNull(source.getObservationTool()).isEmpty()
                && registers.stream().mapToInt(LinceProjectImporter::rows).sum() == 0) {
            throw new ImportException(TransferException.Reason.EMPTY_PROJECT,
                    "The file holds no instrument and no observations");
        }
        target.setObservationTool(new ArrayList<>(nonNull(source.getObservationTool())));
        target.setRegister(registers);
        ResearchProfile research = firstResearchProfile(target);
        research.getUserProfiles().clear();
        for (LinceRegisterWrapper register : registers) {
            register.getUserProfile().setRegisterAmount(rows(register));
            research.getUserProfiles().add(register.getUserProfile());
        }
        int observations = registers.stream().mapToInt(LinceProjectImporter::rows).sum();
        return ImportOutcome.of(target, observations, List.of());
    }

    private ImportOutcome mergeObservations(ILinceProject target, ILinceProject source) {
        List<Criteria> tool = nonNull(target.getObservationTool());
        if (tool.isEmpty()) {
            throw new ImportException(TransferException.Reason.INSTRUMENT_REQUIRED,
                    "The loaded project has no observation instrument: import the full project first");
        }
        Map<String, Category> instrumentCategories = instrumentCategories(tool);
        Set<String> informationNodes = informationNodeCodes(tool);
        Map<String, Integer> unknownCodes = new LinkedHashMap<>();
        int skippedRows = 0;
        int observations = 0;
        List<LinceRegisterWrapper> incoming = nonNull(source.getRegister());
        for (LinceRegisterWrapper register : incoming) {
            List<RegisterItem> kept = new ArrayList<>();
            for (RegisterItem item : nonNull(register.getRegisterData())) {
                List<Category> mapped = new ArrayList<>();
                for (Category category : nonNull(item.getRegister())) {
                    String code = category == null ? null : category.getCode();
                    if (instrumentCategories.containsKey(code)) {
                        mapped.add(instrumentCategories.get(code));
                    } else if (informationNodes.contains(code)) {
                        // the observed value lives on this object, so it is kept as read
                        mapped.add(category);
                    } else {
                        unknownCodes.merge(category == null ? "null" : String.valueOf(category.getCode()), 1, Integer::sum);
                    }
                }
                if (mapped.isEmpty() && !nonNull(item.getRegister()).isEmpty()) {
                    skippedRows++;
                } else {
                    item.setRegister(mapped);
                    kept.add(item);
                }
            }
            register.setRegisterData(kept);
            observations += kept.size();
        }
        List<LinceRegisterWrapper> targetRegisters = new ArrayList<>(nonNull(target.getRegister()));
        for (LinceRegisterWrapper register : incoming) {
            register.setId(UUID.randomUUID());
            register.getUserProfile().setKey(UUID.randomUUID());
            register.getUserProfile().setRegisterCode(register.getId());
            addObserver(target, register.getUserProfile());
        }
        targetRegisters.addAll(incoming);
        target.setRegister(targetRegisters);
        return ImportOutcome.of(target, observations, warnings(unknownCodes, skippedRows));
    }

    /**
     * As {@code DataHubServiceBase.addObserver}: the observer goes to the first research profile, if there is one.
     * Its key was just regenerated, so the collision check of the original is not needed.
     */
    private static void addObserver(ILinceProject target, UserProfile profile) {
        List<ResearchProfile> profiles = nonNull(target.getProfiles());
        if (profiles.isEmpty()) {
            return;
        }
        ResearchProfile research = profiles.get(0);
        if (research.getUserProfiles() == null) {
            research.setUserProfiles(new ArrayList<>());
        }
        research.getUserProfiles().add(profile);
    }

    /**
     * Same as {@code DataHubServiceBase.getResearchProfile}: the first research profile, created when missing.
     */
    private static ResearchProfile firstResearchProfile(ILinceProject target) {
        if (target.getProfiles() == null) {
            target.setProfiles(new ArrayList<>());
        }
        if (target.getProfiles().isEmpty()) {
            target.getProfiles().add(new ResearchProfile());
        }
        ResearchProfile research = target.getProfiles().get(0);
        if (research.getUserProfiles() == null) {
            research.setUserProfiles(new ArrayList<>());
        }
        return research;
    }

    /**
     * @return the loaded instrument's categories by code, so imported rows reference them instead of the file's copies
     */
    private static Map<String, Category> instrumentCategories(List<Criteria> tool) {
        Map<String, Category> categories = new HashMap<>();
        for (Criteria criteria : tool) {
            for (Category category : nonNull(criteria.getInnerCategories())) {
                if (StringUtils.isNotEmpty(category.getCode())) {
                    categories.putIfAbsent(category.getCode(), category);
                }
            }
        }
        return categories;
    }

    /**
     * @return codes of information-node criteria: a row carries their observed value as a category with the criteria
     * code (see {@code LinceCsvExporter.handleCriteriaColumn})
     */
    private static Set<String> informationNodeCodes(List<Criteria> tool) {
        Set<String> codes = new HashSet<>();
        for (Criteria criteria : tool) {
            if (criteria.isInformationNode() && StringUtils.isNotEmpty(criteria.getCode())) {
                codes.add(criteria.getCode());
            }
        }
        return codes;
    }

    private static List<String> warnings(Map<String, Integer> unknownCodes, int skippedRows) {
        List<String> warnings = new ArrayList<>();
        unknownCodes.forEach((code, count) -> warnings.add(String.format(
                "Category '%s' is not in the loaded instrument: dropped from %d observation%s",
                code, count, count == 1 ? "" : "s")));
        if (skippedRows > 0) {
            warnings.add(String.format("Skipped %d observation%s: none of their categories is in the loaded instrument",
                    skippedRows, skippedRows == 1 ? "" : "s"));
        }
        return warnings;
    }

    private static int rows(LinceRegisterWrapper register) {
        return register == null || register.getRegisterData() == null ? 0 : register.getRegisterData().size();
    }

    private static <T> List<T> nonNull(List<T> list) {
        return list == null ? List.of() : list;
    }
}
