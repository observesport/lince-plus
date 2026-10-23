package com.lince.observer.legacy;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import com.lince.observer.data.component.PackageAwareXMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Alberto Soto. 17/2/25
 */
public class PackageAwareXMLSerializerTest {

    private static final Logger logger = LoggerFactory.getLogger(PackageAwareXMLSerializerTest.class);

    @Test
    public void testCategoriaMapping() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <java version="1.8.0_231" class="java.beans.XMLDecoder">
                  <object class="lince.modelo.InstrumentoObservacional.Categoria">
                      <void property="codigo">
                       <string>G0</string>
                      </void>
                      <void property="nombre">
                       <string>G0</string>
                      </void>
                 </object>
                </java>
                """;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
             PackageAwareXMLSerializer decoder = new PackageAwareXMLSerializer(inputStream)) {
            logger.info(PackageAwareXMLSerializer.getPackageMappingsText());
            Object decodedObject = decoder.readObject();
            assertNotNull(decodedObject, "Decoded object should not be null");
            assertEquals("com.lince.observer.legacy.instrumentoObservacional.Categoria",
                    decodedObject.getClass().getName());

        } catch (IllegalStateException e) {
            fail("Illegal state: " + e.getMessage());
        } catch (IOException e) {
            fail("IO Exception: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void importLegacyLinceToolFile() {
        try {
            Object decodedObject = loadLegacyObject("lince1/RandomLinceLegacy.ilince");
            assertNotNull(decodedObject, "Decoded object should not be null");
            assertEquals("com.lince.observer.legacy.instrumentoObservacional.RootInstrumentoObservacional",
                    decodedObject.getClass().getName());
        } catch (IOException e) {
            fail("IO Exception: " + e.getMessage());
        }
    }

    @Test
    public void importLegacyLinceRegisterFile() {
        try {
            Object decodedObject = loadLegacyObject("lince1/RandomLinceLegacy.rlince");
            assertNotNull(decodedObject, "Decoded object should not be null");
            assertEquals("java.util.HashMap", decodedObject.getClass().getName());
        } catch (IOException e) {
            fail("IO Exception: " + e.getMessage());
        }
    }

    @Test
    public void importLegacyLinceToolFileAdvance() {
        try {
            Object decodedObject = loadLegacyObject("lince1/instrumentoObservacional-adv.ilince");
            assertNotNull(decodedObject, "Decoded object should not be null");
            assertEquals("com.lince.observer.legacy.instrumentoObservacional.RootInstrumentoObservacional",
                    decodedObject.getClass().getName());
        } catch (IOException e) {
            fail("IO Exception: " + e.getMessage());
        }
    }

    @Test
    public void detectedLegacyKeys() {
        try {
            boolean isValid = getLegacyKeys("lince1/registroObservacional-adv.rlince");
            assertTrue(isValid, "Unique classes set should not be empty");
        } catch (IOException e) {
            fail("IO Exception: " + e.getMessage());
        }
    }

    private boolean getLegacyKeys(String filePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            assert inputStream != null;
            PackageAwareXMLSerializer decoder = new PackageAwareXMLSerializer(inputStream);
            return decoder.validate();
        } catch (IOException e) {
            fail("IO Exception: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        return false;
    }

    private Object loadLegacyObject(String testFileName) throws IOException {
        logger.info("Loading file: {}", testFileName);
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(testFileName);
             PackageAwareXMLSerializer decoder = new PackageAwareXMLSerializer(inputStream)) {
            logger.info(PackageAwareXMLSerializer.getPackageMappingsText());
            return decoder.readObject();
        } catch (IOException e) {
            fail("IO Exception: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        return null;
    }


}



