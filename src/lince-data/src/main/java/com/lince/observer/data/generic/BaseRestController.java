package com.lince.observer.data.generic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lince.observer.data.common.CgCoreMessage;
import com.lince.observer.data.common.ICgBaseService;
import com.lince.observer.data.structure.BeanCRUDConfig;
import com.lince.observer.data.structure.FieldActionType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * coachgate-core-root
 *
 * @author berto (alberto.soto@gmail.com)in 20/01/2016.
 * Description:
 * <p/>
 * TO: transfer object from bussiness layer, can be mapped or not from db
 * S: Available profileService which implements TO actions, autowired
 * <p>
 * http://www.adictosaltrabajo.com/tutoriales/spring-mvc-api-rest-oauth-2/
 */
@Controller
@RequestMapping(value = "/")
public abstract class BaseRestController<TO extends CgCoreMessage, S extends ICgBaseService>
        extends BaseRestControllerWrapper {
    protected abstract String getActionName();

    public static final String ACTION_EDIT = "/set/{id}";
    public static final String ACTION_GET = "/find/{id}";
    public static final String ACTION_LIST = "/get/";
    public static final String ACTION_ADD = "/add/";
    public static final String ACTION_REMOVE = "/clear/{id}";


    @Autowired
    protected S service;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Autowired
    private HttpServletRequest context;


    /**
     * Abstract method for returning all elements
     *
     * @return
     */
    @RequestMapping(value = ACTION_LIST, method = RequestMethod.GET)
    public ResponseEntity<List<TO>> getAll() {
        try {
            return getResponseItemList(service.findAll());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Abstract method for getting one element by id
     *
     * @param id
     * @return
     */
    @RequestMapping(value = ACTION_GET, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TO> get(@PathVariable("id") long id) {
        try {
            return getResponseItem((TO) service.findOne(id));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = ACTION_ADD, method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<TO> add(@RequestBody TO item, UriComponentsBuilder ucBuilder) {
        try {
            log.info("Creating item " + item.toString());
            if (service.doesExist(item)) {
                log.info("A Item with value " + item.toString() + " already exist");
                return new ResponseEntity<TO>(HttpStatus.CONFLICT);
            }
            item = (TO) service.save(item);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(ucBuilder.path("/get/{id}").buildAndExpand(item.getId()).toUri());
            return new ResponseEntity<>(item, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }


    @RequestMapping(value = ACTION_EDIT, method = RequestMethod.PUT, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<TO> update(@PathVariable("id") long id, @RequestBody TO item) {
        try {
            log.info("Updating Item " + id);
            TO currentItem = (TO) service.findOne(id);
            if (currentItem == null) {
                log.info("Item with id " + id + " not found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            //BeanUtils.copyProperties(item, currentItem);
            BeanUtils.copyProperties(item, currentItem, getNullPropertyNames(item));
            service.update(currentItem);
            return new ResponseEntity<>(currentItem, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/structure", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<BeanCRUDConfig> getStructure(TO item) {
        BeanCRUDConfig response = new BeanCRUDConfig();
        response.addAction(getActionName() + ACTION_EDIT
                , BeanCRUDConfig.ACTION_TYPE_CELL
                , BeanCRUDConfig.ICON_EDIT
                , FieldActionType.FORM);
        response.addAction(getActionName() + ACTION_GET
                , BeanCRUDConfig.ACTION_TYPE_CELL
                , BeanCRUDConfig.ICON_FIND
                , FieldActionType.FORM);
        response.addAction(getActionName() + ACTION_REMOVE
                , BeanCRUDConfig.ACTION_TYPE_CELL
                , BeanCRUDConfig.ICON_DELETE
                , FieldActionType.AJAX);
        response.addAction(getActionName() + ACTION_ADD
                , BeanCRUDConfig.ACTION_TYPE_BUTTON
                , BeanCRUDConfig.ICON_ADD
                , FieldActionType.FORM);
//        response.addAction("http://www.nba.com"
//                , BeanCRUDConfig.ACTION_TYPE_BUTTON
//                , BeanCRUDConfig.ICON_ADD
//                , FieldActionType.SEND);
        /*
        Field[] attributes = item.getClass().getDeclaredFields();
        int i = 0;
        for (Field field : attributes) {
            i++;
            response.addField(i, field.getName(), StringUtils.substringAfterLast(field.getType().toString(), "."), null);
        }*/
        response.setFields(item.viewFormConfig());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = ACTION_REMOVE, method = RequestMethod.DELETE)
    public ResponseEntity<TO> delete(@PathVariable("id") long id) {
        try {
            log.info("Fetching & Deleting User with id " + id);
            TO item = (TO) service.findOne(id);
            if (item == null) {
                log.info("Unable to delete. User with id " + id + " not found");
                return new ResponseEntity<TO>(HttpStatus.NOT_FOUND);
            }
            service.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
