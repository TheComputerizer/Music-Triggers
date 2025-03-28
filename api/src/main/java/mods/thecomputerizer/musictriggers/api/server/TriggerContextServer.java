package mods.thecomputerizer.musictriggers.api.server;

import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.ResourceContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerBiome;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerMob;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.network.MessageCurrentStructure;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.EntityAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.structure.StructureAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.ChampionsAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.ChampionsAPI.ChampionData;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.InfernalMobsAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.InfernalMobsAPI.InfernalData;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.ModHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.MinecraftServerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.ServerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TriggerContextServer extends TriggerContext {
    
    private StructureAPI<?> structure;
    @Setter private String getPreviousStructureID = "?";
    @Setter private String previousStructureName = "?";

    public TriggerContextServer(ChannelAPI channel) {
        super(channel,"server_context");
    }

    @Override public void cache() {
        MinecraftServerAPI<?> server = ServerHelper.getAPI();
        if(Objects.nonNull(server)) {
            try {
                this.player = server.getPlayerByUUID(this.channel.getHelper().getPlayerID());
            } catch(NullPointerException ignored) { //This is only thrown when the server is closing and the player list does not exist anymore
                logDebug("Caught NPE while trying to get player by UUID");
                this.player = null;
            }
            this.world = Objects.nonNull(this.player) ? this.player.getWorld() : null;
        } else {
            this.player = null;
            this.world = null;
        }
        this.pos = hasBoth() ? this.player.getPosRounded() : null;
        this.structure = Objects.nonNull(this.pos) ? this.world.getStructureAt(this.pos) : null;
        if(Objects.nonNull(this.structure)) {
            String name = this.structure.getName(this.world);
            if(Objects.isNull(name) || name.isEmpty()) name = "?";
            ResourceLocationAPI<?> registryName = this.structure.getRegistryName(this.world);
            checkStructureSync(name,Objects.nonNull(registryName) ? registryName.toString() : "?");
        } else if(Objects.nonNull(this.player)) checkStructureSync("?","?");
    }

    private boolean checkEntity(TriggerMob trigger, EntityAPI<?,?> entity) {
        ResourceContext ctx = trigger.getResourceCtx();
        return Objects.nonNull(ctx) && checkEntityName(ctx,entity) && trigger.checkTarget(entity,this.player) &&
               checkEntityNBT(trigger,entity) && checkEntityMods(trigger,entity);
    }

    @SuppressWarnings("unchecked")
    private boolean checkEntityChampion(TriggerMob trigger, EntityAPI<?,?> entity) {
        ChampionsAPI champions = ModHelper.champions();
        if(Objects.nonNull(champions)) {
            List<String> championNames = (List<String>)trigger.getParameterAsList("champion");
            if(championNames.isEmpty() || (championNames.contains("any"))) return true;
            ChampionData data = champions.getChampionData(entity);
            if(Objects.nonNull(data))
                for(String name : championNames)
                    for(String affix : data.getAffixes())
                        if(affix.contains(name)) return true;
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean checkEntityInfernal(TriggerMob trigger, EntityAPI<?,?> entity) {
        InfernalMobsAPI infernalMobs = ModHelper.infernalMobs();
        if(Objects.nonNull(infernalMobs)) {
            List<String> infernalNames = (List<String>)trigger.getParameterAsList("infernal");
            if(infernalNames.isEmpty() || infernalNames.contains("any")) return true;
            InfernalData<?> data = infernalMobs.getInfernalData(entity);
            if(Objects.nonNull(data))
                for(String name : infernalNames)
                    for(String display : data.getDisplayNames())
                        if(display.contains(name)) return true;
            return false;
        }
        return true;
    }

    private boolean checkEntityMods(TriggerMob trigger, EntityAPI<?,?> entity) {
        return checkEntityChampion(trigger,entity) && checkEntityInfernal(trigger,entity);
    }

    private boolean checkEntityName(ResourceContext ctx, EntityAPI<?,?> entity) {
        ResourceLocationAPI<?> regName = entity.getRegistryName(this.world);
        if(Objects.isNull(regName)) return false;
        String name = entity.getName();
        return ctx.checkMatch(regName.toString(),TextHelper.isNotBlank(name) ? name : null);
    }
    
    private boolean checkEntityNBT(TriggerMob trigger, EntityAPI<?,?> entity) {
        List<?> nbtCheckers = trigger.getNBTParameter();
        if(nbtCheckers.contains("any")) return true;
        CompoundTagAPI<?> entityData = entity.getData();
        for(Object checkThis : nbtCheckers)
            if(checkNBT(entityData,String.valueOf(checkThis))) return true;
        return false;
    }

    private Set<EntityAPI<?,?>> getEntitiesAround(TriggerMob trigger) {
        int range = trigger.getParameterAsInt("detection_range");
        float rangeRatioY = trigger.getParameterAsFloat("detection_y_ratio");
        List<EntityAPI<?,?>> entities = getEntitiesAround(getBox(range,rangeRatioY));
        Set<EntityAPI<?,?>> aliveEntities = new HashSet<>();
        for(EntityAPI<?,?> entity : entities)
            if(entity.isAlive()) aliveEntities.add(entity);
        return aliveEntities;
    }
    
    private void checkStructureSync(String name, String id) {
        if(this.previousStructureName.equals(name) && this.getPreviousStructureID.equals(id)) return;
        //In the case of multiple server channels, this ensures only 1 will send a structure update to the client
        ChannelHelper helper = this.channel.getHelper();
        for(ChannelAPI channel : helper.getChannels().values()) {
            TriggerContext ctx = channel.getSelector().getContext();
            if(ctx instanceof TriggerContextServer) {
                TriggerContextServer serverCtx = (TriggerContextServer)ctx;
                serverCtx.previousStructureName = name;
                serverCtx.getPreviousStructureID = id;
            }
        }
        MessageCurrentStructure<?> message = new MessageCurrentStructure<>(helper,name,id);
        MTNetwork.sendToClient(message,message.getUuid());
    }

    @Override public boolean isActiveAcidRain() {
        return false;
    }

    @Override public boolean isActiveAdvancement(ResourceContext ctx) {
        return false;
    }

    @Override public boolean isActiveAdventure() {
        return false;
    }

    @Override public boolean isActiveBiome(TriggerBiome trigger) {
        return false;
    }

    @Override public boolean isActiveBlizzard() {
        return false;
    }

    @Override public boolean isActiveBlockEntity(ResourceContext ctx, int range, float yRatio) {
        return false;
    }

    @Override public boolean isActiveBloodMoon() {
        return false;
    }

    @Override public boolean isActiveBlueMoon() {
        return false;
    }

    @Override public boolean isActiveCloudy() {
        return false;
    }

    @Override public boolean isActiveCommand() {
        return false;
    }

    @Override public boolean isActiveCreative() {
        return false;
    }

    @Override public boolean isActiveDead() {
        return false;
    }

    @Override public boolean isActiveDifficulty(int level) {
        return false;
    }

    @Override public boolean isActiveDimension(ResourceContext ctx) {
        return false;
    }

    @Override public boolean isActiveDrowning(int level) {
        return false;
    }

    @Override public boolean isActiveEffect(ResourceContext ctx) {
        return false;
    }

    @Override public boolean isActiveElytra() {
        return false;
    }

    @Override public boolean isActiveStarShower() {
        return false;
    }

    @Override public boolean isActiveFishing() {
        return false;
    }

    @Override public boolean isActiveGamestage(ResourceContext ctx, boolean whitelist) {
        return false;
    }

    @Override public boolean isActiveGeneric() {
        return false;
    }

    @Override public boolean isActiveGUI(ResourceContext ctx) {
        return false;
    }

    @Override public boolean isActiveHarvestMoon() {
        return false;
    }

    @Override public boolean isActiveHeight(int level, boolean checkSky, boolean checkAbove) {
        return false;
    }

    @Override public boolean isActiveHome(int range, float yRatio) {
        return false;
    }

    @Override public boolean isActiveHurricane(int range) {
        return false;
    }

    @Override public boolean isActiveInventory(List<String> items, List<String> slots) {
        return false;
    }

    @Override public boolean isActiveLight(int level, String type) {
        return false;
    }

    @Override public boolean isActiveLightRain() {
        return false;
    }

    @Override public boolean isActiveLoading() {
        return false;
    }

    @Override public boolean isActiveLowHP(float percent) {
        return false;
    }

    @Override public boolean isActiveMenu() {
        return false;
    }

    @Override public boolean isActiveMob(TriggerMob trigger) {
        if(Objects.isNull(this.pos)) return false;
        Set<EntityAPI<?,?>> entitiesAround = getEntitiesAround(trigger);
        trigger.deduplicate(entitiesAround);
        trigger.revalidateCache(this.pos,this.player);
        for(EntityAPI<?,?> entity : entitiesAround)
            if(checkEntity(trigger,entity)) trigger.cacheValidEntity(entity);
        return trigger.checkCacheSize();
    }

    @Override public boolean isActiveMoon(ResourceContext ctx) {
        return false;
    }

    @Override public boolean isActivePet(int range, float yRatio) {
        return false;
    }

    @Override public boolean isActivePVP() { //TODO
        return false;
    }

    @Override public boolean isActiveRaid(List<?> statusChecks, int wave) {
        if(Objects.isNull(this.pos) || this.world.getRaidWave(this.pos)<wave) return false;
        if(statusChecks.contains("any")) return true;
        String status = this.world.getRaidStatus(this.pos);
        if(Objects.isNull(status)) return false;
        for(Object check : statusChecks) if(status.equalsIgnoreCase(String.valueOf(check))) return true;
        return false;
    }

    @Override public boolean isActiveRaining() {
        return false;
    }

    @Override public boolean isActiveRainIntensity(float level) {
        return false;
    }

    @Override public boolean isActiveRiding(ResourceContext ctx) {
        return false;
    }

    @Override public boolean isActiveSandstorm(int range) {
        return false;
    }

    @Override public boolean isActiveSeason(int level) {
        return false;
    }

    @Override public boolean isActiveSnowing() {
        return false;//Objects.nonNull(this.pos) && this.world.canSnowAt(this.pos);
    }

    @Override public boolean isActiveSpectator() {
        return false;
    }

    @Override public boolean isActiveStatistic(ResourceContext ctx, int level) {
        return false;
    }

    @Override public boolean isActiveStorming() {
        return false;
    }

    @Override public boolean isActiveStructure(ResourceContext ctx) {
        return Objects.nonNull(this.structure) && ctx.checkMatch(
                this.structure.getRegistryName(this.world).toString(),this.structure.getName(this.world));
    }

    @Override public boolean isActiveTime(String bundle, float startHour, float endHour, int startDay, int endDay, int moonPhase) {
        return false;
    }

    @Override public boolean isActiveTornado(int range, int level) {
        return false;
    }

    @Override public boolean isActiveUnderwater() {
        return false;
    }

    @Override public boolean isActiveVictory(int timeout) { //TODO
        return false;
    }

    @Override public boolean isActiveZones(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return false;
    }

    @Override public boolean isClient() {
        return false;
    }
}