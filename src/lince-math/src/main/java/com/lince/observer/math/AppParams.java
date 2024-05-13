package com.lince.observer.math;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.util.stream.Stream;

/**
 * coachgate-core-root
 * com.deicos.coachgate.core.rest.config
 *
 * @author berto (alberto.soto@gmail.com)in 21/01/2016.
 * Description:
 */
public class AppParams {

    public static final String YOUTUBE_SERVER = "youtube.com";

    /**
     * gets null properties values to use with copy bean propertis
     *
     * @param source
     * @return
     */
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }
}
