package com.lince.observer.data.controller;

import com.lince.observer.data.bean.KeyValue;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.agreement.AgreementResult;
import com.lince.observer.data.bean.highcharts.HighChartsWrapper;
import com.lince.observer.data.bean.wrapper.SceneWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * Created by Alberto Soto. 15/4/24
 */
public interface AnalysisController {

    String RQ_MAPPING_NAME = "/register";

    @RequestMapping(value = {"/get/", "/get"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<RegisterItem>> getOrderedRegister(Principal principal);

    @RequestMapping(value = "/get/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<RegisterItem>> getRegisterById(@PathVariable UUID uuid);

    @RequestMapping(value = "/clear", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<RegisterItem>> clearRegisterData(HttpServletRequest request, @RequestBody SceneWrapper items);

    @RequestMapping(value = "/pushscene", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    ResponseEntity<List<RegisterItem>> pushScene(HttpServletRequest request, @RequestBody SceneWrapper items, Principal principal);

    @Deprecated
    @RequestMapping(value = "/set/{momentID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    ResponseEntity<List<RegisterItem>> saveScene(HttpServletRequest request, @PathVariable String momentID, @RequestBody SceneWrapper items, Principal principal);

    @RequestMapping(value = {"/setMomentData", "/setMomentData/"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    ResponseEntity<List<RegisterItem>> saveScene(HttpServletRequest request, @RequestBody SceneWrapper items, Principal principal);

    @RequestMapping(value = "/timeStats", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<HighChartsWrapper> getTimeStats();

    @RequestMapping(value = "/pieStats", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> getPieStats();

    @RequestMapping(value = "/kappa/{uuid1}/{uuid2}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<KeyValue<String, Double>>> getLegacyKappaIndex(@PathVariable String uuid1, @PathVariable String uuid2);

    @RequestMapping(value = "/kappaPro/{uuid1}/{uuid2}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<AgreementResult>> getAgreementKappaPro(@PathVariable String uuid1, @PathVariable String uuid2);

    @RequestMapping(value = "/matrix/{uuid1}/{uuid2}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<AgreementResult>> getContingencyMatrix(@PathVariable String uuid1, @PathVariable String uuid2);

    @RequestMapping(value = "/krippendorf", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    ResponseEntity<List<AgreementResult>> getAgreementKrippendorf(HttpServletRequest request, @RequestBody UUID[] uuids);

    @RequestMapping(value = "/percentageAgreement", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    ResponseEntity<List<AgreementResult>> getAgreementPercentage(HttpServletRequest request, @RequestBody UUID[] uuids);
}
