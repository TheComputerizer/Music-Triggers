package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;

public class audioConverter {
    private static final Integer bitrate = 256000;//Minimal bitrate only
    private static final Integer channels = 2; //2 for stereo, 1 for mono
    private static final Integer samplingRate = 44100;//For good quality.

    /* Data structures for the audio
     *  and Encoding attributes
     */
    private static AudioAttributes audioAttr = new AudioAttributes();
    private static EncodingAttributes encoAttrs = new EncodingAttributes();
    private static Encoder encoder = new Encoder();
    private static MultimediaObject obj;

    /*
     * File formats that will be converted
     */
    private static String oggFormat = "ogg";
    private static String mp3Format = "mp3";
    private static String wavFormat = "wav";

    /*
     * Codecs to be used
     */
    private static String oggCodec = "vorbis";

    public static void mp3ToOgg(File source, File folder, String name){
        File target = new File(folder,name);
        encoAttrs.setOutputFormat(oggFormat);
        audioAttr.setCodec(oggCodec);
        encoAttrs.setAudioAttributes(audioAttr);
        obj = new MultimediaObject(source);
        try{
            encoder.encode(obj, target, encoAttrs);
        }catch(Exception e){
            MusicTriggers.logger.warn("Encoding Failed");
        }
    }
}
