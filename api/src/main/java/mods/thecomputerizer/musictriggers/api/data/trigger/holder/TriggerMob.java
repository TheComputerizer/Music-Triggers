package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.EntityAPI;

import java.util.*;

@Getter
public class TriggerMob extends HolderTrigger {

    private final Set<EntityAPI<?,?>> validEntities;

    public TriggerMob(ChannelAPI channel) {
        super(channel,"mob");
        this.validEntities = new HashSet<>();
    }
    
    @Override
    public boolean imply(String id) {
        setExistingParameterValue("resource_name",Collections.singletonList(id));
        return super.imply(id);
    }

    public boolean hasCorrectSize(int min, int max) {
        int size = this.validEntities.size();
        return size>=min && size<=max;
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveMob(this);
    }

    @Override
    public boolean isServer() {
        return true;
    }

    public void markEntityValid(EntityAPI<?,?> entity) {
        this.validEntities.add(entity);
    }

    public Set<EntityAPI<?,?>> removeDuplicates(Collection<EntityAPI<?,?>> entitiesAround) {
        Set<EntityAPI<?,?>> set = new HashSet<>(entitiesAround);
        set.removeAll(this.validEntities);
        return set;
    }

    @Override
    public boolean verifyRequiredParameters() {
        if(hasValidIdentifier()) {
            String[] parameters = new String[]{"display_name","resource_name"};
            if(hasAnyNonDefaultParameter(parameters)) return true;
            logMissingPotentialParameter(parameters);
        }
        return false;
    }
}
