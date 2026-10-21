/**
 * This file Copyright (c) 2024 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 * <p>
 * <p>
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 * <p>
 * Any modifications to this file must keep this entire header
 * intact.
 */
package com.lince.observer.data.component;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.util.LinceFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 *
 */
@Component
public class LinceFileImporter {
    private static final Logger log = LoggerFactory.getLogger(LinceFileImporter.class);

    /**
     * @param file Lince PLUS project file
     * @return the whole project (instrument, registers, profiles, videos), null when the file is missing or unreadable
     */
    public ILinceProject importLinceProject(File file) {
        if (file != null && file.exists()) {
            try (InputStream in = Files.newInputStream(file.toPath())) {
                return new LinceFileHelper().readProject(in);
            } catch (Exception e) {
                log.error("Importing Lince Project with file name {}", file.getPath(), e);
            }
        }
        return null;
    }

}
