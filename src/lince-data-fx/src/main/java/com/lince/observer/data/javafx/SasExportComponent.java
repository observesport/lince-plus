package com.lince.observer.data.javafx;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.service.ExportService;
import com.lince.observer.data.transfer.exporters.SasExporter;

import java.util.UUID;

/**
 * SAS export (LO-349): the register as .sds, produced by {@link SasExporter} through {@link ExportService}. Only
 * criteria are selectable: a converted project has no Lince 1 fixed or mixed information nodes.
 * <p>
 * Created by Alberto Soto. 7/11/24
 */
public class SasExportComponent extends GenericExportComponent {

    public SasExportComponent(ILinceProject linceProject, UUID observerId, ExportService exportService) {
        super(linceProject, observerId, exportService, SasExporter.TYPE_ID, "EXPORT_SAS");
    }

    @Override
    String getFileExtension() {
        return "*.sds";
    }

    @Override
    String getExportTitle() {
        return "EXPORT_SAS";
    }
}
