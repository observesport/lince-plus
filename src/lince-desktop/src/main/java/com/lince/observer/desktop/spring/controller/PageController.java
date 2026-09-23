package com.lince.observer.desktop.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Lince_v2
 * .controller
 *
 * @author berto (alberto.soto@gmail.com)in 26/01/2016.
 * Description: server-side entry points that hand over to the static frontends.
 * Any other route falls through to the SPA fallback configured in WebConfig.
 */
@Controller
public class PageController {

    @RequestMapping(value = {"/dashboard", "/dashboard/*"})
    public String dashboard() {
        return "forward:/index.html";
    }

    @RequestMapping("/desktop")
    public String desktop() {
        return "forward:/desktop/index.html";
    }

}
