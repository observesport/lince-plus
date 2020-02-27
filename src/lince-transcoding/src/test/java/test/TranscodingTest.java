package test;

import com.deicos.lince.transcoding.VideoFileType;
import com.deicos.lince.transcoding.component.TranscodingProvider;
import org.apache.commons.lang3.StringUtils;
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
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;

/**
 * com.deicos.lince.app.test
 * Class TranscodingTest
 * 11/12/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {com.deicos.lince.transcoding.component.TranscodingProvider.class})
public class TranscodingTest {

    private String getDemoFileURI(VideoFileType videoFileType) {
        return String.format("/demo-file.%s", StringUtils.lowerCase(videoFileType.toString()));
    }

    private File getDemoFile(VideoFileType videoFileType) {
        String uri = getDemoFileURI(videoFileType);
        return new File(getClass().getResource(uri).getFile());
    }

    @Autowired
    private TranscodingProvider transcodingProvider;

    private static final Logger log = LoggerFactory.getLogger(TranscodingTest.class);


    /**
     * TODO 2020
     */
    @Test
    public void audio2Video() {
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(getDemoFile(VideoFileType.MOV));
            grabber.start();
            FileOutputStream outputStream = new FileOutputStream("/tst-out.MP4");
            FrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0);//grabber.getAudioChannels()
            recorder.setSampleRate(grabber.getSampleRate());
            recorder.start();
            Frame frame;
            while ((frame = grabber.grabFrame()) != null) {
                recorder.record(frame);
            }
            recorder.stop();
            grabber.stop();
        } catch (Exception e) {
            log.error("PUM", e);
            //let's compile. Must be changeAssert.fail();
        }
    }

    /**
     * Opens a file, shows info and saves frames. It works
     */
    @Test
    public void videoTest() {
        try {
            System.out.println("Read few frame and write to image");
            File file = getDemoFile(VideoFileType.MP4);
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
