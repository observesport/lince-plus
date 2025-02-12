package com.lince.observer.data.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;

/**
 * @apiNote https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html
 * @apiNote https://www.baeldung.com/get-user-in-spring-security
 * @apiNote https://www.springcloud.io/post/2022-02/spring-security-get-current-user/#gsc.tab=0
 */
@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade {

    private static String ANONYMOUS_USER_NAME = "anonymousUser";

    @Override
    public Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        checkAuthentication(authentication);
        return authentication;
    }

    @Override
    public void checkAuthentication(Principal principal) throws RuntimeException {
        if (principal == null || StringUtils.isEmpty(principal.getName()) || StringUtils.equals(ANONYMOUS_USER_NAME, principal.getName())) {
            throw new RuntimeException("Unauthorized access");
        }
    }

}
