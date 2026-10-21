package com.lince.observer.data.transfer;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The data an exporter consumes, resolved from an {@code ExportRequest} against a project (LO-349).
 * Replaces what {@code SelectionPanelComponent} and {@code RootLayoutController.getResearchSelection()} do in
 * the JavaFX menu.
 * <p>
 * <b>Criteria identity.</b> A requested criteria id is matched, after trimming, first against
 * {@code String.valueOf(Criteria.getId())} (the numeric {@code id} that {@code GET /observationTool/getAll}
 * returns to the dashboard), and only when no criteria has that id, against {@code Criteria.getCode()}
 * (exact, case sensitive). Names are never used: they are not unique. Ids that match nothing are reported
 * together in one {@link ExportException} with reason {@link TransferException.Reason#UNKNOWN_CRITERIA}.
 * An empty or null id list selects every criteria in instrument order; an explicit list keeps the request
 * order and ignores duplicates.
 * <p>
 * <b>Register identity.</b> {@code researchId} is matched against {@code LinceRegisterWrapper.getId()} and then
 * against the register's {@code UserProfile.getRegisterCode()} (the value {@code getResearchSelection()} returns
 * today). Null selects the first register, which is the only one in single-observer projects. An unknown id
 * fails with {@link TransferException.Reason#UNKNOWN_RESEARCH}. A project without registers yields no rows.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public final class ExportSelection {

    private final ILinceProject project;
    private final LinceRegisterWrapper register;
    private final List<Criteria> criteria;
    private final boolean allCriteria;

    /**
     * @param project     project to export, never null
     * @param researchId  register/observer id, null for the first register
     * @param criteriaIds criteria ids or codes, null or empty for all
     * @throws ExportException UNKNOWN_RESEARCH or UNKNOWN_CRITERIA listing the offending ids
     */
    public ExportSelection(ILinceProject project, UUID researchId, List<String> criteriaIds) {
        this.project = Objects.requireNonNull(project, "project");
        this.register = resolveRegister(project, researchId);
        this.allCriteria = criteriaIds == null || criteriaIds.isEmpty();
        this.criteria = Collections.unmodifiableList(resolveCriteria(project, criteriaIds));
    }

    public static ExportSelection of(ILinceProject project, UUID researchId, List<String> criteriaIds) {
        return new ExportSelection(project, researchId, criteriaIds);
    }

    /**
     * @return the full project (instrument, all registers, profiles)
     */
    public ILinceProject getProject() {
        return project;
    }

    /**
     * @return the selected register, null when the project has none
     */
    public LinceRegisterWrapper getRegister() {
        return register;
    }

    /**
     * @return id of the selected register, null when the project has none
     */
    public UUID getResearchId() {
        return register == null ? null : register.getId();
    }

    /**
     * @return the observation rows of the selected register, empty when there is none
     */
    public List<RegisterItem> getRows() {
        if (register == null || register.getRegisterData() == null) {
            return List.of();
        }
        return Collections.unmodifiableList(new ArrayList<>(register.getRegisterData()));
    }

    /**
     * @return selected criteria (the project's own instances), all of them when no ids were requested
     */
    public List<Criteria> getCriteria() {
        return criteria;
    }

    /**
     * @return {@code Criteria.getId()} of the selected criteria, in selection order
     */
    public List<Integer> getCriteriaIds() {
        return criteria.stream().map(Criteria::getId).toList();
    }

    /**
     * @return true when the request did not restrict the criteria
     */
    public boolean isAllCriteria() {
        return allCriteria;
    }

    /**
     * For types that need at least one criterion (today's "select at least one criteria" alert).
     *
     * @return this selection
     * @throws ExportException EMPTY_SELECTION when no criteria is selected
     */
    public ExportSelection requireCriteria() {
        if (criteria.isEmpty()) {
            throw new ExportException(TransferException.Reason.EMPTY_SELECTION, "Select at least one criteria");
        }
        return this;
    }

    private static LinceRegisterWrapper resolveRegister(ILinceProject project, UUID researchId) {
        List<LinceRegisterWrapper> registers = project.getRegister() == null ? List.of() : project.getRegister();
        if (researchId == null) {
            return registers.isEmpty() ? null : registers.get(0);
        }
        for (LinceRegisterWrapper candidate : registers) {
            if (researchId.equals(candidate.getId())) {
                return candidate;
            }
        }
        for (LinceRegisterWrapper candidate : registers) {
            if (candidate.getUserProfile() != null && researchId.equals(candidate.getUserProfile().getRegisterCode())) {
                return candidate;
            }
        }
        throw new ExportException(TransferException.Reason.UNKNOWN_RESEARCH,
                "Unknown research id: " + researchId, List.of(researchId.toString()));
    }

    private static List<Criteria> resolveCriteria(ILinceProject project, List<String> criteriaIds) {
        List<Criteria> tool = project.getObservationTool() == null ? List.of() : project.getObservationTool();
        if (criteriaIds == null || criteriaIds.isEmpty()) {
            return new ArrayList<>(tool);
        }
        Map<Criteria, Boolean> selected = new IdentityHashMap<>();
        List<Criteria> result = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        for (String requested : criteriaIds) {
            Criteria match = find(tool, StringUtils.trimToEmpty(requested));
            if (match == null) {
                unknown.add(requested);
            } else if (selected.put(match, Boolean.TRUE) == null) {
                result.add(match);
            }
        }
        if (!unknown.isEmpty()) {
            throw new ExportException(TransferException.Reason.UNKNOWN_CRITERIA,
                    "Unknown criteria ids: " + String.join(", ", unknown), unknown);
        }
        return result;
    }

    private static Criteria find(List<Criteria> tool, String token) {
        if (token.isEmpty()) {
            return null;
        }
        for (Criteria c : tool) {
            if (c.getId() != null && token.equals(String.valueOf(c.getId()))) {
                return c;
            }
        }
        for (Criteria c : tool) {
            if (token.equals(c.getCode())) {
                return c;
            }
        }
        return null;
    }
}
