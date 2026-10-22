package com.lince.observer.data.transfer.exporters;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Checked-in expected output of the legacy exporters ({@code src/test/resources/transfer/golden}), so their tests
 * do not only compare against the same legacy {@code exportTo*} calls the exporters make.
 * <p>
 * Charset: the text exporters write {@link LegacyExportSupport#CHARSET} (the platform charset, as the menu does),
 * the .ilince is always UTF-8. The golden files are stored as UTF-8 text and re-encoded with the charset the
 * exporter writes before the byte-for-byte comparison, so they hold on any platform whose charset can encode the
 * fixture. The files are marked {@code -text} in the
 * folder's {@code .gitattributes} so their CRLF line ends survive checkout.
 * <p>
 * To regenerate after an intended format change, run the exporter tests with {@code -Dgolden.update=true} from
 * the module folder and review the diff.
 */
final class Golden {

    private static final Path SOURCE_DIR = Path.of("src", "test", "resources", "transfer", "golden");

    private Golden() {
    }

    /**
     * @param name   golden file name
     * @param actual exporter output written in the platform default charset ({@code ExportedFile.text})
     */
    static void assertMatches(String name, byte[] actual) {
        assertMatches(name, actual, Charset.defaultCharset());
    }

    /**
     * @param name    golden file name
     * @param actual  exporter output
     * @param charset charset the exporter writes
     */
    static void assertMatches(String name, byte[] actual, Charset charset) {
        if (Boolean.getBoolean("golden.update")) {
            write(name, actual, charset);
            return;
        }
        assertArrayEquals(read(name).getBytes(charset), actual, name);
    }

    /**
     * Like {@link #assertMatches(String, byte[], Charset)} for a {@code java.beans.XMLEncoder} document (UTF-8),
     * ignoring only the JDK version the encoder writes in its root element, so the file holds on any Java 17 build.
     *
     * @param name   golden file name
     * @param actual exporter output
     */
    static void assertXmlEncoderMatches(String name, byte[] actual) {
        String normalized = JAVA_VERSION.matcher(new String(actual, StandardCharsets.UTF_8))
                .replaceFirst("<java version=\"*\"");
        assertMatches(name, normalized.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    private static final Pattern JAVA_VERSION = Pattern.compile("<java version=\"[^\"]*\"");

    private static String read(String name) {
        try (InputStream in = Golden.class.getResourceAsStream("/transfer/golden/" + name)) {
            assertNotNull(in, "missing golden file " + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(String name, byte[] actual, Charset charset) {
        try {
            Files.createDirectories(SOURCE_DIR);
            Files.writeString(SOURCE_DIR.resolve(name), new String(actual, charset),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
