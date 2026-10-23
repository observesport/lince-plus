package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.transfer.legacy.LegacyProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Shared plumbing of the legacy exporters (LO-349).
 * <p>
 * Text exports go through {@link com.lince.observer.data.bean.transfer.ExportedFile#text}: the JVM default charset, as the former Swing panels used
 * ({@code String.getBytes()}), named in the MIME type. The Lince 1 exporters are XML and always UTF-8, as
 * {@code PackageAwareXMLSerializer} writes them.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
final class LegacyExportSupport {

    static final String TEXT_PLAIN = "text/plain";
    static final String TEXT_CSV = "text/csv";
    static final String APPLICATION_XML = "application/xml";

    private static final Logger log = LoggerFactory.getLogger(LegacyExportSupport.class);

    private LegacyExportSupport() {
    }

    /**
     * Exports have no warnings channel; conversion warnings are logged.
     */
    static LegacyProject logWarnings(String type, LegacyProject legacy) {
        legacy.warnings().forEach(w -> log.warn("{} export: {}", type, w));
        return legacy;
    }
}
