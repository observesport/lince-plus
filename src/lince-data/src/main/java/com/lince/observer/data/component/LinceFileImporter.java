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
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.util.LinceFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.security.Principal;

/**
 *
 */
@Component
public class LinceFileImporter {
    private static final Logger log = LoggerFactory.getLogger(LinceFileImporter.class);

    public ILinceProject importLinceProject(File file) {
        if (file != null && file.exists()) {
            try {
                LinceFileProjectWrapper linceProject = new LinceFileProjectWrapper();
                LinceFileHelper linceFileHelper = new LinceFileHelper();
                linceFileHelper.readProjectFile(file, linceFileProjectWrapper -> {
                    linceProject.setObservationTool(linceFileProjectWrapper.getObservationTool());
                    linceProject.setRegister(linceFileProjectWrapper.getRegister());
                    linceProject.setProfiles(linceFileProjectWrapper.getProfiles());
                    linceProject.setVideoPlayList(linceFileProjectWrapper.getVideoPlayList());
                    linceProject.setYoutubeVideoPlayList(linceFileProjectWrapper.getYoutubeVideoPlayList());
                });
                return linceProject;
            } catch (Exception e) {
                log.error("Importing Lince Project with file name {}", file.getPath());
            }
        }
        return null;
    }

}
