package com.lince.observer.data.javafx;

import com.lince.observer.data.ILinceProject;

/**
 *
 */
@FunctionalInterface
public interface ExportCommandInitCallback {
    void onExport(ILinceProject project);
}