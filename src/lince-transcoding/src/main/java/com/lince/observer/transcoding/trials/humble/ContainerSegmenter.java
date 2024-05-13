package com.lince.observer.transcoding.trials.humble;

/*
import io.humble.video.*;
import io.humble.video.MediaDescriptor.Type;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import java.io.IOException;
*/

/**
 * .transcoding.trials.humble
 * Class ContainerSegmenter
 * 02/03/2020
 *
 * @author berto (alberto.soto@gmail.com)
 */
@Deprecated
public class ContainerSegmenter {
    /*private static void segmentFile(String input, String output, int hls_start,
                                    int hls_time, int hls_list_size, int hls_wrap, String hls_base_url,
                                    String vFilter,
                                    String aFilter) throws InterruptedException, IOException {

        final Demuxer demuxer = Demuxer.make();

        demuxer.open(input, null, false, true, null, null);

        // we're forcing this to be HTTP Live Streaming for this demo.
        final Muxer muxer = Muxer.make(output, null, "hls");
        muxer.setProperty("start_number", hls_start);
        muxer.setProperty("hls_time", hls_time);
        muxer.setProperty("hls_list_size", hls_list_size);
        muxer.setProperty("hls_wrap", hls_wrap);
        if (hls_base_url != null && hls_base_url.length() > 0)
            muxer.setProperty("hls_base_url", hls_base_url);

        final MuxerFormat format = MuxerFormat.guessFormat("mp4", null, null);

        *//**
         * Create bit stream filters if we are asked to.
         *//*
        final BitStreamFilter vf = vFilter != null ? BitStreamFilter.make(vFilter) : null;
        final BitStreamFilter af = aFilter != null ? BitStreamFilter.make(aFilter) : null;

        int n = demuxer.getNumStreams();
        final Decoder[] decoders = new Decoder[n];
        for (int i = 0; i < n; i++) {
            final DemuxerStream ds = demuxer.getStream(i);
            decoders[i] = ds.getDecoder();
            final Decoder d = decoders[i];

            if (d != null) {
                // neat; we can decode. Now let's see if this decoder can fit into the mp4 format.
                if (!format.getSupportedCodecs().contains(d.getCodecID())) {
                    throw new RuntimeException("Input filename (" + input + ") contains at least one stream with a codec not supported in the output format: " + d.toString());
                }
                if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
                    d.setFlag(Coder.Flag.FLAG_GLOBAL_HEADER, true);
                d.open(null, null);
                muxer.addNewStream(d);
            }
        }
        muxer.open(null, null);
        final MediaPacket packet = MediaPacket.make();
        while (demuxer.read(packet) >= 0) {
            *//**
             * Now we have a packet, but we can only write packets that had decoders we knew what to do with.
             *//*
            final Decoder d = decoders[packet.getStreamIndex()];
            if (packet.isComplete() && d != null) {
                // check to see if we are using bit stream filters, and if so, filter the audio
                // or video.
                if (vf != null && d.getCodecType() == Type.MEDIA_VIDEO)
                    vf.filter(packet, null);
                else if (af != null && d.getCodecType() == Type.MEDIA_AUDIO)
                    af.filter(packet, null);
                muxer.write(packet, true);
            }
        }

        // It is good practice to close demuxers when you're done to free
        // up file handles. Humble will EVENTUALLY detect if nothing else
        // references this demuxer and close it then, but get in the habit
        // of cleaning up after yourself, and your future girlfriend/boyfriend
        // will appreciate it.
        muxer.close();
        demuxer.close();

    }


    *//**
     *
     *//*
    @SuppressWarnings("static-access")
    public static void main(String[] args) throws InterruptedException, IOException {
        final Options options = new Options();
        options.addOption("h", "help", false, "displays help");
        options.addOption("v", "version", false, "version of this library");
        options.addOption(OptionBuilder.withArgName("hls_start")
                .withLongOpt("hls_start")
                .hasArg().
                        withDescription("first number in the sequence (defaults to 0).")
                .create("s"));
        options.addOption(OptionBuilder.withArgName("hls_time")
                .withLongOpt("hls_time")
                .hasArg()
                .withDescription("segment length in seconds (defaults to 2).")
                .create("t"));
        options.addOption(OptionBuilder.withArgName("hls_list_size")
                .withLongOpt("hls_list_size")
                .hasArg()
                .withDescription("maximum number of playlist entries (defaults to 0).")
                .create("l"));
        options.addOption(OptionBuilder.withArgName("hls_wrap")
                .withLongOpt("hls_wrap")
                .hasArg()
                .withDescription("set number after which the index wraps (defaults to 0).")
                .create("w"));
        options.addOption(OptionBuilder.withArgName("hls_base_url")
                .withLongOpt("hls_base_url")
                .hasArg()
                .withDescription("URL to prepend to each playlist entry (defaults to '').")
                .create("b"));
        options.addOption(OptionBuilder.withArgName("v_filter")
                .withLongOpt("v_filter")
                .hasArg()
                .withDescription("bitstream filter to use for video packets (defaults to '').")
                .create("vf"));
        options.addOption(OptionBuilder.withArgName("a_filter")
                .withLongOpt("a_filter")
                .hasArg()
                .withDescription("bitstream filter to use for audio packets (defaults to '').")
                .create("af"));

        final org.apache.commons.cli.CommandLineParser parser = new org.apache.commons.cli.DefaultParser();
        try {
            final org.apache.commons.cli.CommandLine cmd = parser.parse(options, args);
            final String[] parsedArgs = cmd.getArgs();
            if (cmd.hasOption("version")) {
                // let's find what version of the library we're running
                final String version = io.humble.video_native.Version.getVersionInfo();
                System.out.println("Humble Version: " + version);
            } else if (cmd.hasOption("help") || parsedArgs.length != 2) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(ContainerSegmenter.class.getCanonicalName() + "<input-filename> <output-filename>", options);
            } else {
                *//**
                 * Read in some option values and their defaults.
                 *//*
                final int hls_start = Integer.parseInt(cmd.getOptionValue("hls_start", "0"));
                if (hls_start < 0)
                    throw new IllegalArgumentException("hls_time must be >= 0");
                final int hls_time = Integer.parseInt(cmd.getOptionValue("hls_time", "2"));
                if (hls_time <= 0)
                    throw new IllegalArgumentException("hls_time must be > 0");
                final int hls_list_size = Integer.parseInt(cmd.getOptionValue("hls_list_size", "0"));
                if (hls_list_size < 0)
                    throw new IllegalArgumentException("hls_list_size must be > 0");
                final int hls_wrap = Integer.parseInt(cmd.getOptionValue("hls_wrap", "0"));
                if (hls_wrap < 0)
                    throw new IllegalArgumentException("hls_wrap must be >= 0");
                final String hls_base_url = cmd.getOptionValue("hls_base_url");
                final String vFilter = cmd.getOptionValue("v_filter");
                final String aFilter = cmd.getOptionValue("a_filter");
                final String input = cmd.getArgs()[0];
                final String output = cmd.getArgs()[1];
                segmentFile(input, output, hls_start, hls_time, hls_list_size, hls_wrap, hls_base_url, vFilter, aFilter);
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.err.println("Exception parsing command line: " + e.getLocalizedMessage());
        }
    }*/
}
