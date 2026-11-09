package com.lince.observer.data.service;

import com.lince.observer.data.bean.transfer.ExportRequest;
import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.transfer.ExportException;

import java.util.List;
import java.util.Optional;

/**
 * Exports the loaded project in any registered format (LO-349). Desktop and cloud provide their own
 * implementation ({@code @DesktopQualifier} / {@code @CloudQualifier}) that resolves the current project into an
 * {@code ExportSelection} and dispatches to the matching {@code LinceExporter} bean.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public interface ExportService {

    /**
     * @return every export type
     */
    List<ExportTypeDescriptor> types();

    /**
     * @param id type id
     * @return the descriptor with that id
     */
    default Optional<ExportTypeDescriptor> type(String id) {
        return types().stream().filter(t -> t.id().equals(id)).findFirst();
    }

    /**
     * @param request type, research, criteria and options
     * @return the produced files; zipping a multi-file result is up to the caller (controller)
     * @throws ExportException with a reason code
     */
    List<ExportedFile> export(ExportRequest request);
}
