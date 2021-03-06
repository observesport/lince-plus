package com.deicos.lince.app.spring.controller.rest;

import com.deicos.lince.app.base.common.DataTableWrapper;
import com.deicos.lince.data.bean.categories.CategoryData;
import com.deicos.lince.math.service.AnalysisService;
import com.deicos.lince.math.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * lince-scientific-base
 * com.deicos.lince.app.spring.controller.rest
 * Created by Alberto Soto Fernandez in 04/04/2017.
 * Description:
 */
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping(value = DataTableController.RQ_MAPPING_NAME)
public class DataTableController {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    static final String RQ_MAPPING_NAME = "/datatable";
    private final CategoryService categoryService;
    private final AnalysisService analysisService;

    @Autowired
    public DataTableController(CategoryService categoryService, AnalysisService analysisService) {
        this.categoryService = categoryService;
        this.analysisService = analysisService;
    }

    private String getActionName() {
        return RQ_MAPPING_NAME;
    }


    @RequestMapping(value = "/categories", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataTableWrapper<CategoryData>> getCategories() {
        try {
            return new ResponseEntity<>(new DataTableWrapper<>(categoryService.getCollection()), HttpStatus.OK);
        } catch (Exception e) {
            log.error(getActionName() + ":get/", e);
            return new ResponseEntity<>(new DataTableWrapper<CategoryData>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/example", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataTableWrapper<CategoryData>> getVideoData() {
        return getCategories();
    }
}
