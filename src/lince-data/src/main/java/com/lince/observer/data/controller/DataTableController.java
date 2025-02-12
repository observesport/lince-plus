package com.lince.observer.data.controller;

import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.common.DataTableWrapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Alberto Soto. 14/5/24
 */
@Deprecated
public interface DataTableController {
    String RQ_MAPPING_NAME = "/datatable";

    @RequestMapping(value = "/categories", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<DataTableWrapper<CategoryData>> getCategories();
}
