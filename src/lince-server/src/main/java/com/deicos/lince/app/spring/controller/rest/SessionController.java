package com.deicos.lince.app.spring.controller.rest;

import com.deicos.lince.app.base.BaseRestControllerWrapper;
import com.deicos.lince.data.bean.Tuple;
import com.deicos.lince.math.SessionDataAttributes;
import com.deicos.lince.math.service.DataHubService;
import com.deicos.lince.math.service.SessionService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.Function;

/**
 * com.deicos.lince.app.spring.controller
 * Class SessionController
 *
 * @author berto (alberto.soto@gmail.com). 17/04/2018
 */
@RestController
@RequestMapping(value = SessionController.RQ_MAPPING_NAME)
public class SessionController extends BaseRestControllerWrapper {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    static final String RQ_MAPPING_NAME = "/session";
    private final LocaleResolver localeResolver;

    private String getActionName() {
        return RQ_MAPPING_NAME;
    }

    protected final SessionService service;
    private final DataHubService dataHubService;

    @Autowired
    public SessionController(SessionService service, DataHubService dataHubService, LocaleResolver localeResolver) {
        this.service = service;
        this.dataHubService = dataHubService;
        this.localeResolver = localeResolver;
    }


    /**
     * Gets value from session
     *
     * @param httpSession httpSession by IoC
     * @param key         session parameter
     * @return value from session
     */
    @RequestMapping(value = "/get/{key}", method = RequestMethod.GET
            , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, String>> getSessionData(HttpSession httpSession, @PathVariable String key) {
        HashMap<String, String> rtn = new HashMap<String, String>();
        try {
            String result = service.getSessionData(httpSession, SessionDataAttributes.castString(key));
            rtn.put(key, result);
            return new ResponseEntity<>(rtn, HttpStatus.OK);
        } catch (Exception e) {
            log.error(getActionName() + ":get/", e);
            return new ResponseEntity<>(rtn, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/locale")
    public ResponseEntity<String> getLocale(HttpServletRequest request) {
        return new ResponseEntity<>(getLocaleString(request), HttpStatus.OK);
    }

    /**
     * Returns simplified way of locale, like "es", "en", etc
     *
     * @param request injected request
     * @return locale string
     */
    private String getLocaleString(HttpServletRequest request) {
        try {
            Locale currentLocale = localeResolver.resolveLocale(request);
            final String langCode = currentLocale.getLanguage();
            return StringUtils.lowerCase(langCode);
        } catch (Exception e) {
            return "es";
        }
    }

    /**
     * Returns all current session params
     *
     * @param httpSession injected httpSession
     * @return key-map list
     */
    @RequestMapping(value = "/getAll", method = RequestMethod.GET
            , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, String>> getAllSessionData(HttpServletRequest rq,
                                                                     HttpSession httpSession) {
        HashMap<String, String> rtn = new HashMap<>();
        try {
            dataHubService.getCurrentDataRegister();//sets register by default
            for (SessionDataAttributes item : SessionDataAttributes.values()) {
                String result;
                if (item.equals(SessionDataAttributes.LOCALE)) {
                    result = getLocaleString(rq);
                } else {
                    result = service.getSessionData(httpSession, SessionDataAttributes.castString(item.getItemValue()));
                }

                rtn.put(item.getItemValue(), result);
            }
            return new ResponseEntity<>(rtn, HttpStatus.OK);
        } catch (Exception e) {
            log.error(getActionName() + ":get/", e);
            return new ResponseEntity<>(rtn, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Sets value in session if value is correct
     *
     * @param httpSession httpSession from spring
     * @param key         pathVar. Attribute to set in sessión
     * @param value       pathVar. Attribute's value
     * @return current value
     */
    @RequestMapping(value = "/set/{key}/{value}", method = RequestMethod.GET
            , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, String>> setSessionData(HttpServletRequest rq, HttpSession httpSession
            , @PathVariable String key
            , @PathVariable String value) {
        Function<Tuple<String, String>, String> func = data -> {
            boolean isSet = service.setSessionData(httpSession, SessionDataAttributes.castString(data.key), data.value);
            return isSet ? data.toString() : "ERROR";
        };
        executeResponseItem(new Tuple<>(key, value), func);
        return getAllSessionData(rq, httpSession);
    }

    @RequestMapping(value = "/getProjectInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMiniInfo() {
        JSONObject rtn = new JSONObject();
        try {
            rtn.put("videos", dataHubService.getVideoPlayList().size());
            rtn.put("scenes", dataHubService.getCurrentDataRegister().size());
            int observers = 0;
            if (!dataHubService.getUserData().isEmpty()) {
                observers = dataHubService.getUserData().get(0).getUserProfiles().size();
            }
            rtn.put("observers", observers);
        } catch (Exception e) {
            log.error("on project info", e);
        }
        return new ResponseEntity<>(rtn.toString(), HttpStatus.OK);
    }

}
