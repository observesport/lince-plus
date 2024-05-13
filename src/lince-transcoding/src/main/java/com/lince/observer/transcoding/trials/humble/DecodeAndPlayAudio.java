package com.lince.observer.transcoding.trials.humble;

/**
 * .transcoding.trials.humble
 * Class DecodeAndPlayAudio
 * 02/03/2020
 *
 * @author berto (alberto.soto@gmail.com)
 */
/*

import java.io.IOException;
import java.nio.ByteBuffer;

import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.MediaAudio;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.Muxer;
import io.humble.video.javaxsound.AudioFrame;
import io.humble.video.javaxsound.MediaAudioConverter;
import io.humble.video.javaxsound.MediaAudioConverterFactory;
import javax.sound.sampled.LineUnavailableException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
*/

/**
 * Opens a media file, finds the first audio stream, and then plays it.
 * This is meant as a demonstration program to teach the use of the Humble API.
 * <p>
 * Concepts introduced:
 * </p>
 * <ul>
 * <li>MediaPacket: An object can read from Media objects and written {Muxer} objects, and represents encoded/compressed media-data.</li>
 * <li>MediaAudio: MediaAudio} objects represent uncompressed audio in Humble.</li>
 * <li>Decoder: { Decoder} objects can be used to convert {MediaPacket} objects into uncompressed { MediaAudio} objects.
 * <li>Decoding loops: This introduces the concept of reading {MediaPacket} objects from a {Demuxer} and then decoding them into raw data.
 * </ul>
 *
 * <p>
 * To run from maven, do:
 * </p>
 * <pre>
 * mvn install exec:java -Dexec.mainClass="io.humble.video.demos.DecodeAndPlayAudio" -Dexec.args="filename.mp4"
 * </pre>
 *
 * @author aclarke
 *
 */
