package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.transfer.legacy.LegacyProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * Shared plumbing of the legacy exporters (LO-349).
 * <p>
 * Text exports use the JVM default charset, as the JavaFX panels do ({@code GenericExportComponent} and
 * {@code SasExportComponent} write {@code String.getBytes()}), so the bytes match the menu on the same machine.
 * That is UTF-8 on macOS/Linux and may be cp1252 on Windows with Java 17. The Lince 1 exporters are XML and
 * always UTF-8, as {@code Registro.save()} writes them.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
final class LegacyExportSupport {

    static final Charset CHARSET = Charset.defaultCharset();
    static final String TEXT_PLAIN = "text/plain";
    static final String TEXT_CSV = "text/csv";
    static final String APPLICATION_XML = "application/xml";

    private static final Logger log = LoggerFactory.getLogger(LegacyExportSupport.class);

    private LegacyExportSupport() {
    }

    static ExportedFile text(String name, String mimeType, String content) {
        return new ExportedFile(name, mimeType, content == null ? new byte[0] : content.getBytes(CHARSET));
    }

    /**
     * Exports have no warnings channel; conversion warnings are logged.
     */
    static LegacyProject logWarnings(String type, LegacyProject legacy) {
        legacy.warnings().forEach(w -> log.warn("{} export: {}", type, w));
        return legacy;
    }
}
