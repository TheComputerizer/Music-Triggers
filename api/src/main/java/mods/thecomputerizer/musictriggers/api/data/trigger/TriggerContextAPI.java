package mods.thecomputerizer.musictriggers.api.data.trigger;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerBiome;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerMob;
import mods.thecomputerizer.shadow.org.joml.Vector3i;

import java.util.List;

public abstract class TriggerContextAPI extends ChannelElement {

    public static boolean LOADED = false;

    protected TriggerContextAPI(ChannelAPI channel) {
        super(channel);
    }

    protected abstract int getAir();
    protected abstract int getGamemode();
    protected abstract float getHealth();
    protected abstract float getMaxHealth();
    protected abstract Vector3i getRoundedPos();
    protected abstract boolean hasVisibleSky(Vector3i pos);
    protected abstract boolean hasWorld();

    public boolean isActiveAdventure() {
        return getGamemode()==3;
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
        return LOADED && hasWorld();
    }

    public boolean isActiveSpectator() {
        return getGamemode()==2;
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
}