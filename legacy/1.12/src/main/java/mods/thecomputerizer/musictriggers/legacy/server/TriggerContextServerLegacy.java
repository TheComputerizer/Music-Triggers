package mods.thecomputerizer.musictriggers.legacy.server;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.ResourceContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerBiome;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerMob;
import mods.thecomputerizer.musictriggers.api.server.TriggerContextServer;
import mods.thecomputerizer.shadow.org.joml.Vector3i;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class TriggerContextServerLegacy extends TriggerContextServer<EntityPlayerMP,WorldServer> {

    private BlockPos pos;
    private Biome biome;
    private StructureRef structure;

    public TriggerContextServerLegacy(ChannelAPI channel) {
        super(channel);
    }

    @Override
    public void cache() {
        boolean both = hasBoth();
        this.pos = both ? getRoundedBlockPos() : null;
        this.biome = both ? this.world.getBiome(this.pos) : null;
        this.structure = both ? StructureRef.getStructureAt(this.world,this.pos) : null;
    }

    private boolean checkBiomeNameAndType(TriggerBiome trigger) {
        ResourceLocation regName = this.biome.getRegistryName();
        if(Objects.isNull(regName)) return false;
        ResourceContext ctx = trigger.getResourceCtx();
        if(ctx.checkMatch(regName.toString(),null)) return true; //TODO Sync biome names or check biomes on the client
        ctx = trigger.getTagCtx();
        for(Type type : BiomeDictionary.getTypes(this.biome))
            if(ctx.checkMatch(type.getName(),null)) return true;
        return false;
    }

    private boolean checkBiomeRain(TriggerBiome trigger) {
        String rainType = trigger.getParameterAsString("rain_type").toUpperCase();
        if(!this.biome.canRain()) return rainType.equals("ANY") || rainType.equals("NONE");
        if(this.biome.isSnowyBiome() && !rainType.equals("SNOW") && !rainType.equals("ANY")) return false;
        float rainfall = trigger.getParameterAsFloat("biome_rainfall");
        return trigger.getParameterAsBoolean("rainfall_greater_than") ?
                this.biome.getRainfall()>=rainfall : this.biome.getRainfall()<=rainfall;
    }

    private boolean checkBiomeExtras(TriggerBiome trigger) {
        if(checkBiomeRain(trigger)) {
            float temperature = trigger.getParameterAsFloat("biome_temperature");
            return trigger.getParameterAsBoolean("temperature_greater_than") ?
                    this.biome.getTemperature(this.pos)>=temperature : this.biome.getTemperature(this.pos)<=temperature;
        }
        return false;
    }

    private boolean checkEntity(TriggerMob<Entity> trigger, Entity entity) {
        ResourceContext ctx = trigger.getResourceCtx();
        return Objects.nonNull(ctx) && checkEntityName(ctx,entity);
    }

    private boolean checkEntityName(ResourceContext ctx, Entity entity) {
        EntityEntry entry = EntityRegistry.getEntry(entity.getClass());
        ResourceLocation id = Objects.nonNull(entry) && ForgeRegistries.ENTITIES.containsValue(entry) ?
                ForgeRegistries.ENTITIES.getKey(entry) : null;
        if(Objects.isNull(id)) return false;
        String name = entity.getName();
        return ctx.checkMatch(id.toString(),StringUtils.isNotBlank(name) ? name : null);
    }

    @Override
    protected int getAir() {
        return hasPlayer() ? this.player.getAir() : 300;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E> List<E> getEntitiesAround(
            Class<E> clazz, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return hasWorld() ? (List<E>)this.world.getEntitiesWithinAABB((Class<? extends Entity>)clazz,
                new AxisAlignedBB(minX,minY,minZ,maxX,maxY,maxZ)) : Collections.emptyList();
    }

    private Set<Entity> getEntitiesAround(TriggerMob<Entity> trigger) {
        int range = trigger.getParameterAsInt("detection_range");
        float rangeRatioY = trigger.getParameterAsFloat("detection_y_ratio");
        return trigger.removeDuplicates(getEntitiesAround(Entity.class,range,rangeRatioY));
    }

    private BlockPos getBlockPos(Vector3i vector) {
        return new BlockPos(vector.x,vector.y,vector.z);
    }

    @Override
    protected int getGamemode() {
        return Objects.nonNull(this.player.interactionManager) ? this.player.interactionManager.getGameType().getID() : 0;
    }

    @Override
    protected float getHealth() {
        return hasPlayer() ? this.player.getHealth() : 20f;
    }

    @Override
    protected float getMaxHealth() {
        return hasPlayer() ? this.player.getMaxHealth() : 20f;
    }

    private BlockPos getRoundedBlockPos() {
        return getBlockPos(getRoundedPos());
    }

    @Override
    protected Vector3i getRoundedPos() {
        return hasPlayer() ? new Vector3i((int)(Math.round(this.player.posX*2d)/2d),
                (int)(Math.round(this.player.posY*2d)/2d),(int)(Math.round(this.player.posZ*2d)/2d)) :
                new Vector3i(0,0,0);
    }

    @Override
    protected boolean hasVisibleSky() {
        return Objects.nonNull(this.pos) && this.world.canSeeSky(this.pos);
    }

    @Override
    public boolean isActiveBiome(TriggerBiome trigger) { //TODO Better caching
        return Objects.nonNull(this.biome) && checkBiomeNameAndType(trigger) && checkBiomeExtras(trigger);
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public boolean isActiveHome(int range, float yRatio) {
        if(Objects.isNull(this.pos)) return false;
        BlockPos bed = this.player.getBedLocation(this.player.dimension);
        return Objects.nonNull(bed) && isCloseEnough(bed.getX(),bed.getY(),bed.getZ(),range,yRatio,
                this.pos.getX(),this.pos.getY(),this.pos.getZ());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isActiveMob(TriggerMob<?> baseTrigger) { //TODO Cache stuff & implement the rest of this
        if(Objects.isNull(this.pos)) return false;
        TriggerMob<Entity> trigger = (TriggerMob<Entity>)baseTrigger;
        validateEntities(trigger,getEntitiesAround(trigger));
        int min = trigger.getParameterAsInt("min_entities");
        int max = trigger.getParameterAsInt("max_entities");
        return trigger.hasCorrectSize(min,max);
    }

    @Override
    public boolean isActivePVP() { //TODO implement this
        if(!hasBoth()) return false;
        return false;
    }

    @Override
    public boolean isActiveRaid(int wave) {
        return false;
    }

    @Override
    public boolean isActiveSnowing() {
        return Objects.nonNull(this.pos) && this.world.canSnowAtBody(this.pos,false);
    }

    @Override
    public boolean isActiveStructure(ResourceContext ctx) { //TODO Expand upon this
        return Objects.nonNull(this.structure) && ctx.checkMatch(this.structure.getId().toString(),this.structure.getName());
    }

    @Override
    public boolean isActiveVictory(int timeout) { //TODO implement this
        if(!hasBoth()) return false;
        return false;
    }

    private void validateEntities(TriggerMob<Entity> trigger, Collection<Entity> entitiesAround) {
        Set<Entity> entities = trigger.getValidEntities();
        entities.addAll(entitiesAround);
        trigger.getValidEntities().removeIf(entity -> !checkEntity(trigger,entity));
    }
}
