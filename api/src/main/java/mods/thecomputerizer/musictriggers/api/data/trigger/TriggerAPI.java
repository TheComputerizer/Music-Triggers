package mods.thecomputerizer.musictriggers.api.data.trigger;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventHandler;
import mods.thecomputerizer.musictriggers.api.data.nbt.NBTLoadable;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.HolderTrigger;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State.*;

@Getter
public abstract class TriggerAPI extends ChannelElement implements NBTLoadable {

    private static final Map<TriggerAPI,Map<String,Timer>> TIMER_MAP = new HashMap<>(); // This needs to be static due to super stuff

    private final Set<TriggerCombination> parents;
    protected final Set<Link> links;
    @Setter protected Link activeLink;
    private ResourceContext resourceCtx;
    private State state;
    private int tracksPlayed;

    protected TriggerAPI(ChannelAPI channel, String name) {
        super(channel,name);
        this.parents = new HashSet<>();
        this.links = new HashSet<>();
        this.state = IDLE;
    }

    @Override
    public void activate() {
        setState(ACTIVE);
        setTimer("ticks_before_audio",ACTIVE);
    }

    protected void addTimedParameter(String name, State state, Parameter<?> parameter) {
        if(Objects.nonNull(parameter)) {
            TIMER_MAP.putIfAbsent(this,new HashMap<>());
            TIMER_MAP.get(this).put(name,new Timer(this,name,state));
        }
    }

    public boolean canActivate() {
        return getState().activatable && !hasTime("active_cooldown") && !hasTime("ticks_before_active") &&
                hasNonEmptyAudioPool();
    }

    protected boolean canPersist() {
        return hasTime("persistence") &&
                (getState()==ACTIVE || (getState()==PLAYABLE && getParameterAsBoolean("passive_persistence")));
    }

    public boolean canPlayAudio() {
        if(this.tracksPlayed==0) return hasTime("ticks_before_audio");
        int maxTracks = getParameterAsInt("max_tracks");
        return (maxTracks<=0 || this.tracksPlayed<maxTracks) && !hasTime("ticks_between_audio");
    }

    protected boolean checkSidedContext(TriggerContext context) {
        return isSynced() ? context.getSyncedContext(this) : isPlayableContext(context);
    }

    protected void clearTimers(State state) {
        consumeTimers(timer -> timer.clear(state));
    }

    @Override
    public void close() {
        if(TIMER_MAP.containsKey(this)) {
            TIMER_MAP.get(this).clear();
            TIMER_MAP.remove(this);
        }
        this.parents.clear();
        this.links.clear();
        this.resourceCtx = null;
        this.state = DISABLED;
        this.tracksPlayed = 0;
    }

    protected void consumeTimers(Consumer<Timer> consumer) {
        if(TIMER_MAP.containsKey(this)) TIMER_MAP.get(this).values().forEach(consumer);
    }

    @Override
    public void deactivate() {
        clearTimers(ACTIVE);
        setTimer("active_cooldown",PLAYABLE);
        this.tracksPlayed = 0;
        if(!isDisabled()) setState(query(this.channel.getSelector().context) ? PLAYABLE : IDLE);
    }

    public void encode(ByteBuf buf) {
        NetworkHelper.writeString(buf,getName());
        NetworkHelper.writeString(buf,getIdentifier());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TriggerAPI && ((TriggerAPI)other).getNameWithID().equals(getNameWithID());
    }

    public @Nullable AudioPool getAudioPool() {
        Collection<ChannelEventHandler> handlers = this.channel.getData().getTriggerEventMap().get(this);
        if(Objects.isNull(handlers)) return null;
        for(ChannelEventHandler handler : handlers)
            if(handler instanceof AudioPool) return (AudioPool)handler;
        return null;
    }

    public String getIdentifier() {
        String id = getParameterAsString("identifier");
        return Objects.nonNull(id) ? id : "not_set";
    }

    public String getNameWithID() {
        return getName();
    }
    
