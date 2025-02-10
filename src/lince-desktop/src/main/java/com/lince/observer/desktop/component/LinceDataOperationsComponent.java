package com.lince.observer.desktop.component;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.component.ILinceDataOperationsComponent;
import com.lince.observer.math.service.DataHubService;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class LinceDataOperationsComponent implements ILinceDataOperationsComponent {

    protected final DataHubService dataHubService;

    public LinceDataOperationsComponent(DataHubService dataHubService) {
        this.dataHubService = dataHubService;
    }

    @Override
    public ILinceProject getCurrentProject() {
        LinceFileProjectWrapper wrapper = new LinceFileProjectWrapper();
        wrapper.setProfiles(dataHubService.getUserData());
        wrapper.setObservationTool(dataHubService.getCriteria());
        wrapper.setRegister(dataHubService.getDataRegister());
        wrapper.setYoutubeVideoPlayList(dataHubService.getYoutubeVideoPlayList());
        wrapper.setVideoPlayList(dataHubService.getVideoPlayList());
        return wrapper;
    }

}
