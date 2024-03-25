package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventHandler;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.basic.BasicTrigger;

import javax.annotation.Nullable;
import java.util.*;

import static mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State.IDLE;
import static mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State.PLAYABLE;

@Getter
public abstract class TriggerSelectorAPI<PLAYER,WORLD> extends ChannelElement {

    protected final TriggerContextAPI<PLAYER,WORLD> context;
    protected Collection<TriggerAPI> playables;
    protected Collection<TriggerAPI> previousPlayables;
    protected TriggerAPI activeTrigger;
    protected TriggerAPI previousTrigger;
    protected AudioPool activePool;
    protected AudioPool previousPool;
    protected String crashHelper = "";

    protected TriggerSelectorAPI(ChannelAPI channel, TriggerContextAPI<PLAYER,WORLD> context) {
        super(channel);
        this.context = context;
        this.playables = Collections.emptySet();
    }

    @Override
    public void activate() {
        if(Objects.nonNull(this.previousTrigger))
            this.previousTrigger.setState(isPlayable(this.previousTrigger) ? PLAYABLE : IDLE);
    }

    public void clear() {
        this.playables = Collections.emptyList();
        this.previousPlayables = Collections.emptyList();
        this.activeTrigger = null;
        this.previousTrigger = null;
        this.crashHelper = "cleared";
    }

    protected Collection<TriggerAPI> collectPlayableTriggers(Collection<TriggerAPI> triggers) {
        setCrashHelper("playable (trigger collection)");
        Set<TriggerAPI> playable = new HashSet<>();
        for(TriggerAPI trigger : triggers) {
            setCrashHelper("playable ("+trigger.getNameWithID()+")");
            if(trigger.isDisabled()) continue;
            if(trigger.query(this.context)) {
                playable.add(trigger);
                trigger.setState(PLAYABLE);
            }
        }
        setPlayables(playable);
        playable.removeIf(trigger -> !trigger.canActivate());
        return playable;
    }

    public @Nullable PLAYER getContextPlayer() {
        return Objects.nonNull(this.context) ? this.context.getPlayer() : null;
    }

    public @Nullable WORLD getContextWorld() {
        return Objects.nonNull(this.context) ? this.context.getWorld() : null;
    }

    /**
     * Only used when COMBINE_EQUAL_PRIORITY is enabled
     */
    public Collection<TriggerAPI> getPriorityTriggers(Collection<TriggerAPI> triggers) {
        Set<TriggerAPI> priority = new HashSet<>();
        int priorityVal = 0;
        for(TriggerAPI trigger : triggers) {
            int tPriority = trigger.getParameterAsInt("priority");
            if(priority.isEmpty()) {
                priority.add(trigger);
                priorityVal = tPriority;
            }
            else if(tPriority==priorityVal) priority.add(trigger);
            else {
                if(ChannelHelper.getDebugBool("REVERSE_PRIORITY")) {
                    if(tPriority<priorityVal) {
                        priority.clear();
                        priority.add(trigger);
                        priorityVal = tPriority;
                    }
                } else if(tPriority>priorityVal) {
                    priority.clear();
                    priority.add(trigger);
                    priorityVal = tPriority;
                }
            }
        }
        return priority;
    }

    protected @Nullable AudioPool getAudioPool(Collection<TriggerAPI> registeredTriggers) {
        Collection<TriggerAPI> triggers = collectPlayableTriggers(registeredTriggers);
        TriggerAPI trigger = ChannelHelper.getDebugBool("COMBINE_EQUAL_PRIORITY") ?
                new TriggerMerged(this.channel,getPriorityTriggers(triggers)) :
                TriggerHelper.getPriorityTrigger(triggers);
        return setActiveTrigger(trigger);
    }

    public abstract boolean isClient();

    public boolean isPlayable(TriggerAPI trigger) {
        return this.playables.contains(trigger);
    }

    @Override
    public boolean isResource() {
        return false;
    }

    public abstract void select();

    protected void select(@Nullable PLAYER player, @Nullable WORLD world) {
        if(Objects.isNull(this.context)) {
            clear();
            return;
        }
        setContextPlayer(player);
        setContextWorld(world);
        setCrashHelper("trigger selection");
        AudioPool pool = this.activePool;
        if(Objects.isNull(player)) {
            setCrashHelper("early triggers");
            if(isClient()) {
                setCrashHelper("loading trigger");
                BasicTrigger loading = this.channel.getData().getLoadingTrigger();
                if(Objects.nonNull(loading) && loading.query(this.context)) pool = setBasicTrigger(loading);
                else {
                    setCrashHelper("menu trigger");
                    BasicTrigger menu = this.channel.getData().getMenuTrigger();
                    if(Objects.nonNull(menu) && menu.query(this.context)) pool = setBasicTrigger(menu);
                }
            }
        } else {
            setCrashHelper("normal triggers");
            pool = getAudioPool(this.channel.getData().getTriggerEventMap().keySet());
            if(this.playables.isEmpty()) {
                setCrashHelper("generic trigger");
                BasicTrigger generic = this.channel.getData().getGenericTrigger();
                if(Objects.nonNull(generic) && generic.query(this.context)) pool = setBasicTrigger(generic);
            }
        }
        setActivePool(pool);
    }

    protected @Nullable AudioPool setActiveTrigger(TriggerAPI trigger) {
        if(trigger!=this.activeTrigger) this.channel.deactivate();
        this.previousTrigger = this.activeTrigger;
        this.activeTrigger = trigger;
        if(this.activeTrigger!=this.previousTrigger) this.channel.activate();
        return Objects.nonNull(this.activeTrigger) ? this.activeTrigger.getAudioPool() : null;
    }

    protected void setActivePool(AudioPool pool) {
        this.previousPool = this.activePool;
        this.activePool = pool;
    }

    protected @Nullable AudioPool setBasicTrigger(TriggerAPI trigger) {
        setPlayables(trigger);
        return setActiveTrigger(trigger);
    }

    public void setContextPlayer(PLAYER player) {
        this.context.setPlayer(player);
    }

    public void setContextWorld(WORLD world) {
        this.context.setWorld(world);
    }

    protected void setCrashHelper(@Nullable String status) {
        this.crashHelper = Objects.nonNull(status) ? status : "";
    }

    protected void setPlayables(TriggerAPI ... triggers) {
        setPlayables(Arrays.asList(triggers));
    }

    protected void setPlayables(Collection<TriggerAPI> triggers) {
        this.previousPlayables = this.playables;
        this.playables = Collections.unmodifiableCollection(triggers);
        for(TriggerAPI trigger : this.playables)
            if(!this.previousPlayables.contains(trigger))
                for(ChannelEventHandler handler : this.channel.getData().getEventHandlers(trigger))
                    handler.playable();
        for(TriggerAPI trigger : this.previousPlayables)
            if(!this.playables.contains(trigger))
                for(ChannelEventHandler handler : this.channel.getData().getEventHandlers(trigger))
                    handler.unplayable();
    }

    @Override
    public String toString() {
        return (isClient() ? "Client" : "Server")+" Trigger Selector ["+this.crashHelper+"]";
    }
}