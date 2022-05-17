package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.util.audio.AudioConverter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Json {
    public static List<String> allSongs = new ArrayList<>();
    public static List<String> js = new ArrayList<>();

    public static List<String> create() {
        MusicTriggers.logger.info("Creating sounds.json");
        format();
        collector();
        if (allSongs != null && !allSongs.isEmpty()) {
            System.out.print(allSongs.size());
            js.add("{");
            for (int i = 0; i < allSongs.size() - 1; i++) {
                js.add("  \"music." + allSongs.get(i) + "\": {");
                js.add("\t\t\"category\": \"music\","); 
                js.add("\t\t\"sounds\": [{");
                js.add("\t\t\t\"name\": \"" + MusicTriggers.MODID + ":music/" + allSongs.get(i) + "\",");
                js.add("\t\t\t\"stream\": true");
                js.add("\t\t}]");
                js.add("\t},");
            }
            js.add("  \"music." + allSongs.get(allSongs.size() - 1).toLowerCase() + "\": {");
            js.add("\t\t\"category\": \"music\",");
            js.add("\t\t\"sounds\": [{");
            js.add("\t\t\t\"name\": \"" + MusicTriggers.MODID + ":music/" + allSongs.get(allSongs.size() - 1) + "\",");
            js.add("\t\t\t\"stream\": true");
            js.add("\t\t}]");
            js.add("\t}");
            js.add("}");
        }
        return js;
    }

    public static List<String> lang() {
        format();
        collector();
        if (allSongs != null && !allSongs.isEmpty()) {
            js.add("{");
            for (String allSong : allSongs) {
                js.add("item.musictriggers:" + allSong.toLowerCase() + ".name=Music Disc");
                js.add("item.record." + allSong.toLowerCase() + ".desc=Music Triggers - " + allSong);
            }
            js.add("}");
        }
        return js;
    }

    public static void collector() {
        File folder = new File("." + "/config/MusicTriggers/songs/assets/musictriggers/sounds/music/");
        File[] listOfMP3 = folder.listFiles((dir, name) -> name.endsWith(".mp3"));
        if (listOfMP3 != null) {
            for (File mp3 : listOfMP3) {
                AudioConverter.mp3ToOgg(mp3, folder, mp3.getName().replaceAll(".mp3",".wav"));
            }
        }
        File[] listOfWav = folder.listFiles((dir, name) -> name.endsWith(".wav"));
        if (listOfWav != null) {
            for (File wav : listOfWav) {
                AudioConverter.WavToOgg(wav.getPath(), wav.getPath().replaceAll(".wav",".ogg"), false,44100);
            }
        }
        File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".ogg"));
        if(listOfFiles!=null) {
            String curfile;
            allSongs = new ArrayList<>();
            for (File f : listOfFiles) {
                curfile = FilenameUtils.getBaseName(f.getName());
                if (!allSongs.contains(curfile)) allSongs.add(curfile);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void format() {
        MusicTriggers.logger.info("Formatting songs");
        File folder = new File("." + "/config/MusicTriggers/songs/assets/musictriggers/sounds/music/");
        File[] music = folder.listFiles();
        if (music!=null) {
            for (File f : music) {
                f.renameTo(new File(folder, f.getName().toLowerCase()));
            }
        }
    }
}
