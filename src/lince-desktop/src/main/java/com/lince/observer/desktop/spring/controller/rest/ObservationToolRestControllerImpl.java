package com.lince.observer.desktop.spring.controller.rest;


import com.lince.observer.data.CgEnum;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.common.DataTableWrapper;
import com.lince.observer.data.common.EnumStructure;
import com.lince.observer.data.controller.ObservationToolRestController;
import com.lince.observer.data.service.CategoryService;
import com.lince.observer.math.service.CategoryServiceImpl;
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
 * .controller.rest
 *
 * @author berto (alberto.soto@gmail.com)in 29/02/2016.
 * Description:
 * <p>
 * Refactors and listens all Observation tool. Uses "newcategoryService"
 */
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping(value = ObservationToolRestController.RQ_MAPPING_NAME)
public class ObservationToolRestControllerImpl implements ObservationToolRestController {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final CategoryService categoryService;

    @Autowired
    public ObservationToolRestControllerImpl(CategoryServiceImpl categoryService) {
        this.categoryService = categoryService;
    }

    protected String getActionName() {
        return RQ_MAPPING_NAME;
    }

    private ResponseEntity<HashMap<String, String>> getItemCollectionForEnum(CgEnum enumType) {
        EnumStructure cont = new EnumStructure();
        cont.setEnumData(enumType.getClass().getEnumConstants());
        return new ResponseEntity<>(cont.getDataValues(), HttpStatus.OK);
    }

    @Override
    @RequestMapping(value = "/getAll", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataTableWrapper<CategoryData>> getCategories() {
        try {
            return new ResponseEntity<>(new DataTableWrapper<>(categoryService.getCollection()), HttpStatus.OK);
        } catch (Exception e) {
            log.error("category:load", e);
            return new ResponseEntity<>(new DataTableWrapper<CategoryData>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
