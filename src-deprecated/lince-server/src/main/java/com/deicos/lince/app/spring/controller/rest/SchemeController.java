package com.deicos.lince.app.spring.controller.rest;

import com.deicos.lince.data.BeanSchemeHelper;
import com.deicos.lince.data.bean.categories.Category;
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
 * com.deicos.lince.app.spring.controller.rest
 * Class SchemeController
 * @author berto (alberto.soto@gmail.com). 13/04/2018
 * Rest controller to map valid beans structure for data editors
 *
 * TODO: Needs implementation. By now let's work with static schemes
 * @see BeanSchemeHelper
 */
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping(value = SchemeController.RQ_MAPPING_NAME)
public class SchemeController {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    static final String RQ_MAPPING_NAME = "/scheme";
    private String getActionName() {
        return RQ_MAPPING_NAME;
    }

    /**
     * Commmon method to get json scheme for any bean
     * @param className
     * @return
     */
    private ResponseEntity<String> getSchemeRender(Class className){
        try {
            return new ResponseEntity<>(BeanSchemeHelper.generateJsonScheme(className), HttpStatus.OK);
        } catch (Exception e) {
            log.error(getActionName() + ":get/", e);
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Userprofile json scheme
     * @return scheme to json editor
     */
    @RequestMapping(value = "/test", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCurrentUser() {
        return getSchemeRender(Category.class);
    }
}
