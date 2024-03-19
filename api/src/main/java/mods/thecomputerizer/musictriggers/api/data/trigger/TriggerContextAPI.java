package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.nbt.NBTHelper;
import mods.thecomputerizer.musictriggers.api.data.nbt.mode.NBTMode;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerBiome;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerMob;
import mods.thecomputerizer.shadow.org.joml.Vector3i;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Setter @Getter
public abstract class TriggerContextAPI<PLAYER,WORLD> extends ChannelElement {

    public static final List<String> NBT_MODES = Arrays.asList("KEY_PRESENT","VAL_PRESENT","GREATER","LESSER","EQUAL","INVERT");
    protected static final float DAY = 24f;
    protected static final float NIGHT = 13f;
    protected static final float SUNRISE = 23f;
    protected static final float SUNSET = 12f;

    public static boolean LOADED = false;

    protected PLAYER player;
    protected WORLD world;

    protected TriggerContextAPI(ChannelAPI channel) {
        super(channel);
    }

    protected boolean checkNBT(@Nullable CompoundTagAPI tag, String tagStr) {
        if(Objects.isNull(tag) || StringUtils.isBlank(tagStr) || tagStr.toUpperCase().matches("ANY")) return true;
        NBTMode mode = NBTHelper.getAndInitMode(tagStr.split(";"));
        try {
            if(Objects.nonNull(mode)) mode.checkMatch(this.channel,tag);
        } catch(NumberFormatException ex) {
            logError("Tried to check numerical value of NBT data against a non numerical value in `{}`",tagStr,ex);
        } catch(Exception ex) {
            logError("Caught an unknown error when attempting to check NBT data for `{}`",tagStr,ex);
        }
        return false;
    }

    protected abstract int getAir();

    protected <E> List<E> getEntitiesAround(Class<E> clazz, int range) {
        return getEntitiesAround(clazz,range,1f);
    }

    protected <E> List<E> getEntitiesAround(Class<E> clazz, int range, float yRatio) {
        return getEntitiesAround(clazz,range,(double)range*yRatio);
    }

    protected <E> List<E> getEntitiesAround(Class<E> clazz, double hRange, double vRange) {
        Vector3i pos = getRoundedPos();
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;
        return getEntitiesAround(clazz,x-hRange,y-vRange,z-hRange,x+hRange,y+vRange,z+hRange);
    }

    protected abstract <E> List<E> getEntitiesAround(Class<E> clazz, double minX, double minY, double minZ, double maxX,
                                                     double maxY, double maxZ);
    protected abstract int getGamemode();
    protected abstract float getHealth();
    protected abstract float getMaxHealth();
    protected abstract Vector3i getRoundedPos();

    protected boolean hasPlayer() {
        return Objects.nonNull(this.player);
    }

    protected abstract boolean hasVisibleSky(Vector3i pos);

    protected boolean hasWorld() {
        return Objects.nonNull(this.world);
    }

    public boolean isActiveAdventure() {
        return getGamemode()==2;
    }

    public boolean isActiveCreative() {
        return getGamemode()==1;
    }

    public boolean isActiveDrowning(int level) {
        return getAir()<level;
    }

    public boolean isActiveGeneric() {
        return true;
    }

    public boolean isActiveHeight(int level, boolean checkSky, boolean checkAbove) {
        Vector3i pos = getRoundedPos();
        return checkAbove ? pos.y>level : pos.y<level && (!checkSky || hasVisibleSky(pos));
    }

    public boolean isActiveLoading() {
        return !LOADED;
    }

    public boolean isActiveLowHP(float percent) {
        return (percent/100f)<getHealth()/getMaxHealth();
    }

    public boolean isActiveMenu() {
        return LOADED && !hasWorld();
    }

    public boolean isActiveSpectator() {
        return getGamemode()==3;
    }

    public boolean isActiveZones(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        Vector3i pos = getRoundedPos();
        return pos.x>minX && pos.x<maxX && pos.y>minY && pos.y<maxY && pos.z>minZ && pos.z<maxZ;
    }

    public abstract boolean isActiveAcidRain();
    public abstract boolean isActiveAdvancement(ResourceContext ctx);
    public abstract boolean isActiveBiome(TriggerBiome trigger);
    public abstract boolean isActiveBlizzard();
    public abstract boolean isActiveBlockEntity(ResourceContext ctx, int range, float yRatio);
    public abstract boolean isActiveBloodMoon();
    public abstract boolean isActiveBlueMoon();
    public abstract boolean isActiveCloudy();
    public abstract boolean isActiveCommand();
    public abstract boolean isActiveDead();
    public abstract boolean isActiveDifficulty(int level);
    public abstract boolean isActiveDimension(ResourceContext ctx);
    public abstract boolean isActiveEffect(ResourceContext ctx);
    public abstract boolean isActiveElytra();
    public abstract boolean isActiveFallingStars();
    public abstract boolean isActiveFishing();
    public abstract boolean isActiveGamestage(ResourceContext ctx, boolean whitelist);
    public abstract boolean isActiveGUI(ResourceContext ctx);
    public abstract boolean isActiveHarvestMoon();
    public abstract boolean isActiveHome(int range, float yRatio);
    public abstract boolean isActiveHurricane(int range);
    public abstract boolean isActiveInventory(List<String> items, List<String> slots);
    public abstract boolean isActiveLight(int level, String type);
    public abstract boolean isActiveLightRain();
    public abstract boolean isActiveMob(TriggerMob trigger);
    public abstract boolean isActiveMoon(ResourceContext ctx);
    public abstract boolean isActivePet(int range, float yRatio);
    public abstract boolean isActivePVP();
    public abstract boolean isActiveRaid(int wave);
    public abstract boolean isActiveRaining();
    public abstract boolean isActiveRainIntensity(float level);
    public abstract boolean isActiveRiding(ResourceContext ctx);
    public abstract boolean isActiveSandstorm(int range);
    public abstract boolean isActiveSeason(int level);
    public abstract boolean isActiveSnowing();
    public abstract boolean isActiveStatistic(ResourceContext ctx, int level);
    public abstract boolean isActiveStorming();
    public abstract boolean isActiveStructure(ResourceContext ctx);
    public abstract boolean isActiveTime(String bundle, float startHour, float endHour, int startDay, int endDay, int moonPhase);
    public abstract boolean isActiveTornado(int range, int level);
    public abstract boolean isActiveUnderwater();
    public abstract boolean isActiveVictory(int timeout);

    public abstract boolean isClient();

    @Override
    public boolean isResource() {
        return false;
    }
}