package com.lince.observer.data.component;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.LinceDataConstants;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface ILinceFileExporter {

    default Double getFps(ILinceProject linceProject) {
        try {
            return Optional.ofNullable(linceProject)
                    .map(ILinceProject::getProfiles)
                    .filter(profiles -> !profiles.isEmpty())
                    .flatMap(profiles -> profiles.stream().findFirst())
                    .map(profile -> {
                        double fps = profile.getFps();
                        return fps > 0 ? fps : LinceDataConstants.DEFAULT_FPS;
                    })
                    .orElse(LinceDataConstants.DEFAULT_FPS);
        } catch (Exception e) {
            return LinceDataConstants.DEFAULT_FPS;
        }
    }

    String getFileFormat();
    String executeFormatConversion(ILinceProject linceProject);
    ByteArrayOutputStream save(String serialization);
    List<String> getColumnDefinitions(ILinceProject linceProject);

}
