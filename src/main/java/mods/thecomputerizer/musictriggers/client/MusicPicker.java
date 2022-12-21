package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.data.Audio;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.data.Universal;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.stream.Collectors;

public class MusicPicker {
    private final Channel channel;
    private final Info info;

    public final HashMap<Trigger, MutableInt> triggerPersistence = new HashMap<>();
    public final HashMap<Trigger, MutableInt> startMap = new HashMap<>();
    public final HashMap<Trigger, Boolean> boolMap = new HashMap<>();
    public final HashMap<Integer, Boolean> victory = new HashMap<>();
    public final List<Trigger> dynamicTemp = new ArrayList<>();
    public final List<Trigger> savePlayable = new ArrayList<>();
    public final List<Trigger> titleCardEvents = new ArrayList<>();
    public final List<Trigger> timeSwitch = new ArrayList<>();

    public static final List<String> effectList = new ArrayList<>();

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

    public void initializePersistence(Trigger trigger) {
        this.triggerPersistence.putIfAbsent(trigger,new MutableInt(0));
    }

    public Packeted querySongList(Universal universalParameters) {
        Packeted packet = new Packeted();
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if(player == null) {
            Trigger menu = Trigger.getTriggerWithNoID(this.channel.getChannelName(),"menu");
            if (mc.currentScreen!=null && mc.world==null && Objects.nonNull(menu)) {
                this.getInfo().updatePlayableTriggers(Collections.singletonList(menu));
                this.getInfo().updateActiveTriggers(Collections.singletonList(menu));
                this.getInfo().updateSongList(Trigger.getPotentialSongs(menu));
                this.info.runToggles();
                return packet;
            }
        } else {
            packet.setMenuSongs(allMenuSongs());
            List<Audio> activeSongs = comboChecker(
                    priorityHandler(
                            playableTriggers(packet,player,universalParameters),
                            universalParameters),
                    universalParameters);
            if (activeSongs != null && !activeSongs.isEmpty()) {
                this.getInfo().updatePlayableTriggers(savePlayable);
                for (Trigger trigger : timeSwitch) {
                    if (!this.getInfo().getActiveTriggers().contains(trigger) && triggerPersistence.get(trigger).getValue() > 0)
                        triggerPersistence.get(trigger).setValue(0);
                }
                timeSwitch.clear();
                dynamicTemp.clear();
                this.getInfo().updateSongList(activeSongs);
                this.info.runToggles();
                return packet;
            }
            Trigger generic = Trigger.getTriggerWithNoID(this.channel.getChannelName(),"generic");
            if (Objects.nonNull(generic)) {
                this.getInfo().updatePlayableTriggers(Collections.singletonList(generic));
                this.getInfo().updateActiveTriggers(Collections.singletonList(generic));
                this.getInfo().updateSongList(Trigger.getPotentialSongs(generic));
                this.triggerDelay = generic.getParameter("trigger_delay");
                if (this.triggerDelay .matches("0")) this.triggerDelay = universalParameters.getTriggerDelay();
                this.songDelay = generic.getParameter("song_delay");
                if (this.songDelay .matches("0")) this.songDelay  = universalParameters.getSongDelay();
                this.fadeIn = generic.getParameterInt("fade_in");
                if (this.fadeIn == 0) this.fadeIn = MusicTriggers.randomInt("universal_fade_in",
                        universalParameters.getFadeIn(),0);
                this.fadeOut = generic.getParameterInt("fade_out");
                if (this.fadeOut == 0) this.fadeIn = MusicTriggers.randomInt("universal_fade_out",
                        universalParameters.getFadeOut(),0);
                this.info.runToggles();
                return packet;
            }
        }
        this.getInfo().updatePlayableTriggers(new ArrayList<>());
        this.getInfo().updateActiveTriggers(new ArrayList<>());
        this.getInfo().updateSongList(new ArrayList<>());
        this.info.runToggles();
        return packet;
    }

