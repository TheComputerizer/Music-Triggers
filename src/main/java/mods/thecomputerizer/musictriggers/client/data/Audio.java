package mods.thecomputerizer.musictriggers.client.data;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import org.apache.logging.log4j.Level;

import java.util.*;

public class Audio {

    private final String name;
    private final int doubleBracketIndex;
    private final List<Trigger> triggers;
    private final HashMap<Integer, Loop> loopMap;
    private float volume;
    private float pitch;
    private int chance;
    private int playOnce;
    private boolean mustFinish;
    private int loadOrder;

    public Audio(Toml table, String channel, String name, int loadOrder, int multiIndex) {
        this.name = name;
        this.loadOrder = loadOrder;
        this.doubleBracketIndex = multiIndex;
        this.triggers = new ArrayList<>();
        this.loopMap = new HashMap<>();
        String[] data = new String[]{"1","1","100","0","false"};
        if(Objects.nonNull(table)) data = readInfo(table, channel);
        this.volume = readFloat("volume", data[0]);
        this.pitch = readFloat("pitch", data[1]);
        this.chance = readInt("chance", data[2], 100);
        this.playOnce = readInt("play_once", data[3], 0);
        this.mustFinish = Boolean.parseBoolean(data[4]);
    }

    private float readFloat(String parameter, String element) {
        return MusicTriggers.randomFloat(parameter, element, 1f);
    }

    private int readInt(String parameter, String element, int def) {
        return MusicTriggers.randomInt(parameter, element, def);
    }

    private String[] readInfo(Toml table, String channel) {
        String[] songData = new String[]{TomlUtil.sneakyFloat(table, "volume", 1f),
                TomlUtil.sneakyFloat(table, "pitch", 1f),
                TomlUtil.sneakyInt(table, "chance", 100),
                TomlUtil.sneakyInt(table, "play_once", 0),
                TomlUtil.sneakyBool(table, "must_finish", false)};
        if (table.containsTable("trigger")) {
            Toml triggerTable = table.getTable("trigger");
            if (triggerTable.contains("name"))
                readTrigger(triggerTable, channel);
            else MusicTriggers.logExternally(Level.WARN, "Skipping trigger block in channel {} because there " +
                    "was no name!",channel);
        } else if (table.containsTableArray("trigger")) {
            for(Toml triggerTable : table.getTables("trigger")) {
                if (triggerTable.contains("name"))
                    readTrigger(triggerTable, channel);
                else MusicTriggers.logExternally(Level.WARN, "Skipping trigger block in channel {} because there " +
                        "was no name!",channel);
            }
        }
        int loopIndex = 0;
        if (table.containsTableArray("loop"))
            for (Toml loopTable : table.getTables("loop"))
                loopIndex = readLoop(loopTable, loopIndex);
        else if (table.containsTable("loop"))
            readLoop(table.getTable("loop"), loopIndex);
        return songData;
    }

    private void readTrigger(Toml table, String channel) {
        String triggerName = table.getString("name");
        if (Trigger.getAcceptedTriggers().contains(triggerName)) {
            Trigger parsed = Trigger.createOrGetInstance(triggerName, channel,
                    this, table);
            if (parsed != null) this.triggers.add(parsed);
        } else if (Trigger.getAllTriggers().contains(triggerName))
            MusicTriggers.logExternally(Level.WARN, "Trigger {} in channel {} is not a valid trigger for " +
                    "the current version of Music Triggers so it will be skipped!", triggerName, channel);
        else MusicTriggers.logExternally(Level.WARN, "Trigger {} in channel {} is not recognized as a valid " +
                    "trigger name so it will be skipped!", triggerName, channel);
    }

    private int readLoop(Toml loopTable, int index) {
        Loop readLoop = new Loop(loopTable);
        if (readLoop.isValid()) {
            this.loopMap.put(index, readLoop);
            index++;
        } else {
            if (this.doubleBracketIndex < 0)
                MusicTriggers.logExternally(Level.WARN, "Loop table at index {} for song {} was invalid! " +
                        "Please double check that the parameters are correct, the from and to are different, and " +
                        "that the num_loops is set to a value greater than 0.", index + 1, this.name);
            else MusicTriggers.logExternally(Level.WARN, "Loop table at index {} for song {} (instance {})was" +
                            " invalid! Please double check that the parameters are correct, the from and to are different," +
                            " and that the num_loops is set to a value greater than 0.", index + 1, this.name,
                    this.doubleBracketIndex + 1);
        }
        return index;
    }

