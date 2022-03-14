package mods.thecomputerizer.musictriggers.config;

import com.rits.cloning.Cloner;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.Mappings;
import net.minecraft.util.ResourceLocation;
import scala.collection.immutable.List$;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class configObject {
    private Map<String, String> songholder;
    private Map<String, Map<String, String[]>> triggerholder;
    private Map<String, String[]> otherinfo;
    private Map<String, Map<String, String[]>> otherlinkinginfo;
    private Map<String, Map<String, String[]>> triggerlinking;
    private Map<Integer, configTitleCards.Title> titlecards;
    private Map<Integer, configTitleCards.Image> imagecards;
    private Map<Integer, Boolean> ismoving;
    private Map<ResourceLocation, configTitleCards.ImageDimensions> imageDimensions;

    private Map<String, List<Integer>> markSongInfoForWriting;
    private Map<String, Map<String, List<Integer>>> markTriggerInfoForWriting;

    private final File mainConfig;
    private final File titleCardConfig;

    public static final String[] otherInfoDefaults = new String[]{"1", "false", "false", "100", "1"};
    public static final String[] triggerInfoDefaults = new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
            "minecraft", "_", "16", "false", "100", "100", "100",
            "false", "0", "minecraft", "true", "true", "0", "0", "nope", "nope",
            "-111", "false", "nope","_", "true"};

    private configObject(Map<String, String> songholder, Map<String, Map<String, String[]>> triggerholder, Map<String, String[]> otherinfo,
                         Map<String, Map<String, String[]>> otherlinkinginfo, Map<String, Map<String, String[]>> triggerlinking, Map<Integer, configTitleCards.Title> titlecards,
                         Map<Integer, configTitleCards.Image> imagecards, Map<Integer, Boolean> ismoving, Map<ResourceLocation, configTitleCards.ImageDimensions> imageDimensions) {
        this.songholder = songholder;
        this.triggerholder = triggerholder;
        this.otherinfo = otherinfo;
        this.otherlinkinginfo = otherlinkinginfo;
        this.triggerlinking = triggerlinking;
        this.titlecards = titlecards;
        this.imagecards = imagecards;
        this.ismoving = ismoving;
        this.imageDimensions = imageDimensions;

        this.markSongInfoForWriting = new HashMap<>();
        this.markTriggerInfoForWriting = new HashMap<>();

        this.mainConfig = new File("config/MusicTriggers/musictriggers.toml");
        this.titleCardConfig = new File("config/MusicTriggers/transitions.toml");
    }

    public static configObject createFromCurrent() {
        Cloner cloner=new Cloner();
        return new configObject(cloner.deepClone(configToml.songholder),cloner.deepClone(configToml.triggerholder),cloner.deepClone(configToml.otherinfo),
                cloner.deepClone(configToml.otherlinkinginfo),cloner.deepClone(configToml.triggerlinking),cloner.deepClone(configTitleCards.titlecards),
                cloner.deepClone(configTitleCards.imagecards),cloner.deepClone(configTitleCards.ismoving),cloner.deepClone(configTitleCards.imageDimensions));
    }

    public List<String> getAllSongs() {
        List<String> ret = new ArrayList<>();
        for(Map.Entry<String, String> stringEntry : this.songholder.entrySet()) {
            ret.add(stringEntry.getValue());
        }
        return  ret;
    }

    public List<String> getAllCodes() {
        List<String> ret = new ArrayList<>();
        for(Map.Entry<String, String> stringEntry : this.songholder.entrySet()) {
            ret.add(stringEntry.getKey());
        }
        return  ret;
    }

    public List<String> getAllTriggerForCode(String code) {
        List<String> ret = new ArrayList<>();
        for(Map.Entry<String, String[]> stringEntry : this.triggerholder.get(code).entrySet()) {
            ret.add(stringEntry.getKey());
        }
        return ret;
    }

    public String addSong(String name) {
        String code = "song" + this.songholder.keySet().size();
        this.songholder.put(code, name);
        this.triggerholder.put(code, new HashMap<>());
        this.otherinfo.put(code, new String[]{"1", "false", "false", "100", "1"});
        return code;
    }

    public void removeSong(String code) {
        this.songholder.remove(code);
        this.triggerholder.remove(code);
        this.otherinfo.remove(code);
    }

    public void addTrigger(String code, String trigger) {
        this.triggerholder.get(code).put(trigger, new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
                "minecraft", "_", "16", "false", "100", "100", "100",
                "false", "0", "minecraft", "true", "true", "0", "0", "nope", "nope",
                "-111", "false", "nope","_", "true"});
    }

    public void removeTrigger(String code, String trigger) {
        this.triggerholder.get(code).remove(trigger);
    }

    public String getSongInfoAtIndex(String code, int index) {
        if(index<5) return this.otherinfo.get(code)[index];
        else return "Trigger";
    }

    public String getTriggerInfoAtIndex(String code, String trigger, int index) {
        return this.triggerholder.get(code).get(trigger)[index];
    }

    public void editOtherInfoParameter(String code, int index, String newVal) {
        this.markSongInfoForWriting.putIfAbsent(code, new ArrayList<>());
        if(!this.markSongInfoForWriting.get(code).contains(index)) {
            this.markSongInfoForWriting.get(code).add(index);
        }
        this.otherinfo.get(code)[index] = newVal;
    }

    public void addAllExistingParameters() {
        for(Map.Entry<String, Map<String, String[]>> stringMapEntry : this.triggerholder.entrySet()) {
            addExistingEditedOtherInfoParameters(stringMapEntry.getKey());
            for(Map.Entry<String, String[]> stringEntry : this.triggerholder.get(stringMapEntry.getKey()).entrySet()) {
                addExistingEditedTriggerInfoParameters(stringMapEntry.getKey(),stringEntry.getKey());
            }
        }
    }

    private void addExistingEditedOtherInfoParameters(String code) {
        this.markSongInfoForWriting.putIfAbsent(code, new ArrayList<>());
        for(int i=0;i<this.otherinfo.get(code).length;i++) {
            if(!this.otherinfo.get(code)[i].matches(otherInfoDefaults[i])) {
                if(!this.markSongInfoForWriting.get(code).contains(i)) {
                    this.markSongInfoForWriting.get(code).add(i);
                }
            }
        }
    }

    private void addExistingEditedTriggerInfoParameters(String code, String trigger) {
        this.markTriggerInfoForWriting.putIfAbsent(code, new HashMap<>());
        this.markTriggerInfoForWriting.get(code).putIfAbsent(trigger, new ArrayList<>());
        for(int i=0;i<this.triggerholder.get(code).get(trigger).length;i++) {
            if(!this.triggerholder.get(code).get(trigger)[i].matches(triggerInfoDefaults[i])) {
                if(!this.markTriggerInfoForWriting.get(code).get(trigger).contains(i)) {
                    this.markTriggerInfoForWriting.get(code).get(trigger).add(i);
                }
            }
        }
    }

    public void editTriggerInfoParameter(String code, String trigger, int index, String newVal) {
        this.markTriggerInfoForWriting.putIfAbsent(code, new HashMap<>());
        this.markTriggerInfoForWriting.get(code).putIfAbsent(trigger, new ArrayList<>());
        if(!this.markTriggerInfoForWriting.get(code).get(trigger).contains(index)) {
            this.markTriggerInfoForWriting.get(code).get(trigger).add(index);
        }
        this.triggerholder.get(code).get(trigger)[index] = newVal;
    }

    public void write() throws IOException {
        StringBuilder mainBuilder = new StringBuilder();
        FileWriter writeTransitions = new FileWriter(this.titleCardConfig);
        for(Map.Entry<String, Map<String, String[]>> stringMapEntry : this.triggerholder.entrySet()) {
            String code = stringMapEntry.getKey();
            MusicTriggers.logger.info("writing code: "+code);
            mainBuilder.append(formatSongBrackets(this.songholder.get(code))).append("\n");
            if(this.markSongInfoForWriting.get(code)!=null) {
                for (int i : this.markSongInfoForWriting.get(code)) {
                    mainBuilder.append("\t").append(Mappings.songparameters.get(i)).append(" = \"").append(this.otherinfo.get(code)[i]).append("\"\n");
                }
            }
            for(Map.Entry<String, String[]> stringEntry : this.triggerholder.get(stringMapEntry.getKey()).entrySet()) {
                String trigger = stringEntry.getKey();
                mainBuilder.append("\t").append(formatTriggerBrackets(code, this.songholder.get(code))).append("\n");
                mainBuilder.append("\t\tname = \"").append(trigger).append("\"\n");
                if(this.markTriggerInfoForWriting.get(code)!=null && this.markTriggerInfoForWriting.get(code).get(trigger)!=null) {
                    for (int i : this.markTriggerInfoForWriting.get(code).get(trigger)) {
                        mainBuilder.append("\t\t").append(Mappings.parameters.get(i)).append(" = \"").append(this.triggerholder.get(code).get(trigger)[i]).append("\"\n");
                    }
                }
            }
            mainBuilder.append("\n");
        }
        FileWriter writeMain = new FileWriter(this.mainConfig);
        writeMain.write(mainBuilder.toString());
        writeMain.close();
        this.delete();
    }

    private String formatSongBrackets(String name) {
        if ((Collections.frequency(this.songholder.values(), name))>1) return "[["+name+"]]";
        else return "["+name+"]";
    }

    private String formatTriggerBrackets(String code, String song) {
        if (this.triggerholder.get(code).entrySet().size()>1) return "[["+song+".trigger]]";
        else return "["+song+".trigger]";
    }

    private void delete() {
        this.songholder = new HashMap<>();
        this.triggerholder = new HashMap<>();
        this.otherinfo = new HashMap<>();
        this.otherlinkinginfo = new HashMap<>();
        this.triggerlinking = new HashMap<>();
        this.titlecards = new HashMap<>();
        this.imagecards = new HashMap<>();
        this.ismoving = new HashMap<>();
        this.imageDimensions = new HashMap<>();
        this.markSongInfoForWriting = new HashMap<>();
        this.markTriggerInfoForWriting = new HashMap<>();
    }
}
