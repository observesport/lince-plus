package com.deicos.lince.app.spring.controller;

import com.deicos.lince.app.ServerAppParams;
import com.deicos.lince.app.service.VideoService;
import com.deicos.lince.data.LinceDataConstants;
import com.deicos.lince.data.bean.RegisterItem;
import com.deicos.lince.data.bean.categories.CategoryData;
import com.deicos.lince.math.RenjinDataAttribute;
import com.deicos.lince.math.SessionDataAttributes;
import com.deicos.lince.math.service.AnalysisService;
import com.deicos.lince.math.service.CategoryService;
import com.deicos.lince.math.service.ProfileService;
import com.deicos.lince.math.service.SessionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;

/**
 * Lince_v2
 * com.deicos.lince.controller
 *
 * @author berto (alberto.soto@gmail.com)in 26/01/2016.
 * Description:
 */
@Controller
public class PageController {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Environment environment;
    private final ProfileService profileService;
    private final CategoryService categoryService;
    private final AnalysisService analysisService;
    private final VideoService videoService;
    private final SessionService sessionService;
    private final LocaleResolver localeResolver;
    private final String linceVersion;

    @Autowired
    public PageController(Environment environment, ProfileService profileService, CategoryService categoryService, AnalysisService analysisService, VideoService videoService,    SessionService sessionController, LocaleResolver localeResolver) {
        this.environment = environment;
        this.profileService = profileService;
        this.categoryService = categoryService;
        this.analysisService = analysisService;
        this.videoService = videoService;
        this.sessionService = sessionController;
        this.localeResolver = localeResolver;
        this.linceVersion = environment.getProperty(ServerAppParams.PARAM_LINCE_VERSION);
    }

    private void addVideoConstants(HttpSession httpSession, Model model, String videoUrl) {
        List<CategoryData> data = categoryService.getCollection();
        if (StringUtils.isNotEmpty(videoUrl)) {
            videoService.setVideoUrl(videoUrl);
            if (StringUtils.equals(VideoService.DEFAULT_VIDEO_EXAMPLE, videoUrl)) {
                videoService.setLocal(false);
            }
        }
        List<RegisterItem> register = analysisService.getOrderedRegister();
        //buscamos las categorías para añadirlas correctamente
        String currentVideo = videoService.getVideoUrl();
        String fileName = StringUtils.substringAfterLast(currentVideo, "/");
        model.addAttribute(LinceDataConstants.CTX_VIDEO_URL, currentVideo);
        model.addAttribute(LinceDataConstants.CTX_VIDEO_URL_LOCAL, "/m/");
        model.addAttribute(LinceDataConstants.CTX_VIDEO_URL_VIDEO, fileName);
        model.addAttribute(LinceDataConstants.CTX_CATEGORIES, data);
        model.addAttribute(LinceDataConstants.CTX_VIDEO_REGISTER, register);
        model.addAttribute(LinceDataConstants.CTX_PLAYLIST, videoService.getPlaylistData());
        model.addAttribute(LinceDataConstants.CTX_FRAME_RATE, null);
        String colVideos = StringUtils.defaultIfEmpty(sessionService.getSessionData(httpSession, SessionDataAttributes.VIDEO_VIEW), "col-sm-12");
        if (colVideos != null) {
            switch (colVideos) {
                case "1":
                    colVideos = "col-sm-12";
                    break;
                case "2":
                    colVideos = "col-sm-6";
                    break;
                case "3":
                    colVideos = "col-sm-4";
                    break;
                default:
                    colVideos = "col-sm-6";
                    break;
            }
        }
        model.addAttribute(LinceDataConstants.CTX_NUM_VIDEOS, colVideos);
        //r Vars
        model.addAttribute(LinceDataConstants.CTX_R_ATTRIBUTES, RenjinDataAttribute.getLabels());
        String port = environment.getProperty(ServerAppParams.PARAM_PORT);
        model.addAttribute(LinceDataConstants.CTX_PORT, port);

    }

    /**
     * Sets profile settings or research info for additional user settings and actions:
     * - Quickbar needed info for rendering
     *
     * @param session current http session
     * @param model   model for data loading
     */
    private void addCommonSettings(HttpServletRequest request, HttpSession session, Model model) {
        try {
            Locale currentLocale = localeResolver.resolveLocale(request);
            final String langCode = currentLocale.getLanguage();
            model.addAttribute(LinceDataConstants.CTX_LOCALE, StringUtils.lowerCase(langCode));
            model.addAttribute(LinceDataConstants.CTX_LINCE_VERSION, linceVersion);
            /*if (StringUtils.isEmpty(sessionService.getSessionData(session, SessionDataAttributes.REGISTER))) {
                sessionService.setSessionData(session, SessionDataAttributes.REGISTER, dataHubService.getDataRegister().get(0).getId());
            }*/
            // model.addAttribute("researchInfo", profileService.getAllResearchInfo());
        } catch (Exception e) {
            log.error("common settings", e);
        }
    }

    @RequestMapping(value = {"/", "/dashboard", "/dashboard/*"})
    public String index(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        addVideoConstants(session, model, null);
        return "/index.html";
    }

    @RequestMapping("/desktop")
    public String desktop(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        return "/index.html";
    }

    @RequestMapping("/videoPlayerOpenBrowser")
    public String videoPlayerBrowser(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        addVideoConstants(session, model, null);
        return "video";
    }

    @RequestMapping("/base")
    public String base(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        addVideoConstants(session, model, null);
        return "base-page";
    }


    @RequestMapping("/videoPlayer")
    public String videoPlayer(HttpServletRequest request, HttpSession session
            , @RequestParam(value = "videoUrl", required = false, defaultValue = "") String videoUrl
            , @RequestParam(value = "react", required = false, defaultValue = "true") Boolean react
            , Model model) {
        addCommonSettings(request, session, model);
        addVideoConstants(session, model, videoUrl);
        if (!react) {
            return "video";
        } else {
            return "react-video";
        }
    }

    @RequestMapping("/profile")
    public String profile(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        return "profile";
    }

    @RequestMapping("/categoryConfig")
    public String categories(HttpServletRequest request, HttpSession session, Model model, @RequestParam(value = "react", required = false, defaultValue = "false") Boolean react) {
        addCommonSettings(request, session, model);
        if (!react) {
            return "categories";
        } else {
            return "react-tool";
        }

    }

    @RequestMapping("/dummy")
    public String dummy(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        return "dummy";
    }

    @RequestMapping("/sceneScanner")
    public String videoPlayer(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        addVideoConstants(session, model, StringUtils.EMPTY);
        return "scenes";
    }

    @RequestMapping("/export")
    public String export(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        return "export";
    }

    @RequestMapping("/r-console")
    public String renjin(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        addVideoConstants(session, model, StringUtils.EMPTY);
        return "r-console";
    }

    @RequestMapping("/stats")
    public String stats(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        return "react-stats";
    }


    @RequestMapping("/results")
    public String results(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        return "react-results";
    }

    @RequestMapping("/import")
    public String importData(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        return "categories";
    }

}
