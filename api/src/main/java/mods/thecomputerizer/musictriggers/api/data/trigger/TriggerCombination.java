package mods.thecomputerizer.musictriggers.api.data.trigger;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.UniversalParameters;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;

import java.util.*;

@SuppressWarnings("SameParameterValue") 
public class TriggerCombination extends TriggerAPI {

    public static TriggerCombination make(ChannelAPI channel, Collection<TriggerAPI> triggers) {
        TriggerCombination combo = new TriggerCombination(channel);
        for(TriggerAPI trigger : triggers) combo.addChild(trigger);
        return combo;
    }

    private final Collection<TriggerAPI> triggers;

    protected TriggerCombination(ChannelAPI channel) {
        super(channel,"combination");
        this.triggers = new HashSet<>();
    }

    public void addChild(TriggerAPI trigger) {
        this.triggers.add(trigger);
        setParentStatus(trigger,false);
        recalculateParameters();
    }
    
    @Override
    public boolean checkSidedContext(TriggerContext context) {
        for(TriggerAPI trigger : this.triggers)
            if(!trigger.checkSidedContext(context)) return false;
        return true;
    }

    @Override
    public void close() {
        super.close();
        this.triggers.clear();
    }

    @Override
    public void encode(ByteBuf buf) {
        super.encode(buf);
        NetworkHelper.writeCollection(buf,this.triggers,trigger -> trigger.encode(buf));
    }
    
    @Override
    public String getName() {
        return this.triggers.toString();
    }
    
    @Override
    public Parameter<?> getParameter(String name) {
        TriggerAPI priority = TriggerHelper.getPriorityTrigger(this.channel.getHelper(),this.triggers);
        return Objects.nonNull(priority) ? priority.getParameter(name) : super.getParameter(name);
    }
    
    @Override
    protected String getSubTypeName() {
        return "Combination";
    }

    public boolean isContained(TriggerAPI trigger) {
        return trigger.matches(this.triggers);
    }
    
    @Override
    public boolean isDisabled() {
        for(TriggerAPI trigger : this.triggers)
            if(trigger.isDisabled()) return true;
        return false;
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {}

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return false;
    }

    @Override
    public boolean isContained(Collection<TriggerAPI> triggers) {
        return TriggerHelper.matchesAny(this.triggers,triggers);
    }

    @Override
    public boolean matches(Collection<TriggerAPI> triggers) {
        return TriggerHelper.matchesAll(this.triggers,triggers);
    }

    @Override
    public boolean matches(TriggerAPI trigger) {
        return trigger instanceof TriggerCombination && matches(((TriggerCombination)trigger).triggers);
    }
    
    protected void recalculateParameters() {
        TriggerAPI reference = TriggerHelper.getPriorityTrigger(this.channel.getHelper(),this.triggers);
        if(Objects.nonNull(reference)) inheritParameters(reference);
    }

    protected void setParentStatus(TriggerAPI trigger, boolean removal) {
        Set<TriggerCombination> parents = trigger.getParents();
        if(removal) parents.remove(this);
        else parents.add(this);
    }
    
    @Override
    public void setUniversals(UniversalParameters universals) {
        super.setUniversals(universals);
        for(TriggerAPI trigger : this.triggers) trigger.setUniversals(universals);
    }

    @Override
    public String toString() {
        return getTypeName()+getName();
    }

    @Override
    public boolean verifyRequiredParameters() {
        for(TriggerAPI trigger : this.triggers) {
            if(!trigger.verifyRequiredParameters()) {
                logError("Unable to construct trigger combination due to 1 or more triggers failing verification!");
                setParentStatus(trigger,true);
                return false;
            }
        }
        return true;
    }
}