package com.deicos.lince.app.spring.controller.rest;

import com.deicos.lince.ai.agreement.AgreementResult;
import com.deicos.lince.data.bean.KeyValue;
import com.deicos.lince.data.bean.RegisterItem;
import com.deicos.lince.data.bean.categories.Category;
import com.deicos.lince.data.bean.categories.CategoryInformation;
import com.deicos.lince.data.bean.wrapper.SceneWrapper;
import com.deicos.lince.math.highcharts.HighChartsSerie;
import com.deicos.lince.math.highcharts.HighChartsWrapper;
import com.deicos.lince.math.service.AnalysisService;
import com.deicos.lince.math.service.CategoryService;
import com.deicos.lince.math.service.StatsService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * lince-scientific-desktop
 * com.deicos.lince.controller.rest
 *
 * @author berto (alberto.soto@gmail.com)in 22/07/2016.
 * Description:
 */
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping(value = AnalysisController.RQ_MAPPING_NAME)
public class AnalysisController {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    static final String RQ_MAPPING_NAME = "/register";

    private final AnalysisService analysisService;
    private final CategoryService categoryService;
    private final StatsService statsService;

    @Autowired
    public AnalysisController(AnalysisService analysisService, CategoryService categoryService, StatsService statsService) {
        this.analysisService = analysisService;
        this.categoryService = categoryService;
        this.statsService = statsService;
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RegisterItem>> getData() {
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
    @RequestMapping(value = "/get/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RegisterItem>> getRegisterById(@PathVariable UUID uuid) {
        try {
            return new ResponseEntity<>(analysisService.getDataRegisterById(uuid), HttpStatus.OK);
        } catch (Exception e) {
            log.error("register:get/", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "/clear", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RegisterItem>> clearData(HttpServletRequest request, @RequestBody SceneWrapper items) {
        try {
            if (items.getMoment() != null) {
                analysisService.deleteMomentInfo(items.getMoment());
            }
            List<RegisterItem> nodeList = analysisService.getDataRegister();
            return new ResponseEntity<>(nodeList, HttpStatus.OK);
        } catch (Exception e) {
            log.error("register:clear/", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/pushscene", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<List<RegisterItem>> pushScene(HttpServletRequest request, @RequestBody SceneWrapper items) {
        try {
            List<Category> data = new ArrayList<>();
            Double sceneMoment = items.getMoment();
            analysisService.pushRegister(sceneMoment, null);
            return getData();
        } catch (Exception e) {
            log.error("register:push", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Deprecated
    @RequestMapping(value = "/set/{momentID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<List<RegisterItem>> saveData(HttpServletRequest request
            , @PathVariable String momentID, @RequestBody SceneWrapper items) {
        try {
            List<Category> data = new ArrayList<>();
            Double sceneMoment = items.getMoment();//(moment==0)? :moment;
            /*
            List<Category> data = new ArrayList<>();
            for (Category item : items) {
                if (item.getId() != null) {
                    Pair<Criteria, Category> pair = categoryService.findTreeEntryById(item.getId());
                    if (pair.getValue().getId() == item.getId()) {
                        data.add(pair.getValue());
                    }
                }
            }*/
            RegisterItem item = new RegisterItem();
            if (items.getMoment() != null) {
                item.setVideoTime(items.getMoment());
            }
            item.setName(items.getName());
            item.setDescription(items.getDescription());
            item.setId(new Integer(momentID)); //SUPER Error TODO:review urgently
            analysisService.pushRegister(item); //data.toArray(new Category[data.size()])
            return getData();
        } catch (Exception e) {
            log.error("register:save", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/setMomentData", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<List<RegisterItem>> saveData(HttpServletRequest request, @RequestBody SceneWrapper items) {
        try {
            if (items.getMoment() == null) {
                throw new NullPointerException();
            }
            RegisterItem scene = new RegisterItem();
            scene.setVideoTime(items.getMoment());
            scene.setRegister(scene.getRegister());//=>no tiene sentido
            // TODO 2020 ASF: This logic should be in another layer
            if (CollectionUtils.isNotEmpty(items.getCategories())) {
                List<Category> dataValues = new ArrayList<>();
                for (Category userCategory : items.getCategories()) {
                    boolean doAdd = true;
                    Category fullCat = (Category) categoryService.findCategoryByCode(userCategory.getCode());
                    if (CategoryInformation.class.isAssignableFrom(fullCat.getClass())) {
                        if (StringUtils.isEmpty(userCategory.getNodeInformation())) {
                            doAdd = false;
                        } else {
                            fullCat.setNodeInformation(userCategory.getNodeInformation());
                        }
                    }
                    if (doAdd) {
                        dataValues.add(fullCat);
                    }
                }
                scene.setRegister(dataValues);
            }
            analysisService.pushRegister(scene);
            return getData();
        } catch (Exception e) {
            log.error("register:save", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Stats for time visualization chart
     *
     * @return
     */
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
            /*if (rtn == null) {
                //misterios sin resolver!petan las stats si el registro esta vacio
                rtn = new JSONObject();
            }*/
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
    @RequestMapping(value = "/kappa/{uuid1}/{uuid2}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<KeyValue<String, Double>>> calculateKappa(@PathVariable String uuid1, @PathVariable String uuid2) {
        try {
            return new ResponseEntity<>(statsService.calculateKappa(UUID.fromString(uuid1), UUID.fromString(uuid2)), HttpStatus.OK);
        } catch (Exception e) {
            log.error("analysis/kappa", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/kappaPro/{uuid1}/{uuid2}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AgreementResult>> getKappaPro(@PathVariable String uuid1, @PathVariable String uuid2) {
        try {
            return new ResponseEntity<>(statsService.calculateKappaPro(UUID.fromString(uuid1)
                    , UUID.fromString(uuid2)), HttpStatus.OK);
        } catch (Exception e) {
            log.error("analysis/kappa", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/matrix/{uuid1}/{uuid2}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AgreementResult>> getMatrix(@PathVariable String uuid1, @PathVariable String uuid2) {
        try {
            return new ResponseEntity<>(statsService.getContingencyMatrix(UUID.fromString(uuid1)
                    , UUID.fromString(uuid2)), HttpStatus.OK);
        } catch (Exception e) {
            log.error("analysis/kappa", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/dummy", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<List<AgreementResult>> dummy(HttpServletRequest request, @RequestBody String uuids) {
        try {
            return new ResponseEntity<>(new ArrayList<AgreementResult>(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("krippendorf:stats", e);
            return new ResponseEntity<>(new ArrayList<AgreementResult>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/krippendorf", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<List<AgreementResult>> getKrippendorf(HttpServletRequest request, @RequestBody UUID[] uuids) {
        try {
            return new ResponseEntity<>(statsService.calculateKrippendorf(uuids), HttpStatus.OK);
        } catch (Exception e) {
            log.error("krippendorf:stats", e);
            return new ResponseEntity<>(new ArrayList<AgreementResult>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/percentageAgreement", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<List<AgreementResult>> getPercentageAgreement(HttpServletRequest request, @RequestBody UUID[] uuids) {
        try {
            return new ResponseEntity<>(statsService.calculateAgreementPercentage(uuids), HttpStatus.OK);
        } catch (Exception e) {
            log.error("krippendorf:stats", e);
            return new ResponseEntity<>(new ArrayList<AgreementResult>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
