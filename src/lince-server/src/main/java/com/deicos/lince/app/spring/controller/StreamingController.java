package com.deicos.lince.app.spring.controller;

import com.deicos.lince.app.ServerAppParams;
import com.deicos.lince.app.service.MultiPartFileSenderService;
import com.deicos.lince.app.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static com.deicos.lince.app.ServerAppParams.*;

/**
 * Lince_v2
 * com.deicos.lince.controller
 *
 * @author berto (alberto.soto@gmail.com)in 26/01/2016.
 * Description:
 */
@Controller
public class StreamingController {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private final VideoService videoService;
//    private final MultiPartFileSenderService multiPartFileSenderService;

    @Autowired
    public StreamingController(VideoService videoService) {
        this.videoService = videoService;
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
     * Legacy version for partial rendering, using Spring MVC
     *
     * @param fileRQ
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = ServerAppParams.BASE_URL_STREAMING + "/legacy/{fileRQ}", method = RequestMethod.GET)
    public ResponseEntity<UrlResource> getMedia(@PathVariable String fileRQ) throws IOException {
        try {
            File videoFile = videoService.getSelectedVideoFile(fileRQ);
            if (videoFile != null) {
                /*
                multiPartFileSenderService.fromPath(result.toPath()).with(request).with(response).serveResource();
                */
                UrlResource video = new UrlResource(videoFile.toURI());
                long rangeStart = 0;
                long rangeEnd = CHUNK_SIZE;
                final Long fileSize = videoService.getFileSize(videoFile.getAbsolutePath());
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .header(CONTENT_TYPE, VIDEO_CONTENT + "mp4")
                        .header(ACCEPT_RANGES, BYTES)
                        .header(CONTENT_LENGTH, String.valueOf(fileSize))
                        .header(CONTENT_RANGE, BYTES + " " + rangeStart + "-" + rangeEnd + "/" + fileSize)
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

    /**
     * Streaming files with partial rendering using webflux
     * <p>
     * Check The annotation flavour of Spring Reactive (using @GetMapping) and the functional flavor (using ServerResponse).
     * Important note: while using webflux with spring mvc, the renderer filterchain swaps to Spring MVC by default.
     * This causes an internal redirect that is hard to see, using getVideo/getVideo while searching the view.
     * <p>
     * To avoid it, we must:
     * - Use responsebody & getmapping annotation instead of requestmapping
     * - In case of null response we should introduce import org.springframework.http.server.reactive.ServerHttpResponse;
     * injected and place partial content and response body Mono.void
     *
     * @param fileRQ
     * @param range
     * @return
     */
    @CrossOrigin
    @GetMapping(value = ServerAppParams.BASE_URL_STREAMING + "{fileRQ}")
    @ResponseBody
    public Mono<Resource> getVideos(
            @PathVariable String fileRQ
            , @RequestHeader(value = "Range", required = false) String range) {
        File videoFile = videoService.getSelectedVideoFile(fileRQ);
        log.debug("Video range in bytes() : " + range);
        return videoService.getMonoVideo(videoFile.getAbsolutePath());
    }

}
