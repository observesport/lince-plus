package com.lince.observer.data.javafx;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.service.ExportService;
import com.lince.observer.data.transfer.exporters.Theme6Exporter;

import java.util.UUID;

/**
 * Theme 6 export (LO-349): the register (.txt) and a fixed {@code vvt.vvt} instrument next to it, produced by
 * {@link Theme6Exporter} through {@link ExportService}.
 * <p>
 * Created by Alberto Soto. 8/11/24
 */
public class Theme6ExportComponent extends GenericExportComponent {

    public Theme6ExportComponent(ILinceProject linceProject, UUID observerId, ExportService exportService) {
        super(linceProject, observerId, exportService, Theme6Exporter.TYPE_ID, "EXPORTAR THEME 6");
    }

    @Override
    String getFileExtension() {
        return "*.txt";
    }

    @Override
    String getExportTitle() {
        return "Theme 6 Export";
    }
}
