package com.lince.observer.math;

import com.lince.observer.data.service.AnalysisService;
import com.lince.observer.data.service.CategoryService;
import com.lince.observer.data.service.ProfileService;
import com.lince.observer.data.service.SessionService;
import com.lince.observer.math.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

/**
 * Created by Alberto Soto. 17/2/25
 */
@Configuration
public class LinceServiceTestConfig {
    @Bean
    public HttpServletRequest httpServletRequest() {
        return mock(HttpServletRequest.class);
    }

    @Bean
    public SessionService sessionService() {
        return mock(SessionService.class);
    }

    @Bean
    public DataHubService dataHubService(SessionService sessionService, HttpServletRequest httpServletRequest) {
        return new DataHubService(sessionService, httpServletRequest);
    }

    @Bean
    public CategoryService categoryService(DataHubService dataHubService) {
        return new CategoryServiceImpl(dataHubService);
    }

    @Bean
    public ProfileService profileService(DataHubService dataHubService) {
        return new ProfileServiceImpl(dataHubService);
    }

    @Bean
    public AnalysisService analysisService(CategoryService categoryService, ProfileService profileService, DataHubService dataHubService) {
        return new AnalysisServiceImpl(categoryService, profileService, dataHubService);
    }

    @Bean
    public LegacyConverterService legacyConverterService(AnalysisService analysisService,DataHubService dataHubService,CategoryService categoryService) {
        return new LegacyConverterService( categoryService, analysisService, dataHubService);
    }

}
