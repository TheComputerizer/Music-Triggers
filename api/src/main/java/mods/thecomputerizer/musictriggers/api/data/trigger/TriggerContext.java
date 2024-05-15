package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.nbt.NBTHelper;
import mods.thecomputerizer.musictriggers.api.data.nbt.mode.NBTMode;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerBiome;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerMob;
import mods.thecomputerizer.shadow.org.joml.Vector3i;
import mods.thecomputerizer.theimpossiblelibrary.api.common.blockentity.BlockEntityAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.EntityAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.PlayerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Box;
import mods.thecomputerizer.theimpossiblelibrary.api.world.WorldAPI;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;

import static mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State.DISABLED;

public abstract class TriggerContext extends ChannelElement {

    protected final Set<TriggerSynced> syncedTriggers;
    @Getter protected PlayerAPI<?,?> player;
    protected WorldAPI<?> world;

    protected TriggerContext(ChannelAPI channel, String name) {
        super(channel,name);
        this.syncedTriggers = new HashSet<>();
    }

    public abstract void cache();

    protected boolean checkNBT(@Nullable CompoundTagAPI<?> tag, String tagStr) {
        if(Objects.isNull(tag) || StringUtils.isBlank(tagStr) || tagStr.equalsIgnoreCase("any")) return true;
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
    
    public void clearSync() {
        logInfo("Clearing syncable {} side data",isClient() ? "client" : "server");
        this.syncedTriggers.clear();
    }

    @Override
    public void close() {
        this.player = null;
        this.world = null;
    }

    protected Box getBox(int range, float yRatio) {
        return getBox(range,(double)range*yRatio);
    }

    protected Box getBox(double hRange, double vRange) {
        Vector3i pos = this.player.getPosRounded().getPosVec();
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;
        return getBox(x-hRange,y-vRange,z-hRange,x+hRange,y+vRange,z+hRange);
    }

    protected Box getBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new Box(minX,minY,minZ,maxX,maxY,maxZ);
    }

    protected Collection<BlockEntityAPI<?,?>> getBlockEntitiesAround(Box box) {
        return hasBoth() ? this.world.getBlockEntitiesInBox(box) : Collections.emptyList();
    }

    protected List<EntityAPI<?,?>> getEntitiesAround(Box box) {
        return hasBoth() ? this.world.getEntitiesInBox(box) : Collections.emptyList();
    }
    
    @Override protected TableRef getReferenceData() {
        return null;
    }
    
    @Override protected String getSubTypeName() {
        return "Trigger";
    }

    protected boolean getSyncedContext(TriggerAPI trigger) {
        TriggerSynced synced = getSyncedTrigger(trigger);
        return Objects.nonNull(synced) && synced.isPlayableContext(this);
    }
    
    public State getSyncedState(TriggerAPI trigger) {
        TriggerSynced synced = getSyncedTrigger(trigger);
        return Objects.nonNull(synced) ? synced.getState() : DISABLED;
    }
    
    protected TriggerSynced getSyncedTrigger(TriggerAPI trigger) {
        for(TriggerSynced synced : this.syncedTriggers)
            if(synced.matches(trigger)) return synced;
        return null;
    }
    
    @Override public Class<? extends ParameterWrapper> getTypeClass() {
        return TriggerContext.class;
    }

    protected boolean hasBoth() {
        return hasPlayer() && hasWorld();
    }

    protected boolean hasPlayer() {
        return Objects.nonNull(this.player);
    }

    protected boolean hasWorld() {
        return Objects.nonNull(this.world);
    }

    public void initSync() {
        logInfo("Initializing syncable {} side data",isClient() ? "client" : "server");
        for(TriggerAPI trigger : this.channel.getData().getTriggers())
            if(trigger.isSynced()) this.syncedTriggers.add(new TriggerSynced(this.channel,trigger));
    }

    public abstract boolean isActiveAcidRain();
    public abstract boolean isActiveAdvancement(ResourceContext ctx);
    public abstract boolean isActiveAdventure();
    public abstract boolean isActiveBiome(TriggerBiome trigger);
    public abstract boolean isActiveBlizzard();
    public abstract boolean isActiveBlockEntity(ResourceContext ctx, int range, float yRatio);
    public abstract boolean isActiveBloodMoon();
    public abstract boolean isActiveBlueMoon();
    public abstract boolean isActiveCloudy();
    public abstract boolean isActiveCommand();
    public abstract boolean isActiveCreative();
    public abstract boolean isActiveDead();
    public abstract boolean isActiveDifficulty(int level);
    public abstract boolean isActiveDimension(ResourceContext ctx);
    public abstract boolean isActiveDrowning(int level);
    public abstract boolean isActiveEffect(ResourceContext ctx);
    public abstract boolean isActiveElytra();
    public abstract boolean isActiveStarShower();
    public abstract boolean isActiveFishing();
    public abstract boolean isActiveGamestage(ResourceContext ctx, boolean whitelist);
    public abstract boolean isActiveGeneric();
    public abstract boolean isActiveGUI(ResourceContext ctx);
    public abstract boolean isActiveHarvestMoon();
    public abstract boolean isActiveHeight(int level, boolean checkSky, boolean checkAbove);
    public abstract boolean isActiveHome(int range, float yRatio);
    public abstract boolean isActiveHurricane(int range);
    public abstract boolean isActiveInventory(List<String> items, List<String> slots);
    public abstract boolean isActiveLight(int level, String type);
    public abstract boolean isActiveLightRain();
    public abstract boolean isActiveLoading();
    public abstract boolean isActiveLowHP(float percent);
    public abstract boolean isActiveMenu();
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
    public abstract boolean isActiveSpectator();
    public abstract boolean isActiveStatistic(ResourceContext ctx, int level);
    public abstract boolean isActiveStorming();
    public abstract boolean isActiveStructure(ResourceContext ctx);
    public abstract boolean isActiveTime(String bundle, float startHour, float endHour, int startDay, int endDay, int moonPhase);
    public abstract boolean isActiveTornado(int range, int level);
    public abstract boolean isActiveUnderwater();
    public abstract boolean isActiveVictory(int timeout);
    public abstract boolean isActiveZones(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);

    public abstract boolean isClient();

    protected boolean isCloseEnough(int x1, int y1, int z1, double range, double yFactor, int x2, int y2, int z2) {
        return x2>=(x1-range) && x2<=(x1+range) && z2>=(z1-range) && z2<=z1+range &&
                y2>=(y1-(range*yFactor)) && y2<=(y1+(range*yFactor));
    }

    @Override
    public boolean isResource() {
        return false;
    }

    public void updateSyncedState(TriggerAPI trigger, State state) {
        for(TriggerSynced synced : this.syncedTriggers) {
            if(synced.matches(trigger)) {
                synced.sync(state);
                break;
            }
        }
    }
}