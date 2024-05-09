package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioHelper;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.command.CommandElement;
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.parameter.UniversalParameters;
import mods.thecomputerizer.musictriggers.api.data.redirect.RedirectElement;
import mods.thecomputerizer.musictriggers.api.data.render.CardAPI;
import mods.thecomputerizer.musictriggers.api.data.render.CardHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerCombination;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerMerged;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerRegistry;
import mods.thecomputerizer.musictriggers.api.data.trigger.basic.BasicTrigger;
import mods.thecomputerizer.musictriggers.api.network.MessageInitChannels.ChannelMessage;
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
        super(channel,"channel_data");
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
        map.put(AudioRef.class,UniversalParameters.get(this.channel,MTDataRef.UNIVERSAL_AUDIO));
        map.put(TriggerAPI.class,UniversalParameters.get(this.channel,MTDataRef.UNIVERSAL_TRIGGERS));
    }
    
    protected void appendUniversals() {
        logDebug("Appending {} universal types",this.universalMap.size());
        for(Entry<Class<? extends ChannelElement>,UniversalParameters> entry : this.universalMap.entrySet()) {
            for(Collection<ChannelEventHandler> handlers : this.triggerEventMap.values()) {
                for(ChannelEventHandler handler : handlers) {
                    if(handler instanceof ParameterWrapper) {
                        ParameterWrapper wrapper = (ParameterWrapper)handler;
                        if(wrapper.getTypeClass()==entry.getKey()) wrapper.setUniversals(entry.getValue());
                    }
                }
            }
        }
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
    
    private String getFilePath(String path) {
        return MTRef.CONFIG_PATH+"/"+getChannelName()+"/"+path;
    }

    public Collection<ChannelEventHandler> getPlayableEventHandlers() {
        Set<ChannelEventHandler> handlers = new HashSet<>();
        Collection<TriggerAPI> playables = this.channel.getPlayableTriggers();
        for(TriggerAPI active : this.triggerEventMap.keySet())
            if(active.isContained(playables)) handlers.addAll(getEventHandlers(active));
        return Collections.unmodifiableSet(handlers);
    }
    
    @Override protected TableRef getReferenceData() {
        return null;
    }
    
    @Override public Class<? extends ParameterWrapper> getTypeClass() {
        return ChannelData.class;
    }

    public @Nullable UniversalParameters getUniversals(Class<? extends ChannelElement> clazz) {
        return this.universalMap.get(clazz);
    }
    
    boolean implyTrigger(String name, String id) {
        TriggerAPI trigger = TriggerRegistry.getTriggerInstance(this.channel,name);
        if(Objects.nonNull(trigger) && trigger.imply(id)) {
            trigger.successfullyParsed();
            this.triggers.add(trigger);
            return true;
        }
        return false;
    }

    private Map<Class<? extends ChannelElement>,UniversalParameters> initUniversals() {
        Map<Class<? extends ChannelElement>,UniversalParameters> map = new HashMap<>();
        addUniversals(map);
        return map;
    }
    
    @Override protected String getSubTypeName() {
        return "Data";
    }
    
    @Override
    public boolean isResource() {
        return false;
    }
    
    public void load(ChannelMessage message) {
        logInfo("Loading external data");
        readRedirect(message.getRedirects());
        readMain(message.getTomls().get("main"));
        readRenders(message.getTomls().get("renders"));
        readCommands(message.getTomls().get("commands"));
        readJukebox(message.getRecords());
        organize();
        logInfo("Finished loading external data");
    }

    public void loadTracks(boolean loadResources) {
        logInfo("Loading {} audio tracks",this.audio.size());
        for(AudioRef ref : this.audio) {
            String name = ref.getName();
            boolean found = false;
            for(RedirectElement redirect : this.redirects) {
                if(name.equals(redirect.getKey())) {
                    found = true;
                    if(redirect.isRemote() || loadResources) ref.loadRemote(redirect.getValue());
                    break;
                }
            }
            if(!found) {
                String file = ref.getParameterAsString("location");
                ref.loadLocal(StringUtils.isNotBlank(file) && !"_".equals(file) ? file : ref.getName());
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
        setupAudioPools();
        addEmptyTriggers();
        for(Map.Entry<TriggerAPI,Collection<ChannelEventHandler>> entry : this.triggerEventMap.entrySet()) {
            entry.getValue().add(entry.getKey());
            logInfo("{} is mapped to event handlers {}",entry.getKey(),entry.getValue());
        }
        appendUniversals();
    }

    public void parse() {
        logInfo("Parsing local data");
        ChannelInfo info = this.channel.getInfo();
        readRedirect(ChannelHelper.openTxt(getFilePath(info.getRedirectPath()),this));
        readMain(ChannelHelper.openToml(getFilePath(info.getMainPath()),true,this));
        readRenders(ChannelHelper.openToml(getFilePath(info.getRendersPath()),true,this));
        readCommands(ChannelHelper.openToml(getFilePath(info.getCommandsPath()),true,this));
        readJukebox(ChannelHelper.openTxt(getFilePath(info.getJukeboxPath()),this));
        organize();
        logInfo("Finished parsing local data");
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
        CardHelper.parseImageCards(this.channel,this.cards,renders.getTableArray("image"));
        CardHelper.parseTitleCards(this.channel,this.cards,renders.getTableArray("title"));
    }

    protected void setupAudioPools() {
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
}