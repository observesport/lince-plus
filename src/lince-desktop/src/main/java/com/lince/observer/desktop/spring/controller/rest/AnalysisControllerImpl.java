package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.bean.agreement.AgreementResult;
import com.lince.observer.data.bean.KeyValue;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.wrapper.SceneWrapper;
import com.lince.observer.data.bean.highcharts.HighChartsSerie;
import com.lince.observer.data.bean.highcharts.HighChartsWrapper;
import com.lince.observer.data.controller.AnalysisController;
import com.lince.observer.data.service.AnalysisService;
import com.lince.observer.data.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * lince-scientific-desktop
 * .controller.rest
 *
 * @author berto (alberto.soto@gmail.com)in 22/07/2016.
 * Description:
 */
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping(value = AnalysisController.RQ_MAPPING_NAME)
public class AnalysisControllerImpl implements AnalysisController {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private final AnalysisService analysisService;
    private final StatsService statsService;

    @Autowired
    public AnalysisControllerImpl(AnalysisService analysisService, StatsService statsService) {
        this.analysisService = analysisService;
        this.statsService = statsService;
    }

    @Override
    @RequestMapping(value = {"/get/","/get"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RegisterItem>> getOrderedRegister(Principal principal) {
        try {
            List<RegisterItem> nodeList = analysisService.getOrderedRegister();
            return new ResponseEntity<>(nodeList, HttpStatus.OK);
        } catch (Exception e) {
            log.error("register:get/", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check it works perfectly
     *
     * @param uuid
     * @return
     */
    @Override
    @RequestMapping(value = "/get/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RegisterItem>> getRegisterById(@PathVariable UUID uuid) {
        try {
            return new ResponseEntity<>(analysisService.getDataRegisterById(uuid), HttpStatus.OK);
        } catch (Exception e) {
            log.error("register:get/", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    @RequestMapping(value = "/clear", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RegisterItem>> clearRegisterData(HttpServletRequest request, @RequestBody SceneWrapper items) {
        try {
            if (!analysisService.deleteRegisterById(items.getId())) {
                if (items.getMoment() != null) {
                    analysisService.deleteMomentInfo(items.getMoment());
                }
            }
            List<RegisterItem> nodeList = analysisService.getDataRegister();
            return new ResponseEntity<>(nodeList, HttpStatus.OK);
        } catch (Exception e) {
            log.error("register:clear/", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @RequestMapping(value = "/pushscene", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<List<RegisterItem>> pushScene(HttpServletRequest request, @RequestBody SceneWrapper items, Principal principal) {
        try {
            Double sceneMoment = items.getMoment();
            analysisService.pushRegister(sceneMoment, null);
            return getOrderedRegister(principal);
        } catch (Exception e) {
            log.error("register:push", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Deprecated
    @RequestMapping(value = "/set/{momentID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<List<RegisterItem>> saveScene(HttpServletRequest request,
                                                        @PathVariable String momentID,
                                                        @RequestBody SceneWrapper items,
                                                        Principal principal) {
        try {
            RegisterItem item = new RegisterItem();
            if (items.getMoment() != null) {
                item.setVideoTime(items.getMoment());
            }
            item.setName(items.getName());
            item.setDescription(items.getDescription());
            item.setId(Integer.valueOf(momentID)); //SUPER Error TODO:review urgently
            analysisService.pushRegister(item);
        } catch (Exception e) {
            log.error("register:save", e);
//            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return getOrderedRegister(principal);
    }

    @Override
    @RequestMapping(value = {"/setMomentData","/setMomentData/"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<List<RegisterItem>> saveScene(HttpServletRequest request,
                                                        @RequestBody SceneWrapper items,
                                                        Principal principal) {
        try {
            if (items.getMoment() != null) {
                RegisterItem scene = new RegisterItem();
                scene.setVideoTime(items.getMoment());
                if (items.getId() != null){
                    scene.setId(items.getId());
                }
                scene = analysisService.loadCategoriesByCode(scene, items.getCategories());
                analysisService.pushRegister(scene);
            }
        } catch (Exception e) {
            log.error("register:setMomentData - push scene", e);
//            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR); BREAKS rendering
        }
        return getOrderedRegister(principal);
    }

    /**
     * Stats for time visualization chart
     *
     * @return
     */
    @Override
    @RequestMapping(value = "/timeStats", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HighChartsWrapper> getTimeStats() {
        try {
            HighChartsWrapper data = analysisService.getRegisterStatsByScene();
            data.setTitle("Criterios visualizados");
            data.setSubtitle("Registro de acciones por escenas");
            return new ResponseEntity<>(data, HttpStatus.OK);
        } catch (Exception e) {
            log.error("register:statistics/", e);
            return new ResponseEntity<>(new HighChartsWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Stats for pie percentage Category counter
     *
     * @return
     */
    @Override
    @RequestMapping(value = "/pieStats", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPieStats() {
        try {
            HighChartsWrapper data = analysisService.getRegisterStatsByCategory();
            JSONObject rtn = new JSONObject();
            for (HighChartsSerie serie : data.getSeries()) {
                rtn.put("criteria", serie.getDataBean());
            }
            if (!data.getDrilldown().isEmpty()) {
                for (HighChartsWrapper wrap : data.getDrilldown()) {
                    for (HighChartsSerie serie : wrap.getSeries()) {
                        rtn.put(serie.getName(), serie.getDataBean());
                    }
                }
            }
            return new ResponseEntity<>(rtn.toString(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("register:statistics/", e);
            return new ResponseEntity<>(StringUtils.EMPTY, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * lince.modelo.Registro#calcularKappa(java.io.File, java.util.List)
     *
     * @return
     */
    @Override
    @RequestMapping(value = "/kappa/{uuid1}/{uuid2}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<KeyValue<String, Double>>> getLegacyKappaIndex(@PathVariable String uuid1, @PathVariable String uuid2) {
        try {
            return new ResponseEntity<>(statsService.calculateLegacyKappa(UUID.fromString(uuid1), UUID.fromString(uuid2)), HttpStatus.OK);
        } catch (Exception e) {
            log.error("analysis/kappa", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @RequestMapping(value = "/kappaPro/{uuid1}/{uuid2}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AgreementResult>> getAgreementKappaPro(@PathVariable String uuid1, @PathVariable String uuid2) {
        try {
            return new ResponseEntity<>(statsService.calculateKappaPro(UUID.fromString(uuid1)
                    , UUID.fromString(uuid2)), HttpStatus.OK);
        } catch (Exception e) {
            log.error("analysis/kappa", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @RequestMapping(value = "/matrix/{uuid1}/{uuid2}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AgreementResult>> getContingencyMatrix(@PathVariable String uuid1, @PathVariable String uuid2) {
        try {
            return new ResponseEntity<>(statsService.getContingencyMatrix(UUID.fromString(uuid1)
                    , UUID.fromString(uuid2)), HttpStatus.OK);
        } catch (Exception e) {
            log.error("analysis/kappa", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @RequestMapping(value = "/dummy", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
//    public ResponseEntity<List<AgreementResult>> dummy(HttpServletRequest request, @RequestBody String uuids) {
//        try {
//            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
//        } catch (Exception e) {
//            log.error("krippendorf:stats", e);
//            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @Override
    @RequestMapping(value = "/krippendorf", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<List<AgreementResult>> getAgreementKrippendorf(HttpServletRequest request, @RequestBody UUID[] uuids) {
        try {
            return new ResponseEntity<>(statsService.calculateKrippendorf(uuids), HttpStatus.OK);
        } catch (Exception e) {
            log.error("krippendorf:stats", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @RequestMapping(value = "/percentageAgreement", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<List<AgreementResult>> getAgreementPercentage(HttpServletRequest request, @RequestBody UUID[] uuids) {
        try {
            return new ResponseEntity<>(statsService.calculateAgreementPercentage(uuids), HttpStatus.OK);
        } catch (Exception e) {
            log.error("krippendorf:stats", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
