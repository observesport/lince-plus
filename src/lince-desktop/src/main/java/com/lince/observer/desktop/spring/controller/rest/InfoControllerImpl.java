package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.bean.info.ApplicationHealth;
import com.lince.observer.data.bean.info.ApplicationInfo;
import com.lince.observer.data.controller.InfoController;
import com.lince.observer.data.service.ApplicationInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desktop implementation of {@link InfoController} (LO-343).
 * Same-origin {@code GET /info} and {@code GET /info/health} for the dashboard.
 * <p>
 * Created by Alberto Soto. 23/9/26
 */
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping(value = InfoController.RQ_MAPPING_NAME)
public class InfoControllerImpl implements InfoController {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ApplicationInfoService applicationInfoService;

    public InfoControllerImpl(ApplicationInfoService applicationInfoService) {
        this.applicationInfoService = applicationInfoService;
    }

    @Override
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationInfo> getInfo() {
        try {
            return new ResponseEntity<>(applicationInfoService.getApplicationInfo(), HttpStatus.OK);
        } catch (Exception e) {
            log.error(RQ_MAPPING_NAME, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @RequestMapping(value = HEALTH_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationHealth> getHealth() {
        try {
            return new ResponseEntity<>(applicationInfoService.getHealth(), HttpStatus.OK);
        } catch (Exception e) {
            log.error(RQ_MAPPING_NAME + HEALTH_PATH, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
