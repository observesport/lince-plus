package com.lince.observer.data.controller;

import com.lince.observer.data.bean.VideoPlayerData;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Created by Alberto Soto. 14/5/24
 */
public interface VideoDataController {
    String RQ_MAPPING_NAME = "/videoData";

    @CrossOrigin
    @RequestMapping(value = "/getAll", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<VideoPlayerData>> getVideos();

    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<VideoPlayerData> getVideoData();

    @RequestMapping(value = "/set", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    ResponseEntity<VideoPlayerData> setVideoData(HttpServletRequest request, @RequestBody VideoPlayerData videoData);
}
