package com.lince.observer.data.javafx;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.service.ExportService;
import com.lince.observer.data.transfer.exporters.SdisGseqExporter;
import com.lince.observer.legacy.export.GseqExporterFactory.GseqFormatType;

import java.util.UUID;

/**
 * SDIS-GSEQ export (LO-349): the register as .sds in one {@link GseqFormatType}, produced by
 * {@link SdisGseqExporter} through {@link ExportService}. Replaces the five {@code RegistroSdisGseq*Export} panels.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public class SdisGseqExportComponent extends GenericExportComponent {

    public SdisGseqExportComponent(ILinceProject linceProject, UUID observerId, ExportService exportService,
                                   GseqFormatType format) {
        super(linceProject, observerId, exportService, SdisGseqExporter.typeId(format), SdisGseqExporter.labelKey(format));
    }

    @Override
    String getFileExtension() {
        return "*.sds";
    }

    @Override
    String getExportTitle() {
        return "GSEQ";
    }
}