    public void initializeTriggerPersistence(String channel) {
        for(Trigger trigger : this.triggers)
            ChannelManager.getChannel(channel).initializeTriggerPersistence(trigger);
    }

    public List<String> getAsTomlLines() {
        List<String> lines = new ArrayList<>();
        if(this.triggers.isEmpty()) return lines;
        lines.add(this.doubleBracketIndex>=0 ? "[["+this.name+"]]" : "["+this.name+"]");
        if(this.volume!=1f) lines.add("\tvolume = "+this.volume);
        if(this.pitch!=1f) lines.add("\tpitch = "+this.pitch);
        if(this.chance!=100) lines.add("\tchance = "+this.chance);
        if(this.playOnce!=0) lines.add("\tplay_once = "+this.playOnce);
        if(this.mustFinish) lines.add("\tmust_finish = "+true);
        for(Trigger trigger : this.triggers)
            lines.addAll(trigger.getAsTomlLines(this.name,this.triggers.size()>1));
        for(Loop loop : this.loopMap.values())
            lines.addAll(loop.getAsTomlLines(this.name, this.loopMap.size()>1));
        lines.add("");
        return lines;
    }

    public String getName() {
        return this.name;
    }

    public int getDoubleBracketIndex() {
        return this.doubleBracketIndex;
    }

    public float getVolume() {
        return this.volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public int getChance() {
        return this.chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public int getPlayOnce() {
        return this.playOnce;
    }

    public void setPlayOnce(int playOnce) {
        this.playOnce = playOnce;
    }

    public boolean mustFinish() {
        return this.mustFinish;
    }

    public void setMustFinish(boolean mustFinish) {
        this.mustFinish = mustFinish;
    }

    public int getLoadOrder() {
        return loadOrder;
    }

    public void setLoadOrder(int loadOrder) {
        this.loadOrder = loadOrder;
    }

    public List<Trigger> getTriggers() {
        return this.triggers;
    }

    public void removeTrigger(Trigger trigger) {
        this.triggers.remove(trigger);
    }

    public void addTrigger(Trigger trigger) {
        if(!this.triggers.contains(trigger)) triggers.add(trigger);
    }

    public Collection<Loop> getLoops() {
        return this.loopMap.values();
    }

    public static final class Loop {

        private final long whenAt;
        private final long setTo;
        private final int num_loops;
        private int loopsLeft;

        private Loop(Toml table) {
            long[] data = readTable(table);
            this.whenAt = data[0];
            this.setTo = data[1];
            this.num_loops = (int) data[2];
        }

        private long[] readTable(Toml table) {
            return new long[]{readEntry("from", TomlUtil.sneakyInt(table,"from",0)),
                    readEntry("to", TomlUtil.sneakyInt(table,"to",0)),
                    readEntry("num_loops", TomlUtil.sneakyInt(table,"num_loops",0))};
        }

        private long readEntry(String key, String parameter) {
            try {
                return Long.parseLong(parameter);
            } catch (NumberFormatException ignored) {
                MusicTriggers.logExternally(Level.ERROR,"Tried to set loop parameter {} to {} but that is not a " +
                        "number!",key,parameter);
                return 0L;
            }
        }

        public boolean isValid() {
            return this.setTo!=this.whenAt || this.num_loops>0;
        }

        public List<String> getAsTomlLines(String songName, boolean multi) {
            List<String> lines = new ArrayList<>();
            lines.add(multi ? "\t[["+songName+".loop]]" : "\t["+songName+".loop]");
            lines.add("\t\tfrom = "+this.whenAt);
            lines.add("\t\tto = "+this.setTo);
            lines.add("\t\tnum_loops = "+this.num_loops);
            return lines;
        }

        public void initialize() {
            this.loopsLeft = this.num_loops;
        }

        public long checkForLoop(long from, long total) {
            if(this.loopsLeft>0) {
                if(from>=this.whenAt) {
                    if(this.setTo>=total)
                        MusicTriggers.logExternally(Level.ERROR,"Tried to set the position of a song at or past" +
                                "its duration! attempt: {} duration: {}",this.setTo,total);
                    else {
                        this.loopsLeft--;
                        return this.setTo;
                    }
                }
            }
            return from;
        }
    }
}