    private List<Audio> comboChecker(Trigger priorityTrigger, Universal universalParameters) {
        if(ConfigDebug.COMBINE_EQUAL_PRIORITY) {
            if (priorityTrigger == null) return null;
            int priority = priorityTrigger.getParameterInt("priority");
            List<Audio> playableSongs = this.savePlayable.stream()
                    .map(Trigger::getPotentialSongs).flatMap(Collection::stream)
                    .distinct().filter(audio -> containsAtLeastOneValidTrigger(audio,priority))
                    .collect(Collectors.toList());
            if(!playableSongs.isEmpty()) return playableSongs;
            this.info.getPlayableTriggers().remove(priorityTrigger);
            if (this.info.getPlayableTriggers().isEmpty()) return null;
            playableSongs = separatePools(priorityHandler(this.getInfo().getPlayableTriggers(),universalParameters),universalParameters);
            return playableSongs;

        } else return separatePools(priorityTrigger,universalParameters);
    }

    private boolean containsAtLeastOneValidTrigger(Audio audio, int priority) {
        if(!new HashSet<>(this.savePlayable).containsAll(audio.getTriggers())) return false;
        for(Trigger trigger : audio.getTriggers())
            if(trigger.getParameterInt("priority")==priority)
                return true;
        return false;
    }

    private List<Audio> separatePools(Trigger priorityTrigger, Universal universalParameters) {
        if (priorityTrigger == null) return null;
        List<Audio> playableSongs = new ArrayList<>();
        List<Trigger> orderedTriggers = new ArrayList<>();
        HashMap<Audio, List<Trigger>> foundCombinations = new HashMap<>();
        for (Audio audio : Trigger.getPotentialSongs(priorityTrigger)) {
            List<Trigger> triggers = audio.getTriggers();
            if(triggers.size()>1 && new HashSet<>(this.info.getPlayableTriggers()).containsAll(triggers)) {
                foundCombinations.put(audio, triggers);
                orderedTriggers.addAll(triggers);
            }
        }
        if(!orderedTriggers.isEmpty()) {
            for(Audio audio : foundCombinations.keySet())
                foundCombinations.get(audio).remove(priorityTrigger);
            orderedTriggers.remove(priorityTrigger);
            orderedTriggers = orderedTriggers.stream().distinct().sorted(Comparator.comparingInt(
                    trigger -> trigger.getParameterInt("priority")))
                    .collect(Collectors.toList());
            Collections.reverse(orderedTriggers);
            List<Audio> recursedSongs = recursiveCombination(foundCombinations, orderedTriggers);
            playableSongs.addAll(recursedSongs);
            List<Trigger> activeTriggers = recursedSongs.get(0).getTriggers();
            this.getInfo().updateActiveTriggers(activeTriggers);
        }
        if (playableSongs.isEmpty()) {
            for (Audio audio : Trigger.getPotentialSongs(priorityTrigger)) {
                if(audio.getTriggers().size()==1)
                    playableSongs.add(audio);
            }
            if(!playableSongs.isEmpty()) this.getInfo().updateActiveTriggers(Collections.singletonList(priorityTrigger));
        }
        if (playableSongs.isEmpty()) {
            this.info.getPlayableTriggers().remove(priorityTrigger);
            if (this.info.getPlayableTriggers().isEmpty()) return null;
            playableSongs = separatePools(priorityHandler(this.getInfo().getPlayableTriggers(),universalParameters),universalParameters);
        }
        return playableSongs;
    }

    private List<Audio> recursiveCombination(Map<Audio, List<Trigger>> multipleTriggers, List<Trigger> orderedTriggers) {
        Trigger highest = orderedTriggers.get(0);
        List<Audio> songsWithNextTrigger = multipleTriggers.keySet().stream()
                .filter(audio -> multipleTriggers.get(audio).contains(highest)).collect(Collectors.toList());
        HashMap<Audio, List<Trigger>> nextMap = new HashMap<>();
        orderedTriggers.clear();
        for(Audio audio : songsWithNextTrigger) {
            List<Trigger> withoutHighest = multipleTriggers.get(audio);
            withoutHighest.remove(highest);
            nextMap.put(audio, withoutHighest);
        }
        List<Audio> ret = new ArrayList<>(multipleTriggers.keySet());
        nextMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        if(nextMap.isEmpty()) return ret;
        for(Audio audio : nextMap.keySet()) orderedTriggers.addAll(nextMap.get(audio));
        orderedTriggers = orderedTriggers.stream().distinct().sorted(Comparator.comparingInt(trigger ->
                        trigger.getParameterInt("priority")))
                .collect(Collectors.toList());
        Collections.reverse(orderedTriggers);
        return recursiveCombination(nextMap,orderedTriggers);
    }

