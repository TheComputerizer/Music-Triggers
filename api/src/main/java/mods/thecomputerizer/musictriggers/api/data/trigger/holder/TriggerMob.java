package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.EntityAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.LivingEntityAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.PlayerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.world.BlockPosAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.wrappers.WrapperHelper;

import java.util.*;

import static java.lang.Integer.MAX_VALUE;

public class TriggerMob extends HolderTrigger {

    protected final Set<EntityAPI<?,?>> cachedEntities;
    protected int cachedHordeHealthCount;
    protected int cachedHordeTargetingCount;
    protected int cachedMaxEntities;
    protected int cachedMinEntities;
    protected float cachedMaxHealth;
    protected float cachedMinHealth;
    protected int cachedRangeXZ;
    protected int cachedRangeY;

    public TriggerMob(ChannelAPI channel) {
        super(channel,"mob");
        this.cachedEntities = new HashSet<>();
        this.cachedMaxEntities = MAX_VALUE;
        this.cachedMaxHealth = 1f;
    }
    
    protected int cacheHordeParameter(String name) {
        float percent = Math.min(1f,getParameterAsFloat(name)/100f);
        if(this.cachedMaxEntities==MAX_VALUE) return percent<=0f ? 0 : this.cachedMinEntities;
        return (int)(((float)this.cachedMaxEntities)*percent);
    }
    
    public void cacheParameters() {
        this.cachedMaxEntities = getParameterAsInt("max_entities");
        this.cachedMinEntities = getParameterAsInt("min_entities");
        this.cachedHordeHealthCount = cacheHordeParameter("horde_health_percentage");
        this.cachedHordeTargetingCount= cacheHordeParameter("horde_targeting_percentage");
        this.cachedMinHealth = getParameterAsFloat("min_health")/100f;
        this.cachedMaxHealth = getParameterAsFloat("max_health")/100f;
        this.cachedRangeXZ = getParameterAsInt("detection_range");
        this.cachedRangeY = (int)((float)this.cachedRangeXZ*getParameterAsFloat("detection_y_ratio"));
    }
    
    public void cacheValidEntity(EntityAPI<?,?> entity, PlayerAPI<?,?> player) {
        if(checkHealth(entity) && checkTarget(entity,player)) this.cachedEntities.add(entity);
    }
    
    public boolean checkCacheSize() {
        int size = this.cachedEntities.size();
        return size>=this.cachedMinEntities && size<=this.cachedMaxEntities &&
               size>=this.cachedHordeHealthCount && size>=this.cachedHordeTargetingCount;
    }
    
    /**
     * Returns true is the entity is outside the required distance
     */
    public boolean checkEntityFarAway(EntityAPI<?,?> entity, BlockPosAPI<?> pos) {
        if(Objects.isNull(entity) || Objects.isNull(pos) || (entity.isLiving() && !entity.isAlive())) return true;
        BlockPosAPI<?> ePos = entity.getPos();
        int xDif = Math.abs(ePos.x()-pos.x());
        int yDif = Math.abs(ePos.y()-pos.y());
        int zDif = Math.abs(ePos.z()-pos.z());
        return xDif>this.cachedRangeXZ || yDif>this.cachedRangeY || zDif>this.cachedRangeXZ;
    }
    
    /**
     * Returns false is the entity is null, is dead, or has health is outside the required percentage range.
     * Nonliving entities will only return true if they are null.
     */
    public boolean checkHealth(EntityAPI<?,?> entity) {
        if(Objects.isNull(entity) || !entity.isAlive()) return false;
        if(entity.isLiving()) {
            LivingEntityAPI<?,?> living = WrapperHelper.wrapLivingEntity(entity.getEntity());
            if(Objects.isNull(living) || !living.isAlive()) return false;
            float percent = living.getHealthPercent();
            return this.cachedMinHealth<=percent && this.cachedMaxHealth>=percent;
        }
        return true;
    }
    
    /**
     * Return true if the target check passes
     */
    public boolean checkTarget(EntityAPI<?,?> entity, PlayerAPI<?,?> player) {
        if(!getParameterAsBoolean("mob_targeting") || !entity.canTarget() || !entity.isHostile()) return true;
        EntityAPI<?,?> target = entity.getAttackTarget();
        if(Objects.isNull(target) || !target.isPlayer() || Objects.isNull(player)) return false;
        return getParameterAsBoolean("target_any_player") || target.getEntity()==player.getEntity();
    }
    
    public void deduplicate(Collection<EntityAPI<?,?>> entities) {
        for(EntityAPI<?,?> entity : this.cachedEntities) {
            final Object unwrapped = entity.getEntity();
            entities.removeIf(e -> e.getEntity()==unwrapped);
        }
    }
    
    public List<?> getNBTParameter() {
        return getParameterAsList("mob_nbt");
    }
    
    @Override public boolean imply(String id) {
        setExistingParameterValue("resource_name",Collections.singletonList(id));
        return super.imply(id);
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveMob(this);
    }

    @Override public boolean isServer() {
        return true;
    }
    
    public void revalidateCache(BlockPosAPI<?> pos, PlayerAPI<?,?> player) {
        this.cachedEntities.removeIf(entity -> checkEntityFarAway(entity,pos) ||
                                             !checkHealth(entity) || !checkTarget(entity,player));
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