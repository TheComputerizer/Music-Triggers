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
        format();
        allSongs = collector();
        if (allSongs != null && !allSongs.isEmpty()) {
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
        allSongs = collector();
        if (allSongs != null && !allSongs.isEmpty()) {
            js.add("{");
            for (int i=0;i<allSongs.size()-1;i++) {
                js.add("\t\"item.musictriggers." + allSongs.get(i).toLowerCase() + "\": \"Music Disc\",");
                js.add("\t\"item.musictriggers." + allSongs.get(i).toLowerCase() + ".desc\": \"Music Triggers - "+allSongs.get(i)+"\",");
            }
            if(allSongs.size()>=1) {
                js.add("\t\"item.musictriggers." + allSongs.get(allSongs.size() - 1).toLowerCase() + "\": \"Music Disc\",");
                js.add("\t\"item.musictriggers." + allSongs.get(allSongs.size() - 1).toLowerCase() + ".desc\": \"Music Triggers - " + allSongs.get(allSongs.size() - 1) + "\"");
            }
            js.add("}");
        }
        return js;
    }
    public static List<String> collector() {
        File folder = new File("."+"/config/MusicTriggers/songs/assets/musictriggers/sounds/music/");
        File[] listOfMP3 = folder.listFiles((dir, name) -> name.endsWith(".mp3"));
        if (listOfMP3 != null) {
            for (File mp3 : listOfMP3) {
                AudioConverter.mp3ToOgg(mp3, folder, mp3.getName().replaceAll(".mp3",".wav"));
            }
        }
        File[] listOfWav = folder.listFiles((dir, name) -> name.endsWith(".wav"));
        if (listOfWav != null) {
            for (File wav : listOfWav) {
                AudioConverter.WavToOgg(wav.getPath(), wav.getPath().replaceAll(".wav",".ogg"), false);
            }
        }
        File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".ogg"));
        if(listOfFiles!=null) {
            for (File f : listOfFiles) {
                //noinspection ResultOfMethodCallIgnored
                f.renameTo(new File(folder.getPath(), f.getName().toLowerCase()));
            }
            boolean matchCheck = false;
            String curfile;
            for (File listOfFile : listOfFiles) {
                curfile = FilenameUtils.getBaseName(listOfFile.getName());
                for (String checker : allSongs) {
                    if (checker.matches(curfile)) {
                        matchCheck = true;
                        break;
                    }
                }
                if (!matchCheck) {
                    allSongs.add(curfile);
                }
                matchCheck = false;
            }
            return allSongs;
        }
        else return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void format() {
        File folder = new File("." + "/config/MusicTriggers/songs/assets/musictriggers/sounds/music/");
        File[] music = folder.listFiles();
        if (music!=null) {
            for (File f : music) {
                f.renameTo(new File(folder, f.getName().toLowerCase().replaceAll(" ","_")));
            }
        }
    }
}
