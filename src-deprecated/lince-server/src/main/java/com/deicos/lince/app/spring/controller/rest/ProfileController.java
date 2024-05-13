package com.deicos.lince.app.spring.controller.rest;

import com.deicos.lince.app.base.common.DataTableWrapper;
import com.deicos.lince.data.bean.RegisterItem;
import com.deicos.lince.data.bean.user.ResearchProfile;
import com.deicos.lince.math.service.DataHubService;
import com.deicos.lince.math.service.ProfileService;
import com.deicos.lince.math.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * lince-scientific-base
 * com.deicos.lince.app.spring.controller.rest
 * Created by Alberto Soto Fernandez in 04/04/2017.
 * Description:
 */
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping(value = ProfileController.RQ_MAPPING_NAME)
public class ProfileController {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    static final String RQ_MAPPING_NAME = "/profile";
    private final ProfileService profileService;
    private final DataHubService dataHubService;
    private final SessionService sessionService;

    @Autowired
    public ProfileController(ProfileService profileService, DataHubService dataHubService, SessionService sessionService) {
        this.profileService = profileService;
        this.dataHubService = dataHubService;
        this.sessionService = sessionService;
    }

    private String getActionName() {
        return RQ_MAPPING_NAME;
    }


    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataTableWrapper<ResearchProfile>> getResearchInfo() {
        try {
            return new ResponseEntity<>(new DataTableWrapper<>(profileService.getAllResearchInfo()), HttpStatus.OK);
        } catch (Exception e) {
            log.error(getActionName() + ":get/", e);
            return new ResponseEntity<>(new DataTableWrapper<ResearchProfile>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity<DataTableWrapper<ResearchProfile>> saveAll(HttpServletRequest request
            , @RequestBody ResearchProfile item) {
        try {
            if (item != null) {
                profileService.setResearchProfile(item);
            }
            return getResearchInfo();
        } catch (Exception e) {
            log.error("ProfileController:save", e);
            return new ResponseEntity<>(new DataTableWrapper<ResearchProfile>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * TODO 2020
     *
     * @param httpSession
     * @param key
     * @return
     */
    @RequestMapping(value = "/getRegister/{key}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RegisterItem>> getRegisterByKey(HttpSession httpSession, @PathVariable String key) {
        try {
            log.info("asking for register 4 " + key);
            //profileService.getCurrentResearch().getUserProfiles()
            return new ResponseEntity<>(dataHubService.getCurrentDataRegister(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("rest:save", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* Equivalente a /session/set/register/{value}
    @RequestMapping(value = "/setRegisterActive/{key}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RegisterItem>> getRegisterByProfile(HttpSession httpSession, @PathVariable String key) {
        try {
            log.info("asking for register 4 " + key);
            //profileService.getCurrentResearch().getUserProfiles()
            return new ResponseEntity<>(dataHubService.getCurrentDataRegister(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("rest:save", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    */

}
