package com.lince.observer.desktop.spring.controller;

import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.barcode.QRCodeGenerator;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.service.CategoryService;
import com.lince.observer.data.system.operations.OSUtils;
import com.lince.observer.desktop.component.ApplicationContextProvider;
import com.lince.observer.data.service.AnalysisService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * lince-scientific-desktop
 * .controller.component
 *
 * @author berto (alberto.soto@gmail.com)in 07/08/2016.
 * Description:
 */
@Controller
@RequestMapping(value = "/component")
public class ComponentController {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final CategoryService categoryService;
    private final AnalysisService analysisService;
    private final ApplicationContextProvider applicationContextProvider;

    @Autowired
    public ComponentController(CategoryService categoryService, AnalysisService analysisService, ApplicationContextProvider applicationContextProvider) {
        this.categoryService = categoryService;
        this.analysisService = analysisService;
        this.applicationContextProvider = applicationContextProvider;
    }

    @RequestMapping(value = "/getQRCode", method = RequestMethod.GET)
    public void showImage(HttpServletResponse response) throws Exception {

        ByteArrayOutputStream jpegOutputStream = QRCodeGenerator.getQRCodeImage();
        if (jpegOutputStream == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            byte[] imgByte = jpegOutputStream.toByteArray();
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("image/png");
            ServletOutputStream responseOutputStream = response.getOutputStream();
            responseOutputStream.write(imgByte);
            responseOutputStream.flush();
            responseOutputStream.close();
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET)
    public void updater(HttpServletResponse response) throws Exception {
        OSUtils osUtils = new OSUtils();
        JSONObject o = applicationContextProvider.getLastLinceVersion();
        if (o != null && o.has("link")) {
            osUtils.openLinkInBrowser((String) o.get("link"), false);
        }
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
    }


    @RequestMapping("/currentRegisteredDataTable")
    public String currentRegisteredDataTable(Model model) {
        List<CategoryData> data = categoryService.getCollection();
        List<RegisterItem> register = analysisService.getOrderedRegister();
        model.addAttribute(LinceDataConstants.CTX_CATEGORIES, data);
        model.addAttribute(LinceDataConstants.CTX_VIDEO_REGISTER, register);
        model.addAttribute(LinceDataConstants.CTX_IS_SCENE, false);
        return "component/currentRegisteredDataTable";
    }

    @RequestMapping("/currentRegisteredScenes")
    public String currentRegisteredScenes(Model model) {
        List<CategoryData> data = categoryService.getCollection();
        List<RegisterItem> register = analysisService.getOrderedRegister();
        model.addAttribute(LinceDataConstants.CTX_CATEGORIES, data);
        model.addAttribute(LinceDataConstants.CTX_VIDEO_REGISTER, register);
        model.addAttribute(LinceDataConstants.CTX_IS_SCENE, true);
        return "component/currentRegisteredDataTable";
    }
}
