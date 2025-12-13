package com.lince.observer.desktop.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

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

        // Serve desktop resources at /desktop/** path
        registry.addResourceHandler("/desktop/**")
                .addResourceLocations("classpath:/public/desktop/");

        // Serve new frontend resources from public/ root
        // Use custom resolver for SPA fallback - forwards to index.html for unmatched routes
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/public/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    public Resource resolveResource(HttpServletRequest request, String requestPath,
                                                     List<? extends Resource> locations, ResourceResolverChain chain) {
                        Resource resource = super.resolveResource(request, requestPath, locations, chain);

                        // If resource found, return it
                        if (resource != null) {
                            return resource;
                        }

                        // For SPA: if no resource found and path doesn't start with special prefixes,
                        // return index.html to let React Router handle it
                        if (!requestPath.startsWith("api/") &&
                            !requestPath.startsWith("actuator/") &&
                            !requestPath.startsWith("m/") &&
                            !requestPath.contains(".")) {
                            return new ClassPathResource("/public/index.html");
                        }

                        return null;
                    }
                });
    }
}