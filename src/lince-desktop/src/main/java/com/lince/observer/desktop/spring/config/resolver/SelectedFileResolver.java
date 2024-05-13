package com.lince.observer.desktop.spring.config.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.AbstractResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import java.util.List;

/**
 * lince-scientific-desktop
 * .config.resolver
 * @author berto (alberto.soto@gmail.com)in 21/07/2016.
 * Description:
 */
public class SelectedFileResolver extends AbstractResourceResolver  {
    @Override
    protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
        Resource resource = chain.resolveResource(request, requestPath, locations);
        /*if ((resource == null) || (request != null && !isGzipAccepted(request))) {
            return resource;
        }

        try {
            Resource gzipped = new GzipResourceResolver.GzippedResource(resource);
            if (gzipped.exists()) {
                return gzipped;
            }
        }
        catch (IOException ex) {
            logger.trace("No gzipped resource for [" + resource.getFilename() + "]", ex);
        }*/

        return resource;
        //return null;
    }

    @Override
    protected String resolveUrlPathInternal(String resourceUrlPath, List<? extends Resource> locations, ResourceResolverChain chain) {
        return chain.resolveUrlPath(resourceUrlPath, locations);
    }
}