    protected State getParameterTimeState(String name) {
        return Misc.equalsAny(name,"persistence","ticks_before_audio","ticks_between_audio") ?
                ACTIVE : (Misc.equalsAny(name,"active_cooldown","ticks_before_active") ? PLAYABLE : DISABLED);
    }
    
    @Override public TableRef getReferenceData() {
        return MTDataRef.findTriggerRef(this.name);
    }

    public List<String> getRequiredMods() {
        return Collections.emptyList();
    }

    @Override
    public Class<? extends ChannelElement> getTypeClass() {
        return TriggerAPI.class;
    }

    @Override
    protected String getSubTypeName() {
        return "Trigger";
    }
    
    @Override
    public boolean hasDataToSave() {
        AudioPool pool = getAudioPool();
        return Objects.nonNull(pool) && pool.hasDataToSave();
    }

    public boolean hasNonEmptyAudioPool() {
        AudioPool pool = getAudioPool();
        return Objects.nonNull(pool) && pool.hasAudio();
    }

    public boolean hasTime(String name) {
        Timer timer = TIMER_MAP.containsKey(this) ? TIMER_MAP.get(this).get(name) : null;
        return Objects.nonNull(timer) && timer.hasTime();
    }
    
    public boolean imply(String id) {
        setExistingParameterValue("identifier",id);
        return verifyRequiredParameters();
    }
    
    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> parameters) {
        for(Entry<String,Parameter<?>> entry : parameters.entrySet()) {
            State timeState = getParameterTimeState(entry.getKey());
            if(timeState!=DISABLED) addTimedParameter(entry.getKey(),timeState,entry.getValue());
        }
    }

    public boolean isContained(Collection<TriggerAPI> triggers) {
        return TriggerHelper.matchesAny(triggers,this);
    }

    public boolean isDisabled() {
        return getState()==State.DISABLED;
    }
    
    public boolean isFirstTrack() {
        return this.tracksPlayed==1;
    }

    public abstract boolean isPlayableContext(TriggerContext context);

    @Override
    public boolean isResource() {
        return false;
    }

    protected boolean isServer() {
        return false;
    }

    public boolean isSynced() {
        if(isServer()) return this.channel.isClientChannel();
        return !this.channel.isClientChannel();
    }

    public boolean matches(Collection<TriggerAPI> triggers) {
        return triggers.size()==1 && isContained(triggers);
    }

    public boolean matches(TriggerAPI trigger) {
        return this.equals(trigger);
    }

    @Override
    public void onConnected(CompoundTagAPI<?> worldData) {
        AudioPool pool = getAudioPool();
        if(Objects.nonNull(pool)) pool.onConnected(worldData);
    }
    
   
    public void onDisconnected() {
        AudioPool pool = getAudioPool();
        if(Objects.nonNull(pool)) pool.onDisconnected();
    }
    
    @Override
    public void onLoaded(CompoundTagAPI<?> globalData) {
        AudioPool pool = getAudioPool();
        if(Objects.nonNull(pool)) pool.onLoaded(globalData);
    }

    @Override
    public boolean parse(Toml table) {
        if(super.parse(table)) {
            if(table.hasTable("link")) {
                for(Toml linkTable : table.getTableArray("link")) {
                    Link link = new Link(this,linkTable);
                    if(link.valid) {
                        logDebug("Adding link to triggers Channel[{}]: {}",link.targetChannel.getName(),link.linkedTriggers);
                        this.links.add(link);
                    } else logDebug("Skipping invalid link");
                }
            }
            successfullyParsed();
            logInfo("Successfully parsed");
            return true;
        }
        return false;
    }

    @Override
    public void play() {
        this.tracksPlayed++;
    }

    @Override
    public void playable() {
        setTimer("ticks_before_active",PLAYABLE);
    }

    /**
     * Queries the active state of the trigger & wraps isActive with additional checks
     */
    public boolean query(TriggerContext context) {
        if(isDisabled()) return false;
        if(checkSidedContext(context) || getParameterAsBoolean("not")) {
            setTimer("persistence",ACTIVE);
            return true;
        }
        return canPersist();
    }
    
    @Override
    public void saveGlobalTo(CompoundTagAPI<?> globalData) {
        savePersistentData(globalData,true);
    }
    
    protected void savePersistentData(CompoundTagAPI<?> data, boolean global) {
        boolean written = false;
        AudioPool pool = getAudioPool();
        if(Objects.nonNull(pool) && !global) {
            pool.saveWorldTo(data);
            written = true;
        }
        if(written) {
            data.putString("name",getName());
            data.putString("id",getIdentifier());
        }
    }
    
    @Override
    public void saveWorldTo(CompoundTagAPI<?> worldData) {
        savePersistentData(worldData,false);
    }
    
    protected <V> void setExistingParameterValue(String name, V value) {
        Parameter<?> parameter = getParameter(name);
        if(Objects.nonNull(parameter)) parameter.setValue(value);
    }

    public void setState(State state) { //TODO Move activate and deactive handlers here too
        if(!isSynced()) {
            if(this.state!=state) {
                logInfo("Setting state from {} to {}",this.state,state);
                switch(this.state) {
                    case ACTIVE:
                    case PLAYABLE: {
                        if(state==DISABLED || state==IDLE)
                            for(ChannelEventHandler handler : this.channel.getData().getEventHandlers(this))
                                handler.unplayable();
                        break;
                    }
                    case DISABLED:
                    case IDLE: {
                        if(state==PLAYABLE || state==ACTIVE)
                            for(ChannelEventHandler handler : this.channel.getData().getEventHandlers(this))
                                handler.playable();
                        break;
                    }
                }
                this.state = state;
                this.channel.getSync().queueTriggerSync(this);
            }
        }
    }

    protected void setTimer(String name, State state) {
        Timer timer = TIMER_MAP.containsKey(this) ? TIMER_MAP.get(this).get(name) : null;
        if(Objects.nonNull(timer)) timer.set(state);
    }
    
    public void setToggle(boolean on) {
        State state = getState();
        logInfo("Toggling from {} to {}",state!=DISABLED,on);
        if(state==DISABLED && on) setState(IDLE);
        else if(state!=DISABLED && !on) setState(DISABLED);
    }
    
    public void switchToggle() {
        State state = getState();
        logInfo("Toggling from {} to {}",state!=DISABLED,state==DISABLED);
        if(state==DISABLED) setState(IDLE);
        else setState(DISABLED);
    }

    @Override
    public void stopped() {
        setTimer("ticks_between_audio",ACTIVE);
    }

    /**
     * Runs after this trigger has been successfully parsed
     */
    @SuppressWarnings("unchecked")
    public void successfullyParsed() {
        if(hasParameter("resource_name")) {
            List<String> resourceName = (List<String>)getParameterAsList("resource_name");
            List<String> displayeName = (List<String>)getParameterAsList("display_name");
            String resourceMatcher = getParameterAsString("resource_matcher");
            String displayMatcher = getParameterAsString("display_matcher");
            this.resourceCtx = new ResourceContext(resourceName,displayeName,resourceMatcher,displayMatcher);
        } else this.resourceCtx = null;
        setState(getParameterAsBoolean("start_as_disabled") ? DISABLED : IDLE);
    }

    @Override
    public void tickActive() {
        tickTimers(ACTIVE);
    }

    @Override
    public void tickPlayable() {
        tickTimers(PLAYABLE);
    }

    protected void tickTimers(State state) {
        consumeTimers(timer -> timer.tick(state));
    }

    @Override
    public String toString() {
        return getSubTypeName()+"["+getNameWithID()+"]";
    }
    
    @Override protected Toml toTomlExtra(Toml toml) {
        toml = super.toTomlExtra(toml);
        for(Link link : this.links) toml.addTable("link",link.toToml());
        return toml;
    }

    @Override
    public void unplayable() {
        clearTimers(PLAYABLE);
    }
    
    public void write(CompoundTagAPI<?> tag) {
        tag.putString("name",getName());
        if(this instanceof HolderTrigger) tag.putString("id",getIdentifier());
    }
    
    @Getter
    public static class Link extends ChannelElement {
        
        private final TriggerAPI parent;
        private final ChannelAPI targetChannel;
        private final boolean inheritor;
        private final boolean resumeAfterLink;
        private final List<TriggerAPI> linkedTriggers;
        private final List<TriggerAPI> requiredTriggers;
        private final boolean valid;
        @Setter private long snapshotInherit;
        @Setter private long snapshotLink;
        
        protected Link(TriggerAPI parent, Toml table) {
            super(parent.channel,parent.getNameWithID()+"-link");
            this.parent = parent;
            this.linkedTriggers = new ArrayList<>();
            this.requiredTriggers = new ArrayList<>();
            if(!parse(table)) {
                logError("Failed to parse");
                this.targetChannel = null;
                this.inheritor = false;
                this.resumeAfterLink = false;
                this.valid = false;
            } else {
                String channelName = getParameterAsString("target_channel");
                ChannelAPI channel = this.channel.getHelper().findChannel(this,channelName);
                if(Objects.isNull(channel)) {
                    logWarn("Could not find target_channel {}! Falling back to current {}",channelName,this.channel.getName());
                    this.targetChannel = this.channel;
                } else this.targetChannel = channel;
                this.inheritor = getParameterAsBoolean("inherit_time");
                this.resumeAfterLink = getParameterAsBoolean("resume_after_link");
                if(!parseTriggers(this.targetChannel,this.linkedTriggers,"linked_triggers")) {
                    logWarn("Failed to parse linked triggers from channel {}",channelName);
                    this.valid = false;
                }
                else if(!parseTriggers(this.targetChannel,this.requiredTriggers,"required_triggers")) {
                    logWarn("Failed to parse required triggers");
                    this.valid = false;
                }
                else {
                    this.requiredTriggers.add(this.parent);
                    this.valid = true;
                }
            }
        }
        
        @Override public void activate() {
            if(this.channel.areTheseActive(this.requiredTriggers)) {
                this.snapshotLink = this.channel.getPlayingSongTime();
                this.targetChannel.getActiveTrigger().activeLink = this;
                this.channel.disable(this);
            }
        }
        
        @Override public void close() {}
        
        @Override public TableRef getReferenceData() {
            return MTDataRef.LINK;
        }
        
        @Override protected String getSubTypeName() {
            return "Link";
        }
        
        @Override public Class<? extends ParameterWrapper> getTypeClass() {
            return Link.class;
        }
        
        @Override public boolean isResource() {
            return false;
        }
        
        public void setupTarget() {
            this.targetChannel.getData().addActiveTriggers(this,this.linkedTriggers,true);
        }
        
        public void unlink() {
            this.channel.enable();
        }
    }

    @Getter
    public enum State {

        ACTIVE(true,true),
        DISABLED(false,false),
        IDLE(true,false),
        PLAYABLE(true,true);

        private static final Map<String,State> BY_NAME = new HashMap<>();

        private final boolean activatable;
        private final boolean playable;

        State(boolean activatable, boolean playable) {
            this.activatable = activatable;
            this.playable = playable;
        }
        
        @Override
        public String toString() {
            return name();
        }

    }

    protected static class Timer {

        private final TriggerAPI parent;
        private final String name;
        private final State state;
        private final MutableInt counter;

        protected Timer(TriggerAPI parent, String name, State state) {
            this.parent = parent;
            this.name = name;
            this.state = state;
            this.counter = new MutableInt();
        }

        protected void clear(State state) {
            if(this.state==state) this.counter.setValue(0);
        }

        protected boolean hasTime() {
            return this.counter.getValue()>0;
        }

        protected void set(State state) {
            if(this.state==state) this.counter.setValue(this.parent.getParameterAsInt(this.name));
        }

        protected void tick(State state) {
            if(this.state==state && this.counter.getValue()>0) this.counter.decrement();
        }
    }
}