package com.lince.observer.desktop.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for serving static resources.
 * Configures resource handlers to serve both new frontend (public/) and deprecated frontend (public/deprecated/).
 *
 * Created for frontend migration - serves new React app from public/ root
 * and legacy desktop.html from public/deprecated/
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve deprecated resources at /deprecated/** path
        registry.addResourceHandler("/deprecated/**")
                .addResourceLocations("classpath:/public/deprecated/");

        // Serve new frontend resources from public/ root
        // This must come after deprecated handler to avoid conflicts
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/public/");
    }
}