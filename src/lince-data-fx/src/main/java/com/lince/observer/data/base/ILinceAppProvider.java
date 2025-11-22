package com.lince.observer.data.base;

import java.util.ServiceLoader;

/**
 * Provider interface for ILinceApp instances
 * This allows proper dependency injection from desktop module
 *
 * @author berto (alberto.soto@gmail.com)
 */
public interface ILinceAppProvider {
    /**
     * Gets the current ILinceApp instance
     * @return ILinceApp instance
     */
    ILinceApp getLinceApp();

    /**
     * Static helper method to get ILinceApp instance using ServiceLoader
     * @return ILinceApp instance
     * @throws IllegalStateException if no provider implementation is found
     */
    static ILinceApp getInstance() {
        ServiceLoader<ILinceAppProvider> loader = ServiceLoader.load(ILinceAppProvider.class);
        ILinceAppProvider provider = loader.findFirst()
                .orElseThrow(() -> new IllegalStateException("No ILinceAppProvider implementation found"));
        return provider.getLinceApp();
    }
}