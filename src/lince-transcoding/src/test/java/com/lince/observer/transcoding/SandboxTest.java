package com.lince.observer.transcoding;

import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.ffmpeg.global.swscale;
import org.bytedeco.ffmpeg.swscale.SwsContext;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.PointerPointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * com.lince.observer.transcoding
 * Class SandboxTest
 * 04/03/2020
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class SandboxTest {
    private TranscodingProvider transcodingProvider = new TranscodingProvider();

    private static final Logger log = LoggerFactory.getLogger(SandboxTest.class);
    TranscodingTest test = new TranscodingTest();
    /*public void audio2Video() {
        try {
            TranscodingTest test = new TranscodingTest();
            FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(test.getDemoFile(MediaFileType.OGG));
            grabber.start();
            int height = grabber.getImageHeight();
            int width = grabber.getImageWidth();
            File tmpFile = File.createTempFile("test-out", ".mp4");
            FrameRecorder recorder = FFmpegFrameRecorder.createDefault(tmpFile, height, width);
            recorder.setVideoCodecName("h262");
            recorder.setFormat("mp4");
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setVideoQuality(0);
            recorder.setAudioQuality(0);
            recorder.setAudioChannels(0);
            recorder.start();
            Frame frame;
            while ((frame = grabber.grabFrame()) != null) {
                recorder.setTimestamp(grabber.getTimestamp());
                recorder.record(frame);
            }
            recorder.stop();
            grabber.stop();
            log.info("tmp file @" + tmpFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("PUM", e);
            //let's compile. Must be changeAssert.fail();
        }
    }*/

    /**
     * Works
     */
    public void videoTest() {
        try {
            System.out.println("Read few frame and write to image");
            File file = test.getDemoFile(MediaFileType.MP4);
            String filePath = file.getPath();
            int rtn = -1, i = 0, v_stream_idx = -1;

            AVFormatContext avFormatContext = new AVFormatContext(null);
            AVPacket avPacket = new AVPacket();
            rtn = avformat.avformat_open_input(avFormatContext, filePath, null, null);
            if (rtn < 0) {
                System.out.printf("Open video file %s failed \n", filePath);
                throw new IllegalStateException();
            }

            // i dont know but without this function, sws_getContext does not work
            if (avformat.avformat_find_stream_info(avFormatContext, (PointerPointer) null) < 0) {
                System.exit(-1);
            }

            avformat.av_dump_format(avFormatContext, 0, filePath, 0);

            for (i = 0; i < avFormatContext.nb_streams(); i++) {
                if (avFormatContext.streams(i).codecpar().codec_type() == avutil.AVMEDIA_TYPE_VIDEO) {
                    v_stream_idx = i;
                    break;
                }
            }
            if (v_stream_idx == -1) {
                System.out.println("Cannot find video stream");
                throw new IllegalStateException();
            } else {
                System.out.printf("Video stream %d with resolution %dx%d\n", v_stream_idx,
                        avFormatContext.streams(i).codecpar().width(),
                        avFormatContext.streams(i).codecpar().height());
            }

            AVCodecContext codec_ctx = avcodec.avcodec_alloc_context3(null);
            avcodec.avcodec_parameters_to_context(codec_ctx, avFormatContext.streams(v_stream_idx).codecpar());

            AVCodec codec = avcodec.avcodec_find_decoder(codec_ctx.codec_id());
            if (codec == null) {
                System.out.println("Unsupported codec for video file");
                throw new IllegalStateException();
            }
            rtn = avcodec.avcodec_open2(codec_ctx, codec, (PointerPointer) null);
            if (rtn < 0) {
                System.out.println("Can not open codec");
                throw new IllegalStateException();
            }

            AVFrame frm = avutil.av_frame_alloc();

            // Allocate an AVFrame structure
            AVFrame pFrameRGB = avutil.av_frame_alloc();
            if (pFrameRGB == null) {
                System.exit(-1);
            }

            // Determine required buffer size and allocate buffer
            int numBytes = avutil.av_image_get_buffer_size(avutil.AV_PIX_FMT_RGB24, codec_ctx.width(),
                    codec_ctx.height(), 1);
            BytePointer buffer = new BytePointer(avutil.av_malloc(numBytes));

            SwsContext sws_ctx = swscale.sws_getContext(
                    codec_ctx.width(),
                    codec_ctx.height(),
                    codec_ctx.pix_fmt(),
                    codec_ctx.width(),
                    codec_ctx.height(),
                    avutil.AV_PIX_FMT_RGB24,
                    swscale.SWS_BILINEAR,
                    null,
                    null,
                    (DoublePointer) null
            );

            if (sws_ctx == null) {
                System.out.println("Can not use sws");
                throw new IllegalStateException();
            }

            avutil.av_image_fill_arrays(pFrameRGB.data(), pFrameRGB.linesize(),
                    buffer, avutil.AV_PIX_FMT_RGB24, codec_ctx.width(), codec_ctx.height(), 1);

            i = 0;
            int ret1 = -1, ret2 = -1, fi = -1;
            while (avformat.av_read_frame(avFormatContext, avPacket) >= 0) {
                if (avPacket.stream_index() == v_stream_idx) {
                    ret1 = avcodec.avcodec_send_packet(codec_ctx, avPacket);
                    ret2 = avcodec.avcodec_receive_frame(codec_ctx, frm);
                    System.out.printf("ret1 %d ret2 %d\n", ret1, ret2);
                    // avcodec_decode_video2(codec_ctx, frm, fi, pkt);
                }
                // if not check ret2, error occur [swscaler @ 0x1cb3c40] bad src image pointers
                // ret2 same as fi
                // if (fi && ++i <= 5) {
                if (ret2 >= 0 && ++i <= 5) {
                    swscale.sws_scale(
                            sws_ctx,
                            frm.data(),
                            frm.linesize(),
                            0,
                            codec_ctx.height(),
                            pFrameRGB.data(),
                            pFrameRGB.linesize()
                    );

                    transcodingProvider.saveFrameAsImage(pFrameRGB, codec_ctx.width(), codec_ctx.height(), i);
                    // save_frame(frm, codec_ctx.width(), codec_ctx.height(), i);
                }
                avcodec.av_packet_unref(avPacket);
                if (i >= 5) {
                    break;
                }
            }
            avutil.av_frame_free(frm);
            avcodec.avcodec_close(codec_ctx);
            avcodec.avcodec_free_context(codec_ctx);
            avformat.avformat_close_input(avFormatContext);
        } catch (Exception e) {
            log.error("Fail", e);
        }

    }

}
