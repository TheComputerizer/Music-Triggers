package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventHandler;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;

@Getter
public abstract class TriggerAPI extends ParameterWrapper {

    private final Map<String,MutableInt> activeTimers;
    private final Map<String,MutableInt> playableTimers;
    private final Set<TriggerCombination> parents;
    private final String name;
    private ResourceContext resourceCtx;
    @Setter private State state;
    private int tracksPlayed;

    protected TriggerAPI(ChannelAPI channel, String name) {
        super(channel);
        this.activeTimers = new HashMap<>();
        this.playableTimers = new HashMap<>();
        this.parents = new HashSet<>();
        this.name = name;
    }

    @Override
    public void activate() {
        this.state = State.ACTIVE;
    }

    public boolean canActivate() {
        return this.state.activatable && checkPlayableTimedParameter("ticks_before_active");
    }

    public boolean checkActiveTimedParameter(String name) {
        return checkTimedParameter(name,this.activeTimers);
    }

    public boolean checkPlayableTimedParameter(String name) {
        return checkTimedParameter(name,this.playableTimers);
    }

    public boolean checkTimedParameter(String name, Map<String,MutableInt> map) {
        return map.entrySet().removeIf(entry -> entry.getKey().equals(name) && entry.getValue().getValue()<=0);
    }

    public String getIdentifier() {
        String id = getParameterAsString("identifier");
        return Objects.nonNull(id) ? id : "not_set";
    }

    public String getNameWithID() {
        return getName();
    }

    public List<String> getRequiredMods() {
        return Collections.emptyList();
    }

    @Override
    public Class<? extends ChannelElement> getTypeClass() {
        return TriggerAPI.class;
    }

    @Override
    protected String getTypeName() {
        return "Trigger `"+getName()+"`";
    }

    public boolean hasActiveTime(String name) {
        return hasTime(name,this.activeTimers);
    }

    public boolean hasNonEmptyAudioPool() {
        List<ChannelEventHandler> handlers = this.channel.getData().getTriggerEventMap().get(this);
        if(Objects.isNull(handlers)) return false;
        for(ChannelEventHandler handler : handlers)
            if(handler instanceof AudioPool && ((AudioPool)handler).hasAudio()) return true;
        return false;
    }

    public boolean hasMaxedTracks() {
        int maxTracks = getParameterAsInt("max_tracks");
        return maxTracks>0 && this.tracksPlayed>=maxTracks;
    }

    public boolean hasPlayableTime(String name) {
        return hasTime(name,this.playableTimers);
    }

    public boolean hasTime(String name, Map<String,MutableInt> map) {
        return map.containsKey(name) && map.get(name).getValue()>0;
    }

    @Override
    protected Map<String,Parameter<?>> initParameterMap() {
        Map<String,Parameter<?>> map = new HashMap<>();
        addParameter(map,"active_cooldown",new ParameterInt(0));
        addParameter(map,"fade_in",new ParameterInt(0));
        addParameter(map,"fade_out",new ParameterInt(0));
        addParameter(map,"max_tracks",new ParameterInt(0));
        addParameter(map,"not",new ParameterBoolean(false));
        addParameter(map,"passive_persistence",new ParameterBoolean(false));
        addParameter(map,"persistence",new ParameterInt(0));
        addParameter(map,"priority",new ParameterInt(0));
        addParameter(map,"start_toggled",new ParameterBoolean(true));
        addParameter(map,"ticks_before_active",new ParameterInt(0));
        addParameter(map,"ticks_before_audio",new ParameterInt(0));
        addParameter(map,"ticks_between_audio",new ParameterInt(0));
        addParameter(map,"toggle_inactive_playable",new ParameterBoolean(false));
        addParameter(map,"toggle_save_status",new ParameterInt(0));
        initExtraParameters(map);
        return map;
    }

    public abstract boolean isActive(TriggerContextAPI<?,?> context);

    public boolean isContained(Collection<TriggerAPI> triggers) {
        return TriggerHelper.matchesAny(triggers,this);
    }

    public boolean isDisabled() {
        return this.state==State.DISABLED;
    }

    public boolean isServer() {
        return false;
    }

    public boolean matches(Collection<TriggerAPI> triggers) {
        return triggers.size()==1 && isContained(triggers);
    }

    public abstract boolean matches(TriggerAPI trigger);

    public void onConnect() {

    }

    public void onDisconnect() {

    }

    public boolean parse(Table table) {
        if(parseParameters(table)) {
            setResourceContext();
            return true;
        }
        return false;
    }

    @Override
    public void playable() {
        setPlayableTimedParameter("ticks_before_active");
    }

    /**
     * Queries the active state of the trigger & wraps isActive with additional checks
     */
    public boolean query(TriggerContextAPI<?,?> context) {
        return hasNonEmptyAudioPool() && (hasActiveTime("persistence") || isActive(context));
    }

    @SuppressWarnings("unchecked")
    protected void setResourceContext() {
        if(hasParameter("resource_name")) {
            List<String> resourceName = (List<String>)getParameterAsList("resource_name");
            List<String> displayeName = (List<String>)getParameterAsList("display_name");
            String resourceMatcher = getParameterAsString("resource_matcher");
            String displayMatcher = getParameterAsString("display_matcher");
            this.resourceCtx = new ResourceContext(resourceName,displayeName,resourceMatcher,displayMatcher);
        } else this.resourceCtx = null;
    }

    public void setActiveTimedParameter(String name) {
        setTimedParameter(name,this.activeTimers);
    }

    public void setPlayableTimedParameter(String name) {
        setTimedParameter(name,this.playableTimers);
    }

    private void setTimedParameter(String name, Map<String,MutableInt> map) {
        int time = getParameterAsInt(name);
        if(time>0) Misc.consumeNullable(map.putIfAbsent(name,new MutableInt(time)),timer -> timer.setValue(time));
    }

    public void tickActive() {
        this.activeTimers.forEach((key,timer) -> timer.decrement());
    }

    public void tickPlayable() {
        this.playableTimers.forEach((key,timer) -> timer.decrement());
    }

    @Getter
    public enum State {

        ACTIVE(false),
        DISABLED(false),
        IDLE(true),
        PLAYABLE(true);

        private final boolean activatable;

        State(boolean activatable) {
            this.activatable = activatable;
        }
    }
}