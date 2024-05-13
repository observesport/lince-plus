package com.deicos.lince.app.spring.controller.rest;


import com.deicos.lince.app.base.common.CgEnum;
import com.deicos.lince.app.base.common.DataTableWrapper;
import com.deicos.lince.app.base.common.EnumStructure;
import com.deicos.lince.data.bean.categories.CategoryData;
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

import java.util.HashMap;

/**
 * lince-scientific-desktop
 * com.deicos.lince.controller.rest
 *
 * @author berto (alberto.soto@gmail.com)in 29/02/2016.
 * Description:
 * <p>
 * Refactors and listens all Observation tool. Uses "newcategoryService"
 */
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping(value = ObservationToolRestController.RQ_MAPPING_NAME)
public class ObservationToolRestController {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    static final String RQ_MAPPING_NAME = "/rest/categories";
    private final CategoryService newCategoryService;

    @Autowired
    public ObservationToolRestController(CategoryService newCategoryService) {
        this.newCategoryService = newCategoryService;
    }

    protected String getActionName() {
        return RQ_MAPPING_NAME;
    }

    private ResponseEntity<HashMap<String, String>> getItemCollectionForEnum(CgEnum enumType) {
        EnumStructure cont = new EnumStructure();
        cont.setEnumData(enumType.getClass().getEnumConstants());
        return new ResponseEntity<>(cont.getDataValues(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getAll", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataTableWrapper<CategoryData>> getCategories() {
        try {
            return new ResponseEntity<>(new DataTableWrapper<>(newCategoryService.getCollection()), HttpStatus.OK);
        } catch (Exception e) {
            log.error("category:load", e);
            return new ResponseEntity<>(new DataTableWrapper<CategoryData>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
