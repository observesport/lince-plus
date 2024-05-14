package com.lince.observer.data.controller;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.common.DataTableWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Created by Alberto Soto. 14/5/24
 */
public interface ProfileController {
    String RQ_MAPPING_NAME = "/profile";

    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<DataTableWrapper<ResearchProfile>> getResearchInfo();

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    ResponseEntity<DataTableWrapper<ResearchProfile>> saveAll(HttpServletRequest request
            , @RequestBody ResearchProfile item);

    @RequestMapping(value = "/getRegister/{key}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<RegisterItem>> getRegisterByKey(HttpSession httpSession, @PathVariable String key);
}
