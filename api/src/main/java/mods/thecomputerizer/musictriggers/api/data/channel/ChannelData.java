package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioHelper;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.command.CommandElement;
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.parameter.UniversalParameters;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.redirect.RedirectElement;
import mods.thecomputerizer.musictriggers.api.data.render.CardAPI;
import mods.thecomputerizer.musictriggers.api.data.render.CardHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerCombination;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerMerged;
import mods.thecomputerizer.musictriggers.api.data.trigger.basic.BasicTrigger;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

@Getter
public class ChannelData extends ChannelElement {

    private final Set<AudioRef> audio;
    private final Set<CardAPI> cards;
    private final Set<CommandElement> commands;
    private final Set<RecordElement> records;
    private final Set<RedirectElement> redirects;
    private final Set<TriggerAPI> triggers;
    private final Map<TriggerAPI,Collection<ChannelEventHandler>> triggerEventMap;
    private final Map<Class<? extends ChannelElement>,UniversalParameters> universalMap;
    private BasicTrigger genericTrigger;
    private BasicTrigger loadingTrigger;
    private BasicTrigger menuTrigger;

    public ChannelData(ChannelAPI channel) {
        super(channel);
        this.audio = new HashSet<>();
        this.cards = new HashSet<>();
        this.commands = new HashSet<>();
        this.records = new HashSet<>();
        this.redirects = new HashSet<>();
        this.triggers = new HashSet<>();
        this.triggerEventMap = new HashMap<>();
        this.universalMap = initUniversals();
    }

    protected <E extends ChannelEventHandler> void addActiveTriggers(
            Collection<E> elements, Function<E,Collection<TriggerAPI>> triggers, boolean isEvent) {
        for(E element : elements) addActiveTriggers(element,triggers.apply(element),isEvent);
    }

    protected <E extends ChannelEventHandler> void addActiveTriggers(
            E element, Collection<TriggerAPI> triggers, boolean isEvent) {
        TriggerAPI active = null;
        for(TriggerAPI trigger : this.triggerEventMap.keySet()) {
            if(trigger.matches(triggers)) {
                active = trigger;
                break;
            }
        }
        if(Objects.isNull(active)) {
            if(triggers.size()==1) {
                for(TriggerAPI trigger : triggers) {
                    if(Objects.nonNull(trigger)) {
                        active = trigger;
                        break;
                    }
                }
            } else active = TriggerCombination.make(this.channel,triggers);
            if(Objects.nonNull(active)) this.triggerEventMap.put(active,new HashSet<>());
        }
        if(Objects.nonNull(active) && isEvent) this.triggerEventMap.get(active).add(element);
    }

    protected boolean addBasicTrigger(BasicTrigger trigger) {
        switch(trigger.getName()) {
            case "generic": {
                this.genericTrigger = trigger;
                return true;
            }
            case "loading": {
                this.loadingTrigger = trigger;
                return true;
            }
            case "menu": {
                this.menuTrigger = trigger;
                return true;
            }
            default: return false;
        }
    }

    protected void addEmptyTriggers() {
        for(TriggerAPI trigger : this.triggers) {
            if(trigger instanceof BasicTrigger && addBasicTrigger((BasicTrigger)trigger)) continue;
            boolean needsAdding = true;
            for(TriggerAPI active : this.triggerEventMap.keySet()) {
                if(active instanceof TriggerCombination) {
                    if(((TriggerCombination)active).isContained(trigger)) {
                        needsAdding = false;
                        break;
                    }
                } else if(active.matches(trigger)) {
                    needsAdding = false;
                    break;
                }
            }
            if(needsAdding) this.triggerEventMap.put(trigger,new HashSet<>());
        }
    }

    protected void addUniversals(Map<Class<? extends ChannelElement>,UniversalParameters> map) {
        map.put(AudioRef.class,universalAudio());
        map.put(TriggerAPI.class,universalTriggers());
    }

