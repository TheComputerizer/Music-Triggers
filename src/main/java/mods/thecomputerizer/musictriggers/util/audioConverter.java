package mods.thecomputerizer.musictriggers.util;

import javazoom.jl.converter.Converter;
import javazoom.jl.player.AudioDeviceBase;
import org.apache.commons.compress.utils.IOUtils;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.opus.OpusFile;
import org.tritonus.share.sampled.AudioFormatSet;
import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.AudioSystemShadow;
import org.tritonus.share.sampled.AudioUtils;
import sun.audio.AudioTranslatorStream;

import java.io.*;

public class audioConverter {

    public static void mp3ToOgg(File source, File folder, String name){
        File target = new File(folder,name);
        if(target.exists()) {
            target.delete();
        }
        Converter c = new Converter();
        try {
            c.convert(source.getPath(),target.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        WavToOgg(target.getPath(), target.getPath().replaceAll(".wav",".ogg"));
    }

    public static void WavToOgg(String sourcePath, String targetPath) {
        try {
            File target = new File(targetPath);
            if(target.exists()) {
                target.delete();
            }
            OutputStream out = new FileOutputStream(targetPath);
            InputStream audioStream = new BufferedInputStream(new FileInputStream(sourcePath));
            OpusFile opus = new OpusFile(new OggPacketReader(audioStream));
            opus.getInfo().setSampleRate(48000);
            opus.getInfo().setNumChannels(2);
            opus.getTags().addComment("title", "music triggers links implementation");
            OpusFile file = new OpusFile(out,opus.getInfo(),opus.getTags());
            while (opus.getNextAudioPacket()!=null) {
                file.writeAudioData(opus.getNextAudioPacket());
            }
            IOUtils.closeQuietly(audioStream);
            IOUtils.closeQuietly(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
