package com.lince.observer.data.service;

import com.lince.observer.data.bean.transfer.ImportResult;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.transfer.ImportTypeDescriptor;
import com.lince.observer.data.transfer.ImportException;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Imports a file into the loaded project (LO-349). Desktop and cloud provide their own implementation
 * ({@code @DesktopQualifier} / {@code @CloudQualifier}) over the registered {@code LinceImporter} beans.
 * <p>
 * Type resolution: explicit {@code type} → file extension → {@link ImportException} with
 * {@code UNKNOWN_TYPE}. An explicit type contradicting a known extension fails with {@code TYPE_MISMATCH}.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public interface ImportService {

    /**
     * @return every importable type
     */
    List<ImportTypeDescriptor> types();

    /**
     * @param id type id
     * @return the descriptor with that id
     */
    default Optional<ImportTypeDescriptor> type(String id) {
        return types().stream().filter(t -> t.id().equals(id)).findFirst();
    }

    /**
     * @param fileName     uploaded file name, used for type detection
     * @param in           file content; the caller closes it
     * @param explicitType type id forced by the client, nullable
     * @param scope        nullable, null means {@link ImportScope#FULL}
     * @param options      per-type options, nullable
     * @return what was imported
     * @throws ImportException with a reason code
     */
    ImportResult importFile(String fileName, InputStream in, String explicitType, ImportScope scope,
                            Map<String, String> options);
}
