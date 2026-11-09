package com.lince.observer.data.bean.transfer;

import java.util.List;

/**
 * Describes one importable file type for {@code GET /api/import/types} (LO-349).
 *
 * @param id         stable type id sent back as {@code type} ({@code lince-plus}, {@code lince1-register}, ...)
 * @param label      human readable label
 * @param extensions lowercase extensions without the dot, used for detection ({@code xml}, {@code rlince}, ...)
 * @param options    per-type options, never null
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public record ImportTypeDescriptor(String id, String label, List<String> extensions, List<TransferOption> options) {

    public ImportTypeDescriptor {
        extensions = extensions == null ? List.of() : List.copyOf(extensions);
        options = options == null ? List.of() : List.copyOf(options);
    }
}
