package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.data.Audio;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.MainMenuScreen;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class MusicPicker {
    private final Channel channel;
    private final Info info;

    public final HashMap<Trigger, MutableInt> triggerPersistence = new HashMap<>();
    public final HashMap<Trigger, MutableInt> startMap = new HashMap<>();
    public final HashMap<Integer, Boolean> victory = new HashMap<>();
    public final List<Trigger> dynamicTemp = new ArrayList<>();
    public final List<Trigger> removePersistentPlayable = new ArrayList<>();

    public static final List<String> effectList = new ArrayList<>();

    private boolean hasLoaded = false;
    public int fadeIn = 0;
    public int fadeOut = 0;
    public String triggerDelay = "0";
    public String songDelay = "0";
    public String crashHelper;

    public MusicPicker(Channel channel) {
        this.channel = channel;
        this.info = new Info(channel.getChannelName());
    }

    public Info getInfo() {
        return this.info;
    }

    public void querySongList(Optional<Table> universal) {
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;
        this.getInfo().updatePlayableTriggers(new ArrayList<>());
        if(player == null) {
            if(Objects.nonNull(mc.screen)) {
                if(mc.screen instanceof MainMenuScreen || Objects.nonNull(mc.level)) this.hasLoaded = true;
                Trigger menu = this.channel.getSimpleTrigger("menu");
                if (mc.level == null && Objects.nonNull(menu)) {
                    this.getInfo().updatePlayableTriggers(Collections.singletonList(menu));
                    this.getInfo().updateActiveTriggers(Collections.singletonList(menu));
                    this.getInfo().updateSongList(this.channel.getSongPool(menu));
                    this.info.runToggles();
                    return;
                }
            }
            if(!this.hasLoaded) {
                Trigger loading = this.channel.getSimpleTrigger("loading");
                if (Objects.nonNull(loading)) {
                    this.getInfo().updatePlayableTriggers(Collections.singletonList(loading));
                    this.getInfo().updateActiveTriggers(Collections.singletonList(loading));
                    this.getInfo().updateSongList(this.channel.getSongPool(loading));
                    this.info.runToggles();
                    return;
                }
            }
        } else {
            List<Audio> activeSongs = comboChecker(priorityHandler(playableTriggers(player,universal),universal));
            if (!activeSongs.isEmpty()) {
                this.getInfo().updateActiveTriggers(activeSongs.stream().map(Audio::getTriggers).flatMap(Collection::stream)
                        .distinct().collect(Collectors.toList()));
                for (Trigger trigger : this.removePersistentPlayable)
                    if (!this.getInfo().getActiveTriggers().contains(trigger) && this.triggerPersistence.containsKey(trigger))
                        this.triggerPersistence.get(trigger).setValue(0);
                this.removePersistentPlayable.clear();
                this.dynamicTemp.clear();
                this.getInfo().updateSongList(activeSongs);
                this.info.runToggles();
                return;
            }
            Trigger generic = this.channel.getSimpleTrigger("generic");
            if (Objects.nonNull(generic)) {
                this.getInfo().updatePlayableTriggers(Collections.singletonList(generic));
                this.getInfo().updateActiveTriggers(Collections.singletonList(generic));
                this.getInfo().updateSongList(this.channel.getSongPool(generic));
                this.triggerDelay = generic.getParameter("trigger_delay");
                if (this.triggerDelay .matches("0"))
                    this.triggerDelay = universal.isPresent() ?
                            universal.get().getValOrDefault("trigger_delay","0") : "0";
                this.songDelay = generic.getParameter("song_delay");
                if (this.songDelay .matches("0"))
                    this.songDelay = universal.isPresent() ?
                            universal.get().getValOrDefault("song_delay","0") : "0";
                this.fadeIn = generic.getParameterInt("fade_in");
                if (this.fadeIn == 0) this.fadeIn = universal.map(table -> MusicTriggers.randomInt("universal_fade_in",
                        table.getValOrDefault("fade_in", "0"), 0)).orElse(0);
                this.fadeOut = generic.getParameterInt("fade_out");
                if (this.fadeOut == 0) this.fadeIn = universal.map(table -> MusicTriggers.randomInt("universal_fade_out",
                        table.getValOrDefault("fade_out", "0"), 0)).orElse(0);
                this.info.runToggles();
                return;
            }
        }
        this.getInfo().updateActiveTriggers(new ArrayList<>());
        this.getInfo().updateSongList(new ArrayList<>());
        this.info.runToggles();
        this.triggerDelay = "0";
        this.songDelay = "0";
        this.fadeIn = 0;
        this.fadeOut = 0;
    }

    private List<Audio> removeEmptyCombinations(Trigger priorityTrigger, List<Audio> found) {
        if(!found.isEmpty() || this.dynamicTemp.isEmpty()) return found;
        this.dynamicTemp.remove(priorityTrigger);
        return comboChecker(priorityHandler(this.dynamicTemp));
    }


    private List<Audio> comboChecker(Trigger priorityTrigger) {
        if (priorityTrigger == null) return new ArrayList<>();
        List<Audio> found = new ArrayList<>();
        if(ConfigDebug.COMBINE_EQUAL_PRIORITY) {
            int priority = priorityTrigger.getParameterInt("priority");
            List<Trigger> equalPriority = new ArrayList<>();
            for(Trigger trigger : this.dynamicTemp)
                if(trigger.getParameterInt("priority")==priority) equalPriority.add(trigger);
            for(Trigger trigger : equalPriority)
                for(Audio audio : this.channel.getSongPool(trigger))
                    if(!found.contains(audio) && isAudioPlayable(audio)) found.add(audio);
            this.getInfo().updateActiveTriggers(equalPriority);
            return found;
        }
        for(Audio audio : this.channel.getSongPool(priorityTrigger))
            if(!found.contains(audio) && isAudioPlayable(audio)) found.add(audio);
        List<Audio> combinations = new ArrayList<>();
        for(Audio audio : found)
            if(audio.getTriggers().size()>1) combinations.add(audio);
        if(combinations.size()==0) return removeEmptyCombinations(priorityTrigger,found);
        return removeEmptyCombinations(priorityTrigger,recursiveCombination(priorityTrigger,combinations));
    }

    private List<Audio> recursiveCombination(Trigger priorityTrigger, List<Audio> combinations) {
        List<Trigger> activeTriggers = new ArrayList<>();
        for(Audio audio : combinations)
            for(Trigger trigger : audio.getTriggers())
                if(!activeTriggers.contains(trigger) && trigger != priorityTrigger) activeTriggers.add(trigger);
        Trigger nextPriority = priorityHandler(activeTriggers);
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
        return new HashSet<>(this.getInfo().getPlayableTriggers()).containsAll(audio.getTriggers());
    }

    public Trigger priorityHandler(List<Trigger> playableTriggers, Optional<Table> universal) {
        playableTriggers.removeIf(Objects::isNull);
        this.dynamicTemp.clear();
        this.dynamicTemp.addAll(playableTriggers);
        Trigger priorityTrigger = priorityHandler(playableTriggers);
        if(Objects.isNull(priorityTrigger)) return null;
        this.triggerDelay = priorityTrigger.getParameter("trigger_delay");
        if (this.triggerDelay.matches("0"))
            this.triggerDelay = universal.isPresent() ? universal.get().getValOrDefault("trigger_delay", "0") : "0";
        this.songDelay = priorityTrigger.getParameter("song_delay");
        if (this.songDelay.matches("0"))
            this.songDelay = universal.isPresent() ? universal.get().getValOrDefault("song_delay", "0") : "0";
        this.fadeIn = priorityTrigger.getParameterInt("fade_in");
        if (this.fadeIn == 0) this.fadeIn = universal.map(table -> MusicTriggers.randomInt("universal_fade_in",
                table.getValOrDefault("fade_in", "0"), 0)).orElse(0);
        this.fadeOut = priorityTrigger.getParameterInt("fade_out");
        if (this.fadeOut == 0) this.fadeOut = universal.map(table -> MusicTriggers.randomInt("universal_fade_out",
                table.getValOrDefault("fade_out", "0"), 0)).orElse(0);
        return priorityTrigger;
    }

    private Trigger priorityHandler(List<Trigger> playableTriggers) {
        if(playableTriggers.isEmpty()) return null;
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

    public List<Trigger> playableTriggers(ClientPlayerEntity player, Optional<Table> universal) {
        crashHelper = "";
        try {
            List<Trigger> events = this.channel.getRegisteredTriggers().stream().filter(this::nonStaticTrigger)
                    .map(trigger -> addPlayableTrigger(trigger, universal, trigger.runActivationFunction(player)))
                    .filter(Objects::nonNull).distinct().collect(Collectors.toList());
            events.removeIf(trigger -> !trigger.isToggled());
            for(Map.Entry<Trigger, MutableInt> persistentEntry : this.triggerPersistence.entrySet())
                if(!events.contains(persistentEntry.getKey())) persistentEntry.getValue().setValue(0);
            this.info.updatePlayableTriggers(events);
            return events;
        } catch (Exception e) {
            e.printStackTrace();
            if(crashHelper.isEmpty())
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

    private Trigger addPlayableTrigger(Trigger trigger, Optional<Table> universal, boolean isActive) {
        this.crashHelper = trigger.getNameWithID();
        Trigger ret = null;
        this.triggerPersistence.putIfAbsent(trigger, new MutableInt(0));
        boolean persistent = this.triggerPersistence.get(trigger).getValue()>0;
        if(isActive || persistent) {
            if (!this.startMap.containsKey(trigger)) {
                int start = trigger.getParameterInt("start_delay");
                if (start == 0) start = universal.map(table -> MusicTriggers.randomInt("universal_start_delay",
                        table.getValOrDefault("start_delay", "0"), 0)).orElse(0);
                this.startMap.put(trigger, new MutableInt(start));
            }
            if (this.startMap.get(trigger).getValue() <= 0) {
                ret = trigger;
                this.dynamicTemp.add(trigger);
                if (isActive) {
                    int check = trigger.getParameterInt("persistence");
                    check = check > 0 ? check :
                            universal.map(table -> MusicTriggers.randomInt("universal_persistence",
                                    table.getValOrDefault("persistence", "0"), 0)).orElse(0);
                    this.triggerPersistence.get(trigger).setValue(check);
                }
                if (!trigger.getParameterBool("passive_persistence")) this.removePersistentPlayable.add(trigger);
            }
        }
        return ret;
    }

    public boolean getVictory(int id) {
        boolean ret = this.victory.get(id)!=null && this.victory.get(id);
        if(ret) this.victory.put(id,false);
        return ret;
    }

    public static final class Info {
        private final String channel;
        private final List<Audio> currentSongList;
        private final List<Audio> previousSongList;
        private final List<Trigger> playableTriggers;
        private final List<Trigger> toggledPlayableTriggers;
        private final List<Trigger> activeTriggers;
        private final List<Trigger> toggledActiveTriggers;
        public Info(String channel) {
            this.channel = channel;
            this.currentSongList = new ArrayList<>();
            this.previousSongList = new ArrayList<>();
            this.playableTriggers = Collections.synchronizedList(new ArrayList<>());
            this.toggledPlayableTriggers = new ArrayList<>();
            this.activeTriggers = new ArrayList<>();
            this.toggledActiveTriggers = new ArrayList<>();
        }

        public List<Audio> getCurrentSongList() {
            return this.currentSongList;
        }

        public boolean songListChanged() {
            return !this.currentSongList.equals(this.previousSongList);
        }

        public void updateSongList(List<Audio> newSongs) {
            this.previousSongList.clear();
            this.previousSongList.addAll(MusicTriggers.clone(this.currentSongList));
            this.currentSongList.clear();
            this.currentSongList.addAll(newSongs);
        }

        public List<Trigger> getPlayableTriggers() {
            synchronized(this.playableTriggers) {
                return this.playableTriggers;
            }
        }

        public void updatePlayableTriggers(List<Trigger> newTriggers) {
            synchronized(this.playableTriggers) {
                this.playableTriggers.clear();
                this.playableTriggers.addAll(newTriggers);
            }
        }

        public List<Trigger> getActiveTriggers() {
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
        }

        public void runToggles() {
            runPlayableToggle();
            runActiveToggle();
        }

        private void runPlayableToggle() {
            synchronized(this.playableTriggers) {
                this.toggledPlayableTriggers.removeIf(trigger -> !this.playableTriggers.contains(trigger));
                List<Trigger> toggleList = new ArrayList<>();
                for (Trigger trigger : this.playableTriggers)
                    if (!this.toggledPlayableTriggers.contains(trigger)) {
                        toggleList.add(trigger);
                        this.toggledPlayableTriggers.add(trigger);
                    }
                if (!toggleList.isEmpty()) ChannelManager.getChannel(this.channel).runToggle(2, toggleList);
            }
        }

        private void runActiveToggle() {
            this.toggledActiveTriggers.removeIf(trigger -> !this.activeTriggers.contains(trigger));
            List<Trigger> toggleList = new ArrayList<>();
            for(Trigger trigger : this.activeTriggers) {
                if(!this.toggledActiveTriggers.contains(trigger)) {
                    toggleList.add(trigger);
                    this.toggledActiveTriggers.add(trigger);
                }
            }
            if(!toggleList.isEmpty()) ChannelManager.getChannel(this.channel).runToggle(3,toggleList);
        }

        public void clearSongLists() {
            this.currentSongList.clear();
            this.previousSongList.clear();

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