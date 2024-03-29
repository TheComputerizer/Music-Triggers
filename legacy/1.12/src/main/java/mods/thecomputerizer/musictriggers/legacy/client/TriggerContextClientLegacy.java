package mods.thecomputerizer.musictriggers.legacy.client;

import mods.thecomputerizer.musictriggers.api.client.TriggerContextClient;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.ResourceContext;
import mods.thecomputerizer.shadow.org.joml.Vector3i;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumSkyBlock;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static net.minecraft.world.EnumSkyBlock.BLOCK;
import static net.minecraft.world.EnumSkyBlock.SKY;

public class TriggerContextClientLegacy extends TriggerContextClient<EntityPlayerSP,WorldClient> {

    private NetworkPlayerInfo network;
    private BlockPos pos;

    public TriggerContextClientLegacy(ChannelAPI channel) {
        super(channel);
    }

    @Override
    public void cache() {
        boolean both = hasBoth();
        this.network = getNetworkInfo(both);
        this.pos = both ? getRoundedBlockPos() : null;
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
        return Objects.nonNull(this.network) ? this.network.getGameType().getID() : 0;
    }

    @Override
    protected float getHealth() {
        return hasPlayer() ? this.player.getHealth() : 20f;
    }

    @Override
    protected float getMaxHealth() {
        return hasPlayer() ? this.player.getMaxHealth() : 20f;
    }

    private @Nullable NetworkPlayerInfo getNetworkInfo(boolean both) {
        NetHandlerPlayClient connection = both ? Minecraft.getMinecraft().getConnection() : null;
        return Objects.nonNull(connection) ? connection.getPlayerInfo(this.player.getGameProfile().getId()) : null;
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
    public boolean isActiveAcidRain() {
        return false;
    }

    @Override
    public boolean isActiveAdvancement(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActiveBlizzard() {
        return false;
    }

    @Override
    public boolean isActiveBlockEntity(ResourceContext ctx, int range, float yRatio) {
        return false;
    }

    @Override
    public boolean isActiveBloodMoon() {
        return false;
    }

    @Override
    public boolean isActiveBlueMoon() {
        return false;
    }

    @Override
    public boolean isActiveCloudy() {
        return false;
    }

    @Override
    public boolean isActiveCommand() {
        return false;
    }

    @Override
    public boolean isActiveDead() {
        return hasPlayer() && (this.player.isDead || getHealth()<=0f);
    }

    @Override
    public boolean isActiveDifficulty(int level) {
        int difficulty = hasWorld() ? this.world.getDifficulty().getId() : -1;
        if(difficulty==3 && this.world.getWorldInfo().isHardcoreModeEnabled()) difficulty++;
        return level==difficulty;
    }

    @Override
    public boolean isActiveDimension(ResourceContext ctx) {
        if(!hasWorld()) return false;
        int dimension = this.world.provider.getDimension();
        DimensionType type = this.world.provider.getDimensionType();
        return ctx.checkMatch(String.valueOf(dimension),type.getName());
    }

    @Override
    public boolean isActiveEffect(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActiveElytra() {
        return hasPlayer() && this.player.isElytraFlying();
    }

    @Override
    public boolean isActiveFallingStars() {
        return false;
    }

    @Override
    public boolean isActiveFishing() {
        return hasPlayer() && Objects.nonNull(this.player.fishEntity) && this.player.fishEntity.isOverWater();
    }

    @Override
    public boolean isActiveGamestage(ResourceContext ctx, boolean whitelist) {
        return false;
    }

    @Override
    public boolean isActiveGUI(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActiveHarvestMoon() {
        return false;
    }

    @Override
    public boolean isActiveHurricane(int range) {
        return false;
    }

    @Override
    public boolean isActiveInventory(List<String> items, List<String> slots) {
        return false;
    }

    @Override
    public boolean isActiveLight(int level, String typeStr) {
        if(!hasWorld()) return false;
        EnumSkyBlock type = typeStr.matches("block") ? BLOCK : (typeStr.matches("sky") ? SKY : null);
        return Objects.nonNull(type) ? this.world.checkLightFor(type,this.pos) : this.world.checkLight(this.pos);
    }

    @Override
    public boolean isActiveLightRain() {
        return false;
    }

    @Override
    public boolean isActiveMoon(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActivePet(int range, float yRatio) {
        if(!hasPlayer()) return false;
        for(EntityTameable entity : getEntitiesAround(EntityTameable.class,range,yRatio))
            if(entity.isTamed() && entity.isOwner(this.player)) return true;
        return false;
    }

    @Override
    public boolean isActiveRaining() {
        return Objects.nonNull(this.pos) && this.world.isRainingAt(this.pos);
    }

    @Override
    public boolean isActiveRainIntensity(float level) {
        return false;
    }

    @Override
    public boolean isActiveRiding(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActiveSandstorm(int range) {
        return false;
    }

    @Override
    public boolean isActiveSeason(int level) {
        return false;
    }

    @Override
    public boolean isActiveStatistic(ResourceContext ctx, int level) {
        return false;
    }

    @Override
    public boolean isActiveStorming() {
        return isActiveRaining() && this.world.isThundering();
    }

    @Override
    public boolean isActiveTime(String bundle, float startHour, float endHour, int startDay, int endDay, int moonPhase) {
        if(!hasWorld()) return false;
        double time = (double)this.world.getWorldTime()/1000d;
        int day = (int)(time/24d);
        float hour = (float)(time%24d);
        switch(bundle) {
            case "day": {
                startHour = DAY;
                endHour = NIGHT;
                break;
            }
            case "night": {
                startHour = NIGHT;
                endHour = DAY;
                break;
            }
            case "sunrise": {
                startHour = SUNRISE;
                endHour = DAY;
                break;
            }
            case "sunset": {
                startHour = SUNSET;
                endHour = NIGHT;
                break;
            }
        }
        startHour = startHour%24f;
        endHour = endHour%24f;
        return hasWorld() && (endHour==startHour || (endHour>startHour ? hour>=startHour && hour<=endHour :
                hour>=startHour || hour<=endHour)) && (day>=startDay && day<=endDay) &&
                moonPhase==this.world.getMoonPhase()+1;
    }

    @Override
    public boolean isActiveTornado(int range, int level) {
        return false;
    }

    @Override
    public boolean isActiveUnderwater() {
        return Objects.nonNull(this.pos) && this.world.getBlockState(this.pos).getMaterial()==Material.WATER &&
                this.world.getBlockState(this.pos.up()).getMaterial()==Material.WATER;
    }

}
