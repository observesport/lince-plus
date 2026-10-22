package com.lince.observer.data.transfer;

import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;

import java.util.List;
import java.util.Map;

/**
 * Strategy for one export type (LO-349). Implementations are pure Java (no JavaFX, Swing, dialogs) and are
 * registered as Spring beans so the export service builds its registry from {@code List<LinceExporter>}.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public interface LinceExporter {

    /**
     * @return id, label, extension, MIME type, flags and options of this type
     */
    ExportTypeDescriptor descriptor();

    /**
     * Produces the export content. The service zips the result when there is more than one file.
     *
     * @param selection resolved project, register rows and criteria
     * @param options   per-type options, never null
     * @return one file, or several for {@link ExportTypeDescriptor#multiFile()} types
     * @throws ExportException with a reason code on any failure
     */
    List<ExportedFile> export(ExportSelection selection, Map<String, String> options);
}
