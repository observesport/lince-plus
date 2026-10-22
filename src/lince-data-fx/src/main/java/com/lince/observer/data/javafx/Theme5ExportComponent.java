package com.lince.observer.data.javafx;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.service.ExportService;
import com.lince.observer.data.transfer.exporters.Theme5Exporter;

import java.util.UUID;

/**
 * Theme 5 export (LO-349): the register (.csv) and the instrument (.vvt) under the chosen base name, produced
 * by {@link Theme5Exporter} through {@link ExportService}. One button writes both files; the panel had one button
 * per file before.
 * <p>
 * Created by Alberto Soto. 8/11/24
 */
public class Theme5ExportComponent extends GenericExportComponent {

    public Theme5ExportComponent(ILinceProject linceProject, UUID observerId, ExportService exportService) {
        super(linceProject, observerId, exportService, Theme5Exporter.TYPE_ID, "EXPORTAR THEME 5");
    }

    @Override
    String getFileExtension() {
        return "*.csv";
    }

    @Override
    String getExportTitle() {
        return "Theme 5 Export";
    }
}
