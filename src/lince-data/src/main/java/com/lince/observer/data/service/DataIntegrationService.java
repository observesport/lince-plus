package com.lince.observer.data.service;

import com.lince.observer.data.component.PackageAwareXMLSerializer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by Alberto Soto. 17/2/25
 *
 * Supports the integration of legacy data under lince plus v1 with the new system.
 *
 *
 */
@Service
public class DataIntegrationService {

//    public <T> T exportLinceLegacyRegister(InputStream in) throws UnsupportedOperationException {
//        throw new UnsupportedOperationException("This method is not implemented yet");
//    }
//    public <T> T exportLinceLegacyObservationTool(InputStream in) throws UnsupportedOperationException {
//        throw new UnsupportedOperationException("This method is not implemented yet");
//    }

    @SuppressWarnings("unchecked")
    public <T> T importLinceLegacyRegister(InputStream in) throws IOException {
        try (PackageAwareXMLSerializer decoder = new PackageAwareXMLSerializer(in)) {
            Object decodedObject = decoder.readObject();
            if (decodedObject instanceof HashMap<?,?>) {
                return (T) decodedObject;
            } else {
                throw new IllegalArgumentException("Unexpected object type: " + decodedObject.getClass().getName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T importLinceLegacyObservationTool(InputStream in) throws IOException {
        try (PackageAwareXMLSerializer decoder = new PackageAwareXMLSerializer(in)) {
            Object decodedObject = decoder.readObject();
            if (decodedObject.getClass().getName().equals("com.lince.observer.legacy.instrumentoObservacional.RootInstrumentoObservacional")) {
                return (T) decodedObject;
            } else {
                throw new IllegalArgumentException("Unexpected object type: " + decodedObject.getClass().getName());
            }
        }
    }

}
