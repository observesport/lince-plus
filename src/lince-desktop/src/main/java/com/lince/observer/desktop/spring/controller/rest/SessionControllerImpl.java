package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.bean.Tuple;
import com.lince.observer.data.controller.SessionController;
import com.lince.observer.data.generic.BaseRestControllerWrapper;
import com.lince.observer.data.security.AuthenticationFacade;
import com.lince.observer.desktop.component.ApplicationContextProvider;
import com.lince.observer.data.service.SystemService;
import com.lince.observer.data.common.SessionDataAttributes;
import com.lince.observer.math.service.DataHubService;
import com.lince.observer.data.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * .app.spring.controller
 * Class SessionController
 *
 * @author berto (alberto.soto@gmail.com). 17/04/2018
 */
@RestController
@RequestMapping(value = SessionController.RQ_MAPPING_NAME)
public class SessionControllerImpl extends BaseRestControllerWrapper implements SessionController {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ApplicationContextProvider applicationContextProvider;
    private final LocaleResolver localeResolver;
    protected final SessionService service;
    private final DataHubService dataHubService;
    private final SystemService systemService;
    private ResourceBundle resourceBundle =null;
    private final AuthenticationFacade authenticationFacade;

    private String getActionName() {
        return RQ_MAPPING_NAME;
    }

    @Autowired
    public SessionControllerImpl(SessionService service, DataHubService dataHubService, LocaleResolver localeResolver, ApplicationContextProvider applicationContextProvider, SystemService systemService, AuthenticationFacade authenticationFacade) {
        this.service = service;
        this.dataHubService = dataHubService;
        this.localeResolver = localeResolver;
        this.applicationContextProvider = applicationContextProvider;
        this.systemService = systemService;
        this.authenticationFacade = authenticationFacade;
    }

    /**
     * Gets value from session
     *
     * @param httpSession httpSession by IoC
     * @param key         session parameter
     * @return value from session
     */
    @Override
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

    @Override
    @RequestMapping(value = "/locale")
    public ResponseEntity<String> getLocale(HttpServletRequest request) {
        return new ResponseEntity<>(getLocaleString(request), HttpStatus.OK);
    }

    /**
     * Returns all current session params
     *
     * @param httpSession injected httpSession
     * @return key-map list
     */
    @Override
    @RequestMapping(value = "/getAll", method = RequestMethod.GET
            , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, String>> getAllSessionData(HttpServletRequest rq,
                                                                     HttpSession httpSession,
                                                                     Principal principal) {
        HashMap<String, String> rtn = new HashMap<>();
        try {
            dataHubService.getCurrentDataRegister();//sets register by default
            for (SessionDataAttributes item : SessionDataAttributes.values()) {
                String result;
                switch (item) {
                    case LOCALE:
                        result = getLocaleString(rq);
                        break;
                    case OBSERVER_NAME:
                        result = dataHubService.getCurrentUser().getUserName();
                        break;
                    case OBSERVERS:
                        result = String.valueOf(getNumObservers());
                        break;
                    default:
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
    @Override
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
        return getAllSessionData(rq, httpSession, authenticationFacade.getAuthentication());
    }

    @Override
    @RequestMapping(value = "/getProjectInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMiniInfo(HttpServletRequest rq) {
        JSONObject rtn = new JSONObject();
        try {
            rtn = applicationContextProvider.getLastLinceVersion();
            rtn.put("videos", dataHubService.getVideoPlayList().size());
            rtn.put("scenes", dataHubService.getCurrentDataRegister().size());
            rtn.put("observers", getNumObservers());
            rtn.put("locale", getLocaleString(rq));
            rtn.put("url",systemService.getCurrentServerURI());
            rtn.put("i18n",getI18Messages());
            rtn.put("information", applicationContextProvider.getUserInformation());
        } catch (Exception e) {
            log.error("on project info", e);
        }
        return new ResponseEntity<>(rtn.toString(), HttpStatus.OK);
    }

    /**
     * Returns simplified way of locale, like "es", "en", etc
     *
     * @param request injected request
     * @return locale string
     */
    String getLocaleString(HttpServletRequest request) {
        try {
            Locale currentLocale = localeResolver.resolveLocale(request);
            final String langCode = currentLocale.getLanguage();
            return StringUtils.lowerCase(langCode);
        } catch (Exception e) {
            return "es";
        }
    }

    int getNumObservers() {
        int observers = 0;
        try {
            if (!dataHubService.getUserData().isEmpty()) {
                observers = dataHubService.getUserData().get(0).getUserProfiles().size();
            }
        } catch (Exception e) {
            log.error("Counting observers!", e);
        }
        return observers;
    }

    JSONObject getI18Messages() {
        JSONObject lang = new JSONObject();
        if (this.resourceBundle == null) {
            this.resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());
            lang.put("lang_qr", this.resourceBundle.getString("desktop.web.txt.qr"));
            lang.put("lang_desc_1", this.resourceBundle.getString("desktop.web.txt.p1"));
            lang.put("lang_desc_2", this.resourceBundle.getString("desktop.web.txt.p2"));
            lang.put("option_registers", this.resourceBundle.getString("desktop.web.txt.label1"));
            lang.put("option_observers", this.resourceBundle.getString("desktop.web.txt.label3"));
        }
        return lang;
    }
}
