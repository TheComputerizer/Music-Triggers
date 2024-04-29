package mods.thecomputerizer.musictriggers.api.data.trigger;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;

import javax.annotation.Nullable;
import java.util.*;

public class TriggerCombination extends TriggerAPI {

    public static TriggerCombination make(ChannelAPI channel, Collection<TriggerAPI> triggers) {
        TriggerCombination combo = new TriggerCombination(channel);
        for(TriggerAPI trigger : triggers) combo.addChild(trigger);
        return combo;
    }

    /**
     * Each child list is an either/or list of triggers.
     * All child lists must have at least 1 trigger active for the parent combination to be active
     */
    private final Collection<Collection<TriggerAPI>> children;

    private final Collection<TriggerAPI> priorityChildren;
    private TriggerAPI priorityTrigger;

    protected TriggerCombination(ChannelAPI channel) {
        super(channel,"combination");
        this.children = new HashSet<>();
        this.priorityChildren = new HashSet<>();
    }

    @Override
    public void activate() {
        for(TriggerAPI trigger : this.priorityChildren) trigger.activate();
    }

    public void addChild(TriggerAPI ... triggers) {
        this.children.add(Arrays.asList(triggers));
        setParentStatus(false,triggers);
    }

    @Override
    public void close() {
        super.close();
        for(Collection<TriggerAPI> child : this.children) child.clear();
        this.children.clear();
        this.priorityChildren.clear();
        this.priorityTrigger = null;
    }

    @Override
    public void deactivate() {
        for(TriggerAPI trigger : this.priorityChildren) trigger.deactivate();
    }

    @Override
    public void encode(ByteBuf buf) {
        super.encode(buf);
        NetworkHelper.writeCollection(buf,this.children,child -> NetworkHelper.writeCollection(
                buf,child,trigger -> trigger.encode(buf)));
    }

    @Override
    public @Nullable Parameter<?> getParameter(String name) {
        return Objects.nonNull(this.priorityTrigger) ? this.priorityTrigger.getParameter(name) : super.getParameter(name);
    }

    public boolean isContained(TriggerAPI trigger) {
        for(Collection<TriggerAPI> child : this.children)
            if(TriggerHelper.matchesAny(child,trigger)) return true;
        return false;
    }

    @Override
    public boolean isEnabled() {
        for(Collection<TriggerAPI> child : this.children)
            for(TriggerAPI trigger : child)
                if(!trigger.isEnabled()) return false;
        return true;
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        map.clear();
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        this.priorityChildren.clear();
        for(Collection<TriggerAPI> child : this.children)
            if(!isChildActive(ctx,child)) return false;
        updatePriorityTrigger();
        return true;
    }

    protected boolean isChildActive(TriggerContext ctx, Collection<TriggerAPI> triggers) {
        for(TriggerAPI trigger : triggers)
            if(trigger.isPlayableContext(ctx)) {
                this.priorityChildren.add(trigger);
                return true;
            }
        return false;
    }

    @Override
    public boolean isContained(Collection<TriggerAPI> triggers) {
        for(Collection<TriggerAPI> child : this.children)
            if(!TriggerHelper.matchesAny(child,triggers)) return false;
        return true;
    }

    @Override
    public boolean matches(Collection<TriggerAPI> triggers) {
        if(this.children.size()==triggers.size()) {
            for(Collection<TriggerAPI> child : this.children)
                for(TriggerAPI other : triggers)
                    if(!TriggerHelper.matchesAny(child,other)) return false;
            return true;
        }
        return false;
    }

    @Override
    public boolean matches(TriggerAPI trigger) {
        if(trigger instanceof TriggerCombination) {
            TriggerCombination combo = (TriggerCombination)trigger;
            if(this.children.size()==combo.children.size()) {
                for(Collection<TriggerAPI> child : this.children) {
                    if(matchesChild(child,combo)) continue;
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    protected boolean matchesChild(Collection<TriggerAPI> child, TriggerCombination combo) {
        for(Collection<TriggerAPI> otherChild : combo.children)
            if(child.size()==otherChild.size())
                if(!TriggerHelper.matchesAll(child,otherChild)) return false;
        return true;
    }

    @Override
    public void play() {
        for(TriggerAPI trigger : this.priorityChildren) trigger.play();
    }

    @Override
    public void playable() {
        for(TriggerAPI trigger : this.priorityChildren) trigger.playable();
    }

    @Override
    public void playing() {
        for(TriggerAPI trigger : this.priorityChildren) trigger.playing();
    }

    @Override
    public void queue() {
        for(TriggerAPI trigger : this.priorityChildren) trigger.queue();
    }


    @Override
    public void setState(State state) {
        for(Collection<TriggerAPI> child : this.children)
            for(TriggerAPI trigger : child)
                trigger.setState(state);
    }

    protected void setParentStatus(boolean removal) {
        for(Collection<TriggerAPI> child : this.children)
            for(TriggerAPI trigger : child)
                setParentStatus(trigger,removal);
    }

    protected void setParentStatus(TriggerAPI trigger, boolean removal) {
        Set<TriggerCombination> parents = trigger.getParents();
        if(removal) parents.remove(this);
        else parents.add(this);
    }

    protected void setParentStatus(boolean removal, TriggerAPI ... triggers) {
        for(TriggerAPI trigger : triggers) setParentStatus(trigger,removal);
    }

    @Override
    public void stop() {
        for(TriggerAPI trigger : this.priorityChildren) trigger.stop();
    }

    @Override
    public void stopped() {
        for(TriggerAPI trigger : this.priorityChildren) trigger.stopped();
    }

    @Override
    public String toString() {
        return "combination"+this.priorityChildren;
    }

    @Override
    public void tickActive() {
        for(TriggerAPI trigger : this.priorityChildren) trigger.tickActive();
    }

    @Override
    public void tickPlayable() {
        for(TriggerAPI trigger : this.priorityChildren) trigger.tickPlayable();
    }

    @Override
    public void unplayable() {
        for(TriggerAPI trigger : this.priorityChildren) trigger.unplayable();
    }

    private void updatePriorityTrigger() {
        this.priorityTrigger = TriggerHelper.getPriorityTrigger(this.channel.getHelper(),this.priorityChildren);
    }

    @Override
    public boolean verifyRequiredParameters() {
        for(Collection<TriggerAPI> child : this.children) {
            for(TriggerAPI trigger : child) {
                if(!trigger.verifyRequiredParameters()) {
                    logError("Unable to construct trigger combination due to 1 or more triggers failing verification!");
                    setParentStatus(true);
                    return false;
                }
            }
        }
        return true;
    }
}