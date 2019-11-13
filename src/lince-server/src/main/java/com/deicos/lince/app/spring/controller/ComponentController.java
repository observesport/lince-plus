package com.deicos.lince.app.spring.controller;

import com.deicos.lince.data.LinceDataConstants;
import com.deicos.lince.data.bean.RegisterItem;
import com.deicos.lince.data.bean.categories.CategoryData;
import com.deicos.lince.math.service.AnalysisService;
import com.deicos.lince.math.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * lince-scientific-desktop
 * com.deicos.lince.controller.component
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

    @Autowired
    public ComponentController(CategoryService categoryService, AnalysisService analysisService) {
        this.categoryService = categoryService;
        this.analysisService = analysisService;
    }

    @RequestMapping(value = "/getQRCode", method = RequestMethod.GET)
    public void showImage(HttpServletResponse response) throws Exception {

        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();

        try {
            BufferedImage image = ImageIO.read(new File("codeQR.png"));
            ImageIO.write(image, "png", jpegOutputStream);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

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

    @RequestMapping("/dummy")
    public String videoPlayerBrowser(Model model) {
        model.addAttribute(LinceDataConstants.CTX_TIME, System.currentTimeMillis());
        return "component/dummy";
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
