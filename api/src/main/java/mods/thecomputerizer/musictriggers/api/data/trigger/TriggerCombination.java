package mods.thecomputerizer.musictriggers.api.data.trigger;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.UniversalParameters;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.parameter.Parameter;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.ListTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.TagHelper;

import java.util.*;

import static mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State.DISABLED;

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
    
    /**
     * Called from TriggerContext#initSync.
     * Recalculate triggers with synced wrappers if necessary
     */
    @Override public void afterSync(Map<TriggerAPI,TriggerSynced> syncedMap) {
        Set<TriggerAPI> replaceThese = new HashSet<>();
        for(TriggerAPI trigger : this.triggers)
            if(syncedMap.containsKey(trigger)) replaceThese.add(trigger);
        if(!replaceThese.isEmpty()) {
            this.triggers.removeIf(replaceThese::contains);
            for(TriggerAPI trigger : replaceThese) this.triggers.add(syncedMap.get(trigger));
            logInfo("Replaced synced triggers {}",replaceThese);
        }
    }
    
    @Override public boolean canActivate() {
        if(!hasNonEmptyAudioPool() || this.triggers.isEmpty()) return false;
        for(TriggerAPI trigger : this.triggers)
            if(!trigger.canActivate(false)) return false;
        return true;
    }
    
    @Override public boolean checkSidedContext(TriggerContext context) {
        for(TriggerAPI trigger : this.triggers) {
            boolean playableCtx = trigger.isPlayableContext(context);
            boolean not = !(trigger instanceof TriggerCombination) && trigger.getParameterAsBoolean("not");
            return (playableCtx && !not) || (!playableCtx && not);
        }
        return true;
    }

    @Override public void close() {
        super.close();
        this.triggers.clear();
    }

    @Override public void encode(ByteBuf buf) {
        super.encode(buf);
        NetworkHelper.writeCollection(buf,this.triggers,trigger -> trigger.encode(buf));
    }
    
    @Override public String getName() {
        StringJoiner joiner = new StringJoiner("+");
        for(TriggerAPI trigger : this.triggers) joiner.add(trigger.getNameWithID());
        return this.triggers.size()==1 ? joiner.toString() : "Combination = "+joiner;
    }
    
    @Override public Parameter<?> getParameter(String name) {
        TriggerAPI priority = TriggerHelper.getPriorityTrigger(this.triggers);
        return Objects.nonNull(priority) ? priority.getParameter(name) : super.getParameter(name);
    }

    public boolean isContained(TriggerAPI trigger) {
        return trigger.matches(this.triggers);
    }
    
    @Override public boolean isDisabled() {
        for(TriggerAPI trigger : this.triggers)
            if(trigger.isDisabled()) return true;
        return false;
    }

    @Override protected void initExtraParameters(Map<String,Parameter<?>> map) {}

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return false;
    }

    @Override public boolean isContained(Collection<TriggerAPI> triggers) {
        return TriggerHelper.matchesAny(this.triggers,triggers);
    }

    @Override public boolean matches(Collection<TriggerAPI> triggers) {
        return TriggerHelper.matchesAll(this.triggers,triggers);
    }

    @Override public boolean matches(TriggerAPI trigger) {
        return trigger instanceof TriggerCombination && matches(((TriggerCombination)trigger).triggers);
    }
    
    @Override public boolean query(TriggerContext context) {
        return !this.triggers.isEmpty() && super.query(context,true);
    }
    
    protected void recalculateParameters() {
        TriggerAPI reference = TriggerHelper.getPriorityTrigger(this.triggers);
        if(Objects.nonNull(reference)) {
            inheritParameters(reference);
            recalculateTimers(reference);
        }
    }
    
    protected void recalculateTimers(TriggerAPI reference) {
        for(String name : reference.getTimedParameterNames()) {
            State timeState = getParameterTimeState(name);
            if(timeState!=DISABLED) addTimedParameter(name,timeState,getParameter(name));
        }
    }

    protected void setParentStatus(TriggerAPI trigger, boolean removal) {
        Set<TriggerCombination> parents = trigger.getParents();
        if(removal) parents.remove(this);
        else parents.add(this);
    }
    
    @Override public void setUniversals(UniversalParameters universals) {
        super.setUniversals(universals);
        for(TriggerAPI trigger : this.triggers) trigger.setUniversals(universals);
    }
    
    @Override public String toString() {
        return getSubTypeName()+"["+getName()+"]";
    }

    @Override public boolean verifyRequiredParameters() {
        for(TriggerAPI trigger : this.triggers) {
            if(!trigger.verifyRequiredParameters()) {
                logError("Unable to construct trigger combination due to 1 or more triggers failing verification!");
                setParentStatus(trigger,true);
                return false;
            }
        }
        return true;
    }
    
    @Override public void write(CompoundTagAPI<?> tag) {
        tag.putString("name","combination");
        ListTagAPI<?> triggersTag = TagHelper.makeListTag();
        for(TriggerAPI trigger : this.triggers) {
            CompoundTagAPI<?> triggerTag = TagHelper.makeCompoundTag();
            trigger.write(triggerTag);
            triggersTag.addTag(triggerTag);
        }
        tag.putTag("triggers",triggersTag);
    }
}