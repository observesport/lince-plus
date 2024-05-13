package com.deicos.lince.app.spring.controller.rest;

import com.deicos.lince.math.highcharts.HighChartsSerie;
import com.deicos.lince.math.highcharts.HighChartsWrapper;
import com.deicos.lince.math.service.AnalysisService;
import com.deicos.lince.math.service.CategoryService;
import org.fujion.highcharts.Chart;
import org.fujion.highcharts.ChartInstance;
import org.fujion.highcharts.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * com.deicos.lince.app.spring.controller.rest
 * Class ChartsController
 * @author berto (alberto.soto@gmail.com). 07/12/2018
 */
@Deprecated
@RestController
@RequestMapping(value = ChartsController.RQ_MAPPING_NAME)
public class ChartsController {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    static final String RQ_MAPPING_NAME = "/chart";

    private final AnalysisService analysisService;
    private final CategoryService categoryService;

    @Autowired
    public ChartsController(AnalysisService analysisService, CategoryService categoryService) {
        this.analysisService = analysisService;
        this.categoryService = categoryService;
    }

    @RequestMapping(value = "/base", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChartInstance> getData() {
        try {
            //No serializa!!!

            HighChartsWrapper data = analysisService.getRegisterStatsByScene();
            data.setTitle("Criterios visualizados");
            data.setSubtitle("Registro de acciones por escenas");
            //La clase chart me deja controlarlo tb
            Chart chart = new Chart();
            chart.setTitle(data.getTitle());
            chart.setSubtitle(data.getSubtitle());
            for (HighChartsSerie aux : data.getSeries()) {
                Series series = chart.addSeries();
                series.addDataPoints(aux.getData().stream().mapToDouble(Double::doubleValue).toArray());
            }

            //JSONObject elem = new JSONObject();


            return new ResponseEntity<>(chart.instance, HttpStatus.OK);
        } catch (Exception e) {
            log.error("register:get/", e);
            return new ResponseEntity<>(new ChartInstance(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
