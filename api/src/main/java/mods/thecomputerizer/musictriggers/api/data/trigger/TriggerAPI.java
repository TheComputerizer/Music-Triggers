package mods.thecomputerizer.musictriggers.api.data.trigger;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventHandler;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State.*;

@Getter
public abstract class TriggerAPI extends ChannelElement {

    private static final Map<TriggerAPI,Map<String,Timer>> TIMER_MAP = new HashMap<>(); // This needs to be static due to super stuff

    private final Set<TriggerCombination> parents;
    private final Set<Link> links;
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
        this.state = State.ACTIVE;
        setTimer("ticks_before_audio",ACTIVE);
    }

    protected void addTimedParameter(String name, State state, Parameter<?> parameter) {
        if(Objects.nonNull(parameter)) {
            TIMER_MAP.putIfAbsent(this,new HashMap<>());
            TIMER_MAP.get(this).put(name,new Timer(this,name,state));
        }
    }

    public boolean canActivate() {
        return this.state.activatable && !hasTime("active_cooldown") && !hasTime("ticks_before_active") &&
                hasNonEmptyAudioPool();
    }

    protected boolean canPersist() {
        return hasTime("persistence") &&
                (this.state==ACTIVE || (this.state==PLAYABLE && getParameterAsBoolean("passive_persistence")));
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
        setState(this.channel.getSelector().isPlayable(this) ? PLAYABLE : IDLE);
        clearTimers(ACTIVE);
        setTimer("active_cooldown",PLAYABLE);
        this.tracksPlayed = 0;
    }

    public void encode(ByteBuf buf) {
        NetworkHelper.writeString(buf,getName());
        NetworkHelper.writeString(buf,getIdentifier());
        NetworkHelper.writeString(buf,this.state.name());
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
    
    @Override protected TableRef getReferenceData() {
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
        return this.state==State.DISABLED;
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
        return (isServer() && this.channel.isClientChannel()) || (!isServer() && !this.channel.isClientChannel());
    }

    public boolean matches(Collection<TriggerAPI> triggers) {
        return triggers.size()==1 && isContained(triggers);
    }

    public boolean matches(TriggerAPI trigger) {
        return this.equals(trigger);
    }

    public void onConnect() {

    }

    public void onDisconnect() {

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
        setState(PLAYABLE);
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
    
    protected <V> void setExistingParameterValue(String name, V value) {
        Parameter<?> parameter = getParameter(name);
        if(Objects.nonNull(parameter)) parameter.setValue(value);
    }

    public void setState(State state) {
        if(!isSynced()) {
            if(this.state!=state) {
                this.state = state;
                this.channel.getSync().queueTriggerSync(this);
            }
        }
    }

    protected void setTimer(String name, State state) {
        Timer timer = TIMER_MAP.containsKey(this) ? TIMER_MAP.get(this).get(name) : null;
        if(Objects.nonNull(timer)) timer.set(state);
    }

    protected void setTimers(State state) {
        consumeTimers(timer -> timer.set(state));
    }

    @Override
    public void stopped() {
        setTimer("ticks_between_audio",ACTIVE);
    }

    /**
     * Runs after this trigger has been successfully parsed
     */
    @SuppressWarnings("unchecked")
    protected void successfullyParsed() {
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

    @Override
    public void unplayable() {
        if(!isDisabled()) setState(State.IDLE);
        clearTimers(PLAYABLE);
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
                else this.valid = true;
            }
        }
        
        @Override public void close() {}
        
        @Override protected TableRef getReferenceData() {
            return MTDataRef.LINK;
        }
        
        @Override protected String getSubTypeName() {
            return "Link";
        }
        
        @Override protected Class<? extends ParameterWrapper> getTypeClass() {
            return Link.class;
        }
        
        @Override public boolean isResource() {
            return false;
        }
    }

    @Getter
    public enum State {

        ACTIVE(true),
        DISABLED(false),
        IDLE(true),
        PLAYABLE(true);

        private static final Map<String,State> BY_NAME = new HashMap<>();
        public static State get(String name) {
            return BY_NAME.getOrDefault(name,IDLE);
        }

        private final boolean activatable;

        State(boolean activatable) {
            this.activatable = activatable;
        }

        static {
            for(State state : values()) BY_NAME.put(state.name(),state);
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