package com.lince.observer.data.transfer;

import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.util.LinceFileHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Loads the XML fixtures of {@code src/test/resources} for the transfer tests (LO-349).
 */
public final class TransferFixtures {

    public static final String SINGLE = "singleObserverExample.xml";
    public static final String MULTIPLE = "multipleObserverExample.xml";

    private TransferFixtures() {
    }

    public static InputStream open(String resource) {
        InputStream in = TransferFixtures.class.getClassLoader().getResourceAsStream(resource);
        if (in == null) {
            throw new IllegalArgumentException("Missing test resource " + resource);
        }
        return in;
    }

    public static LinceFileProjectWrapper load(String resource) {
        try (InputStream in = open(resource)) {
            return new LinceFileHelper().readProject(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
