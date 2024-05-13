package com.lince.observer.desktop.spring.controller;

import org.springframework.web.servlet.view.RedirectView;

/**
 * .app.spring.controller
 * Class UnhandledCustomizer
 * 24/06/2020
 * Partial solution from https://github.com/dsyer/spring-boot-spa
 *
 * @author berto (alberto.soto@gmail.com)
 */
//@Component
//@ControllerAdvice

@Deprecated
public class UnhandledCustomizer {
//    @ExceptionHandler(NoHandlerFoundException.class)
    public RedirectView notFound() {
        return new RedirectView("/");
    }
}

