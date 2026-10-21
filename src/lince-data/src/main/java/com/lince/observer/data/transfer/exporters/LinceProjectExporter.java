package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.LinceExporter;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.util.LinceFileHelper;
import jakarta.xml.bind.JAXBException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * Exports the whole project as a Lince PLUS {@code .xml} file (LO-349), with the content and JAXB settings of
 * {@link LinceFileHelper#writeProjectFile}: profiles, instrument, YouTube playlist and registers; local videos are
 * not written. The criteria and research selection do not apply.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Component
public class LinceProjectExporter implements LinceExporter {

    public static final String TYPE_ID = "lince-plus";
    static final String MIME_TYPE = "application/xml";
    static final String FILE_NAME = "lince-project.xml";

    private static final ExportTypeDescriptor DESCRIPTOR = new ExportTypeDescriptor(TYPE_ID, "Lince PLUS project",
            "xml", MIME_TYPE, false, false, List.of());

    private final LinceFileHelper fileHelper = new LinceFileHelper();

    @Override
    public ExportTypeDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public List<ExportedFile> export(ExportSelection selection, Map<String, String> options) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            fileHelper.writeProject(out, selection.getProject());
        } catch (JAXBException | RuntimeException e) {
            throw new ExportException(TransferException.Reason.IO, "Could not write the Lince PLUS project", e);
        }
        return List.of(new ExportedFile(FILE_NAME, MIME_TYPE, out.toByteArray()));
    }
}
