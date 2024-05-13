package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.desktop.spring.service.VideoService;
import com.lince.observer.data.bean.VideoPlayerData;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * lince-scientific-desktop
 * .controller.rest
 * @author berto (alberto.soto@gmail.com)in 19/07/2016.
 * Description:
 */
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping(value = VideoDataController.RQ_MAPPING_NAME)
public class VideoDataController {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    static final String RQ_MAPPING_NAME = "/videoData";
    private final VideoService videoService;
    private String getActionName() {
        return RQ_MAPPING_NAME;
    }

    @Autowired
    public VideoDataController(VideoService videoService) {
        this.videoService = videoService;
    }


    /**
     *
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/getAll", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VideoPlayerData>> getVideos() {
        try {
            return new ResponseEntity<>(videoService.getRemotePlayListCollection(), HttpStatus.OK);
        } catch (Exception e) {
            log.error(getActionName() + ":get/", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VideoPlayerData> getVideoData() {
        try {
            return new ResponseEntity<>(videoService.getVideoPlayerData(), HttpStatus.OK);
        } catch (Exception e) {
            log.error(getActionName() + ":get/", e);
            return new ResponseEntity<>(new VideoPlayerData(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Polifunctional method
     *
     * @param request   rq
     * @param videoData custom json data for video player
     * @return current system data
     */
    @RequestMapping(value = "/set", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<VideoPlayerData> setVideoData(HttpServletRequest request, @RequestBody VideoPlayerData videoData) {
        try {
            if (videoData.getSpeed() != null) {
                videoService.setCurrentSpeed(videoData.getSpeed());
            }
            if (videoData.getTime() != null) {
                videoService.setCurrentTime(videoData.getTime());
            }
            if (videoData.getUrl() != null) {
                videoService.setVideoUrl(videoData.getUrl());
            }
            return new ResponseEntity<>(videoService.getVideoPlayerData(), HttpStatus.OK);
        } catch (Exception e) {
            log.error(getActionName() + "/set", e);
            return new ResponseEntity<>(new VideoPlayerData(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
