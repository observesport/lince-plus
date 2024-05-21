package com.lince.observer.data.security;

import org.springframework.security.core.Authentication;
/**
 * Created by Alberto Soto. 20/5/24
 */
public interface AuthenticationFacade {
    Authentication getAuthentication();
}
