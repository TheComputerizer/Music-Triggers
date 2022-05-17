package mods.thecomputerizer.musictriggers.util.audio;

import javazoom.jl.converter.Converter;

import java.io.File;
import java.io.FileInputStream;

public class AudioConverter {

    public static void mp3ToOgg(File source, File folder, String name){
        File target = new File(folder,name);
        if(target.exists()) {
            target.delete();
        }
        try {
            new Converter().convert(source.getPath(),target.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        source.delete();
        WavToOgg(target.getPath(), target.getPath().replaceAll(".wav",".ogg"), false, 44100);
    }

    public static void WavToOgg(String sourcePath, String targetPath, boolean temp, int rate) {
        File target = new File(targetPath);
        File source = new File(sourcePath);
        if (target.exists()) target.delete();
        try {
            FileInputStream stream = new FileInputStream(sourcePath);
            VorbisEncoder.encode(stream,targetPath, rate);
            source.delete();
        } catch(Exception e) {
            e.printStackTrace();
        }
        if(temp) target.deleteOnExit();
    }
}
