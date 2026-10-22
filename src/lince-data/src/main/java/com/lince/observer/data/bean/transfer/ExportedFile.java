package com.lince.observer.data.bean.transfer;

import java.nio.charset.Charset;
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

    /**
     * A text file encoded with the platform default charset, so it is byte-identical with what the JavaFX menu
     * writes, and that charset named in the MIME type.
     *
     * @param name     file name including extension
     * @param mimeType MIME type without charset, e.g. {@code text/csv}
     * @param content  text content
     * @return the file, MIME type {@code <mimeType>; charset=<platform charset>}
     */
    public static ExportedFile text(String name, String mimeType, String content) {
        Charset charset = Charset.defaultCharset();
        return new ExportedFile(name, mimeType + "; charset=" + charset.name(),
                content == null ? null : content.getBytes(charset));
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
