package com.lince.observer.data.security;

import org.springframework.security.core.Authentication;

import java.security.Principal;

/**
 * Created by Alberto Soto. 20/5/24
 */
public interface AuthenticationFacade {
    Authentication getAuthentication();
    void checkAuthentication(Principal principal);
}
