package com.lince.observer.data.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.DefaultPersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Alberto Soto. 17/2/25
 *
 * <p>This class provides both decoding and encoding capabilities for XML serialization with package mapping
 * support for legacy class names.
 * These mappings are unfortunately java-fx based as they extend the DefaultMutableTreeNode
 * </p>
 *
 * <h3>Decoding example</h3>
 * <pre>{@code
 * try (InputStream is = new FileInputStream("input.xml");
 *     PackageAwareXMLSerializer decoder = new PackageAwareXMLSerializer(is)) {
 *     Object decodedObject = decoder.readObject();
 *     // Use the decoded object
 * }
 * }</pre>
 *
 * <h3>Encoding example</h3>
 * <pre>{@code
 * // Encode to string
 * String xml = PackageAwareXMLSerializer.encodeToXml(myObject);
 *
 * // Encode directly to file
 * PackageAwareXMLSerializer.encodeToFile(myObject, new File("output.xml"));
 * }</pre>
 */
public class PackageAwareXMLSerializer implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(PackageAwareXMLSerializer.class);
    private static final Map<String, String> packageMappings = new HashMap<>();
    private final XMLDecoder decoder;
    private final ByteArrayInputStream originalContent;
    private XMLEncoder encoder;
    private ByteArrayOutputStream encoderOutput;

    static {
        Map<String, String> initialMappings = new HashMap<>();
        initialMappings.put("lince.modelo.InstrumentoObservacional.RootInstrumentoObservacional", "com.lince.observer.legacy.instrumentoObservacional.RootInstrumentoObservacional");
        initialMappings.put("lince.modelo.InstrumentoObservacional.NodoInformacion", "com.lince.observer.legacy.instrumentoObservacional.NodoInformacion");
        initialMappings.put("lince.modelo.InstrumentoObservacional.Criterio", "com.lince.observer.legacy.instrumentoObservacional.Criterio");
        initialMappings.put("lince.modelo.InstrumentoObservacional.Categoria", "com.lince.observer.legacy.instrumentoObservacional.Categoria");
        initialMappings.put("lince.modelo.FilaRegistro", "com.lince.observer.legacy.FilaRegistro");
//        initialMappings.put("javax.swing.tree.DefaultMutableTreeNode", "javax.swing.tree.DefaultMutableTreeNode");
        packageMappings.putAll(initialMappings);
    }

    public PackageAwareXMLSerializer(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        in.transferTo(baos);
        byte[] bytes = baos.toByteArray();
        this.originalContent = new ByteArrayInputStream(bytes);
        String xmlContent = new String(this.originalContent.readAllBytes(), StandardCharsets.UTF_8);
        for (Map.Entry<String, String> mapping : packageMappings.entrySet()) {
            String oldPackage = Pattern.quote(mapping.getKey());
            String newPackage = Matcher.quoteReplacement(mapping.getValue());
            xmlContent = xmlContent.replaceAll(
                    String.format("class=\"%s\"", oldPackage),
                    String.format("class=\"%s\"", newPackage)
            );
        }
        decoder = new XMLDecoder(new ByteArrayInputStream(
                xmlContent.getBytes(StandardCharsets.UTF_8)
        ));
    }

    /**
     * Creates a new instance for encoding objects to XML
     */
    public PackageAwareXMLSerializer() {
        this.decoder = null;
        this.originalContent = null;
        this.encoderOutput = new ByteArrayOutputStream();
        this.encoder = new XMLEncoder(encoderOutput);
    }

    public Object readObject() {
        return decoder.readObject();
    }

    /**
     * Writes an object to XML using the XMLEncoder
     * @param object The object to encode
     */
    public void writeObject(Object object) {
        if (encoder == null) {
            throw new IllegalStateException("This instance was not created for encoding");
        }
        try {
            //        encoder.setPersistenceDelegate(FilaRegistro.class, new DefaultPersistenceDelegate(new String[]{"milis", "registro", "datosMixtos"}));
            Class<?> filaRegistroClass = Class.forName("com.lince.observer.legacy.FilaRegistro");
            encoder.setPersistenceDelegate(filaRegistroClass,
                    new DefaultPersistenceDelegate(new String[]{"milis", "registro", "datosMixtos"}));
        } catch (ClassNotFoundException e) {
            logger.warn("FilaRegistro class not found, skipping persistence delegate setup", e);
        }
        encoder.writeObject(object);
    }

    /**
     * Gets the XML string representation of the encoded objects
     * @return XML string with the encoded objects
     */
    public String getEncodedXml() {
        if (encoder == null) {
            throw new IllegalStateException("This instance was not created for encoding");
        }
        encoder.flush();
        String xml = encoderOutput.toString(StandardCharsets.UTF_8);

        // Reverse the package mappings for encoding
        for (Map.Entry<String, String> mapping : packageMappings.entrySet()) {
            String newPackage = Pattern.quote(mapping.getValue());
            String oldPackage = Matcher.quoteReplacement(mapping.getKey());
            xml = xml.replaceAll(
                    String.format("class=\"%s\"", newPackage),
                    String.format("class=\"%s\"", oldPackage)
            );
        }

        return xml;
    }

    /**
     * Writes the encoded XML to the specified output stream
     * @param out The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writeEncodedXml(OutputStream out) throws IOException {
        out.write(getEncodedXml().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes an object to XML and returns the XML string
     * @param object The object to encode
     * @return XML string representation of the object
     */
    public static String encodeToXml(Object object) {
        try (PackageAwareXMLSerializer encoder = new PackageAwareXMLSerializer()) {
            encoder.writeObject(object);
            return encoder.getEncodedXml();
        } catch (Exception e) {
            logger.error("Error encoding object to XML", e);
            throw new RuntimeException("Failed to encode object to XML", e);
        }
    }

    /**
     * Encodes an object to XML and writes it to a file
     * @param object The object to encode
     * @param file The file to write to
     * @throws IOException If an I/O error occurs
     */
    public static void encodeToFile(Object object, File file) throws IOException {
        try (PackageAwareXMLSerializer encoder = new PackageAwareXMLSerializer();
             FileOutputStream fos = new FileOutputStream(file)) {
            encoder.writeObject(object);
            encoder.writeEncodedXml(fos);
        }
    }

    @Override
    public void close() {
        if (decoder != null) {
            decoder.close();
        }
        if (encoder != null) {
            encoder.close();
        }
    }

    public static String getPackageMappingsText() {
        StringBuilder sb = new StringBuilder("Package mappings:\n");
        packageMappings.forEach((oldPackage, newPackage) ->
                sb.append(String.format("  %s -> %s%n", oldPackage, newPackage))
        );
        return sb.toString();
    }

    public Set<String> getLegacyKeys(InputStream in) throws IOException {
        Set<String> uniqueClasses = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            logger.info("Processing input stream");
            logger.info(PackageAwareXMLSerializer.getPackageMappingsText());
            String line;
            Pattern pattern = Pattern.compile("class=\"([^\"]+)\"");
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    uniqueClasses.add(matcher.group(1));
                }
            }
        } catch (IOException e) {
            logger.error("IO Exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected exception: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return uniqueClasses;
    }

    public boolean validate() throws IOException {
        Set<String> legacyKeys = getLegacyKeys(originalContent);
        Set<String> unmappedLinceClasses = new HashSet<>();
        boolean allMapped = true;

        for (String legacyKey : legacyKeys) {
            if (legacyKey.startsWith("lince.") && !packageMappings.containsKey(legacyKey)) {
                unmappedLinceClasses.add(legacyKey);
                allMapped = false;
            }
        }

        if (!unmappedLinceClasses.isEmpty()) {
            logger.warn("The following legacy classes are not mapped but start with 'lince':");
            for (String unmappedClass : unmappedLinceClasses) {
                logger.warn("  {}", unmappedClass);
            }
        }

        return allMapped;
    }
}
