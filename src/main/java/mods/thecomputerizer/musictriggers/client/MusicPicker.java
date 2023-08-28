package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.client.data.Audio;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class MusicPicker {
    private final Map<Trigger, MutableInt> triggerPersistence;
    private final Map<Trigger, MutableInt> startMap;
    private final Map<Trigger, MutableInt> stopMap;
    public final Set<Trigger.Link> activeLinks;
    public final List<Trigger> dynamicTemp;
    public final List<Trigger> removePersistentPlayable;
    private final Channel channel;
    private final Info info;

    private Table universal;
    private boolean hasLoaded;
    public int fadeIn;
    public int fadeOut;
    public String triggerDelay;
    public String songDelay;
    public String crashHelper;

    public MusicPicker(Channel channel) {
        this.channel = channel;
        this.info = new Info(channel);
        this.triggerPersistence = new HashMap<>();
        this.startMap = new HashMap<>();
        this.stopMap = new HashMap<>();
        this.activeLinks = new HashSet<>();
        this.dynamicTemp = new ArrayList<>();
        this.removePersistentPlayable = new ArrayList<>();
        this.triggerDelay = "0";
        this.songDelay = "0";
    }

    public Info getInfo() {
        return this.info;
    }

    public void initUniveral(@Nullable Table universal) {
        this.universal = universal;
    }

    public void initTimers(Trigger trigger) {
        this.triggerPersistence.put(trigger,initTimer(0));
        this.startMap.put(trigger,initTimer(trigger,"start_delay",universal));
        this.stopMap.put(trigger,initTimer(0));
    }

    private MutableInt initTimer(Trigger trigger, String parameter, @Nullable Table universal) {
        return initTimer(trigger.getParameterWithUniversal(parameter,universal,0));
    }

    private MutableInt initTimer(int val) {
        return new MutableInt(val);
    }

    public void tickTimers(int ticks) {
        tickTimers(this.triggerPersistence.values(),ticks);
        tickTimers(this.startMap.values(),ticks);
        tickTimers(this.stopMap.values(),ticks);
    }

    private void tickTimers(Collection<MutableInt> timers, int ticks) {
        for(MutableInt timer : timers)
            if(timer.getValue()>0) timer.add(-1*ticks);
    }

    public void initStopDelay(Trigger trigger) {
        this.stopMap.get(trigger).setValue(trigger.getParameterWithUniversal("stop_delay",this.universal,0));
    }

    /**
     * Only runs when the channel is toggled off
     */
    public void skipQuery() {
        this.getInfo().clear();
        this.activeLinks.clear();
        this.triggerPersistence.clear();
        this.startMap.clear();
        this.dynamicTemp.clear();
        this.removePersistentPlayable.clear();
    }

    public void querySongList() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        this.getInfo().updatePlayableTriggers(new ArrayList<>());
        if(Objects.isNull(player)) {
            if(Objects.nonNull(mc.screen)) {
                if(mc.screen instanceof TitleScreen || Objects.nonNull(mc.level)) this.hasLoaded = true;
                Trigger menu = this.channel.getSimpleTrigger("menu");
                if (Objects.isNull(mc.level) && Objects.nonNull(menu)) {
                    setStaticTriggerValues(menu);
                    return;
                }
            }
            if(!this.hasLoaded) {
                Trigger loading = this.channel.getSimpleTrigger("loading");
                if (Objects.nonNull(loading)) {
                    setStaticTriggerValues(loading);
                    return;
                }
            }
        } else {
            List<Audio> activeSongs = comboChecker(priorityHandler(playableTriggers(player)));
            if (!activeSongs.isEmpty()) {
                this.getInfo().updateActiveTriggers(activeSongs.stream().map(Audio::getTriggers)
                        .flatMap(Collection::stream).distinct().collect(Collectors.toList()));
                for (Trigger trigger : this.removePersistentPlayable)
                    if (!this.getInfo().getActiveTriggers().contains(trigger) && this.triggerPersistence.containsKey(trigger))
                        this.triggerPersistence.get(trigger).setValue(0);
                this.removePersistentPlayable.clear();
                this.dynamicTemp.clear();
                this.getInfo().updateSongSet(activeSongs);
                this.info.runToggles();
                this.activeLinks.removeIf(link -> !link.isActive(this.getInfo().activeTriggers));
                return;
            }
            Trigger generic = this.channel.getSimpleTrigger("generic");
            if (Objects.nonNull(generic)) {
                setStaticTriggerValues(generic);
                return;
            }
        }
        this.getInfo().updateActiveTriggers(new ArrayList<>());
        this.getInfo().updateSongSet(new ArrayList<>());
        this.info.runToggles();
        this.triggerDelay = "0";
        this.songDelay = "0";
        this.fadeIn = 0;
        this.fadeOut = 0;
        this.activeLinks.clear();
    }

    private void setStaticTriggerValues(Trigger trigger) {
        this.getInfo().updatePlayableTriggers(Collections.singletonList(trigger));
        this.getInfo().updateActiveTriggers(Collections.singletonList(trigger));
        this.getInfo().updateSongSet(this.channel.getSongPool(trigger));
        updateTickingParameters(trigger);
        this.info.runToggles();
        this.activeLinks.clear();
        this.activeLinks.addAll(trigger.getLinks());
    }

    private void updateTickingParameters(Trigger trigger) {
        this.triggerDelay = trigger.getParameterWithUniversal("trigger_delay",this.universal,"0");
        this.songDelay = trigger.getParameterWithUniversal("song_delay",this.universal,"0");
        this.fadeIn = trigger.getParameterWithUniversal("fade_in",this.universal,0);
        this.fadeOut = trigger.getParameterWithUniversal("fade_out",this.universal,0);
    }

    private List<Audio> removeEmptyCombinations(Trigger priorityTrigger, List<Audio> found) {
        if(!found.isEmpty() || this.dynamicTemp.isEmpty()) return found;
        this.dynamicTemp.remove(priorityTrigger);
        return comboChecker(innerPriorityHandler(this.dynamicTemp));
    }

    private List<Audio> comboChecker(Trigger priorityTrigger) {
        if (Objects.isNull(priorityTrigger)) return new ArrayList<>();
        List<Audio> found = new ArrayList<>();
        if(ConfigDebug.COMBINE_EQUAL_PRIORITY) {
            int priority = priorityTrigger.getParameterInt("priority");
            List<Trigger> equalPriority = new ArrayList<>();
            for(Trigger trigger : this.dynamicTemp)
                if(trigger.getParameterInt("priority")==priority) equalPriority.add(trigger);
            for(Trigger trigger : equalPriority)
                for(Audio audio : this.channel.getSongPool(trigger))
                    if(!found.contains(audio) && isAudioPlayable(audio)) found.add(audio);
            return found;
        }
        for(Audio audio : this.channel.getSongPool(priorityTrigger))
            if(!found.contains(audio) && isAudioPlayable(audio)) found.add(audio);
        List<Audio> combinations = new ArrayList<>();
        for(Audio audio : found)
            if(audio.getTriggers().size()>1) combinations.add(audio);
        if(combinations.isEmpty()) return removeEmptyCombinations(priorityTrigger,found);
        return removeEmptyCombinations(priorityTrigger,recursiveCombination(priorityTrigger,combinations));
    }

    private List<Audio> recursiveCombination(Trigger priorityTrigger, List<Audio> combinations) {
        List<Trigger> activeTriggers = new ArrayList<>();
        for(Audio audio : combinations)
            for(Trigger trigger : audio.getTriggers())
                if(!activeTriggers.contains(trigger) && trigger != priorityTrigger) activeTriggers.add(trigger);
        Trigger nextPriority = innerPriorityHandler(activeTriggers);
        List<Audio> nextCombinations = new ArrayList<>();
        for(Audio audio : combinations)
            if(audio.getTriggers().contains(nextPriority)) nextCombinations.add(audio);
        if(nextCombinations.size()<combinations.size()) return recursiveCombination(nextPriority,nextCombinations);
        nextCombinations.clear();
        int longestCombination = 2;
        for(Audio audio : combinations)
            if(audio.getTriggers().size()>longestCombination) longestCombination = audio.getTriggers().size();
        for(Audio audio : combinations)
            if(audio.getTriggers().size()==longestCombination) nextCombinations.add(audio);
        if(nextCombinations.size()<combinations.size()) return recursiveCombination(nextPriority,nextCombinations);
        return combinations;
    }


    private boolean isAudioPlayable(Audio audio) {
        return new HashSet<>(this.getInfo().getPlayableTriggers()).containsAll(audio.getTriggers()) && !audio.hasPlayed();
    }

    public Trigger priorityHandler(List<Trigger> playableTriggers) {
        playableTriggers.removeIf(Objects::isNull);
        this.dynamicTemp.clear();
        this.dynamicTemp.addAll(playableTriggers);
        this.dynamicTemp.removeIf(trigger -> trigger.maxedAudioCount() || this.channel.getSongPool(trigger).isEmpty());
        Trigger priorityTrigger = innerPriorityHandler(playableTriggers);
        if(Objects.isNull(priorityTrigger)) return null;
        updateTickingParameters(priorityTrigger);
        this.activeLinks.clear();
        this.activeLinks.addAll(priorityTrigger.getLinks());
        return priorityTrigger;
    }

    private Trigger innerPriorityHandler(List<Trigger> playableTriggers) {
        if(playableTriggers.isEmpty() || this.dynamicTemp.isEmpty()) return null;
        Trigger priorityTrigger = ConfigDebug.REVERSE_PRIORITY ? Collections.min(this.dynamicTemp,
                Comparator.comparingInt(trigger -> trigger.getParameterInt("priority"))) : Collections.max(this.dynamicTemp,
                Comparator.comparingInt(trigger -> trigger.getParameterInt("priority")));
        while (this.channel.getSongPool(priorityTrigger).isEmpty()) {
            this.dynamicTemp.remove(priorityTrigger);
            if (playableTriggers.isEmpty()) return null;
            priorityTrigger = ConfigDebug.REVERSE_PRIORITY ? Collections.min(this.dynamicTemp,
                    Comparator.comparingInt(trigger -> trigger.getParameterInt("priority"))) : Collections.max(this.dynamicTemp,
                    Comparator.comparingInt(trigger -> trigger.getParameterInt("priority")));
        }
        return priorityTrigger;
    }

    public List<Trigger> playableTriggers(LocalPlayer player) {
        this.crashHelper = "playable_triggers_init";
        try {
            List<Trigger> events = this.channel.getRegisteredTriggers().stream().filter(this::nonStaticTrigger)
                    .filter(trigger -> this.stopMap.get(trigger).getValue()<=0)
                    .map(trigger -> addPlayableTrigger(trigger, trigger.runActivationFunction(player)))
                    .filter(Objects::nonNull).distinct().collect(Collectors.toList());
            events.removeIf(trigger -> !trigger.isToggled());
            for(Map.Entry<Trigger, MutableInt> persistentEntry : this.triggerPersistence.entrySet())
                if(!events.contains(persistentEntry.getKey())) persistentEntry.getValue().setValue(0);
            this.info.updatePlayableTriggers(events);
            return events;
        } catch (Exception e) {
            Constants.MAIN_LOG.error("There was an uncaught error when checking trigger conditions!",e);
            if(this.crashHelper.isEmpty())
                throw new RuntimeException("There was an uncaught error! This should be reported! The error was "+
                        e.getMessage()+" and was caught on line "+e.getStackTrace()[0].getLineNumber()+" in the class "+
                        e.getStackTrace()[0]);
            else throw new RuntimeException("There was a problem with your "+this.crashHelper+" trigger! The error was "+
                    e.getMessage()+" and was caught on line "+e.getStackTrace()[0].getLineNumber()+" in the class "+
                    e.getStackTrace()[0]);
        }
    }

    private boolean nonStaticTrigger(Trigger trigger) {
        String name = trigger.getName();
        return !name.matches("menu") && !name.matches("generic") && !name.matches("loading");
    }

    private Trigger addPlayableTrigger(Trigger trigger, boolean isActive) {
        this.crashHelper = trigger.getNameWithID();
        Trigger ret = null;
        MutableInt persistentTimer = this.triggerPersistence.get(trigger);
        MutableInt startTimer = this.startMap.get(trigger);
        if(isActive || persistentTimer.getValue()>0) {
            if(startTimer.getValue() <= 0) {
                ret = trigger;
                this.dynamicTemp.add(trigger);
                if(isActive)
                    persistentTimer.setValue(trigger.getParameterWithUniversal("persistence",this.universal,0));
                if(!trigger.getParameterBool("passive_persistence")) this.removePersistentPlayable.add(trigger);
            }
        } else startTimer.setValue(trigger.getParameterWithUniversal("start_delay",this.universal,0));
        return ret;
    }

    public static final class Info {
        private final Channel channel;
        private final HashSet<Audio> currentSongSet;
        private final HashSet<Audio> previousSongSet;
        private final Set<Trigger> playableTriggers;
        private final HashSet<Trigger> toggledPlayableTriggers;
        private final HashSet<Trigger> activeTriggers;
        private final HashSet<Trigger> toggledActiveTriggers;
        private int highestActivePriority;

        public Info(Channel channel) {
            this.channel = channel;
            this.currentSongSet = new HashSet<>();
            this.previousSongSet = new HashSet<>();
            this.playableTriggers = Collections.synchronizedSet(new HashSet<>());
            this.toggledPlayableTriggers = new HashSet<>();
            this.activeTriggers = new HashSet<>();
            this.toggledActiveTriggers = new HashSet<>();
            this.highestActivePriority = Integer.MIN_VALUE;
        }

        public HashSet<Audio> getCurrentSongSet() {
            return this.currentSongSet;
        }

        public boolean songListChanged() {
            return !this.currentSongSet.equals(this.previousSongSet);
        }

        public boolean canReverseFade(HashSet<Audio> playingAudio) {
            return !this.currentSongSet.isEmpty() && this.currentSongSet.equals(playingAudio);
        }

        public void updateSongSet(List<Audio> newSongs) {
            this.previousSongSet.clear();
            this.previousSongSet.addAll(this.currentSongSet);
            this.currentSongSet.clear();
            this.currentSongSet.addAll(newSongs);
            if(!this.previousSongSet.isEmpty() && songListChanged())
                for(Audio audio : this.previousSongSet)
                    if(audio.getPlayOnce()<3)
                        audio.resetPlayCount();
        }

        public Set<Trigger> getPlayableTriggers() {
            synchronized(this.playableTriggers) {
                return this.playableTriggers;
            }
        }

        public void updatePlayableTriggers(List<Trigger> newTriggers) {
            synchronized(this.playableTriggers) {
                for(Trigger prevPlayable : this.playableTriggers)
                    if(!newTriggers.contains(prevPlayable))
                        prevPlayable.reenableAudio();
                this.playableTriggers.clear();
                this.playableTriggers.addAll(newTriggers);
            }
        }

        public HashSet<Trigger> getActiveTriggers() {
            return this.activeTriggers;
        }

        public Trigger highestPriorityActive() {
            if(this.activeTriggers.isEmpty()) return null;
            return ConfigDebug.REVERSE_PRIORITY ?
                    Collections.min(this.activeTriggers,Comparator.comparingInt(trigger -> trigger.getParameterInt("priority"))) :
                    Collections.max(this.activeTriggers,Comparator.comparingInt(trigger -> trigger.getParameterInt("priority")));
        }

        public void updateActiveTriggers(List<Trigger> activeTriggers) {
            this.activeTriggers.clear();
            this.activeTriggers.addAll(activeTriggers);
            this.highestActivePriority = Integer.MIN_VALUE;
            for(Trigger trigger : this.activeTriggers) {
                int priority = trigger.getParameterInt("priority");
                if (priority > this.highestActivePriority) this.highestActivePriority = priority;
            }
        }

        public int getHighestActivePriority() {
            return this.highestActivePriority;
        }

        public void runToggles() {
            runPlayableToggle();
            runActiveToggle();
        }

        private void runPlayableToggle() {
            synchronized(this.playableTriggers) {
                this.toggledPlayableTriggers.removeIf(trigger -> !this.playableTriggers.contains(trigger));
                HashSet<Trigger> toggleSet = new HashSet<>();
                for (Trigger trigger : this.playableTriggers)
                    if (!this.toggledPlayableTriggers.contains(trigger)) {
                        toggleSet.add(trigger);
                        this.toggledPlayableTriggers.add(trigger);
                    }
                if (!toggleSet.isEmpty()) this.channel.updateFutureToggles(2,toggleSet);
            }
        }

        private void runActiveToggle() {
            this.toggledActiveTriggers.removeIf(trigger -> !this.activeTriggers.contains(trigger));
            HashSet<Trigger> toggleSet = new HashSet<>();
            for(Trigger trigger : this.activeTriggers) {
                if(!this.toggledActiveTriggers.contains(trigger)) {
                    toggleSet.add(trigger);
                    this.toggledActiveTriggers.add(trigger);
                }
            }
            if(!toggleSet.isEmpty()) this.channel.updateFutureToggles(3,toggleSet);
        }

        public void clearSongLists() {
            this.currentSongSet.clear();
            this.previousSongSet.clear();

        }

        public void clear() {
            synchronized(this.playableTriggers) {
                clearSongLists();
                this.playableTriggers.clear();
                this.toggledPlayableTriggers.clear();
                this.activeTriggers.clear();
                this.toggledActiveTriggers.clear();
            }
        }
    }
}