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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TriggerContextServerLegacy extends TriggerContextServer<EntityPlayerMP,WorldServer> {

    public TriggerContextServerLegacy(ChannelAPI channel) {
        super(channel);
    }

    private boolean checkBiomeNameAndType(TriggerBiome trigger, Biome biome) {
        ResourceLocation regName = biome.getRegistryName();
        if(Objects.isNull(regName)) return false;
        ResourceContext ctx = trigger.getResourceCtx();
        if(ctx.checkMatch(regName.toString(),null)) return true; //TODO Sync biome names or check biomes on the client
        ctx = trigger.getTagCtx();
        for(Type type : BiomeDictionary.getTypes(biome))
            if(ctx.checkMatch(type.getName(),null)) return true;
        return false;
    }

    private boolean checkBiomeRain(TriggerBiome trigger, Biome biome) {
        String rainType = trigger.getParameterAsString("rain_type").toUpperCase();
        if(!biome.canRain()) return rainType.equals("ANY") || rainType.equals("NONE");
        if(biome.isSnowyBiome() && !rainType.equals("SNOW") && !rainType.equals("ANY")) return false;
        float rainfall = trigger.getParameterAsFloat("biome_rainfall");
        return trigger.getParameterAsBoolean("rainfall_greater_than") ?
                biome.getRainfall()>=rainfall : biome.getRainfall()<=rainfall;
    }

    private boolean checkBiomeExtras(TriggerBiome trigger, Biome biome, BlockPos pos) {
        if(checkBiomeRain(trigger,biome)) {
            float temperature = trigger.getParameterAsFloat("biome_temperature");
            return trigger.getParameterAsBoolean("temperature_greater_than") ?
                    biome.getTemperature(pos)>=temperature : biome.getTemperature(pos)<=temperature;
        }
        return false;
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
    protected boolean hasVisibleSky(Vector3i pos) {
        return hasWorld() && this.world.canSeeSky(getBlockPos(pos));
    }

    @Override
    public boolean isActiveBiome(TriggerBiome trigger) { //TODO Cache stuff
        if(!hasPlayer() || !hasWorld()) return false;
        BlockPos pos = getRoundedBlockPos();
        Biome biome = this.world.getBiome(pos);
        return checkBiomeNameAndType(trigger,biome) && checkBiomeExtras(trigger,biome,pos);
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public boolean isActiveHome(int range, float yRatio) {
        if(!hasPlayer() || !hasWorld()) return false;
        BlockPos bed = this.player.getBedLocation(this.player.dimension);
        Vector3i pos = getRoundedPos();
        return Objects.nonNull(bed) && isCloseEnough(bed.getX(),bed.getY(),bed.getZ(),range,yRatio,pos.x,pos.y,pos.z);
    }

    @Override
    public boolean isActiveMob(TriggerMob trigger) { //TODO Cache stuff
        if(!hasPlayer() || !hasWorld()) return false;
        return false;
    }

    @Override
    public boolean isActivePVP() {
        if(!hasPlayer() || !hasWorld()) return false;
        return false;
    }

    @Override
    public boolean isActiveRaid(int wave) {
        return false;
    }

    @Override
    public boolean isActiveSnowing() {
        return hasWorld() && this.world.canSnowAtBody(getRoundedBlockPos(),false);
    }

    @Override
    public boolean isActiveStructure(ResourceContext ctx) {
        if(!hasPlayer() || !hasWorld()) return false;
        return false;
    }

    @Override
    public boolean isActiveVictory(int timeout) {
        if(!hasPlayer() || !hasWorld()) return false;
        return false;
    }
}
