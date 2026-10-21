package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.Lince1Fixtures;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * A modern project built from the Lince 1 fixtures plus the expected output computed on the same files loaded
 * the legacy way (what the menu exports). "NIVEL DE INMERSIÓN" has sub-categories, which the instrument
 * conversion does not carry, so the selection leaves it out.
 */
final class ExporterFixture {

    static final String HIERARCHICAL = "NIVEL DE INMERSIÓN";

    final LegacyBridge bridge = new LegacyBridge();
    final ILinceProject project = Lince1Fixtures.project(bridge);
    final List<String> selectedIds = project.getObservationTool().stream()
            .filter(c -> !HIERARCHICAL.equals(c.getName()))
            .map(c -> String.valueOf(c.getId()))
            .toList();

    ExportSelection selection() {
        return ExportSelection.of(project, null, selectedIds);
    }

    /**
     * @param export legacy export call on the directly loaded register and the matching legacy criteria
     * @return what the menu would write for the same selection
     */
    String expected(BiFunction<Registro, List<Criterio>, String> export) {
        List<String> names = project.getObservationTool().stream()
                .filter(c -> selectedIds.contains(String.valueOf(c.getId())))
                .map(Criteria::getName).toList();
        Registro registro = Lince1Fixtures.loadLegacy();
        List<Criterio> criterios = Lince1Fixtures.convertibleCriterios().stream()
                .filter(c -> names.contains(c.getNombre())).toList();
        return export.apply(registro, criterios);
    }

    static ExportSelection withoutCriteria() {
        LinceFileProjectWrapper empty = new LinceFileProjectWrapper();
        empty.setObservationTool(new ArrayList<>());
        return ExportSelection.of(empty, null, List.of());
    }

    static ExportSelection withoutObservations(ILinceProject source) {
        LinceFileProjectWrapper project = new LinceFileProjectWrapper();
        project.setObservationTool(source.getObservationTool());
        return ExportSelection.of(project, null, List.of());
    }
}
