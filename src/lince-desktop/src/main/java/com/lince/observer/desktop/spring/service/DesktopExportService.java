package com.lince.observer.desktop.spring.service;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.LinceQualifier.DesktopQualifier;
import com.lince.observer.data.bean.transfer.ExportRequest;
import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.service.ExportService;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.LinceExporter;
import com.lince.observer.data.transfer.TransferException.Reason;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Desktop edition of {@link ExportService} (LO-349) over the registered {@link LinceExporter} beans.
 * <p>
 * Resolves the loaded project ({@link CurrentProjectProvider#current()}) into an {@link ExportSelection}. A request
 * without {@code researchId} uses the default observer of {@link CurrentProjectProvider#currentResearchId()}, and
 * the first register when that observer has no register. Types with
 * {@link ExportTypeDescriptor#supportsCriteriaSelection()} need at least one criterion ({@code EMPTY_SELECTION}).
 * Zipping several files is left to the controller.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Service
@DesktopQualifier
public class DesktopExportService implements ExportService {

    private static final Logger log = LoggerFactory.getLogger(DesktopExportService.class);

    private final List<LinceExporter> exporters;
    private final CurrentProjectProvider projectProvider;

    public DesktopExportService(List<LinceExporter> exporters, CurrentProjectProvider projectProvider) {
        this.exporters = exporters == null ? List.of() : List.copyOf(exporters);
        this.projectProvider = projectProvider;
    }

    @Override
    public List<ExportTypeDescriptor> types() {
        return exporters.stream().map(LinceExporter::descriptor).toList();
    }

    @Override
    public List<ExportedFile> export(ExportRequest request) {
        String type = request == null ? null : StringUtils.trimToNull(request.type());
        LinceExporter exporter = exporters.stream()
                .filter(candidate -> type != null && type.equalsIgnoreCase(candidate.descriptor().id()))
                .findFirst()
                .orElseThrow(() -> new ExportException(Reason.UNKNOWN_TYPE, "Unknown export type: " + type));

        ExportSelection selection = select(projectProvider.current(), request);
        if (exporter.descriptor().supportsCriteriaSelection()) {
            selection.requireCriteria();
        }
        log.info("Exporting {} with {} criteria", type, selection.getCriteria().size());
        return exporter.export(selection, request.options());
    }

    private ExportSelection select(ILinceProject project, ExportRequest request) {
        if (request.researchId() != null) {
            return new ExportSelection(project, request.researchId(), request.criteriaIds());
        }
        UUID defaultResearch = projectProvider.currentResearchId();
        if (defaultResearch != null) {
            try {
                return new ExportSelection(project, defaultResearch, request.criteriaIds());
            } catch (ExportException e) {
                if (e.getReason() != Reason.UNKNOWN_RESEARCH) {
                    throw e;
                }
                log.warn("Default observer {} has no register, exporting the first one", defaultResearch);
            }
        }
        return new ExportSelection(project, null, request.criteriaIds());
    }
}
