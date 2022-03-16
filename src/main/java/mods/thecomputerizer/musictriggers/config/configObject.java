package mods.thecomputerizer.musictriggers.config;

import com.rits.cloning.Cloner;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.Mappings;
import net.minecraft.util.ResourceLocation;

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
    private Map<String, Map<String, List<Integer>>> markLinkingInfoForWriting;

    private final File mainConfig;
    private final File titleCardConfig;

    public static final String[] otherInfoDefaults = new String[]{"1", "false", "false", "100", "1"};
    public static final String[] triggerInfoDefaults = new String[]{"0", "0", "0", "0", "0", "YouWillNeverGuessThis", "and", "0,0,0,0,0,0", "60",
            "minecraft", "_", "16", "false", "100", "100", "100",
            "false", "0", "minecraft", "true", "true", "0", "0", "nope", "nope",
            "-111", "false","_", "true"};
    public static final String[] linkingInfoDefaults = new String[]{"1", "1"};

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
        this.markLinkingInfoForWriting = new HashMap<>();

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

    public List<String> getAllTriggersForCode(String code) {
        List<String> ret = new ArrayList<>();
        for(Map.Entry<String, String[]> stringEntry : this.triggerholder.get(code).entrySet()) {
            ret.add(stringEntry.getKey());
        }
        return ret;
    }

    public List<String> getAllSongsForLinking(String code) {
        List<String> ret = new ArrayList<>();
        if(this.triggerlinking.get(code)!=null) {
            for (Map.Entry<String, String[]> stringEntry : this.triggerlinking.get(code).entrySet()) {
                if(!stringEntry.getKey().matches(code)) ret.add(stringEntry.getKey());
            }
        }
        return  ret;
    }

    public List<String> getAllLinkingInfo(String code, String song) {
        List<String> ret = new ArrayList<>();
        for(String ignored : this.triggerlinking.get(code).get(song)) {
            ret.add("trigger");
        }
        ret.add("pitch");
        ret.add("volume");
        return  ret;
    }

    public String decode(String code) {
        return this.songholder.get(code);
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
                "-111", "false","_", "true"});
    }

    public void removeTrigger(String code, String trigger) {
        this.triggerholder.get(code).remove(trigger);
    }

    public void addLinkingSong(String code, String name) {
        this.triggerlinking.putIfAbsent(code, new HashMap<>());
        this.otherlinkinginfo.putIfAbsent(code, new HashMap<>());
        this.triggerlinking.get(code).put(name, new String[]{});
        this.otherlinkinginfo.get(code).put(name, new String[]{"1", "1"});
    }

    public void removeLinkingSong(String code, String name) {
        this.triggerlinking.get(code).remove(name);
        this.otherlinkinginfo.get(code).remove(name);
    }

    public void addLinkingTrigger(String code, String name, String trigger) {
        List<String> triggers = Arrays.stream(this.triggerlinking.get(code).get(name)).collect(Collectors.toList());
        triggers.add(trigger);
        this.triggerlinking.get(code).put(name, triggers.toArray(new String[0]));
    }

    public boolean isLinkingInfoTrigger(String code, String song, int index) {
        return index<this.triggerlinking.get(code).get(song).length;
    }
    public void removeLinkingTrigger(String code, String name, int index) {
        List<String> triggers = Arrays.stream(this.triggerlinking.get(code).get(name)).collect(Collectors.toList());
        triggers.remove(index);
        this.triggerlinking.get(code).put(name, triggers.toArray(new String[0]));
    }

    public String getSongInfoAtIndex(String code, int index) {
        if(index<5) return this.otherinfo.get(code)[index];
        else return "Trigger";
    }

    public String getTriggerInfoAtIndex(String code, String trigger, int index) {
        return this.triggerholder.get(code).get(trigger)[index];
    }

    public String getLinkingInfoAtIndex(String code, String song, int index) {
        int triggerSize = this.triggerlinking.get(code).get(song).length;
        if(index<triggerSize) return this.triggerlinking.get(code).get(song)[index];
        else return this.otherlinkinginfo.get(code).get(song)[index-triggerSize];
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
                addExistingEditedTriggerInfoParameters(stringMapEntry.getKey(), stringEntry.getKey());
            }
            if(this.otherlinkinginfo.get(stringMapEntry.getKey()) != null) {
                for (Map.Entry<String, String[]> stringEntry : this.otherlinkinginfo.get(stringMapEntry.getKey()).entrySet()) {
                    addExistingEditedLinkingInfoParameters(stringMapEntry.getKey(), stringEntry.getKey());
                }
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

    private void addExistingEditedLinkingInfoParameters(String code, String song) {
        this.markTriggerInfoForWriting.putIfAbsent(code, new HashMap<>());
        this.markTriggerInfoForWriting.get(code).putIfAbsent(song, new ArrayList<>());
        for(int i=0;i<this.otherlinkinginfo.get(code).get(song).length;i++) {
            if(!this.otherlinkinginfo.get(code).get(song)[i].matches(linkingInfoDefaults[i])) {
                if(!this.markLinkingInfoForWriting.get(code).get(song).contains(i)) {
                    this.markLinkingInfoForWriting.get(code).get(song).add(i);
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

    public void editLinkingInfoParameter(String code, String song, int index, String newVal) {
        this.markLinkingInfoForWriting.putIfAbsent(code, new HashMap<>());
        this.markLinkingInfoForWriting.get(code).putIfAbsent(song, new ArrayList<>());
        int triggerSize = this.triggerlinking.get(code).get(song).length;
        if(index>=triggerSize && !this.markLinkingInfoForWriting.get(code).get(song).contains(index-triggerSize)) this.markLinkingInfoForWriting.get(code).get(song).add(index-triggerSize);
        if(index<triggerSize) this.triggerlinking.get(code).get(song)[index] = newVal;
        else this.otherlinkinginfo.get(code).get(song)[index-triggerSize] = newVal;
    }

    public void write() throws IOException {
        StringBuilder mainBuilder = new StringBuilder();
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
                        if(!Mappings.parameters.get(i).matches("zone")) {
                            mainBuilder.append("\t\t").append(Mappings.parameters.get(i)).append(" = \"").append(this.triggerholder.get(code).get(trigger)[i]).append("\"\n");
                        } else {
                            mainBuilder.append("\t\t").append("[").append(this.songholder.get(code)).append(".trigger.zone]\n");
                            formatZoneParameter(mainBuilder, this.triggerholder.get(code).get(trigger)[i]);
                        }
                    }
                }
            }
            if(this.triggerlinking.get(stringMapEntry.getKey())!=null && !getAllSongsForLinking(code).isEmpty()) {
                mainBuilder.append("\t[").append(this.songholder.get(code)).append(".link]\n");
                mainBuilder.append(this.formatLinkingDefaults(code)).append("\n");
                for (Map.Entry<String, String[]> stringEntry : this.triggerlinking.get(stringMapEntry.getKey()).entrySet()) {
                    String song = stringEntry.getKey();
                    if(!song.matches(code)) {
                        mainBuilder.append(this.formatLinkingBrackets(code, this.songholder.get(code))).append("\n");
                        mainBuilder.append("\t\t\tsong = \"").append(song).append("\"\n");
                        if (this.markLinkingInfoForWriting.get(code) != null && this.markLinkingInfoForWriting.get(code).get(song) != null) {
                            for (int i : this.markLinkingInfoForWriting.get(code).get(song)) {
                                mainBuilder.append("\t\t\t").append(Mappings.linkingparameters.get(i)).append(" = \"").append(this.otherlinkinginfo.get(code).get(song)[i]).append("\"\n");
                            }
                        }
                        mainBuilder.append(this.formatLinkingTriggers(code, song)).append("\n");
                    }
                }
            }
            mainBuilder.append("\n");
        }
        FileWriter writeMain = new FileWriter(this.mainConfig);
        writeMain.write(mainBuilder.toString());
        writeMain.close();
        StringBuilder transitionBuilder = new StringBuilder();
        FileWriter writeTransitions = new FileWriter(this.titleCardConfig);
        writeTransitions.write(transitionBuilder.toString());
        writeTransitions.close();
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

    private String formatLinkingBrackets(String code, String song) {
        if (this.triggerlinking.get(code).entrySet().size()>1) return "\t\t[["+song+".link.trigger]]";
        else return "\t\t["+song+".link.trigger]";
    }

    private String formatLinkingDefaults(String code) {
        List<String> temp = this.getAllTriggersForCode(code);
        List<String> triggers = new ArrayList<>();
        for(String trigger : temp) {
            if(!this.triggerholder.get(code).get(trigger)[10].matches("minecraft")) triggers.add(trigger+"-"+this.triggerholder.get(code).get(trigger)[10]);
            else triggers.add(trigger);
        }
        StringBuilder defaults = new StringBuilder();
        defaults.append("\t\tdefault = [ ");
        for(String trigger : triggers) {
            defaults.append("\"").append(trigger).append("\" ");
        }
        defaults.append("]");
        return defaults.toString();
    }

    private String formatLinkingTriggers(String code, String song) {
        List<String> triggers = Arrays.stream(this.triggerlinking.get(code).get(song)).collect(Collectors.toList());
        StringBuilder triggerbuilder = new StringBuilder();
        triggerbuilder.append("\t\t\tname = [ ");
        for(String trigger : triggers) {
            triggerbuilder.append("\"").append(trigger).append("\" ");
        }
        triggerbuilder.append("]");
        return triggerbuilder.toString();
    }

    private void formatZoneParameter(StringBuilder builder, String zone) {
        String[] broken = stringBreaker(zone, ",");
        builder.append("\t\t\tx_min = \"").append(broken[0]).append("\"\n");
        builder.append("\t\t\ty_min = \"").append(broken[1]).append("\"\n");
        builder.append("\t\t\tz_min = \"").append(broken[2]).append("\"\n");
        builder.append("\t\t\tx_max = \"").append(broken[3]).append("\"\n");
        builder.append("\t\t\ty_max = \"").append(broken[4]).append("\"\n");
        builder.append("\t\t\tz_max = \"").append(broken[5]).append("\"\n");
    }

    public static String[] stringBreaker(String s, String regex) {
        return s.split(regex);
    }

    public void delete() {
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
        this.markLinkingInfoForWriting = new HashMap<>();
    }
}