    public Trigger priorityHandler(List<Trigger> playableTriggers, Universal universalParameters) {
        if (playableTriggers == null || playableTriggers.isEmpty()) return null;
        Trigger priorityTrigger = ConfigDebug.REVERSE_PRIORITY ? Collections.min(dynamicTemp,
                Comparator.comparingInt(trigger -> trigger.getParameterInt("priority"))) : Collections.max(dynamicTemp,
                Comparator.comparingInt(trigger -> trigger.getParameterInt("priority")));
        while (Trigger.getPotentialSongs(priorityTrigger).isEmpty()) {
            playableTriggers.remove(priorityTrigger);
            if (playableTriggers.isEmpty()) return null;
            priorityTrigger = ConfigDebug.REVERSE_PRIORITY ? Collections.min(dynamicTemp,
                    Comparator.comparingInt(trigger -> trigger.getParameterInt("priority"))) : Collections.max(dynamicTemp,
                    Comparator.comparingInt(trigger -> trigger.getParameterInt("priority")));
        }
        this.triggerDelay = priorityTrigger.getParameter("trigger_delay");
        if (this.triggerDelay .matches("0")) this.triggerDelay = universalParameters.getTriggerDelay();
        this.songDelay = priorityTrigger.getParameter("song_delay");
        if (this.songDelay .matches("0")) this.songDelay  = universalParameters.getSongDelay();
        this.fadeIn = priorityTrigger.getParameterInt("fade_in");
        if (this.fadeIn == 0) this.fadeIn = MusicTriggers.randomInt("universal_fade_in",
                universalParameters.getFadeIn(),0);
        this.fadeOut = priorityTrigger.getParameterInt("fade_out");
        if (this.fadeOut == 0) this.fadeIn = MusicTriggers.randomInt("universal_fade_out",
                universalParameters.getFadeOut(),0);
        return priorityTrigger;
    }

    public List<Trigger> playableTriggers(Packeted packet, EntityPlayerSP player, Universal universalParameters) {
        crashHelper = "";
        this.info.updateActiveTriggers(new ArrayList<>());
        try {
            if(!ConfigRegistry.CLIENT_SIDE_ONLY) {
                for (Trigger trigger : Trigger.getTriggerInstances(this.channel.getChannelName(), "home"))
                    packet.addHomeTrigger(trigger.makeHomePacket());
                for (Trigger trigger : Trigger.getTriggerInstances(this.channel.getChannelName(), "structure"))
                    packet.addStructureTrigger(trigger.makeStructurePacket(player.getPosition().toLong(), player.dimension));
                for (Trigger trigger : Trigger.getTriggerInstances(this.channel.getChannelName(), "mob"))
                    packet.addMobTrigger(trigger.makeMobPacket());
            }
            List<Trigger> events = Trigger.getRegisteredTriggers(this.channel.getChannelName()).stream()
                    .filter(trigger -> trigger.runActivationFunction(player))
                    .map(trigger -> addPlayableTrigger(trigger, universalParameters))
                    .filter(Objects::nonNull).distinct().collect(Collectors.toList());
            events.removeIf(this.channel::invertToggle);
            this.info.updatePlayableTriggers(events);
            savePlayable.clear();
            savePlayable.addAll(events);
            return events;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("There was a problem with your "+crashHelper+" trigger! The error was "+
                    e.getMessage()+" and was caught on line "+e.getStackTrace()[0].getLineNumber()+" in the class "+
                    e.getStackTrace()[0]);
        }
    }