    @Override
    public void close() {
        closeHandlers(this.audio);
        closeHandlers(this.cards);
        closeHandlers(this.commands);
        closeHandlers(this.records);
        closeHandlers(this.redirects);
        closeHandlers(this.triggers);
        for(Entry<TriggerAPI,Collection<ChannelEventHandler>> entry : this.triggerEventMap.entrySet()) {
            entry.getKey().close();
            closeHandlers(entry.getValue());
        }
        this.triggerEventMap.clear();
        this.universalMap.clear();
        this.genericTrigger = null;
        this.loadingTrigger = null;
        this.menuTrigger = null;
    }

    private void closeHandlers(Collection<? extends ChannelEventHandler> handlers) {
        for(ChannelEventHandler handler : handlers) handler.close();
        handlers.clear();
    }

    protected void extractActiveTriggers() {
        addActiveTriggers(this.audio,AudioRef::getTriggers,false);
        addActiveTriggers(this.cards,CardAPI::getTriggers,true);
        addActiveTriggers(this.commands,CommandElement::getTriggers,true);
    }

    public Collection<ChannelEventHandler> getActiveEventHandlers() {
        return getEventHandlers(this.channel.getActiveTrigger());
    }

    public @Nullable AudioPool getActivePool() {
        return this.channel.getSelector().getActivePool();
    }

    public Collection<ChannelEventHandler> getEventHandlers(@Nullable TriggerAPI trigger) {
        if(trigger instanceof TriggerMerged) {
            Set<ChannelEventHandler> handlers = new HashSet<>();
            for(TriggerAPI t : ((TriggerMerged)trigger).getTriggers()) handlers.addAll(getEventHandlers(t));
            return Collections.unmodifiableSet(handlers);
        } else if(Objects.nonNull(trigger)) {
            Collection<ChannelEventHandler> c = this.triggerEventMap.get(trigger);
            return Objects.nonNull(c) ? Collections.unmodifiableCollection(c) : Collections.emptySet();
        }
        return Collections.emptySet();
    }

    public Collection<ChannelEventHandler> getPlayableEventHandlers() {
        Set<ChannelEventHandler> handlers = new HashSet<>();
        Collection<TriggerAPI> playables = this.channel.getPlayableTriggers();
        for(TriggerAPI active : this.triggerEventMap.keySet())
            if(active.isContained(playables)) handlers.addAll(getEventHandlers(active));
        return Collections.unmodifiableSet(handlers);
    }

    public Collection<ChannelEventHandler> getPreviousEventHandlers() {
        return getEventHandlers(this.channel.getPreviousTrigger());
    }

    public @Nullable UniversalParameters getUniversals(Class<? extends ChannelElement> clazz) {
        return this.universalMap.get(clazz);
    }

    private Map<Class<? extends ChannelElement>,UniversalParameters> initUniversals() {
        Map<Class<? extends ChannelElement>,UniversalParameters> map = new HashMap<>();
        addUniversals(map);
        return map;
    }

    @Override
    public boolean isResource() {
        return false;
    }

    public void loadTracks(boolean loadResources) {
        logInfo("Loading {} audio tracks",this.audio.size());
        for(AudioRef ref : this.audio) {
            String name = ref.getName();
            boolean found = false;
            for(RedirectElement redirect : this.redirects) {
                if(name.equals(redirect.getName())) {
                    found = true;
                    if(redirect.isRemote() || loadResources) ref.loadRemote(redirect.getValue());
                    break;
                }
            }
            if(!found) {
                String file = ref.getParameterAsString("file_name");
                ref.loadLocal(StringUtils.isNotBlank(file) ? file : ref.getName());
            }
        }
    }

    public void loadResourceTracks() {
        for(AudioRef ref : this.audio) {
            if(ref.isLoaded()) continue;
            String name = ref.getName();
            for(RedirectElement redirect : this.redirects) {
                if(name.equals(redirect.getName())) {
                    if(redirect.isResource()) ref.loadRemote(redirect.getValue());
                    break;
                }
            }
        }
    }

    public void organize() {
        extractActiveTriggers();
        setAudioPools();
        addEmptyTriggers();
        for(Map.Entry<TriggerAPI,Collection<ChannelEventHandler>> entry : this.triggerEventMap.entrySet()) {
            entry.getValue().add(entry.getKey());
            logInfo("{} is mapped to event handlers {}",entry.getKey(),entry.getValue());
        }
    }

