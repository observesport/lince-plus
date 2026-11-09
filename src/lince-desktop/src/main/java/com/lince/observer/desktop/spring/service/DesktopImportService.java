package com.lince.observer.desktop.spring.service;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.LinceQualifier.DesktopQualifier;
import com.lince.observer.data.bean.transfer.ImportResult;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.transfer.ImportTypeDescriptor;
import com.lince.observer.data.service.ImportService;
import com.lince.observer.data.transfer.ImportException;
import com.lince.observer.data.transfer.ImportOutcome;
import com.lince.observer.data.transfer.LinceImporter;
import com.lince.observer.data.transfer.TransferException.Reason;
import com.lince.observer.data.transfer.importers.LinceProjectImporter;
import com.lince.observer.math.service.DataHubService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Desktop edition of {@link ImportService} (LO-349) over the registered {@link LinceImporter} beans.
 * <p>
 * The importer works on a {@link CurrentProjectProvider#snapshot() snapshot} of the loaded project; on success the
 * snapshot's instrument, registers and profiles replace the live ones in {@link DataHubService} and the research
 * profiles are rebuilt from the registers, as the JavaFX "Import Lince PLUS project" entry did. A failed import
 * leaves the live project untouched.
 * <p>
 * Type resolution: explicit {@code type} (unknown id: {@code UNKNOWN_TYPE}; its importer must read the file
 * extension when another importer claims it: {@code TYPE_MISMATCH}) → first importer whose
 * {@link LinceImporter#supports(String)} accepts the file name → {@code UNKNOWN_TYPE}.
 * <p>
 * The scope only applies to {@code lince-plus}; every other type always imports {@link ImportScope#FULL}.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Service
@DesktopQualifier
public class DesktopImportService implements ImportService {

    private static final Logger log = LoggerFactory.getLogger(DesktopImportService.class);

    /** Longest client file name repeated in an error message. */
    static final int MAX_ECHOED_NAME = 80;

    private final List<LinceImporter> importers;
    private final CurrentProjectProvider projectProvider;
    private final DataHubService dataHubService;

    public DesktopImportService(List<LinceImporter> importers, CurrentProjectProvider projectProvider,
                                DataHubService dataHubService) {
        this.importers = importers == null ? List.of() : List.copyOf(importers);
        this.projectProvider = projectProvider;
        this.dataHubService = dataHubService;
    }

    @Override
    public List<ImportTypeDescriptor> types() {
        return importers.stream().map(LinceImporter::descriptor).toList();
    }

    @Override
    public ImportResult importFile(String fileName, InputStream in, String explicitType, ImportScope scope,
                                   Map<String, String> options) {
        LinceImporter importer = resolve(fileName, explicitType);
        String typeId = importer.descriptor().id();
        ImportScope effectiveScope = scope != null && LinceProjectImporter.TYPE_ID.equals(typeId) ? scope : ImportScope.FULL;
        log.info("Importing {} as {} ({})", fileName, typeId, effectiveScope);

        ILinceProject target = projectProvider.snapshot();
        ImportOutcome outcome = importer.importInto(target, in, effectiveScope, options == null ? Map.of() : options);
        apply(target);
        return outcome.toResult(typeId, effectiveScope);
    }

    private LinceImporter resolve(String fileName, String explicitType) {
        if (StringUtils.isNotBlank(explicitType)) {
            String id = explicitType.trim();
            LinceImporter importer = importers.stream()
                    .filter(candidate -> id.equalsIgnoreCase(candidate.descriptor().id()))
                    .findFirst()
                    .orElseThrow(() -> new ImportException(Reason.UNKNOWN_TYPE, "Unknown import type: " + displayName(id)));
            if (!importer.supports(fileName) && detect(fileName).isPresent()) {
                throw new ImportException(Reason.TYPE_MISMATCH,
                        "Import type " + displayName(id) + " does not read " + displayName(fileName));
            }
            return importer;
        }
        return detect(fileName).orElseThrow(() -> new ImportException(Reason.UNKNOWN_TYPE,
                "Cannot detect the import type of " + StringUtils.defaultIfBlank(displayName(fileName), "the file")
                        + "; send 'type' (" + String.join(", ", typeIds()) + ")"));
    }

    /**
     * A client value (file name or type) as echoed in error messages: control characters (below 0x20 and 0x7F) removed and at
     * most {@value #MAX_ECHOED_NAME} characters, the rest replaced by an ellipsis.
     */
    static String displayName(String fileName) {
        if (fileName == null) {
            return null;
        }
        StringBuilder clean = new StringBuilder(Math.min(fileName.length(), MAX_ECHOED_NAME + 1));
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            if (c >= 0x20 && c != 0x7F) {
                clean.append(c);
            }
        }
        return clean.length() <= MAX_ECHOED_NAME ? clean.toString() : clean.substring(0, MAX_ECHOED_NAME) + "\u2026";
    }

    private Optional<LinceImporter> detect(String fileName) {
        return importers.stream().filter(importer -> importer.supports(fileName)).findFirst();
    }

    private List<String> typeIds() {
        return types().stream().map(ImportTypeDescriptor::id).toList();
    }

    private void apply(ILinceProject imported) {
        dataHubService.setCriteria(copy(imported.getObservationTool()));
        dataHubService.setAllDataRegister(copy(imported.getRegister()));
        dataHubService.setUserData(copy(imported.getProfiles()));
        dataHubService.reloadResearchProfileDataFromRegister();
    }

    private static <T> List<T> copy(List<T> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }
}
