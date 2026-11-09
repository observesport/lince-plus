package com.lince.observer.desktop.component;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.component.ILinceDataOperationsComponent;
import com.lince.observer.desktop.spring.service.CurrentProjectProvider;
import org.springframework.stereotype.Component;

/**
 * Loaded project access, delegating to {@link CurrentProjectProvider} (LO-349) so the project is assembled in one place.
 */
@Component
public class LinceDataOperationsComponent implements ILinceDataOperationsComponent {

    protected final CurrentProjectProvider currentProjectProvider;

    public LinceDataOperationsComponent(CurrentProjectProvider currentProjectProvider) {
        this.currentProjectProvider = currentProjectProvider;
    }

    @Override
    public ILinceProject getCurrentProject() {
        return currentProjectProvider.current();
    }

}
