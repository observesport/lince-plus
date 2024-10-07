package com.lince.observer.data.controller;

import com.lince.observer.data.bean.categories.CategoryContainer;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.common.DataTableWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Alberto Soto. 14/5/24
 */
public interface CategoryController {
    String RQ_MAPPING_NAME = "/categories";

    @RequestMapping(value = "/get/{txtId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CategoryContainer> getCategories(@PathVariable String txtId);

    @RequestMapping(value = "/saveAll/{gen}", method = RequestMethod.POST)
    ResponseEntity<DataTableWrapper<CategoryData>> saveAll(HttpServletRequest request
            , @PathVariable("gen") Boolean doGen
            , @RequestBody DataTableWrapper<Criteria> items);

    @RequestMapping(value = "/save", method = RequestMethod.POST/*, produces = MediaType.APPLICATION_JSON_VALUE, headers="Accept=*"*/)
    ResponseEntity<CategoryData> save(HttpServletRequest request, @RequestBody CategoryData item);

    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    ResponseEntity<CategoryData> update(@PathVariable Integer id, @RequestBody CategoryData item);

    @RequestMapping(value = "/move/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    ResponseEntity<CategoryData> move(@PathVariable Integer id, @RequestBody CategoryData item);

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    ResponseEntity<CategoryData> delete(@PathVariable Integer id);
}
