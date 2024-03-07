package mods.thecomputerizer.musictriggers.api.data.trigger;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;

import java.util.*;

public class TriggerCombination extends TriggerAPI {

    /**
     * Each child list is an either/or list of triggers.
     * All child lists must have at least 1 trigger active for the parent combination to be active
     */
    private final List<List<TriggerAPI>> children;

    protected TriggerCombination(ChannelAPI channel) {
        super(channel,"combination");
        this.children = new ArrayList<>();
    }

    public void addChild(TriggerAPI ... triggers) {
        this.children.add(Arrays.asList(triggers));
        setParentStatus(false,triggers);
    }

    @Override
    public boolean isEnabled() {
        for(List<TriggerAPI> child : this.children)
            for(TriggerAPI trigger : child)
                if(!trigger.isEnabled()) return false;
        return true;
    }

    @Override
    protected void initExtraParameters(Map<String, Parameter<?>> map) {
        map.clear();
    }

    @Override
    public boolean isActive(TriggerContextAPI ctx) {
        for(List<TriggerAPI> child : this.children)
            if(!isChildActive(ctx,child)) return false;
        return true;
    }

    protected boolean isChildActive(TriggerContextAPI ctx, List<TriggerAPI> triggers) {
        for(TriggerAPI trigger : triggers)
            if(trigger.isActive(ctx)) return true;
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        for(List<TriggerAPI> child : this.children)
            for(TriggerAPI trigger : child)
                trigger.setEnabled(enabled);
    }

    protected void setParentStatus(boolean removal) {
        for(List<TriggerAPI> child : this.children)
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
    public boolean verifyRequiredParameters() {
        for(List<TriggerAPI> child : this.children) {
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