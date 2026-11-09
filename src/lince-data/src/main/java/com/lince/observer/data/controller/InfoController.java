package com.lince.observer.data.controller;

import com.lince.observer.data.bean.info.ApplicationHealth;
import com.lince.observer.data.bean.info.ApplicationInfo;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Unauthenticated application info and health check (LO-343).
 * <p>
 * Mounted under the same root as the other REST controllers ({@code /register}, {@code /profile}, ...),
 * so the dashboard resolves it as same-origin {@code /info} on desktop and
 * {@code <api-root>/info} on cloud. Cloud must add {@code /info/**} to its auth whitelist.
 * <p>
 * Created by Alberto Soto. 23/9/26
 */
public interface InfoController {

    String RQ_MAPPING_NAME = "/info";
    String HEALTH_PATH = "/health";

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApplicationInfo> getInfo();

    @RequestMapping(value = HEALTH_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApplicationHealth> getHealth();
}
