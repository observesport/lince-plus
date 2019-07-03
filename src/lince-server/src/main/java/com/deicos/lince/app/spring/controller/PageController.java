package com.deicos.lince.app.spring.controller;

import com.deicos.lince.app.ServerAppParams;
import com.deicos.lince.app.service.MultiPartFileSenderService;
import com.deicos.lince.app.service.VideoService;
import com.deicos.lince.data.LinceDataConstants;
import com.deicos.lince.data.bean.RegisterItem;
import com.deicos.lince.data.bean.categories.CategoryData;
import com.deicos.lince.math.RenjinDataAttribute;
import com.deicos.lince.math.SessionDataAttributes;
import com.deicos.lince.math.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

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
    private final DataHubService dataHubService;
    private final MultiPartFileSenderService multiPartFileSenderService;
    private final SessionService sessionService;
    private final LocaleResolver localeResolver;
    private final String linceVersion;

    @Autowired
    public PageController(Environment environment, ProfileService profileService, CategoryService categoryService, AnalysisService analysisService, VideoService videoService, DataHubService dataHubService, MultiPartFileSenderService multiPartFileSenderService, SessionService sessionController, LocaleResolver localeResolver) {
        this.environment = environment;
        this.profileService = profileService;
        this.categoryService = categoryService;
        this.analysisService = analysisService;
        this.videoService = videoService;
        this.dataHubService = dataHubService;
        this.multiPartFileSenderService = multiPartFileSenderService;
        this.sessionService = sessionController;
        this.localeResolver = localeResolver;
        this.linceVersion = environment.getProperty(ServerAppParams.PARAM_LINCE_VERSION);
    }

    private void addVideoConstants(HttpSession httpSession, Model model, String videoUrl) {
        List<CategoryData> data = categoryService.getCollection();
        /*if (CollectionUtils.isEmpty(data)) {
            categoryService.generateDummyCollection();
        }*/
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
            final String langCode =currentLocale.getLanguage();
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

    @RequestMapping("/")
    public String index(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        return "index";
    }

    @RequestMapping("/desktop")
    public String desktop(HttpServletRequest request, HttpSession session, Model model) {
        addCommonSettings(request, session, model);
        return "desktop-landing";
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

    /**
     * Commmon response for streaming without cache support
     *
     * @param response response
     * @param request  request
     * @return
     */
    @RequestMapping(value = "/file", method = RequestMethod.GET, produces = {"application/octet-stream"})
    @ResponseBody
    public FileSystemResource getFile(final HttpServletResponse response, HttpServletRequest request) {
        response.setHeader("Cache-Control", "no-cache");
        try {
            FileSystemResource rtn = new FileSystemResource(videoService.getVideoFile());
            return rtn;
        } catch (Exception e) {
            log.error("ERR accediendo fichero", e);
            return null;
        }
    }

    /**
     * Full support for heading and streaming profileService allowing jumps into specific times for chrome and chromium
     *
     * @param fileRQ
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = ServerAppParams.BASE_URL_STREAMING + "{fileRQ}", method = RequestMethod.GET)
    public ResponseEntity<UrlResource> getMedia(@PathVariable String fileRQ) throws IOException {
        try {
            File result = null;
            if (StringUtils.equals(fileRQ, "test")) {
                //The old version uses profileService, so lets get the damn rabbit into action
                result = videoService.getVideoFile();
            } else {
                for (File f : dataHubService.getVideoPlayList()) {
                    if (StringUtils.contains(f.getPath(), fileRQ)) {
                        result = f;
                    }
                }
            }
            if (result != null) {
                /*
                multiPartFileSenderService.fromPath(result.toPath()).with(request).with(response).serveResource();
                */
                UrlResource video = new UrlResource(result.toURI());
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .contentType(MediaTypeFactory
                                .getMediaType(video)
                                .orElse(MediaType.APPLICATION_OCTET_STREAM))
                        .body(video);
            } else {
                //if reach point, is not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new UrlResource(fileRQ));
            }
        } catch (Exception e) {
            log.error("videoOutputError", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new UrlResource(fileRQ));
        }
    }


}
