package mods.thecomputerizer.musictriggers.util.audio;

import javazoom.jl.converter.Converter;

import java.io.File;

public class audioConverter {

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
        WavToOgg(target.getPath(), target.getPath().replaceAll(".wav",".ogg"), false);
    }

    public static void WavToOgg(String sourcePath, String targetPath, boolean temp) {
        File target = new File(targetPath);
        File source = new File(sourcePath);
        if (target.exists()) target.delete();
        VorbisEncoder.encode(sourcePath,targetPath);
        source.delete();
        if(temp) target.deleteOnExit();
    }
}
