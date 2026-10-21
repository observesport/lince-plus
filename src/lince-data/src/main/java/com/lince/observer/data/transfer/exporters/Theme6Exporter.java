package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.export.Lince2ThemeExport;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.LinceExporter;
import com.lince.observer.data.transfer.TransferException;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Theme 6 export (LO-349): {@code <base>.txt} with the register rows ({@link Lince2ThemeExport#exportToString()})
 * and the fixed sibling {@code vvt.vvt} with the instrument ({@link Lince2ThemeExport#createVvtContent(List)}), as
 * {@code Theme6ExportComponent} writes them ({@code String.getBytes()}, platform charset).
 * <p>
 * Selection: the {@code .vvt} lists the selected criteria. The rows are those of the selected register, unlike the
 * former JavaFX component, which joined every observer. When only some criteria are selected each row keeps only the
 * categories of those criteria and rows left empty are dropped, so the {@code .txt} never refers to an event the
 * {@code .vvt} does not declare. When every criteria is selected (explicitly or by an empty list) the rows are
 * untouched, so the output matches the former JavaFX menu only for all-criteria exports of a single-observer
 * project.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Component
public class Theme6Exporter implements LinceExporter {

    public static final String TYPE_ID = "theme6";
    public static final String VVT_FILE_NAME = "vvt.vvt";
    static final String MIME_TYPE = "text/plain";
    static final String FILE_NAME = "theme6.txt";

    private static final ExportTypeDescriptor DESCRIPTOR = new ExportTypeDescriptor(TYPE_ID, "Theme 6", "txt",
            MIME_TYPE, true, true, List.of());

    @Override
    public ExportTypeDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public List<ExportedFile> export(ExportSelection selection, Map<String, String> options) {
        selection.requireCriteria();
        List<Criteria> criteria = selection.getCriteria();
        boolean everyCriteria = criteria.size() == selection.getProject().getObservationTool().size();
        List<RegisterItem> rows = everyCriteria ? selection.getRows() : filterRows(selection.getRows(), criteria);
        requireFrames(rows);
        String txt;
        String vvt;
        try {
            txt = new Lince2ThemeExport(rows).exportToString();
            vvt = Lince2ThemeExport.createVvtContent(criteria);
        } catch (RuntimeException e) {
            throw new ExportException(TransferException.Reason.IO, "Could not write the Theme 6 files", e);
        }
        String mimeType = MIME_TYPE + "; charset=" + Charset.defaultCharset().name();
        return List.of(new ExportedFile(FILE_NAME, mimeType, txt.getBytes()),
                new ExportedFile(VVT_FILE_NAME, mimeType, vvt.getBytes()));
    }

    /**
     * Fails early with the position of the offending row instead of a generic writer error.
     */
    private static void requireFrames(List<RegisterItem> rows) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getFrames() == null) {
                throw new ExportException(TransferException.Reason.IO,
                        "Observation " + (i + 1) + " has no frame number: Theme 6 needs frames");
            }
        }
    }

    /**
     * Copies of the rows holding only the categories of the selected criteria; the project is not modified.
     */
    private static List<RegisterItem> filterRows(List<RegisterItem> rows, List<Criteria> criteria) {
        Set<String> codes = new HashSet<>();
        for (Criteria c : criteria) {
            if (c.getInnerCategories() != null) {
                c.getInnerCategories().forEach(category -> codes.add(category.getCode()));
            }
        }
        List<RegisterItem> filtered = new ArrayList<>();
        for (RegisterItem row : rows) {
            List<Category> kept = row.getRegister() == null ? List.of()
                    : row.getRegister().stream().filter(category -> codes.contains(category.getCode())).toList();
            if (!kept.isEmpty()) {
                RegisterItem copy = new RegisterItem();
                copy.setFrames(row.getFrames());
                copy.setRegister(new ArrayList<>(kept));
                filtered.add(copy);
            }
        }
        return filtered;
    }
}
