package mods.thecomputerizer.musictriggers.client.data;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.*;

public class Audio {

    private final Table data;
    private final List<Trigger> triggers;
    private final HashMap<Integer, Loop> loopMap;
    private final float volume;
    private final float pitch;
    private final int chance;
    private final int playOnce;
    private final int playX;
    private final boolean mustFinish;
    private final long milliStart;
    private final boolean resume;
    private long lastKnownTime;
    private boolean hasPlayed = false;
    private boolean wasInterrupted = false;
    private int playCounter = 0;

    public Audio(String channel, Table song, List<Trigger> triggers, @Nullable Table universal) {
        this.data = song;
        this.volume = song.getValOrDefault("volume",Objects.nonNull(universal) ?
                universal.getValOrDefault("volume",1f) : 1f);
        this.pitch = song.getValOrDefault("pitch",Objects.nonNull(universal) ?
                universal.getValOrDefault("pitch",1f) : 1f);
        this.chance = song.getValOrDefault("chance",100);
        this.playOnce = song.getValOrDefault("play_once",Objects.nonNull(universal) ?
                universal.getValOrDefault("play_once",0) : 0);
        this.playX = song.getValOrDefault("play_x",1);
        this.mustFinish = song.getValOrDefault("must_finish",Objects.nonNull(universal) ?
                universal.getValOrDefault("must_finish",false) : false);
        this.milliStart = song.getValOrDefault("start_at",0L);
        this.resume = song.getValOrDefault("resume_on_play",false);
        this.lastKnownTime = this.milliStart;
        this.triggers = parseTriggers(channel,triggers,song.getValOrDefault("triggers",new ArrayList<>()));
        this.loopMap = readLoops(channel,song.getTablesByName("loop"));
    }

    private List<Trigger> parseTriggers(String channel, List<Trigger> triggers, List<String> potentialTriggers) {
        List<Trigger> ret = new ArrayList<>();
        for(String potential : potentialTriggers) {
            boolean found = false;
            for(Trigger trigger : triggers) {
                if(trigger.getNameWithID().matches(potential)) {
                    ret.add(trigger);
                    found = true;
                    break;
                }
            }
            if(!found) MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Trigger with name {} under audio " +
                    "{} was not recognized as a registered trigger and will be skipped",channel,potential,getName());
        }
        return ret;
    }

    private HashMap<Integer, Loop> readLoops(String channel, List<Table> loops) {
        HashMap<Integer, Loop> ret = new HashMap<>();
        int index = 0;
        for(Table loop : loops) {
            Loop readLoop = new Loop(loop);
            if (readLoop.isValid()) {
                readLoop.setNum();
                ret.put(index, readLoop);
                index++;
            } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Loop table at index {} for song {} " +
                    "was invalid! Please double check that the parameters are correct, the from and to are different, " +
                    "and that the num_loops is set to a value greater than 0.",channel,index,getName());
        }
        return ret;
    }

    public String getName() {
        return this.data.getName();
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public int getChance() {
        return this.chance;
    }

    public int getPlayOnce() {
        return this.playOnce;
    }

    public boolean mustNotFinish() {
        return !this.mustFinish;
    }

    public long getMilliStart() {
        return this.milliStart;
    }

    public List<Trigger> getTriggers() {
        return this.triggers;
    }

    public Collection<Loop> getLoops() {
        return this.loopMap.values();
    }

    public int getPlayX() {
        return this.playX;
    }

    public void readPlayCount(int count) {
        this.playCounter = count;
    }

    public int getPlayCount() {
        return this.playCounter;
    }

    public void resetPlayCount() {
        this.playCounter = 0;
    }

    public boolean hasPlayedEnough() {
        return this.playCounter>=this.playX;
    }

    public boolean hasPlayed() {
        return this.hasPlayed;
    }

    public void onAudioStarted() {
        if(this.playX>0) this.playCounter++;
        if(hasPlayedEnough() && this.playOnce==3) this.hasPlayed = true;
        this.wasInterrupted = false;
    }

    public long getResumeTime() {
        return this.lastKnownTime;
    }

    public void onAudioStopping(long time) {
        if(!this.wasInterrupted) {
            if (time > 0) this.wasInterrupted = true;
            if (this.resume) this.lastKnownTime = Math.max(time, this.milliStart);
            for (Loop loop : getLoops()) loop.setNum();
        }
    }

    public void onLogOut() {
        if(this.playOnce==3) {
            this.hasPlayed = false;
            this.playCounter = 0;
        }
    }

    public static final class Loop {

        private final long whenAt;
        private final long setTo;
        private final int num_loops;
        private int loopsLeft;

        private Loop(Table data) {
            if(Objects.nonNull(data)) {
                this.whenAt = data.getValOrDefault("from",0);
                this.setTo = data.getValOrDefault("to",0);
                this.num_loops = data.getValOrDefault("num_loops",0);
            } else {
                this.whenAt = 1;
                this.setTo = 2;
                this.num_loops = 1;
            }
        }

        public boolean isValid() {
            return this.setTo!=this.whenAt || this.num_loops>0;
        }

        public void setNum() {
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

        public long getFrom() {
            return this.whenAt;
        }

        public long getTo() {
            return this.setTo;
        }
    }
}
