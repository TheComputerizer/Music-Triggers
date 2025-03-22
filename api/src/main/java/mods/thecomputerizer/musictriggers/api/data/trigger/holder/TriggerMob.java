package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.EntityAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.world.BlockPosAPI;

import java.util.*;

public class TriggerMob extends HolderTrigger {

    private final Set<EntityAPI<?,?>> cachedEntities;

    public TriggerMob(ChannelAPI channel) {
        super(channel,"mob");
        this.cachedEntities = new HashSet<>();
    }
    
    public void cacheValidEntity(EntityAPI<?,?> entity) {
        this.cachedEntities.add(entity);
    }
    
    public void deduplicate(Collection<EntityAPI<?,?>> entities) {
        for(EntityAPI<?,?> entity : this.cachedEntities) {
            final Object unwrapped = entity.getEntity();
            entities.removeIf(e -> e.getEntity()==unwrapped);
        }
    }
    
    @Override public boolean imply(String id) {
        setExistingParameterValue("resource_name",Collections.singletonList(id));
        return super.imply(id);
    }
    
    public List<?> getNBTParameter() {
        return getParameterAsList("mob_nbt");
    }

    public boolean checkCacheSize() {
        int size = this.cachedEntities.size();
        //MTRef.logInfo("Mob trigger has {} cached entities",size);
        return size>=getParameterAsInt("min_entities") && size<=getParameterAsInt("max_entities");
    }
    
    /**
     * Returns true is the entity is outside the required distance
     */
    public boolean checkEntityFarAway(EntityAPI<?,?> entity, BlockPosAPI<?> pos) {
        if(Objects.isNull(entity) || Objects.isNull(pos)) return true;
        int horizontalRange = getParameterAsInt("detection_range");
        float rangeRatioY = getParameterAsFloat("detection_y_ratio");
        int verticalRange = (int)((float)horizontalRange*rangeRatioY);
        BlockPosAPI<?> entityPos = entity.getPos();
        return Math.abs(entityPos.x()-pos.x())>horizontalRange || Math.abs(entityPos.y()-pos.y())>verticalRange ||
               Math.abs(entityPos.z()-pos.z())>horizontalRange;
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveMob(this);
    }

    @Override public boolean isServer() {
        return true;
    }
    
    public void revalidateCache(BlockPosAPI<?> pos) {
        this.cachedEntities.removeIf(entity -> checkEntityFarAway(entity,pos));
    }

    @Override public boolean verifyRequiredParameters() {
        if(hasValidIdentifier()) {
            String[] parameters = new String[]{"display_name","resource_name"};
            if(hasAnyNonDefaultParameter(parameters)) return true;
            logMissingPotentialParameter(parameters);
        }
        return false;
    }
}
