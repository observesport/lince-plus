package com.lince.observer.data.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;
import java.util.HashMap;

/**
 * Created by Alberto Soto. 14/5/24
 */
public interface SessionController {

    String RQ_MAPPING_NAME = "/session";

    @RequestMapping(value = "/get/{key}", method = RequestMethod.GET
            , produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<HashMap<String, String>> getSessionData(HttpSession httpSession, @PathVariable String key);

    @RequestMapping(value = "/locale")
    ResponseEntity<String> getLocale(HttpServletRequest request);

    @RequestMapping(value = "/getAll", method = RequestMethod.GET
            , produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<HashMap<String, String>> getAllSessionData(HttpServletRequest rq,
                                                              HttpSession httpSession,
                                                              Principal principal);

    @RequestMapping(value = "/set/{key}/{value}", method = RequestMethod.GET
            , produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<HashMap<String, String>> setSessionData(HttpServletRequest rq, HttpSession httpSession
            , @PathVariable String key
            , @PathVariable String value
            , Principal principal);

    @RequestMapping(value = "/getProjectInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> getMiniInfo(HttpServletRequest rq);

}
