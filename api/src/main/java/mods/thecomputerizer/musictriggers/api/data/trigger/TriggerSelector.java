package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventHandler;
import mods.thecomputerizer.musictriggers.api.data.trigger.basic.BasicTrigger;

import javax.annotation.Nullable;
import java.util.*;

import static mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State.ACTIVE;

@Getter
public class TriggerSelector extends ChannelElement {

    protected final TriggerContext context;
    protected Collection<TriggerAPI> playables;
    protected Collection<TriggerAPI> previousPlayables;
    protected TriggerAPI activeTrigger;
    protected TriggerAPI previousTrigger;
    protected AudioPool activePool;
    protected AudioPool previousPool;
    protected String crashHelper = "";
    private boolean cleared;

    public TriggerSelector(ChannelAPI channel, TriggerContext context) {
        super(channel);
        this.context = context;
        this.playables = Collections.emptySet();
    }

    public void clear() {
        this.playables = Collections.emptyList();
        this.previousPlayables = Collections.emptyList();
        this.activeTrigger = null;
        this.previousTrigger = null;
        this.activePool = null;
        this.previousPool = null;
        this.crashHelper = "cleared";
    }

    @Override
    public void close() {
        clear();
    }

    protected Collection<TriggerAPI> collectPlayableTriggers(Collection<TriggerAPI> triggers) {
        setCrashHelper("playable (trigger collection)");
        Set<TriggerAPI> playable = new HashSet<>();
        for(TriggerAPI trigger : triggers) {
            if(trigger instanceof BasicTrigger) continue;
            setCrashHelper("playable ("+trigger.getNameWithID()+")");
            if(trigger.query(this.context)) {
                //logTrace("Potential playable trigger: {}",trigger);
                playable.add(trigger);
            }
        }
        setPlayables(playable);
        playable.removeIf(trigger -> !trigger.canActivate());
        return playable;
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
                if(this.channel.getHelper().getDebugBool("REVERSE_PRIORITY")) {
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

    protected @Nullable TriggerAPI getPriorityTrigger(Collection<TriggerAPI> registeredTriggers) {
        Collection<TriggerAPI> triggers = collectPlayableTriggers(registeredTriggers);
        return this.channel.getHelper().getDebugBool("COMBINE_EQUAL_PRIORITY") ?
                new TriggerMerged(this.channel,getPriorityTriggers(triggers)) :
                TriggerHelper.getPriorityTrigger(this.channel.getHelper(),triggers);
    }

    public boolean isClient() {
        return this.channel.isClientChannel();
    }

    public boolean isPlayable(TriggerAPI trigger) {
        return this.playables.contains(trigger);
    }

    @Override
    public boolean isResource() {
        return false;
    }

    public void select() {
        if(!setContext()) return;
        setCrashHelper("trigger selection");
        TriggerAPI priorityTrigger = this.activeTrigger;
        if(!this.context.hasPlayer()) {
            setCrashHelper("early triggers");
            if(isClient()) {
                setCrashHelper("loading trigger");
                BasicTrigger loading = this.channel.getData().getLoadingTrigger();
                if(Objects.nonNull(loading) && loading.query(this.context)) priorityTrigger = loading;
                else {
                    setCrashHelper("menu trigger");
                    BasicTrigger menu = this.channel.getData().getMenuTrigger();
                    if(Objects.nonNull(menu) && menu.query(this.context)) priorityTrigger = menu;
                }
            }
        } else {
            setCrashHelper("normal triggers");
            priorityTrigger = getPriorityTrigger(this.channel.getData().getTriggerEventMap().keySet());
        }
        if(Objects.isNull(priorityTrigger)) {
            setCrashHelper("generic trigger");
            BasicTrigger generic = this.channel.getData().getGenericTrigger();
            if(Objects.nonNull(generic) && generic.query(this.context)) priorityTrigger = generic;
        }
        setActivePool(Objects.nonNull(priorityTrigger) ? (priorityTrigger instanceof BasicTrigger ?
                setBasicTrigger(priorityTrigger) : setActiveTrigger(priorityTrigger)) : null);
    }

    protected @Nullable AudioPool setActiveTrigger(TriggerAPI trigger) {
        if(this.channel.checkDeactivate(this.activeTrigger,trigger)) {
            if(Objects.nonNull(this.activeTrigger)) this.channel.deactivate();
            this.previousTrigger = this.activeTrigger;
            this.activeTrigger = trigger;
            if(Objects.nonNull(this.activeTrigger)) this.channel.activate();
        }
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

    private boolean setContext() {
        if(Objects.isNull(this.context)) {
            if(!this.cleared) {
                clear();
                this.cleared = true;
            }
            return false;
        }
        this.cleared = false;
        this.context.cache();
        return true;
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