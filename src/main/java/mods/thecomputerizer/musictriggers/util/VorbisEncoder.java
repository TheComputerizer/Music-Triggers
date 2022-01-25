// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
// The contents of this file are made publicly available by XJ Music Inc. under the terms of the GNU General Public License, version 2.

package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import org.xiph.libogg.ogg_packet;
import org.xiph.libogg.ogg_page;
import org.xiph.libogg.ogg_stream_state;
import org.xiph.libvorbis.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

public class VorbisEncoder {
    private static final int VORBIS_ANALYSIS_BLOCK_FRAMES = 1024;
    private final vorbis_dsp_state dspState;
    private final vorbis_block block;
    private final ogg_stream_state oggStreamState;
    private final ogg_page page;
    private final ogg_packet packet;
    public final double[][] stream;

    /**
     Instantiate new Vorbis Encoder
     @param stream    to encode
     @param frameRate at which to encode audio
     */
    public VorbisEncoder(double[][] stream, int channels, int frameRate, float quality) {
        MusicTriggers.logger.info("Encoding Vorbis");
        this.stream = stream;
        Random generator = new SecureRandom();  // need to randomize seed

        // structures that store all the vorbis bitstream settings
        vorbis_info vorbisInfo = new vorbis_info();
        vorbisenc encoder = new vorbisenc();
        try {
            encoder.vorbis_encode_init_vbr(vorbisInfo, channels, frameRate, quality);
        } catch (Exception e) {
            MusicTriggers.logger.error("Failed to initialize Vorbis encoding", e);
        }

        // central working state for the packet->PCM decoder
        this.dspState = new vorbis_dsp_state();
        if (!this.dspState.vorbis_analysis_init(vorbisInfo)) {
            MusicTriggers.logger.error("Failed to Initialize vorbis_dsp_state");
        }

        // local working space for packet->PCM decode
        this.block = new vorbis_block(this.dspState);

        // take physical pages, weld into a logical stream of packets
        this.oggStreamState = new ogg_stream_state(generator.nextInt(256));

        // structures for building OGG packets
        ogg_packet header = new ogg_packet();
        ogg_packet header_comm = new ogg_packet();
        ogg_packet header_code = new ogg_packet();

        // structures that stores all the user comments
        vorbis_comment comment = new vorbis_comment();
        comment.vorbis_comment_add_tag("COPYRIGHT", "XJ Music Inc.");
        dspState.vorbis_analysis_headerout(comment, header, header_comm, header_code);

        this.oggStreamState.ogg_stream_packetin(header); // automatically placed in its own page
        this.oggStreamState.ogg_stream_packetin(header_comm);
        this.oggStreamState.ogg_stream_packetin(header_code);

        // one Ogg bitstream page.  VorbisEncoder packets are inside
        this.page = new ogg_page();
        // one raw packet of data for decode
        this.packet = new ogg_packet();

    }

    /**
     Encode OGG_VORBIS VorbisEncoder@param stream      input channels of floating point samples
     @param output to write OGG Vorbis data
     */
    public void encode(FileOutputStream output) throws IOException {
        while (this.oggStreamState.ogg_stream_flush(this.page)) {
            output.write(this.page.header, 0, this.page.header_len);
            output.write(this.page.body, 0, this.page.body_len);
        }
        MusicTriggers.logger.debug("Wrote Header");

        int atFrame = 0;
        int totalFrames = this.stream.length;
        while (atFrame < this.stream[0].length) {
            boolean endOfStream = false;
      /*
      From: https://xiph.org/vorbis/doc/libvorbis/vorbis_analysis_buffer.html
       *
      The Vorbis encoder expects the caller to write audio data as non-interleaved floating point samples into its internal buffers.
      The general procedure is to call this function with the number of samples you have available.
      The encoder will arrange for that much internal storage and return an array of buffer pointers, one for each channel of audio.
      The caller must then write the audio samples into those buffers, as float values,
      and finally call vorbis_analysis_wrote() to tell the encoder the data is available for analysis.
      */
            if (atFrame < totalFrames) {
                float[][] buffer = this.dspState.vorbis_analysis_buffer(VORBIS_ANALYSIS_BLOCK_FRAMES);

                // copy a block of samples into the analysis buffer
                int bufferChannels = 2;
                int wroteBufferLength = 0;
                for (int frame = 0; frame < VORBIS_ANALYSIS_BLOCK_FRAMES; frame++) {
                    if (atFrame < this.stream[0].length) {
                        for (int channel = 0; channel < bufferChannels; channel++) {
                            buffer[channel][this.dspState.pcm_current + frame] = (float) this.stream[channel][atFrame];
                            MusicTriggers.logger.info("Frame: "+atFrame);
                        }
                        wroteBufferLength++;
                    }
                    atFrame++;
                }

                // tell the library how much we actually submitted
                this.dspState.vorbis_analysis_wrote(wroteBufferLength);
            } else {
                this.dspState.vorbis_analysis_wrote(0);
            }

            // vorbis does some data pre-analysis, then divvies up blocks for more involved
            // (potentially parallel) processing.  Get a single block for encoding now
            while (this.block.vorbis_analysis_blockout(this.dspState)) {

                // analysis, assume we want to use bitrate management

                this.block.vorbis_analysis(null);
                this.block.vorbis_bitrate_addblock();

                while (this.dspState.vorbis_bitrate_flushpacket(this.packet)) {

                    // weld the packet into the bitstream
                    this.oggStreamState.ogg_stream_packetin(this.packet);

                    // write out pages (if any)
                    while (!endOfStream) {
                        MusicTriggers.logger.info("final loop...");
                        if (!this.oggStreamState.ogg_stream_pageout(this.page)) {
                            MusicTriggers.logger.info("found break");
                            break;
                        }

                        output.write(this.page.header, 0, this.page.header_len);
                        output.write(this.page.body, 0, this.page.body_len);

                        // this could be set above, but for illustrative purposes, I do
                        // it here (to show that vorbis does know where the stream ends)
                        if (this.page.ogg_page_eos() > 0)
                            endOfStream = true;
                            MusicTriggers.logger.info("found end of stream");
                    }
                }
            }

        }

        output.close();

        MusicTriggers.logger.debug("Wrote {} frames", atFrame);
    }
}