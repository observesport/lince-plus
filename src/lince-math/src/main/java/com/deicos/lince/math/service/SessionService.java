package com.deicos.lince.math.service;

import com.deicos.lince.math.SessionDataAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * com.deicos.lince.math.service
 * Class SessionService
 * 01/03/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
@Service
public class SessionService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Sets an attribute in session in a valid way.
     * Supports generics in a dummy way
     *
     * @param key   id in session to store data. Reflected as LABEL in enum, uppercase
     * @param value value. Allows empty and any kind of object
     * @return boolean as result of operation
     */
    public <T> boolean setSessionData(HttpSession httpSession, SessionDataAttributes key, T value) {
        try {
            if (httpSession != null && key != null) {
                httpSession.setAttribute(key.getItemLabel(), value);
            }
            return true;
        } catch (Exception e) {
            log.error("Managing session: set", e);
        }
        return false;
    }

    /**
     * Get value in session and returns the value as the defined type ob object
     * Supports generic
     *
     * @param key id
     * @param <T> expected data type
     * @return Generic object cast
     */
    @SuppressWarnings(value = "unchecked")
    public <T> T getSessionData(HttpSession httpSession, SessionDataAttributes key) {
        try {
            if (httpSession != null && key != null) {
                return (T) httpSession.getAttribute(key.getItemLabel());
            }
        } catch (Exception e) {
            log.error("Managing session: set", e);
        }
        return null;
    }

    /**
     * Overloaded method to set parameter by default on session
     * @param httpSession   current httpSession injected from controller or another via spring way
     * @param key           parameter key on context
     * @param defaultValue  default parameter. Used to set on context if it doesnt exists
     * @param <T>           Generic Type you want to store on session
     * @return              Session data or default parameter. If null means an error happened.
     */
    public <T> T getSessionData(HttpSession httpSession, SessionDataAttributes key, T defaultValue) {
        T current = null;
        try {
            current = getSessionData(httpSession, key);
            if (current == null && defaultValue != null) {
                setSessionData(httpSession, key, defaultValue);
                return defaultValue;
            }
        } catch (Exception e) {
            log.error("get Session Data with default value", e);
        }
        return current;
    }


}