@Deprecated
public class DecodeAndPlayAudio {
/*

    */
/**
     * Opens a file, and plays the audio from it on the speakers.
     *
     * @param filename The file or URL to play.
     * @throws LineUnavailableException
     *//*

    private static void playSound(String filename) throws InterruptedException, IOException, LineUnavailableException {
        */
/*
         * Start by creating a container object, in this case a demuxer since
         * we are reading, to get audio data from.
         *//*

        Demuxer demuxer = Demuxer.make();

        */
/*
         * Open the demuxer with the filename passed on.
         *//*

        demuxer.open(filename, null, false, true, null, null);

        */
/*
         * Query how many streams the call to open found
         *//*

        int numStreams = demuxer.getNumStreams();

        */
/*
         * Iterate through the streams to find the first audio stream
         *//*

        int audioStreamId = -1;
        Decoder audioDecoder = null;
        for (int i = 0; i < numStreams; i++) {
            final DemuxerStream stream = demuxer.getStream(i);
            final Decoder decoder = stream.getDecoder();
            if (decoder != null && decoder.getCodecType() == MediaDescriptor.Type.MEDIA_AUDIO) {
                audioStreamId = i;
                audioDecoder = decoder;
                // stop at the first one.
                break;
            }
        }
        if (audioStreamId == -1)
            throw new RuntimeException("could not find audio stream in container: " + filename);

        */
/*
         * Now we have found the audio stream in this file.  Let's open up our decoder so it can
         * do work.
         *//*

        audioDecoder.open(null, null);

        */
/*
         * We allocate a set of samples with the same number of channels as the
         * coder tells us is in this buffer.
         *//*

        final MediaAudio samples = MediaAudio.make(
                audioDecoder.getFrameSize(),
                audioDecoder.getSampleRate(),
                audioDecoder.getChannels(),
                audioDecoder.getChannelLayout(),
                audioDecoder.getSampleFormat());

        */
/*
         * A converter object we'll use to convert Humble Audio to a format that
         * Java Audio can actually play. The details are complicated, but essentially
         * this converts any audio format (represented in the samples object) into
         * a default audio format suitable for Java's speaker system (which will
         * be signed 16-bit audio, stereo (2-channels), resampled to 22,050 samples
         * per second).
         *//*


        final MediaAudioConverter converter =
                MediaAudioConverterFactory.createConverter(
                        MediaAudioConverterFactory.DEFAULT_JAVA_AUDIO,
                        samples);

        */
/*
         * An AudioFrame is a wrapper for the Java Sound system that abstracts away
         * some stuff. Go read the source code if you want -- it's not very complicated.
         *//*

        final AudioFrame audioFrame = AudioFrame.make(converter.getJavaFormat());
        if (audioFrame == null)
            throw new LineUnavailableException();

        */
/* We will use this to cache the raw-audio we pass to and from
         * the java sound system.
         *//*

        ByteBuffer rawAudio = null;

        */
/*
         * Now, we start walking through the container looking at each packet. This
         * is a decoding loop, and as you work with Humble you'll write a lot
         * of these.
         *
         * Notice how in this loop we reuse all of our objects to avoid
         * reallocating them. Each call to Humble resets objects to avoid
         * unnecessary reallocation.
         *//*

        final MediaPacket packet = MediaPacket.make();
        while (demuxer.read(packet) >= 0) {
            */
/*
             * Now we have a packet, let's see if it belongs to our audio stream
             *//*

            if (packet.getStreamIndex() == audioStreamId) {
                */
/*
                 * A packet can actually contain multiple sets of samples (or frames of samples
                 * in audio-decoding speak).  So, we may need to call decode audio multiple
                 * times at different offsets in the packet's data.  We capture that here.
                 *//*

                int offset = 0;
                int bytesRead = 0;
                do {
                    bytesRead += audioDecoder.decode(samples, packet, offset);
                    if (samples.isComplete()) {
                        rawAudio = converter.toJavaAudio(rawAudio, samples);
                        audioFrame.play(rawAudio);
                    }
                    offset += bytesRead;
                } while (offset < packet.getSize());
            }
        }

        // Some audio decoders (especially advanced ones) will cache
        // audio data before they begin decoding, so when you are done you need
        // to flush them. The convention to flush Encoders or Decoders in Humble Video
        // is to keep passing in null until incomplete samples or packets are returned.
        do {
            audioDecoder.decode(samples, null, 0);
            if (samples.isComplete()) {
                rawAudio = converter.toJavaAudio(rawAudio, samples);
                audioFrame.play(rawAudio);
            }
        } while (samples.isComplete());

        // It is good practice to close demuxers when you're done to free
        // up file handles. Humble will EVENTUALLY detect if nothing else
        // references this demuxer and close it then, but get in the habit
        // of cleaning up after yourself, and your future girlfriend/boyfriend
        // will appreciate it.
        demuxer.close();

        // similar with the demuxer, for the audio playback stuff, clean up after yourself.
        audioFrame.dispose();
    }

    */
/**
     * Takes a media container (file) as the first argument, opens it,
     * opens up the default audio device on your system, and plays back the audio.
     *
     * @param args Must contain one string which represents a filename
     * @throws IOException
     * @throws InterruptedException
     * @throws LineUnavailableException
     *//*

    public static void main(String[] args) throws InterruptedException, IOException, LineUnavailableException {
        final Options options = new Options();
        options.addOption("h", "help", false, "displays help");
        options.addOption("v", "version", false, "version of this library");

        final CommandLineParser parser = new org.apache.commons.cli.BasicParser();
        try {
            final CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("version")) {
                // let's find what version of the library we're running
                final String version = io.humble.video_native.Version.getVersionInfo();
                System.out.println("Humble Version: " + version);
            } else if (cmd.hasOption("help") || args.length == 0) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(DecodeAndPlayAudio.class.getCanonicalName() + " <filename>", options);
            } else {
                final String[] parsedArgs = cmd.getArgs();
                for (String arg : parsedArgs)
                    playSound(arg);
            }
        } catch (ParseException e) {
            System.err.println("Exception parsing command line: " + e.getLocalizedMessage());
        }
    }

*/

}