    public void parse() {
        logInfo("Parsing channel data");
        readRedirect(ChannelHelper.openTxt(MTRef.CONFIG_PATH+"/"+getChannel().getInfo().getRedirectPath(),getChannel()));
        readMain(ChannelHelper.openToml(MTRef.CONFIG_PATH+"/"+getChannel().getInfo().getMainPath(),getChannel()));
        readRenders(ChannelHelper.openToml(MTRef.CONFIG_PATH+"/"+getChannel().getInfo().getRendersPath(),getChannel()));
        readCommands(ChannelHelper.openToml(MTRef.CONFIG_PATH+"/"+getChannel().getInfo().getCommandsPath(),getChannel()));
        readJukebox(ChannelHelper.openTxt(MTRef.CONFIG_PATH+"/"+getChannel().getInfo().getJukeboxPath(),getChannel()));
        organize();
        logInfo("Finished parsing channel data");
    }

    public void readCommands(@Nullable Toml commands) {
        if(Objects.isNull(commands)) return;
        for(Toml table : commands.getAllTables()) {
            CommandElement command = new CommandElement(getChannel(),table);
            if(command.isValid()) this.commands.add(command);
        }
    }

    public void readJukebox(Collection<String> lines) {
        for(String line : lines) {
            RecordElement record = new RecordElement(getChannel(),line);
            if(record.isValid()) this.records.add(record);
        }
    }

    public void readMain(@Nullable Toml main) {
        if(Objects.isNull(main)) return;
        TriggerHelper.parseTriggers(getChannel(),this.triggers,main.getTable("triggers"));
        AudioHelper.parseAudio(getChannel(),this.audio,main.getTable("songs"));
    }

    public void readRedirect(Collection<String> lines) {
        for(String line : lines) {
            RedirectElement redirect = new RedirectElement(getChannel(),line);
            if(redirect.isValid()) this.redirects.add(redirect);
        }
    }

    public void readRenders(@Nullable Toml renders) {
        if(Objects.isNull(renders)) return;
        CardHelper.parseImageCards(getChannel(),this.cards,renders.getTableArray("image"));
        CardHelper.parseTitleCards(getChannel(),this.cards,renders.getTableArray("title"));
    }

    protected void setAudioPools() {
        Set<AudioRef> added = new HashSet<>();
        for(AudioRef ref : this.audio) {
            TriggerAPI trigger = null;
            for(TriggerAPI active : this.triggerEventMap.keySet()) {
                if(active.matches(ref.getTriggers())) {
                    trigger = active;
                    break;
                }
            }
            if(Objects.isNull(trigger)) continue;
            Set<AudioRef> pooled = new HashSet<>();
            pooled.add(ref);
            for(AudioRef other : this.audio)
                if(other!=ref && !added.contains(other) && trigger.matches(other.getTriggers()))
                    pooled.add(other);
            AudioPool pool = null;
            for(AudioRef a : pooled) {
                if(Objects.nonNull(a)) {
                    if(Objects.isNull(pool)) pool = new AudioPool(a.getName()+"_pool",a);
                    else pool.addAudio(a);
                    added.add(a);
                }
            }
            if(Objects.nonNull(pool) && pool.isValid()) this.triggerEventMap.get(trigger).add(pool);
        }
    }

    protected UniversalParameters universalTriggers() {
        return UniversalParameters.get(this.channel,"Triggers",map -> {
            map.put("fade_in",new ParameterInt(0));
            map.put("fade_out",new ParameterInt(0));
            map.put("persistence",new ParameterInt(0));
            map.put("song_delay",new ParameterInt(0));
            map.put("start_delay",new ParameterInt(0));
            map.put("stop_delay",new ParameterInt(0));
            map.put("trigger_delay",new ParameterInt(0));
        });
    }

    protected UniversalParameters universalAudio() {
        return UniversalParameters.get(this.channel,"Audio",map -> {
            map.put("must_finish",new ParameterBoolean(false));
            map.put("pitch",new ParameterFloat(1f));
            map.put("play_once",new ParameterInt(0));
            map.put("volume",new ParameterFloat(1f));
        });
    }
}