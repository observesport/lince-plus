package com.lince.observer.data.transfer;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.transfer.ImportTypeDescriptor;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

/**
 * Strategy for one importable file type (LO-349). Implementations are pure Java (no JavaFX, Swing,
 * dialogs) and are registered as Spring beans so the import service builds its registry from
 * {@code List<LinceImporter>}.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public interface LinceImporter {

    /**
     * @return id, label, extensions and options of this type
     */
    ImportTypeDescriptor descriptor();

    /**
     * Extension based detection.
     *
     * @param fileName uploaded file name, may be null
     * @return true when the file extension is one of {@link ImportTypeDescriptor#extensions()}, ignoring case
     */
    default boolean supports(String fileName) {
        String extension = StringUtils.substringAfterLast(StringUtils.defaultString(fileName), ".");
        if (extension.isEmpty()) {
            return false;
        }
        String normalised = extension.toLowerCase(Locale.ROOT);
        return descriptor().extensions().stream()
                .map(e -> StringUtils.removeStart(e, ".").toLowerCase(Locale.ROOT))
                .anyMatch(normalised::equals);
    }

    /**
     * Reads the stream and merges its content into {@code target}.
     *
     * @param target  project receiving the data; the importer mutates it
     * @param in      file content; the caller closes it
     * @param scope   never null; importers that do not distinguish scopes treat it as {@link ImportScope#FULL}
     * @param options per-type options, never null
     * @return counts and warnings
     * @throws ImportException with a reason code on any failure
     */
    ImportOutcome importInto(ILinceProject target, InputStream in, ImportScope scope, Map<String, String> options);
}
