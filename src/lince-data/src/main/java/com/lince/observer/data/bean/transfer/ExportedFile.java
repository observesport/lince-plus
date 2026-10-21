package com.lince.observer.data.bean.transfer;

import java.util.Arrays;
import java.util.Objects;

/**
 * One file produced by an exporter (LO-349). Several of them are zipped by the export service.
 * Equality compares the content by value.
 *
 * @param name     file name including extension, no directories
 * @param mimeType MIME type of the content
 * @param content  file bytes
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public record ExportedFile(String name, String mimeType, byte[] content) {

    public ExportedFile {
        Objects.requireNonNull(name, "name");
        content = content == null ? new byte[0] : content;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ExportedFile other
                && name.equals(other.name)
                && Objects.equals(mimeType, other.mimeType)
                && Arrays.equals(content, other.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, mimeType) * 31 + Arrays.hashCode(content);
    }

    @Override
    public String toString() {
        return "ExportedFile[name=" + name + ", mimeType=" + mimeType + ", size=" + content.length + "]";
    }
}
