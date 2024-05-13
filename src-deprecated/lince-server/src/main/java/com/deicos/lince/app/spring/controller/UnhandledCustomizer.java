package com.deicos.lince.app.spring.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.view.RedirectView;

/**
 * com.deicos.lince.app.spring.controller
 * Class UnhandledCustomizer
 * 24/06/2020
 * Partial solution from https://github.com/dsyer/spring-boot-spa
 *
 * @author berto (alberto.soto@gmail.com)
 */
//@Component
//@ControllerAdvice
public class UnhandledCustomizer {
//    @ExceptionHandler(NoHandlerFoundException.class)
    public RedirectView notFound() {
        return new RedirectView("/");
    }
}