    private Trigger addPlayableTrigger(Trigger trigger, Universal universalParameters) {
        this.crashHelper = trigger.getNameWithID();
        Trigger ret = null;
        boolean active = trigger.canStart(this.startMap.get(trigger).getValue(),
                MusicTriggers.randomInt("universal_start_delay",universalParameters.getStartDelay(),0));
        if (active || this.triggerPersistence.get(trigger).getValue() > 0) {
            ret = trigger;
            this.boolMap.put(trigger, true);
            this.startMap.putIfAbsent(trigger, new MutableInt(0));
            this.dynamicTemp.add(trigger);
            if (active) {
                int check = trigger.getParameterInt("persistence");
                this.triggerPersistence.get(trigger).setValue(check>0 ? check :
                                MusicTriggers.randomInt(
                                        "universal_persistence",universalParameters.getPersistence(),0));
            }
            if (trigger.getParameterBool("passive_persistence")) this.timeSwitch.add(trigger);
        } else this.boolMap.put(trigger, false);
        return ret;
    }

    public boolean getVictory(int id) {
        boolean ret = this.victory.get(id)!=null && this.victory.get(id);
        if(ret) this.victory.put(id,false);
        return ret;
    }

    public List<Audio> allMenuSongs() {
        Trigger trigger = Trigger.getTriggerWithNoID(this.channel.getChannelName(),"menu");
        return trigger == null ? new ArrayList<>() : Trigger.getPotentialSongs(trigger);
    }

    public void clearListsAndMaps() {
        this.triggerPersistence.clear();
        this.victory.clear();
        this.dynamicTemp.clear();
        this.savePlayable.clear();
        this.timeSwitch.clear();
        this.startMap.clear();
        this.boolMap.clear();
        this.info.clear();
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
            this.playableTriggers = new ArrayList<>();
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
            return this.playableTriggers;
        }

        public void updatePlayableTriggers(List<Trigger> newTriggers) {
            this.playableTriggers.clear();
            this.playableTriggers.addAll(newTriggers);
        }

        public List<Trigger> getActiveTriggers() {
            return this.activeTriggers;
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
            this.toggledPlayableTriggers.removeIf(trigger -> !this.playableTriggers.contains(trigger));
            List<Trigger> toggleList = new ArrayList<>();
            for(Trigger trigger : this.playableTriggers) if(!this.toggledPlayableTriggers.contains(trigger)) {
                toggleList.add(trigger);
                this.toggledPlayableTriggers.add(trigger);
            }
            if(!toggleList.isEmpty()) ChannelManager.getChannel(this.channel).runToggle(2,toggleList);
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
            clearSongLists();
            this.playableTriggers.clear();
            this.toggledPlayableTriggers.clear();
            this.activeTriggers.clear();
            this.toggledActiveTriggers.clear();
        }
    }

    public static final class Packeted {
        private final List<ServerChannelData.Home> homeTriggers;
        private final List<ServerChannelData.Structure> structureTriggers;
        private final List<ServerChannelData.Mob> mobTriggers;
        private final List<Audio> menuSongs;

        public Packeted() {
            this.homeTriggers = new ArrayList<>();
            this.structureTriggers = new ArrayList<>();
            this.mobTriggers = new ArrayList<>();
            this.menuSongs = new ArrayList<>();
        }

        public void setMenuSongs(List<Audio> songs) {
            this.menuSongs.clear();
            this.menuSongs.addAll(songs);
        }

        public List<Audio> getMenuSongs() {
            return this.menuSongs;
        }

        public List<ServerChannelData.Home> getHomeTriggers() {
            return this.homeTriggers;
        }

        public void addHomeTrigger(ServerChannelData.Home home) {
            this.homeTriggers.add(home);
        }

        public List<ServerChannelData.Structure> getStructureTriggers() {
            return this.structureTriggers;
        }

        public void addStructureTrigger(ServerChannelData.Structure structure) {
            this.structureTriggers.add(structure);
        }

        public List<ServerChannelData.Mob> getMobTriggers() {
            return this.mobTriggers;
        }

        public void addMobTrigger(ServerChannelData.Mob mob) {
            this.mobTriggers.add(mob);
        }
    }
}